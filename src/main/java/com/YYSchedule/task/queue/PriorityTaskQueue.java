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
public class PriorityTaskQueue {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityTaskQueue.class);
	
	private PriorityBlockingQueue<Task> priorityTaskQueue = new PriorityBlockingQueue<Task>();
	
//	private BlockingQueue<FailureTaskMapper> failureTaskQueue = new BlockingQueue<FailureTaskMapper>();
	
	@Value("#{config.max_queue_size}")
	private static int MAX_QUEUE_SIZE;
	
	/**
	 * 
	 */
	private PriorityTaskQueue() {
	}
	
	
	public int getMaxQueueSize() {
		return MAX_QUEUE_SIZE;
	}
	
	public synchronized PriorityBlockingQueue<Task> getPriorityTaskQueue() {
		return priorityTaskQueue;
	}
	
	
//	public synchronized BlockingQueue<FailureTaskMapper> getFailureTaskQueue() {
//		return failureTaskQueue;
//	}
	
	public synchronized void addToPriorityTaskQueue(Task task) {
		if(priorityTaskQueue.size() <= MAX_QUEUE_SIZE-2){
			boolean isAdded = priorityTaskQueue.add(task);
			if(isAdded)
			{
				LOGGER.info("成功更新priorityTaskQueue, size : [ " + priorityTaskQueue.size() + " ].");
			}
		}else{
			LOGGER.error("priorityTaskQueue超过最大容量, size : [ " + priorityTaskQueue.size() + " ].");
		}
	}
	
	public synchronized void addToPriorityTaskQueue(Set<Task> taskSet) {
		if(priorityTaskQueue.size() <= MAX_QUEUE_SIZE-taskSet.size()-1){
			boolean isAdded =  priorityTaskQueue.addAll(taskSet);
			if(isAdded)
			{
				LOGGER.info("成功更新priorityTaskQueue, size : [ " + priorityTaskQueue.size() + " ].");
			}
		}else{
			LOGGER.error("priorityTaskQueue超过最大容量, size : [ " + priorityTaskQueue.size() + " ].");
		}
	}
	
//	public synchronized boolean addToFailureTaskQueue(FailureTaskMapper FailureTaskMap) {
//		return failureTaskQueue.add(FailureTaskMap);
//	}
//	
//	public synchronized boolean addToFailureTaskQueue(Set<FailureTaskMapper> failureTaskSet) {
//		return failureTaskQueue.addAll(failureTaskSet);
//	}
	
	public synchronized List<Long> getTaskIdList() {
		List<Long> taskIdList = new ArrayList<Long>();
		for (Task task : getPriorityTaskQueue()) {
			taskIdList.add(task.getTaskId());
		}
		return taskIdList;
	}
	
//	public synchronized List<Long> getFailureTaskIdList(JobStatus failureStatus) {
//		List<Long> taskIdList = new ArrayList<Long>();
//		for(FailureTaskMapper mission : getFailureTaskQueue()) {
//			try{
//			    taskIdList.add(mission.getFailureTaskMap().get(failureStatus).getTaskId());
//			}catch(NullPointerException npe){}
//		}
//		return taskIdList;
//	}
//	
//	public synchronized List<Task> getFailureTaskList(JobStatus failureStatus) {
//		List<Task> taskList = new ArrayList<Task>();
//		for(FailureTaskMapper mission : getFailureTaskQueue()) {
//			try{
//				Task task = mission.getFailureTaskMap().get(failureStatus);
//				if(task != null){
//					taskList.add(task);
//				}
//			}catch(NullPointerException npe){}
//		}
//		return taskList;
//	}
}
