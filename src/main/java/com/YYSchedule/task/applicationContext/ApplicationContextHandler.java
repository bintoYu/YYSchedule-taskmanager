package com.YYSchedule.task.applicationContext;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextHandler
{
	private AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml","classpath:spring/quartz.xml");
//	private AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
	
	private ApplicationContextHandler(){
		
	}
	
	private static final ApplicationContextHandler applicationContextHandler = new ApplicationContextHandler();
	
	public static ApplicationContextHandler getInstance() {
		return applicationContextHandler;
	}

	public AbstractApplicationContext getApplicationContext()
	{
		return applicationContext;
	}
	
}
