package backup;
//package com.YYSchedule.task.distributor;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jms.core.JmsTemplate;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Component;
//
//import com.YYSchedule.store.service.TaskBasicService;
//import com.YYSchedule.store.service.TaskTimestampService;
//import com.YYSchedule.task.config.Config;
//import com.YYSchedule.task.mapper.NodeItemMapper;
//import com.YYSchedule.task.queue.PriorityTaskPool;
//import com.YYSchedule.task.queue.PriorityTaskQueue;
//
//@Component("TaskDistributorPool")
//public class TaskDistributorPool
//{
//	@Autowired
//	private PriorityTaskQueue taskQueue;
//	@Autowired
//	private ThreadPoolTaskExecutor threadPoolExecutor;
//	@Autowired
//	private Config config;
//	@Autowired
//	private TaskBasicService taskBasicService;
//	@Autowired
//	private TaskTimestampService taskTimestampService;
//	@Autowired
//	private JmsTemplate jmsTemplate;
//
//
//	public void startThreadPool()
//	{
//		TaskDistributor distributeTaskQueueThread = new TaskDistributor(config,taskQueue,taskBasicService,taskTimestampService,jmsTemplate);
//		threadPoolExecutor.execute(distributeTaskQueueThread);
//	}
//}
