/**
 * 
 */
package com.YYSchedule.task.mapper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.mybatis.pojo.MissionBasic;
import com.YYSchedule.store.service.MissionBasicService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;

/**
 * @author ybt
 *
 * @date 2018年7月4日  
 * @version 1.0  
 */
@Component
public class JobMapperProducer 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JobMapperProducer.class);

	@Autowired
	private MissionBasicService missionBasicService;
	
	@Autowired
	private JobMapper jobMapper;
	
	public void start()
	{
		LOGGER.info("Start initializing job mapper ...");
		
		List<MissionBasic> missionBasicList = missionBasicService.getMissionBasicList();
		
		for(MissionBasic missionBasic : missionBasicList)
		{
			Integer missionId = missionBasic.getMissionId();
			Integer jobCount = missionBasic.getJobCount();
			jobMapper.initJobCountMap(missionId, jobCount);
		}
	}
	
}
