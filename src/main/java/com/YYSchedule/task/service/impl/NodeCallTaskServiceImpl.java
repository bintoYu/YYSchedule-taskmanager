/**
 * 
 */
package com.YYSchedule.task.service.impl;

import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskResult;
import com.YYSchedule.common.mybatis.pojo.TaskTimestamp;
import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.engine.EngineLogger;
import com.YYSchedule.common.rpc.domain.node.NodePayload;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.common.utils.Bean2BeanUtils;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.NodeItemMapper;
import com.YYSchedule.task.queue.PriorityTaskPool;

/**
 * @author ybt
 * 
 * @date 2018年7月18日
 * @version 1.0
 */
public class NodeCallTaskServiceImpl implements NodeCallTaskService.Iface
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeCallTaskServiceImpl.class);
	
	/*
	 * 任务节点(node)向控制节点(taskmanager) 默认注册之前会将任务节点进行维修，因此重启任务节点时，
	 * 无论之前该节点是否损坏，都会直接覆盖新的没有损坏的nodeItem
	 */
	@Override
	public int registerNode(NodePayload nodePayload) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		if (nodePayload == null || nodePayload.getNodeRuntime() == null)
		{
			LOGGER.error("node[ " + nodePayload.getNodeId() + " ]心跳信息不合法.");
			throw new InvalidRequestException("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
		}
		NodeItem nodeItem = new NodeItem(nodePayload);
		nodeItem.setUpdatedTime(System.currentTimeMillis());
		
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		NodeItemMapper nodeMapper = applicationContext.getBean(NodeItemMapper.class);
		nodeMapper.updateNode(nodeItem);
		
		LOGGER.info("成功注册任务节点: " + nodePayload.getNodeId());
		
		return 0;
	}
	
	/*
	 * 任务节点发送心跳信息 需要注意的是：由于新的心跳信息不包含“是否损坏”这一信息，因此需要进行更换
	 */
	@Override
	public int reportHeartbeat(NodePayload nodePayload) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		if (nodePayload == null || nodePayload.getNodeRuntime() == null)
		{
			LOGGER.error("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
			throw new InvalidRequestException("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
		}
		LOGGER.debug("Node [ " + nodePayload.getNodeId() + " ] payload info received.");
		NodeItem nodeItem = new NodeItem(nodePayload);
		nodeItem.setUpdatedTime(System.currentTimeMillis());
		
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		PriorityTaskPool taskPool = applicationContext.getBean(PriorityTaskPool.class);
		NodeItemMapper nodeMapper = applicationContext.getBean(NodeItemMapper.class);
		JmsTemplate jmsTemplate = applicationContext.getBean(JmsTemplate.class);
		TaskTimestampService taskTimestampService = applicationContext.getBean(TaskTimestampService.class);
		TaskBasicService taskBasicService = applicationContext.getBean(TaskBasicService.class);
		
		// 由于新的心跳信息不包含“是否损坏”这一信息，因此需要进行重新设置
		NodeItem oldNode = nodeMapper.getNode(nodeItem);
		if (oldNode != null)
		{
			nodeItem.setBroken(oldNode.isBroken());
		}
		nodeMapper.updateNode(nodeItem);
		
		// 监控节点，只有节点未损坏的情况下才会下发任务
		// 如果任务数小于缓存队列长度，则尝试获取任务并发送到activemq中。
		if (!nodeItem.isBroken() && nodeItem.getQueueLength() < nodeItem.getQueueLimit())
		{
			List<Task> list = taskPool.get(nodeItem.getTaskPhase(), nodeItem.getQueueLimit());
			if (!list.isEmpty())
			{
				LOGGER.info(nodeItem.getNodeId() + "[QueueLength:" + nodeItem.getQueueLength() + ", QueueLimit:" + nodeItem.getQueueLimit() + "]");
				LOGGER.info(list.size() + " Tasks --> " + nodeItem.getNodeId());
				for (Task task : list)
				{
					task.setExecutorId(nodeItem.getNodeId());
					task.setTaskStatus(TaskStatus.DISTRIBUTED);
					try
					{
						// 发送到activemq中
						addToDistributeTaskQueue(task, jmsTemplate);
						
						// 数据库更新时间戳
						TaskTimestamp taskTimestamp = new TaskTimestamp();
						taskTimestamp.setTaskId(task.getTaskId());
						taskTimestamp.setDistributedTime(System.currentTimeMillis());
						taskTimestampService.updateTaskTimestamp(taskTimestamp);
					} catch (InterruptedException e)
					{
						task.setTaskStatus(TaskStatus.DISTRIBUTE_FAILED);
						e.printStackTrace();
					}
					
					// 数据库更新taskBasic
					TaskBasic taskBasic = new TaskBasic(task);
					taskBasicService.updateTaskBasic(taskBasic);
				}
			}
		}
		
		// 插入或更新日志信息到数据库中
		List<EngineLogger> engineLoggerList = nodePayload.getEngineLoggerList();
		if (engineLoggerList != null)
		{
			List<TaskResult> taskResultList = Bean2BeanUtils.engineLoggerList2TaskResultList(engineLoggerList);
			TaskResultService taskResultService = applicationContext.getBean(TaskResultService.class);
			taskResultService.updateTaskResultList(taskResultList);
		}
		return 0;
	}
	
	@Override
	public int reportTaskExecutionStatus(String nodeId, long taskId, TaskPhase taskPhase, TaskStatus taskStatus) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		TaskBasicService taskBasicService = applicationContext.getBean(TaskBasicService.class);
		
		TaskBasic taskBasic = new TaskBasic();
		taskBasic.setTaskId(taskId);
		taskBasic.setTaskPhase(taskPhase.toString());
		taskBasic.setTaskStatus(taskStatus.toString());
		taskBasic.setLoadedTime(System.currentTimeMillis());
		
		// 更新taskBasic
		int result = taskBasicService.updateTaskBasic(taskBasic);
		
		// 更新taskTimestamp
		TaskTimestamp taskTimestamp = new TaskTimestamp();
		taskTimestamp.setTaskId(taskId);
		TaskTimestampService taskTimestampService = applicationContext.getBean(TaskTimestampService.class);
		if (taskStatus == TaskStatus.RUNNING)
		{
			taskTimestamp.setStartedTime(System.currentTimeMillis());
			taskTimestampService.updateTaskTimestamp(taskTimestamp);
		}
		else if (taskStatus.equals(TaskStatus.FINISHED) || taskStatus.equals(TaskStatus.FAILURE) || taskStatus.equals(TaskStatus.INTERRUPTED) || taskStatus.equals(TaskStatus.TIMEOUT))
		{
			taskTimestamp.setFinishedTime(System.currentTimeMillis());
			taskTimestampService.updateTaskTimestamp(taskTimestamp);
		}
		
		return result;
	}
	
	private boolean addToDistributeTaskQueue(Task task, JmsTemplate jmsTemplate) throws InterruptedException
	{
		// 确定distributeTaskQueue名称，例如：192.168.2.91:7000:distributeTaskQueue
		String distributeTaskQueue = task.getExecutorId() + ":" + "distributeTaskQueue";
		try
		{
			// 将task发送到distributeTaskQueue中
			ActiveMQUtils.sendTask(jmsTemplate, distributeTaskQueue, task, task.getTaskPriority().getValue());
		} catch (JmsException jmsException)
		{
			LOGGER.error("Task [ " + task.getTaskId() + " ] 放入队列distributeTaskQueue失败！" + jmsException.getMessage());
			throw new InterruptedException("Task [ " + task.getTaskId() + " ] 放入队列distributeTaskQueue失败！" + jmsException.getMessage());
		}
		LOGGER.info("Task [ " + task.getTaskId() + " ] 已放入队列distributeTaskQueue中.");
		return true;
	}
}
