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

public class PriorityTaskQueueProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityTaskQueueProducer.class);
	
	private final BlockingQueue<Task> priorityTaskQueue;
	
	private Task task;
	
	private List<Task> taskList;
	
	private TaskQueue taskQueue;
	
	/**
	 * 
	 */
	public PriorityTaskQueueProducer(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
		this.priorityTaskQueue = taskQueue.getPriorityTaskQueue();
	}
	
	public PriorityTaskQueueProducer(TaskQueue taskQueue, Task task) {
		this.taskQueue = taskQueue;
		this.priorityTaskQueue = taskQueue.getPriorityTaskQueue();
		this.task = task;
	}
	
	public PriorityTaskQueueProducer(TaskQueue taskQueue, List<Task> taskList) {
		this.taskQueue = taskQueue;
		this.priorityTaskQueue = taskQueue.getPriorityTaskQueue();;
		this.taskList = taskList;
		taskList = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("开启globalTaskQueue ...");
		Set<Task> taskSet = produce();
		
		if(taskSet != null && taskSet.size() != 0 && priorityTaskQueue.addAll(taskSet)) {
			LOGGER.info("成功更新globalTaskQueue, size : [ " + priorityTaskQueue.size() + " ].");
		} else {
			LOGGER.info("没有task放入globalTaskQueue中, size : [ " + priorityTaskQueue.size() + " ].");
		}
		taskSet = null;
	}

	/**
	 * 将task或taskList存入taskQueue中
	 * @return
	 */
	private Set<Task> produce() {
		
		Set<Task> taskSet = new HashSet<Task>();
		
		// get upper bound of task num to be added
		int limit = taskQueue.getMaxQueueSize();
		if (task != null && !taskQueue.getTaskIdList().contains(task.getTaskId())) {
			limit = limit - priorityTaskQueue.size() - 1;
			taskSet.add(task);
		} else if (taskList != null && taskList.size() != 0) {
			limit = limit - priorityTaskQueue.size() - taskList.size();
			taskSet.addAll(taskList);
		}
		return taskSet;
	}

	/**
	 * 
	 */
	public void shutdown() {
		Thread.currentThread().interrupt();
	}
}
