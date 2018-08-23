/**
 * 
 */
package com.YYSchedule.task.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskTemp;
import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskTempService;
import com.YYSchedule.task.queue.FailureResultQueue;
import com.YYSchedule.task.queue.PriorityTaskQueue;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * @author ybt
 * 
 * @date 2018年8月8日
 * @version 1.0
 */
public class FailureResultConsumerThread implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FailureResultConsumerThread.class);
	
	private TaskBasicService taskBasicService;
	
	private TaskTempService taskTempService;
	
	private FailureResultQueue failureResultQueue;
	
	private PriorityTaskQueue priorityTaskQueue;
	
	/**
	 * @param jmsTemplate
	 * @param config
	 */
	public FailureResultConsumerThread(TaskBasicService taskBasicService,TaskTempService taskTempService,FailureResultQueue failureResultQueue, PriorityTaskQueue priorityTaskQueue)
	{
		this.taskBasicService = taskBasicService;
		this.priorityTaskQueue = priorityTaskQueue;
		this.failureResultQueue = failureResultQueue;
		this.taskTempService = taskTempService;
	}
	
	@Override
	public void run()
	{
		while (!Thread.currentThread().isInterrupted())
		{
			// 从failureResultQueue中获取result
			Result result = failureResultQueue.takeResult();
			if (result != null)
			{
				// 获取taskBasic,将失败数+1，然后再进行更新
				int failureCount = saveTaskBasic(result.getTaskId());
				
				if (failureCount < 3 && failureCount > 0)
				{
					// 根据result将task重新进行封装
					Task task = getTask(result.getTaskId());
					if (task != null)
					{
						// 把task放到priorityTaskQueue中
						priorityTaskQueue.addToPriorityTaskQueue(task);
					}
				}
			}
		}
	}
	
	private int saveTaskBasic(long taskId)
	{
		TaskBasic taskBasic = taskBasicService.getTaskBasicById(taskId);
		taskBasic.setFailureCount(taskBasic.getFailureCount() + 1);
		taskBasicService.updateTaskBasic(taskBasic);
		
		return taskBasic.getFailureCount();
	}
	
	private Task getTask(long taskId)
	{
		TaskTemp taskTemp = taskTempService.getTaskTempById(taskId);
		
		String taskJson = taskTemp.getTask();
		
		Task task = JSON.parseObject(taskJson, new TypeReference<Task>(){});
		
		return task;
	}
}
