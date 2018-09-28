/**
 * 
 */
package com.YYSchedule.task.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.store.ftp.FtpConnFactory;
import com.YYSchedule.store.service.JobBasicService;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskFileService;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.ResultStatusMapper;
import com.YYSchedule.task.queue.FailureResultQueue;

/**
 * @author ybt
 * 
 * @date 2018年8月8日
 * @version 1.0
 */
@Component
public class ResultQueueConsumer
{
	@Autowired
	private Config config;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	@Qualifier("jedisTemplate")
	public RedisTemplate redisTemplate;
	
	@Autowired
	private FtpConnFactory ftpConnFactory;
	
	@Autowired
	private TaskBasicService taskBasicService;
	
	@Autowired
	private TaskFileService taskFileService;
	
	@Autowired
	private TaskResultService taskResultService;
	
	@Autowired
	private JobBasicService jobBasicService;
	
	@Autowired
	private TaskTimestampService taskTimestampService;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	
	@Autowired
	private ResultStatusMapper resultStatusMapper;
	
	@Autowired
	private FailureResultQueue failureResultQueue;
	
	public void startThreadPool()
	{
		int result_consumer_thread_num = config.getResult_consumer_thread_num();
		
		for(int i = 0; i < result_consumer_thread_num; i++)
		{
			ResultQueueConsumerThread resultQueueConsumerThread = new ResultQueueConsumerThread(config, ftpConnFactory, jmsTemplate, redisTemplate, taskBasicService, taskFileService, taskResultService, taskTimestampService, jobBasicService, resultStatusMapper,failureResultQueue);
			threadPoolExecutor.execute(resultQueueConsumerThread);
		}
	}
}
