package com.YYSchedule.task.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.YYSchedule.common.mybatis.pojo.MissionBasic;
import com.YYSchedule.common.pojo.Job;
import com.YYSchedule.common.rpc.domain.job.JobDistributionMode;
import com.YYSchedule.common.rpc.domain.job.JobOperationRequirement;
import com.YYSchedule.common.rpc.domain.job.JobPriority;
import com.YYSchedule.common.rpc.domain.job.JobResourceRequirement;
import com.YYSchedule.common.rpc.domain.mission.Mission;
import com.YYSchedule.common.rpc.domain.parameter.JobParameter;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.NotFoundException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.utils.JobUtils;
import com.YYSchedule.common.rpc.service.task.UserCallTaskService;
import com.YYSchedule.store.ftp.FtpConnFactory;
import com.YYSchedule.store.service.MissionBasicService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.JobMapper;
import com.YYSchedule.task.mapper.MissionMapper;

public class UserCallTaskServiceImpl implements UserCallTaskService.Iface
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UserCallTaskServiceImpl.class);
	
	@Override
	public String ping() throws UnavailableException, TimeoutException,
			TException
	{
		return new Date().toString();
	}

	@Override
	public long submitMission(Mission mission) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException
	{
		//获取用户上传的mission中的所有属性
		int userId = mission.getUserId();
		String missionName = mission.getMissionName();
		List<String> fileList = mission.getFileList();
		List<TaskPhase> taskPhaseList = mission.getTaskPhaseList();
		JobDistributionMode jobDistributionMode = mission.getJobDistributionMode();
		JobPriority jobPriority = mission.getJobPriority();
		List<JobOperationRequirement> jobOperationRequirementList = mission.getJobOperationRequirementList();
		List<JobResourceRequirement> jobResourceRequirementList = mission.getJobResourceRequirementList();
		List<JobParameter> parameterList = mission.getParameterList();
		long impatienceTime = mission.getImpatienceTime();
		
		//验证属性是否合法
		if (String.valueOf(userId).length() > 11 || taskPhaseList == null
				|| !JobUtils.isJobDistributionModeMember(jobDistributionMode)
				|| !JobUtils.isJobPriorityMember(jobPriority)
				|| jobOperationRequirementList == null
				|| jobResourceRequirementList == null || parameterList == null
				|| String.valueOf(impatienceTime).length() > 18) {
			
			LOGGER.error("Invalid submission parameters [ " + userId + " : "
					+ jobDistributionMode + " : " + jobPriority + " : "
					+ impatienceTime + " ].");
			throw new InvalidRequestException(
					"Invalid submission parameters [ " + userId + " : "
							+ jobDistributionMode + " : " + jobPriority + " : "
							+ impatienceTime + " ].");
		}
		
		//获取spring容器，并得到missionMapper及jobMapper
		ApplicationContext applicationContext =	ApplicationContextHandler.getInstance().getApplicationContext();
		MissionMapper missionMapper = applicationContext.getBean(MissionMapper.class);
		JobMapper jobMapper = applicationContext.getBean(JobMapper.class);
		
		//生成missionId，并且将missionBasic信息存入数据库中
		int missionId = missionMapper.generateMissionId(userId);
		MissionBasicService missionBasicService = applicationContext.getBean(MissionBasicService.class);
		MissionBasic missionBasic;
		try {
			missionBasic = new MissionBasic(missionId,missionName,userId);
			missionBasicService.insertMissionBasic(missionBasic);
			LOGGER.info("Received new mission[ " + missionId + "]" );
		} catch (ParseException pe) {
			LOGGER.error("sql注入mission_start_time时间错误！" + pe.getMessage(), pe);
		}

		//将mission中的所有文件上传到ftp上
		FtpConnFactory.connect(ftpHost)
		
		
		//根据任务种类，生成job并放入观察者中
		for(TaskPhase taskPhase : taskPhaseList)
		{
			Job job = new Job();
			long jobId = jobMapper.generateJobId(missionId);
			
			job.setJobId(jobId);
			job.setSubmitterId(userId);
			job.setJobDistributionMode(jobDistributionMode);
			job.setJobPriority(jobPriority);
			job.setJobOperationRequirementList(jobOperationRequirementList);
			job.setJobResourceRequirementList(jobResourceRequirementList);
			job.setJobStatus(0);
			job.setJobParameterList(parameterList);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String nowTime = df.format(new Date());
			Date time;
			try {
				time = df.parse(nowTime);
				job.setCommittedTime(time);
			} catch (ParseException pe) {
				LOGGER.error("sql注入job_committed_time错误！" + pe.getMessage(), pe);
			}
			job.setMissionId(missionId);
			
			
			
		}
			
			  
		return 0;
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

	
}
