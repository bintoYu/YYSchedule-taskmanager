/**
 * 
 */
package com.YYSchedule.task.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.Result;

@Component
@Scope("singleton")
public class FailureResultQueue {

	private static final Logger LOGGER = LoggerFactory.getLogger(FailureResultQueue.class);
	
	private BlockingQueue<Result> failureResultQueue = new ArrayBlockingQueue<Result>(MAX_QUEUE_SIZE);
	
	@Value("#{config.max_queue_size}")
	private static int MAX_QUEUE_SIZE;
	
	private FailureResultQueue() {
	}
	
	
	public int getMaxQueueSize() {
		return MAX_QUEUE_SIZE;
	}
	
	public synchronized BlockingQueue<Result> getFailureResultQueue() {
		return failureResultQueue;
	}
	
	public synchronized void addToFailureResultQueue(Result result) {
		if(failureResultQueue.size() <= MAX_QUEUE_SIZE-2){
			boolean isAdded = failureResultQueue.add(result);
			if(isAdded)
			{
				LOGGER.info("成功更新failureResultQueue, size : [ " + failureResultQueue.size() + " ].");
			}
		}else{
			LOGGER.error("failureResultQueue超过最大容量, size : [ " + failureResultQueue.size() + " ].");
		}
	}
	
	public synchronized void addToFailureResultQueue(Set<Result> resultSet) {
		if(failureResultQueue.size() <= MAX_QUEUE_SIZE-resultSet.size()-1){
			boolean isAdded =  failureResultQueue.addAll(resultSet);
			if(isAdded)
			{
				LOGGER.info("成功更新failureResultQueue, size : [ " + failureResultQueue.size() + " ].");
			}
		}else{
			LOGGER.error("failureResultQueue超过最大容量, size : [ " + failureResultQueue.size() + " ].");
		}
	}
	
	
	public synchronized List<Long> getTaskIdList() {
		List<Long> taskIdList = new ArrayList<Long>();
		for (Result result : getFailureResultQueue()) {
			taskIdList.add(result.getTaskId());
		}
		return taskIdList;
	}
	
}
