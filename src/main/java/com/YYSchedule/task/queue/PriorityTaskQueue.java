/**
 * 
 */
package com.YYSchedule.task.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.Task;

@Component
@Scope("singleton")
public class PriorityTaskQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityTaskQueue.class);
	
	@Value("#{config.max_queue_size}")
	private int MAX_QUEUE_SIZE;
	
	private PriorityBlockingQueue<Task> priorityTaskQueue = new PriorityBlockingQueue<Task>();
	
	
	public synchronized PriorityBlockingQueue<Task> getPriorityTaskQueue()
	{
		return priorityTaskQueue;
	}
	
	public synchronized void addToPriorityTaskQueue(Task task)
	{
		if(task == null)
		{
			return ;
		}
		
		if (priorityTaskQueue.size() <= MAX_QUEUE_SIZE - 2)
		{
			boolean isAdded = priorityTaskQueue.add(task);
			if (isAdded)
			{
				LOGGER.info("成功更新priorityTaskQueue, size : [ " + priorityTaskQueue.size() + " ].");
			}
		}
		else
		{
			LOGGER.error("priorityTaskQueue超过最大容量, size : [ " + priorityTaskQueue.size() + " ].");
		}
	}
	
	public synchronized void addToPriorityTaskQueue(Set<Task> taskSet)
	{
		if(taskSet.isEmpty())
		{
			return ;
		}
		
		if (priorityTaskQueue.size() <= MAX_QUEUE_SIZE - taskSet.size() - 1)
		{
			boolean isAdded = priorityTaskQueue.addAll(taskSet);
			if (isAdded)
			{
				LOGGER.info("成功更新priorityTaskQueue, size : [ " + priorityTaskQueue.size() + " ].");
			}
		}
		else
		{
			LOGGER.error("priorityTaskQueue超过最大容量, size : [ " + priorityTaskQueue.size() + " ].");
		}
	}
	
	public synchronized List<Long> getTaskIdList()
	{
		List<Long> taskIdList = new ArrayList<Long>();
		for (Task task : priorityTaskQueue)
		{
			taskIdList.add(task.getTaskId());
		}
		return taskIdList;
	}
	
	public Task takeTask()
	{
		Task task = null;
		try
		{
			task = priorityTaskQueue.take(); 
		} catch (Exception e)
		{
			LOGGER.error("无法从priorityTaskQueue中取出task" + e.getMessage(), e);
		}
		
		return task;
	}
	
}
