package com.YYSchedule.task.distributor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.queue.TaskQueue;

@Component("TaskDistributor")
public class TaskDistributor
{
	@Autowired
	private TaskQueue taskQueue;
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	@Autowired
	private Config config;
	@Autowired
	private TaskBasicService taskBasicService;
	@Autowired
	private TaskTimestampService taskTimestampService;
	@Autowired
	private JmsTemplate jmsTemplate;

	public void startThreadPool()
	{
		int distribute_thread_num = config.getDistribute_thread_num();
		
		for(int i = 0; i < distribute_thread_num; i++)
		{
			TaskDistributorThread distributeTaskQueueThread = new TaskDistributorThread(taskQueue,taskBasicService,taskTimestampService,jmsTemplate);
			threadPoolExecutor.execute(distributeTaskQueueThread);
		}
	}
}
