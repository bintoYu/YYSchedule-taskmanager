/**
 * 
 */
package com.YYSchedule.task.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.store.ftp.FtpConnFactory;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.task.config.Config;

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
	private FtpConnFactory ftpConnFactory;
	
	@Autowired
	private TaskResultService taskResultService;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	
	public void startThreadPool()
	{
		int task_consumer_thread_num = config.getResult_consumer_thread_num();
		
		for(int i = 0; i < task_consumer_thread_num; i++)
		{
			ResultQueueConsumerThread resultQueueConsumerThread = new ResultQueueConsumerThread(config, jmsTemplate,taskResultService);
			threadPoolExecutor.execute(resultQueueConsumerThread);
		}
	}
}
