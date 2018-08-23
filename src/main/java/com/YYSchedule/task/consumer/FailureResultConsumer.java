/**
 * 
 */
package com.YYSchedule.task.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskTempService;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.queue.FailureResultQueue;
import com.YYSchedule.task.queue.PriorityTaskQueue;

/**
 * @author ybt
 * 
 * @date 2018年8月8日
 * @version 1.0
 */
@Component
public class FailureResultConsumer
{
	@Autowired
	private Config config;
	
	@Autowired
	private TaskBasicService taskBasicService;
	
	@Autowired
	private TaskTempService taskTempService;
	
	@Autowired
	private FailureResultQueue failureResultQueue;
	
	@Autowired
	private PriorityTaskQueue priorityTaskQueue;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	
	public void startThreadPool()
	{
		int task_consumer_thread_num = config.getResult_consumer_thread_num();
		
		for(int i = 0; i < task_consumer_thread_num; i++)
		{
			FailureResultConsumerThread failureResultConsumerThread = new FailureResultConsumerThread(taskBasicService, taskTempService, failureResultQueue, priorityTaskQueue);
			threadPoolExecutor.execute(failureResultConsumerThread);
		}
	}
}
