package com.YYSchedule.task.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.pojo.Job;
import com.YYSchedule.common.pojo.Task;

public class JobObserver implements Observer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JobObserver.class);
	
	@Override
	public void update(Observable observable, Object obj)
	{
		Job job = (Job)obj;
		LOGGER.info("Received new job [ " + job.getJobId() + " ] ");
		
		List<Task> taskList = new ArrayList<Task>();
	}
	
}
