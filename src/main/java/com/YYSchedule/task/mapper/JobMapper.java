package com.YYSchedule.task.mapper;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.mybatis.pojo.MissionBasic;
import com.YYSchedule.store.service.MissionBasicService;

@Component("JobMapper")
@Scope("singleton")
public class JobMapper
{
	@Autowired
	private MissionBasicService missionBasicService;
	
	/**
	 * Map<MissionId, JobCount>
	 */
	private Map<Integer, AtomicInteger> jobCountMap = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	private JobMapper(){
	}
	
	public synchronized Map<Integer, AtomicInteger> getJobCountMap()
	{
		return jobCountMap;
	}

	
	/**
	 * init job count map
	 * @param jobType
	 * @param jobCount
	 */
	public synchronized void initJobCountMap(Integer missionId, Integer jobCount) {
		if (jobCountMap.get(missionId) == null) {
			jobCountMap.put(missionId, new AtomicInteger(jobCount));
		}
	}
	
	/**
	 * get jobCount
	 * 
	 * @param missionId
	 * @return
	 */
	public synchronized int getJobCount(Integer missionId)
	{
		if(jobCountMap.get(missionId) != null)
		{
			return jobCountMap.get(missionId).get();
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * increase and get job count
	 * 
	 * @param missionId
	 * @return
	 */
	public synchronized int increaseAndGetJobCount(Integer missionId) 
	{
		int jobCount = jobCountMap.get(missionId).incrementAndGet(); 

		// update job count in database
		MissionBasic missionBasic = new MissionBasic();
		missionBasic.setMissionId(missionId);
		missionBasic.setJobCount(jobCount);
		
		missionBasicService.updateMissionBasic(missionBasic);
		return jobCount;
	}
	
	
	/**
	 * generate new job id
	 * 
	 * @param 
	 * @return
	 */
	public synchronized long generateJobId(Integer missionId) {
		
		StringBuilder sbuilder = new StringBuilder();
		
		sbuilder.append(missionId);
		
		long jobCount = increaseAndGetJobCount(missionId); 
		sbuilder.append(new DecimalFormat("00").format(jobCount));
		
		return Long.parseLong(sbuilder.toString());
	}
}
