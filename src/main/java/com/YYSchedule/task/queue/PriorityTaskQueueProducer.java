/**
 * 
 */
package com.YYSchedule.task.queue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.pojo.Task;

public class PriorityTaskQueueProducer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityTaskQueueProducer.class);
	
	private final BlockingQueue<Task> priorityTaskQueue;
	
	private Task task;
	
	private List<Task> taskList;
	
	private PriorityTaskQueue taskQueue;
	
	/**
	 * 
	 */
	public PriorityTaskQueueProducer(PriorityTaskQueue taskQueue) {
		this.taskQueue = taskQueue;
		this.priorityTaskQueue = taskQueue.getPriorityTaskQueue();
	}
	
	public PriorityTaskQueueProducer(PriorityTaskQueue taskQueue, Task task) {
		this.taskQueue = taskQueue;
		this.priorityTaskQueue = taskQueue.getPriorityTaskQueue();
		this.task = task;
	}
	
	public PriorityTaskQueueProducer(PriorityTaskQueue taskQueue, List<Task> taskList) {
		this.taskQueue = taskQueue;
		this.priorityTaskQueue = taskQueue.getPriorityTaskQueue();;
		this.taskList = taskList;
		taskList = null;
	}


	/**
	 * 将task或taskList存入taskQueue中
	 * @return
	 */
	private boolean sendTaskToPriorityTaskQueue(Task task) {
		
		Set<Task> taskSet = new HashSet<Task>();
		
		int limit = taskQueue.getMaxQueueSize();
		if (task != null && !taskQueue.getTaskIdList().contains(task.getTaskId())) {
			limit = limit - priorityTaskQueue.size() - 1;
			taskSet.add(task);
		} 
		
		if(taskSet != null && taskSet.size() != 0 && priorityTaskQueue.addAll(taskSet)) {
			LOGGER.info("成功更新priorityTaskQueue, size : [ " + priorityTaskQueue.size() + " ].");
		} else {
			LOGGER.info("没有task放入priorityTaskQueue中, size : [ " + priorityTaskQueue.size() + " ].");
		}
		taskSet = null;
		
		return taskSet;
	}

	/**
	 * 
	 */
	public void shutdown() {
		Thread.currentThread().interrupt();
	}
}
