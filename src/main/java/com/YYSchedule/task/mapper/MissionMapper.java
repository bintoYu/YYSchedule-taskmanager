package com.YYSchedule.task.mapper;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.mybatis.pojo.UserBasic;
import com.YYSchedule.store.service.UserBasicService;

@Component("MissionMapper")
@Scope("singleton")
public class MissionMapper
{
	@Autowired
	private UserBasicService userBasicService;
	
	/**
	 * Map<UserId, MissionCount>
	 */
	private Map<Integer, AtomicInteger> missionCountMap = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	private MissionMapper(){
	}
	
	public synchronized Map<Integer, AtomicInteger> getMissionCountMap()
	{
		return missionCountMap;
	}

	
	/**
	 * init mission count map
	 * @param missionType
	 * @param missionCount
	 */
	public synchronized void initMissionCountMap(Integer userId, Integer missionCount) {
		if (missionCountMap.get(userId) == null) {
			missionCountMap.put(userId, new AtomicInteger(missionCount));
		}
	}
	
	/**
	 * get missionCount
	 * 
	 * @param userId
	 * @return
	 */
	public synchronized int getMissionCount(Integer userId)
	{
		if(missionCountMap.get(userId) != null)
		{
			return missionCountMap.get(userId).get();
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * increase and get mission count
	 * 
	 * @param userId
	 * @return
	 */
	public synchronized int increaseAndGetMissionCount(Integer userId) 
	{
		int missionCount = missionCountMap.get(userId).incrementAndGet(); 

		// update mission count in database
		UserBasic userBasic = new UserBasic();
		userBasic.setUserId(userId);
		userBasic.setMissionCount(missionCount);
		
		userBasicService.updateUserBasic(userBasic);
		return missionCount;
	}
	
	
	/**
	 * generate new mission id
	 * 
	 * @param 
	 * @return
	 */
	public synchronized int generateMissionId(Integer userId) {
		
		StringBuilder sbuilder = new StringBuilder();
		
		sbuilder.append(userId);
		
		int missionCount = increaseAndGetMissionCount(userId); 
		sbuilder.append(new DecimalFormat("000000").format(missionCount));
		
		return Integer.parseInt(sbuilder.toString());
	}
}
