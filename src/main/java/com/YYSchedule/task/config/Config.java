package com.YYSchedule.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("Config")
public class Config
{
	@Value("#{config.ftp_server_urls}")
	private String ftp_server_urls;
	
	@Value("#{config.max_queue_size}")
	private int max_queue_size;
	
	@Value("#{config.user_call_task_port}")
	private int user_call_task_port;
	
	@Value("#{config.node_call_task_port}")
	private int node_call_task_port;

	@Value("#{config.distribute_thread_num}")
	private int distribute_thread_num;
	
	
	
	public String getFtp_server_urls()
	{
		return ftp_server_urls;
	}

	public int getMax_queue_size()
	{
		return max_queue_size;
	}

	public int getUser_call_task_port()
	{
		return user_call_task_port;
	}

	public int getDistribute_thread_num()
	{
		return distribute_thread_num;
	}

	public int getNode_call_task_port()
	{
		return node_call_task_port;
	}

	
}
