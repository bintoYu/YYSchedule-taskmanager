package com.YYSchedule.task.distributor;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskTimestamp;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.StringUtils;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.matcher.LoadBalancingMatcher;
import com.YYSchedule.task.queue.PriorityTaskQueue;
 
/**
 * 
 * @author ybt
 *
 * @date 2018年7月5日  
 * @version 1.0
 */
public class TaskDistributorThread implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDistributorThread.class);
	
	private PriorityTaskQueue priorityTaskQueue;
	
	private String distributeTaskQueue;
	
	private TaskBasicService taskBasicService;
	
	private TaskTimestampService taskTimestampService;
	
	private JmsTemplate jmsTemplate;
	
	private volatile boolean stop = false;

	/**
	 * 
	 */
	public TaskDistributorThread(PriorityTaskQueue priorityTaskQueue,TaskBasicService taskBasicService,TaskTimestampService taskTimestampService,JmsTemplate jmsTemplate) {
		 this.priorityTaskQueue = priorityTaskQueue;
		 this.taskTimestampService = taskTimestampService;
		 this.taskBasicService = taskBasicService;
		 this.jmsTemplate = jmsTemplate;
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted() && !stop) {

			Task task = null;
			try {
				//从TaskQueue中获取Task，并存到DistributeTaskQueue中
				task = priorityTaskQueue.takeTask();
				if (task != null) {
					LOGGER.info("分发task [ " + task.getTaskId() + " ] 中...");
					schedule(task);
					
					if (task.getTaskStatus().equals(TaskStatus.DISTRIBUTED) && task.getExecutorId()!=null) {
							
						addToDistributeTaskQueue(task);
						
						TaskTimestamp taskTimestamp = new TaskTimestamp();
						taskTimestamp.setTaskId(task.getTaskId());
						taskTimestamp.setDistributedTime(System.currentTimeMillis());
						taskTimestampService.updateTaskTimestamp(taskTimestamp);
					}
				}
			} catch (InterruptedException ie) {
				LOGGER.error("出现异常: " + ie.getMessage(), ie);
			} catch (Exception e) {
				LOGGER.error("分发task失败！ taskId: [ " + task.getTaskId() + " ]" + e.getMessage(), e);
			}
			
			TaskBasic taskBasic = new TaskBasic();
			taskBasic.setTaskId(task.getTaskId());
			taskBasic.setTaskStatus(task.getTaskStatus().toString());
			taskBasic.setNodeId(task.getExecutorId());
			taskBasicService.updateTaskBasic(taskBasic);
		}
	}

	/**
	 * 分发单个任务
	 * 
	 * @param task
	 */
	private void schedule(Task task) throws Exception {

		if (task.getTaskStatus().equals(TaskStatus.DISTRIBUTED) && !StringUtils.isEmpty(task.getExecutorId())) {
			return;
		}

		LoadBalancingMatcher resourceMatcher = new LoadBalancingMatcher();

		String nodeId = resourceMatcher.getMatchedNode(task);
		// String nodeId = "";
		if (!StringUtils.isEmpty(nodeId)) {
			task.setExecutorId(nodeId);
			task.setTaskStatus(TaskStatus.DISTRIBUTED);
			LOGGER.info("已为task [ " + task.getTaskId() + " ] " + "分配好node节点 [ " + nodeId + " ] ");
		}
	}

	/**
	 * 将task放入distributeTaskQueue中
	 * 
	 * @param task
	 * @throws InterruptedException 
	 */
	private boolean addToDistributeTaskQueue(Task task) throws InterruptedException {
		//确定distributeTaskQueue名称，例如：192.168.2.91:7000:distributeTaskQueue
		distributeTaskQueue = task.getExecutorId() + ":" + "distributeTaskQueue";
		try
		{
			//将task发送到distributeTaskQueue中
			ActiveMQUtils.sendTask(jmsTemplate, distributeTaskQueue, task);
		}
		catch(JmsException jmsException)
		{
			LOGGER.error("Task [ " + task.getTaskId() + " ] 放入队列distributeTaskQueue失败！" + jmsException.getMessage());
			throw new InterruptedException("Task [ " + task.getTaskId() + " ] 放入队列distributeTaskQueue失败！" + jmsException.getMessage());
		}
		LOGGER.info("Task [ " + task.getTaskId() + " ] 已放入队列distributeTaskQueue中.");
		return true;
	}

	/**
	 * shut down distribute task queue thread
	 */
	public synchronized void shutdown() {
		this.stop = true;
		Thread.currentThread().interrupt();
	}

}
