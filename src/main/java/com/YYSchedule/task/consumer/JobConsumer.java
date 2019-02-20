/**
 * 
 */
package com.YYSchedule.task.consumer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.YYSchedule.common.mybatis.pojo.JobBasic;
import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.mybatis.pojo.TaskFile;
import com.YYSchedule.common.mybatis.pojo.TaskTemp;
import com.YYSchedule.common.mybatis.pojo.TaskTimestamp;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.job.Job;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.utils.Bean2BeanUtils;
import com.YYSchedule.store.service.JobBasicService;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.store.service.TaskFileService;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.store.service.TaskTempService;
import com.YYSchedule.store.service.TaskTimestampService;
import com.YYSchedule.store.util.RedisUtils;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.queue.JobQueue;
import com.YYSchedule.task.queue.PriorityTaskPool;
import com.YYSchedule.task.splitter.JobSplitter;
import com.alibaba.fastjson.JSONObject;

/**
 * @author ybt
 * 
 * @date 2019年2月1日
 * @version 1.0
 */
public class JobConsumer implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultQueueConsumer.class);
	
	private Config config;
	
	public RedisTemplate<String,String> redisTemplate;
	
	private TaskBasicService taskBasicService;
	
	private TaskFileService taskFileService;
	
	private JobBasicService jobBasicService;
	
	private TaskTempService taskTempService;
	
	private TaskTimestampService taskTimestampService;
	
	private JobQueue jobQueue;
	
	private PriorityTaskPool priorityTaskPool;
	
	public JobConsumer(Config config, RedisTemplate<String,String> redisTemplate, TaskBasicService taskBasicService, TaskFileService taskFileService, JobBasicService jobBasicService,
			TaskTempService taskTempService, TaskTimestampService taskTimestampService, JobQueue jobQueue, PriorityTaskPool priorityTaskPool)
	{
		super();
		this.config = config;
		this.redisTemplate = redisTemplate;
		this.taskBasicService = taskBasicService;
		this.taskFileService = taskFileService;
		this.jobBasicService = jobBasicService;
		this.taskTempService = taskTempService;
		this.taskTimestampService = taskTimestampService;
		this.jobQueue = jobQueue;
		this.priorityTaskPool = priorityTaskPool;
	}



	@Override
	public void run()
	{
		String threadName = "ResultQueueConsumer" + Thread.currentThread().getName().substring(Thread.currentThread().getName().length() - 2);
		LOGGER.info("开启线程" + threadName + "..........");
		while (!Thread.currentThread().isInterrupted())
		{
			Job job = jobQueue.takeJob();
			
			// 将job切分成task，并存入数据库
			List<Task> taskList = new ArrayList<>();
			List<TaskBasic> taskBasicList; 
			List<TaskFile> taskFileList;
			List<TaskTimestamp> taskTimestampList;
			List<TaskTemp> taskTempList;
			taskList = JobSplitter.split(job);
			taskBasicList = Bean2BeanUtils.taskList2TaskBasicList(taskList);
			taskFileList = Bean2BeanUtils.taskList2TaskFileList(taskList);
			taskTimestampList = Bean2BeanUtils.taskList2TaskTimestampList(taskList);
			taskTempList = Bean2BeanUtils.taskList2TaskTempList(taskList);
			
			taskBasicService.insertTaskBasicList(taskBasicList);
			taskFileService.insertTaskFileList(taskFileList);
			taskTimestampService.insertTaskTimestampList(taskTimestampList);
			taskTempService.insertTaskTempList(taskTempList);
			
			LOGGER.info("将切分成的taskList[容量:" + taskList.size() + "]放入缓存池taskPool中........");
			
			// 将taskList放入PriorityTaskQueue中
			int addNum = priorityTaskPool.add(taskList);
			
			LOGGER.info("成功将taskList放入缓存池中，存入个数:" + addNum);
			
			// 最后存储job的信息
			JobBasic jobBasic = Bean2BeanUtils.Job2JobBasic(job, taskList.size());
			try
			{
				jobBasicService.insertJobBasic(jobBasic);
				
			} catch (Exception e)
			{
				LOGGER.error("无法将jobBasic存入数据库: " + jobBasic + " : " + e.getMessage(), e);
				throw new SqlSessionException("无法将jobBasic存入数据库: " + jobBasic + " : " + e.getMessage());
			}
		}
	}
	
}
