package com.YYSchedule.task.start;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.common.rpc.service.task.UserCallTaskService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.consumer.ResultQueueConsumerPool;
import com.YYSchedule.task.distributor.TaskDistributorPool;
import com.YYSchedule.task.mapper.JobMapperProducer;
import com.YYSchedule.task.mapper.MissionMapperProducer;
import com.YYSchedule.task.service.impl.NodeCallTaskServiceImpl;
import com.YYSchedule.task.service.impl.UserCallTaskServiceImpl;

public class StartUp
{
	private AbstractApplicationContext applicationContext;
	
	private Config config;
	
	public StartUp(AbstractApplicationContext applicationContext)
	{
		super();
		this.applicationContext = applicationContext;
		this.config = applicationContext.getBean(Config.class);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(StartUp.class);
	
	public void startQueueThread()
	{
		applicationContext.getBean(TaskDistributorPool.class).startThreadPool();
		applicationContext.getBean(ResultQueueConsumerPool.class).startThreadPool();
	}
	
	public void startMapperProducer()
	{
		applicationContext.getBean(MissionMapperProducer.class).start();
		applicationContext.getBean(JobMapperProducer.class).start();
	}
	
	public void startUserCallTaskService()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				startUpUserCallTaskService();
			}
			
			public void startUpUserCallTaskService()
			{
				try {
					final TServerTransport serverTransport 
					= new TServerSocket(config.getUser_call_task_port());
					final UserCallTaskService.Processor<UserCallTaskService.Iface> processor
					= new UserCallTaskService.Processor<UserCallTaskService.Iface>(new UserCallTaskServiceImpl());
					final TServer server 
					= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("UserCallTask Server start listening on port : [ " + config.getUser_call_task_port() + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup UserCallTask Server at port : " + config.getUser_call_task_port() + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup UserCallTask Server at port : " + config.getUser_call_task_port() + " : " + tte.getMessage(), tte);
				}
			}
		}).start();
	}
	
	public void startNodeCallTaskService()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				startUpNodeCallTaskService();
			}
			
			public void startUpNodeCallTaskService()
			{
				try {
					final TServerTransport serverTransport 
					= new TServerSocket(config.getNode_call_task_port());
					final NodeCallTaskService.Processor<NodeCallTaskService.Iface> processor
					= new NodeCallTaskService.Processor<NodeCallTaskService.Iface>(new NodeCallTaskServiceImpl());
					final TServer server 
					= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("NodeCallTask Server start listening on port : [ " + config.getNode_call_task_port() + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup NodeCallTask Server at port : " + config.getNode_call_task_port() + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup NodeCallTask Server at port : " + config.getNode_call_task_port() + " : " + tte.getMessage(), tte);
				}
			}
		}).start();
	}
	
	public static void main(String[] args)
	{
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		StartUp startUp = new StartUp(applicationContext);
		startUp.startUserCallTaskService();
		startUp.startNodeCallTaskService();
		startUp.startQueueThread();
		startUp.startMapperProducer();
		LOGGER.info("taskmanager注册成功！");
	}
}
