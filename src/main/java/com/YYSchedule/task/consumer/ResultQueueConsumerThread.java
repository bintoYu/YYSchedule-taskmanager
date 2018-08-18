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

import com.YYSchedule.common.mybatis.pojo.JobBasic;
import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskResult;
import com.YYSchedule.common.mybatis.pojo.TaskTimestamp;
import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.pojo.ResultStatus;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.Bean2BeanUtils;
import com.YYSchedule.store.service.JobBasicService;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.ResultStatusMapper;

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
	
	private Config config;
	
	private TaskBasicService taskBasicService;
	
	private TaskResultService taskResultService;
	
	private JobBasicService jobBasicService;
	
	private TaskTimestampService taskTimestampService;
	
	private ResultStatusMapper resultStatusMapper;
	
	/**
	 * @param jmsTemplate
	 * @param config
	 */
	public ResultQueueConsumerThread(Config config, JmsTemplate jmsTemplate,TaskBasicService taskBasicService, TaskResultService taskResultService,TaskTimestampService taskTimestampService,JobBasicService jobBasicService,ResultStatusMapper resultStatusMapper)
	{
		this.jmsTemplate = jmsTemplate;
		this.config = config;
		this.taskBasicService = taskBasicService;
		this.taskResultService = taskResultService;
		this.taskTimestampService = taskTimestampService;
		this.jobBasicService = jobBasicService;
		this.resultStatusMapper = resultStatusMapper;
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
		String resultQueue = address.getHostAddress() + ":"  + "resultQueue";
		
		while (!Thread.currentThread().isInterrupted()) {
			Result result = null;
			try {
				// 从队列distributeTaskQueue取出task
				result = ActiveMQUtils.receiveResult(jmsTemplate, resultQueue);
			} catch (JMSException e) {
				LOGGER.error("从队列" + resultQueue + "取result失败！" + e.getMessage());
			}
			
			if (result != null) {
				LOGGER.info("已从队列" + resultQueue + "中取出result [ " + result.getTaskId() + " ] ");
				
				//更新数据库
				updateDatabase(result);
				
				//将成功或失败结果放入结果状态统计模块中
				ResultStatus resultStatus = new ResultStatus(result);
				resultStatusMapper.updateResultStatus(resultStatus);
				
				//TODO 将result发送到redis中
			}
		}
	}
	
	private void updateDatabase(Result result)
	{
		//修改job的状态信息
		long jobId = result.getTaskId() / 10000;
		JobBasic jobBasic = jobBasicService.getJobBasicById(jobId);
		jobBasic.setFinishNum(jobBasic.getFinishNum() + 1);
		jobBasicService.updateJobBasic(jobBasic);
		
		//更新task的状态信息
		TaskBasic taskBasic = new TaskBasic();
		taskBasic.setTaskId(result.getTaskId());
		taskBasic.setTaskStatus(result.getTaskStatus().toString());
		taskBasicService.updateTaskBasic(taskBasic);
		
		// result转化成taskResult，并存入数据库中
		TaskResult taskResult = Bean2BeanUtils.result2TaskResult(result);
		taskResultService.updateTaskResult(taskResult);
		
		//更新task的结束时间
		TaskTimestamp taskTimestamp = new TaskTimestamp();
		taskTimestamp.setTaskId(result.getTaskId());
		taskTimestamp.setFinishedTime(result.getFinishedTime());
		taskTimestampService.updateTaskTimestamp(taskTimestamp);
	}
	
}
