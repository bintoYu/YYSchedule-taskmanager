package com.YYSchedule.task.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.mybatis.pojo.MissionBasic;
import com.YYSchedule.common.mybatis.pojo.UserBasic;
import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.rpc.domain.job.Job;
import com.YYSchedule.common.rpc.domain.job.JobPriority;
import com.YYSchedule.common.rpc.domain.mission.Mission;
import com.YYSchedule.common.rpc.domain.node.NodePayload;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.NotFoundException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.task.UserCallTaskService;
import com.YYSchedule.common.utils.Bean2BeanUtils;
import com.YYSchedule.store.service.MissionBasicService;
import com.YYSchedule.store.service.UserBasicService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.JobMapper;
import com.YYSchedule.task.mapper.MissionMapper;
import com.YYSchedule.task.mapper.NodeItemMapper;
import com.YYSchedule.task.queue.JobQueue;

/**
 * @author ybt
 *
 * @date 2018年6月  
 * @version 1.0  
 */
public class UserCallTaskServiceImpl implements UserCallTaskService.Iface
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UserCallTaskServiceImpl.class);
	
	@Override
	public String ping() throws UnavailableException, TimeoutException, TException
	{
		return new Date().toString();
	}

	@Override
	public long submitMission(Mission mission) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException
	{	
		//获取用户上传的mission的公用属性
		List<Job> jobList = mission.getJobList();
		//long impatienceTime = mission.getImpatienceTime();
		
		//验证mission是否合法
		validateMission(mission);
		
		//获取spring容器，得到需要的bean
		AbstractApplicationContext applicationContext =	ApplicationContextHandler.getInstance().getApplicationContext();
		MissionMapper missionMapper = applicationContext.getBean(MissionMapper.class);
		JobMapper jobMapper = applicationContext.getBean(JobMapper.class);
		MissionBasicService missionBasicService = applicationContext.getBean(MissionBasicService.class);
		JobQueue jobQueue = applicationContext.getBean(JobQueue.class);
		
		//生成missionId，并且将missionBasic信息存入数据库中
		int missionId = missionMapper.generateMissionId(mission.getUserId());
		LOGGER.info("success to generate missionId:" + missionId);
		mission.setMissionId(missionId);
		MissionBasic missionBasic = Bean2BeanUtils.Mission2MissionBasic(mission);
		missionBasicService.insertMissionBasic(missionBasic);
		LOGGER.info("Received new mission[ " + missionId + "]" );
		
		
		//根据任务种类，获得job,转化成JobBasic存入数据库，再对job进行切分
		//新生成的mission需要对jobCountMap进行初始化
		jobMapper.initJobCountMap(missionId, 0);
		for(Job job : jobList)
		{
			long jobId = jobMapper.generateJobId(missionId);
			job.setJobId(jobId);
			
			jobQueue.add(job);
			
		}
		return missionId;
	}

	@Override
	public int terminateMission(int missionId) throws InvalidRequestException,
			UnavailableException, NotFoundException, TimeoutException,
			TException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String obtainQueueInfo() throws UnavailableException,
			TimeoutException, TException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void validateMission(Mission mission) throws InvalidRequestException
	{
		int userId = mission.getUserId();
		List<Job> jobList = mission.getJobList();
		
		UserBasicService userBasicService = ApplicationContextHandler.getInstance().getApplicationContext().getBean(UserBasicService.class);
		UserBasic userBasic = userBasicService.getUserBasicById(userId);
		if(userBasic == null)
		{
			LOGGER.error("用户提交Mission中的userId不在数据库中, userId : " + userId);
			throw new InvalidRequestException("用户提交Mission中的userId不在数据库中, userId : " + userId);
		}
		
		for(Job job : jobList)
		{
			int isJobPriorityValid = 0;
			JobPriority jobPriority = job.getJobPriority();
			JobPriority[] jobPriorities = JobPriority.values();
			for (JobPriority priority : jobPriorities) {
				if (priority.equals(jobPriority)) {
					isJobPriorityValid = 1;
				}
			}
			if(isJobPriorityValid == 0)
			{
				LOGGER.error("Invalid mission parameters, jobPriority : " + jobPriority);
				throw new InvalidRequestException("Invalid mission parameters, jobPriority : " + jobPriority);
			}
			
			int isTaskPhaseValid = 0;
			TaskPhase taskPhase = job.getTaskPhase();
			TaskPhase[] taskPhases = TaskPhase.values();
			for(TaskPhase phase : taskPhases)
			{
				if(phase.equals(taskPhase))
				{
					isTaskPhaseValid = 1;
				}
			}
			if(isTaskPhaseValid == 0)
			{
				LOGGER.error("Invalid mission parameters, taskPhase : " + taskPhase);
				throw new InvalidRequestException("Invalid mission parameters, taskPhase : " + taskPhase);
			}
		}
	}

	/* 
	 * 获取所有node节点信息
	 * @return nodePayloadList
	 */
	@Override
	public List<NodePayload> obtainAllNode() throws UnavailableException, TimeoutException, TException
	{
		List<NodePayload> nodePayloadList = new ArrayList<>();
		
		/****************获取NodeMapper(taskmanager存储node节点信息的容器)******************/
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		NodeItemMapper nodeMapper = applicationContext.getBean(NodeItemMapper.class);
		List<NodeItem> nodeItemList = nodeMapper.getAllNode();
		
		for (NodeItem nodeItem : nodeItemList) {
			NodePayload nodePayload = Bean2BeanUtils.nodeItem2NodePayload(nodeItem);
			nodePayloadList.add(nodePayload);
		}
		
		return nodePayloadList;
	}
}
