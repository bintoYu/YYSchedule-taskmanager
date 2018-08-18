/**
 * 
 */
package com.YYSchedule.task.service.impl;

import java.nio.ByteBuffer;
import java.security.Timestamp;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskResult;
import com.YYSchedule.common.mybatis.pojo.TaskTimestamp;
import com.YYSchedule.common.pojo.NodeItem;
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
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.NodeItemMapper;

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
	 * 任务节点(node)向控制节点(taskmanager) 
	 * 默认注册之前会将任务节点进行维修，因此可以直接更新nodeItem
	 */
	@Override
	public int registerNode(NodePayload nodePayload) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		if (nodePayload == null || nodePayload.getNodeRuntime() == null) {
			LOGGER.error("node[ " + nodePayload.getNodeId() + " ]心跳信息不合法.");
			throw new InvalidRequestException("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
		}
		NodeItem nodeItem = new NodeItem(nodePayload);
		nodeItem.setUpdatedTime(System.currentTimeMillis());
		
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		NodeItemMapper nodeMapper = applicationContext.getBean(NodeItemMapper.class);
		nodeMapper.updateNode(nodeItem);
		
		LOGGER.info("成功注册任务节点: " + nodePayload.getNodeId());
		
		return 1;
	}
	
	/*
	 * 任务节点发送心跳信息 需要注意的是，如果任务节点已损坏,则保留老的心跳信息，不替换新的心跳信息
	 */
	@Override
	public int reportHeartbeat(NodePayload nodePayload) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		if (nodePayload == null || nodePayload.getNodeRuntime() == null) {
			LOGGER.error("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
			throw new InvalidRequestException("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
		}
		LOGGER.debug("Node [ " + nodePayload.getNodeId() + " ] payload info received.");
		NodeItem nodeItem = new NodeItem(nodePayload);
		nodeItem.setUpdatedTime(System.currentTimeMillis());
		
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		NodeItemMapper nodeMapper = applicationContext.getBean(NodeItemMapper.class);
		
		// 判断任务节点是否损坏,没有损坏才替换任务节点信息
		NodeItem node = nodeMapper.getNode(nodeItem);
		if(node == null)
		{
			nodeMapper.updateNode(nodeItem);
		}
		else if (!node.isBroken())
		{
			nodeMapper.updateNode(nodeItem);
		}

		
		// 插入或更新日志信息到数据库中
		List<EngineLogger> engineLoggerList = nodePayload.getEngineLoggerList();
		if (engineLoggerList != null) {
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
		if (taskStatus == TaskStatus.RUNNING) {
			taskTimestamp.setStartedTime(System.currentTimeMillis());
			taskTimestampService.updateTaskTimestamp(taskTimestamp);
		}
		else if (taskStatus.equals(TaskStatus.FINISHED) || taskStatus.equals(TaskStatus.FAILURE) || taskStatus.equals(TaskStatus.INTERRUPTED) || taskStatus.equals(TaskStatus.TIMEOUT)) {
			taskTimestamp.setFinishedTime(System.currentTimeMillis());
			taskTimestampService.updateTaskTimestamp(taskTimestamp);
		}
		
		return result;
	}
	
}
