package com.YYSchedule.task.applicationContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextHandler
{
	private ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
	
	private ApplicationContextHandler(){
		
	}
	
	private static final ApplicationContextHandler applicationContextHandler = new ApplicationContextHandler();
	
	public static ApplicationContextHandler getInstance() {
		return applicationContextHandler;
	}

	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}
	
}
