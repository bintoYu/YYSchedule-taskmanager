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
import org.springframework.data.redis.core.RedisTemplate;
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
import com.YYSchedule.store.util.RedisUtils;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.ResultStatusMapper;
import com.YYSchedule.task.queue.FailureResultQueue;

/**
 * @author ybt
 * 
 * @date 2018年8月8日
 * @version 1.0
 */
public class ResultQueueConsumerThread implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultQueueConsumerThread.class);
	
	private JmsTemplate jmsTemplate;
	
	private RedisTemplate redisTemplate;
	
	private FtpConnFactory ftpConnFactory;
	
	private TaskBasicService taskBasicService;
	
	private TaskFileService taskFileService;
	
	private TaskResultService taskResultService;
	
	private JobBasicService jobBasicService;
	
	private TaskTimestampService taskTimestampService;
	
	private ResultStatusMapper resultStatusMapper;
	
	private FailureResultQueue failureResultQueue;
	
	/**
	 * @param jmsTemplate
	 * @param config
	 */
	public ResultQueueConsumerThread(FtpConnFactory ftpConnFactory, JmsTemplate jmsTemplate, RedisTemplate redisTemplate,TaskBasicService taskBasicService, TaskFileService taskFileService,
			TaskResultService taskResultService, TaskTimestampService taskTimestampService, JobBasicService jobBasicService, ResultStatusMapper resultStatusMapper,
			FailureResultQueue failureResultQueue)
	{
		this.ftpConnFactory = ftpConnFactory;
		this.jmsTemplate = jmsTemplate;
		this.redisTemplate = redisTemplate;
		this.taskBasicService = taskBasicService;
		this.taskFileService = taskFileService;
		this.taskResultService = taskResultService;
		this.taskTimestampService = taskTimestampService;
		this.jobBasicService = jobBasicService;
		this.resultStatusMapper = resultStatusMapper;
		this.failureResultQueue = failureResultQueue;
	}
	
	@Override
	public void run()
	{
		InetAddress address = null;
		try
		{
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		while (!Thread.currentThread().isInterrupted())
		{
			String resultQueue = address.getHostAddress() + ":" + "resultQueue";
			
			Result result = null;
			try
			{
				// 从队列distributeTaskQueue取出task
				result = ActiveMQUtils.receiveResult(jmsTemplate, resultQueue);
			} catch (JMSException e)
			{
				LOGGER.error("从队列" + resultQueue + "取result失败！" + e.getMessage());
			}
			
			if (result != null)
			{
				LOGGER.info("已从队列" + resultQueue + "中取出result [ " + result.getTaskId() + " ] ");
				
				// 更新数据库
				updateDatabase(result);
				
				// 将成功或失败结果放入结果状态统计模块中
				ResultStatus resultStatus = new ResultStatus(result);
				resultStatusMapper.updateResultStatus(resultStatus);
				
				// TODO 成功：将result发送到redis中,并且将ftp上的文件删除
				if (result.getTaskStatus() == TaskStatus.FINISHED)
				{
					deleteFromftp(result);
					
					RedisUtils.set(redisTemplate,result.getTaskPhase().toString(), result.getResult());
				}
				else
				{
					// 失败，将result存到FailureResultQueue中
					failureResultQueue.addToFailureResultQueue(result);
				}
				
			}
		}
	}
	
	private void updateDatabase(Result result)
	{
		// 修改job的状态信息
		long jobId = result.getTaskId() / 10000;
		JobBasic jobBasic = jobBasicService.getJobBasicById(jobId);
		jobBasic.setFinishNum(jobBasic.getFinishNum() + 1);
		jobBasicService.updateJobBasic(jobBasic);
		
		// 更新task的状态信息
		TaskBasic taskBasic = new TaskBasic();
		taskBasic.setTaskId(result.getTaskId());
		taskBasic.setTaskStatus(result.getTaskStatus().toString());
		taskBasicService.updateTaskBasic(taskBasic);
		
		if (result.getTaskStatus() == TaskStatus.FINISHED || result.getTaskStatus() == TaskStatus.FAILURE)
		{
			// result转化成taskResult，并存入数据库中
			TaskResult taskResult = Bean2BeanUtils.result2TaskResult(result);
			taskResultService.updateTaskResult(taskResult);
		}
		
		// 更新task的结束时间
		TaskTimestamp taskTimestamp = new TaskTimestamp();
		taskTimestamp.setTaskId(result.getTaskId());
		taskTimestamp.setFinishedTime(result.getFinishedTime());
		taskTimestampService.updateTaskTimestamp(taskTimestamp);
	}
	
	private void deleteFromftp(Result result)
	{
		TaskFile taskFile = taskFileService.getTaskFileById(result.getTaskId());
		FTPClient client = ftpConnFactory.connect();
		FtpUtils.deleteFtpFile(client, taskFile.getFilePath());
	}
}
