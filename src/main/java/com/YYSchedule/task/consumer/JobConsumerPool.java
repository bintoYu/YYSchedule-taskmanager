/**
 * 
 */
package com.YYSchedule.task.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.store.service.JobBasicService;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskFileService;
import com.YYSchedule.store.service.TaskTempService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.queue.JobQueue;
import com.YYSchedule.task.queue.PriorityTaskPool;

/**
 * @author ybt
 * 
 * @date 2019年2月1日
 * @version 1.0
 */
@Component
public class JobConsumerPool
{
	@Autowired
	private Config config;
	
	@Autowired
	@Qualifier("jedisTemplate")
	public RedisTemplate<String,String> redisTemplate;
	
	@Autowired
	private TaskBasicService taskBasicService;
	
	@Autowired
	private TaskTempService taskTempService;
	
	@Autowired
	private TaskFileService taskFileService;
	
	@Autowired
	private JobBasicService jobBasicService;
	
	@Autowired
	private TaskTimestampService taskTimestampService;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	
	@Autowired
	private JobQueue jobQueue;
	
	@Autowired
	private PriorityTaskPool priorityTaskPool;
	
	public void startThreadPool()
	{
		int job_consumer_thread_num = config.getJob_consumer_thread_num();
		
		for(int i = 0; i < job_consumer_thread_num; i++)
		{
			JobConsumer jobConsumer = new JobConsumer(config, redisTemplate, taskBasicService, taskFileService, jobBasicService, taskTempService, taskTimestampService, jobQueue,priorityTaskPool);
			threadPoolExecutor.execute(jobConsumer);
		}
	}
}
