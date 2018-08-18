/**
 * 
 */
package com.YYSchedule.task.mapper;

import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.ResultStatus;

/**
 * @author ybt
 *
 * @date 2018年8月14日  
 * @version 1.0  
 */
@Component("ResultStatusMapper")
@Scope("singleton")
public class ResultStatusMapper
{
	private Map<String,ConcurrentSkipListSet<ResultStatus>> resultMap = new ConcurrentHashMap<String, ConcurrentSkipListSet<ResultStatus>>();

	public ResultStatusMapper()
	{
		super();
	}

	public synchronized Map<String, ConcurrentSkipListSet<ResultStatus>> getResultMap()
	{
		return resultMap;
	}

	/**
	 * @param nodeId
	 * @return
	 */
	public synchronized ConcurrentSkipListSet<ResultStatus> initResultMapWithResultStatus(String nodeId)
	{
		if(resultMap.get(nodeId) == null)
		{
			ConcurrentSkipListSet<ResultStatus> set = new ConcurrentSkipListSet<>();
			resultMap.put(nodeId, set);
			return set;
		}else
			return null;
	}
	
	/**
	 * @param resultStatus
	 */
	public synchronized void updateResultStatus(ResultStatus resultStatus) {
		ConcurrentSkipListSet<ResultStatus> set = resultMap.get(resultStatus.getNodeId());
		if (set == null) {
			set = initResultMapWithResultStatus(resultStatus.getNodeId());
		} else if (set.contains(resultStatus)) {
			set.remove(resultStatus);
		} 
	
		if (set != null) {
			set.add(resultStatus);
		}
	}
	
	//注意，resultSet是按照时间顺序从小到大，来进行存储的，也就是说，取出来的第一个是时间最小的。
	public synchronized ConcurrentSkipListSet<ResultStatus> getResultSet(String nodeId) {
		return resultMap.get(nodeId);
	}
	
	public synchronized NavigableSet<ResultStatus> getResultSet(String nodeId,long startTime,long endTime)
	{
		ConcurrentSkipListSet<ResultStatus> set = resultMap.get(nodeId);
		ResultStatus start = new ResultStatus(startTime);
		ResultStatus end = new ResultStatus(endTime);
		
		if(set == null)
			return null;
		else 
			return set.subSet(start, true, end, true);
	}
}
