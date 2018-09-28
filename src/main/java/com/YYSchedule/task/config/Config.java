package com.YYSchedule.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("Config")
public class Config
{
	@Value("#{config.taskmanager_ip}")
	private String taskmanager_ip;
	
	@Value("#{config.ftp_server_urls}")
	private String ftp_server_urls;
	
	@Value("#{config.max_queue_size}")
	private int max_queue_size;
	
	@Value("#{config.user_call_task_port}")
	private int user_call_task_port;
	
	@Value("#{config.node_call_task_port}")
	private int node_call_task_port;

	@Value("#{config.distributor_thread_num}")
	private int distributor_thread_num;
	
	@Value("#{config.result_consumer_thread_num}")
	private int result_consumer_thread_num;
	
	@Value("#{config.offline_monitor_interval}")
	private int offline_monitor_interval;
	
	@Value("#{config.status_monitor_interval}")
	private int status_monitor_interval;

	@Value("#{config.node_success_rate}")
	private double node_success_rate;
	
	public String getTaskmanager_ip()
	{
		return taskmanager_ip;
	}

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

	public int getDistributor_thread_num()
	{
		return distributor_thread_num;
	}

	public int getNode_call_task_port()
	{
		return node_call_task_port;
	}

	public int getResult_consumer_thread_num()
	{
		return result_consumer_thread_num;
	}

	public int getOffline_monitor_interval()
	{
		return offline_monitor_interval;
	}

	public int getStatus_monitor_interval()
	{
		return status_monitor_interval;
	}

	public double getNode_success_rate()
	{
		return node_success_rate;
	}

}
