/**
 * 
 */
package com.YYSchedule.task.queue;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.job.Job;

/**
 * @author ybt
 *
 * @date 2019年1月31日  
 * @version 1.0  
 */
@Component
@Scope("singleton")
public class JobQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JobQueue.class);
	private LinkedBlockingQueue<Job> jobQueue = new LinkedBlockingQueue<Job>();
	
	public void add(Job job)
	{
		if(job == null)
			return ;
		jobQueue.add(job);
	}
	
	public void add(List<Job> jobList)
	{
		if(jobList == null || jobList.isEmpty())
			return ;
		jobQueue.addAll(jobList);
	}
	
	
	public Job takeJob()
	{
		Job job = null;
		try
		{
			job = jobQueue.take();
		} catch (InterruptedException e)
		{
			LOGGER.error("无法从jobQueue中取出job" + e.getMessage(), e);
		}
		
		return job;
	}
}
