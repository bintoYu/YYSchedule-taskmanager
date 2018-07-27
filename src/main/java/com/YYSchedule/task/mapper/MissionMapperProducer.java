/**
 * 
 */
package com.YYSchedule.task.mapper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.mybatis.pojo.UserBasic;
import com.YYSchedule.store.service.UserBasicService;

/**
 * @author ybt
 *
 * @date 2018年7月4日  
 * @version 1.0  
 */
@Component
public class MissionMapperProducer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MissionMapperProducer.class);

	@Autowired
	private UserBasicService userBasicService;
	
	@Autowired
	private MissionMapper missionMapper;
	
	public void start()
	{
		LOGGER.info("Start initializing job mapper ...");
		
		List<UserBasic> userBasicList = userBasicService.getUserBasicList();
		
		for(UserBasic userBasic : userBasicList)
		{
			Integer userId = userBasic.getUserId();
			Integer missionCount = userBasic.getMissionCount();
			missionMapper.initMissionCountMap(userId, missionCount);
		}
	}
	
}
