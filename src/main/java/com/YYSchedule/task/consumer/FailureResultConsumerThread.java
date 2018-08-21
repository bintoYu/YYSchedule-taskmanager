/**
 * 
 */
package com.YYSchedule.task.consumer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.JMSException;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.mybatis.pojo.JobBasic;
import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskFile;
import com.YYSchedule.common.mybatis.pojo.TaskResult;
import com.YYSchedule.common.mybatis.pojo.TaskTimestamp;
import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.pojo.ResultStatus;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.Bean2BeanUtils;
import com.YYSchedule.store.ftp.FtpConnFactory;
import com.YYSchedule.store.ftp.FtpUtils;
import com.YYSchedule.store.service.JobBasicService;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskFileService;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.ResultStatusMapper;
import com.YYSchedule.task.queue.FailureResultQueue;

/**
 * @author ybt
 * 
 * @date 2018年8月8日
 * @version 1.0
 */
public class FailureResultConsumerThread implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FailureResultConsumerThread.class);
	
	private JmsTemplate jmsTemplate;
	
	private Config config;
	
	private TaskBasicService taskBasicService;
	
	private TaskResultService taskResultService;
	
	private JobBasicService jobBasicService;
	
	private TaskTimestampService taskTimestampService;
	
	private ResultStatusMapper resultStatusMapper;
	
	private FailureResultQueue failureResultQueue;
	
	/**
	 * @param jmsTemplate
	 * @param config
	 */
	public FailureResultConsumerThread(Config config, JmsTemplate jmsTemplate,TaskBasicService taskBasicService, TaskResultService taskResultService,TaskTimestampService taskTimestampService,JobBasicService jobBasicService,ResultStatusMapper resultStatusMapper,FailureResultQueue failureResultQueue)
	{
		this.jmsTemplate = jmsTemplate;
		this.config = config;
		this.taskBasicService = taskBasicService;
		this.taskResultService = taskResultService;
		this.taskTimestampService = taskTimestampService;
		this.jobBasicService = jobBasicService;
		this.resultStatusMapper = resultStatusMapper;
		this.failureResultQueue = failureResultQueue;
	}
	
	@Override
	public void run()
	{
		//TODO 从failureResultQueue中获取result
		//TODO 解析，并
	}
	

}
