/**
 * 
 */
package com.YYSchedule.task.consumer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskResult;
import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.utils.Bean2BeanUtils;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.config.Config;

/**
 * @author Administrator
 * 
 * @date 2018年8月8日
 * @version 1.0
 */
public class ResultQueueConsumerThread implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultQueueConsumerThread.class);
	
	private JmsTemplate jmsTemplate;
	
	private Config config;
	
	private TaskResultService taskResultService;
	
	/**
	 * @param jmsTemplate
	 * @param config
	 */
	public ResultQueueConsumerThread(Config config, JmsTemplate jmsTemplate, TaskResultService taskResultService)
	{
		this.jmsTemplate = jmsTemplate;
		this.config = config;
		this.taskResultService = taskResultService;
	}
	
	@Override
	public void run()
	{
		InetAddress address = null;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String resultQueue = address.getHostAddress() + ":" + config.getNode_call_task_port() + ":" + "resultQueue";
		System.out.println(resultQueue);
		
		while (!Thread.currentThread().isInterrupted()) {
			Result result = null;
			TaskBasic taskBasic = null;
			try {
				// 从队列distributeTaskQueue取出task
				result = ActiveMQUtils.receiveResult(jmsTemplate, resultQueue);
			} catch (JMSException e) {
				LOGGER.error("从队列" + resultQueue + "取Task失败！" + e.getMessage());
			}
			
			if (result != null) {
				LOGGER.info("已从队列" + resultQueue + "中取出Task [ " + result.getTaskId() + " ] ");
				
				// result转化成taskResult，并存入数据库中
				TaskResult taskResult = Bean2BeanUtils.result2TaskResult(result);
				taskResultService.updateTaskResult(taskResult);
				
				//TODO 将result发送到redis中
			}
		}
	}
	
}
