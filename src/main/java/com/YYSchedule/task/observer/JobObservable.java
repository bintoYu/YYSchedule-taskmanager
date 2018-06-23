package com.YYSchedule.task.observer;

import java.util.Observable;

import com.YYSchedule.common.pojo.Job;

/**
 * JobObservable.java
 * @author yubingtao
 */
public class JobObservable extends Observable
{
	public void received(Job job)
	{
		setChanged();
		notifyObservers(job);
	}
}
