package com.YYSchedule.task.matcher;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.StringUtils;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.NodeItemMapper;

/**
 * 
 * @author ybt
 * 
 * @date 2018年7月2日
 * @version 1.0
 */
public class LoadBalancingMatcher
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancingMatcher.class);
	
	/**
	 * 根据TaskPhase，以及负载均衡算法 获取最适合的node
	 * 
	 * @param task
	 * @return executorId
	 */
	public String getMatchedNode(Task task) throws Exception
	{
		
		String executorId = task.getExecutorId();
		// 如果已经有了executorId，直接返回
		if (!StringUtils.isEmpty(executorId)) {
			return executorId;
		}
		
		NodeItem nodeItem = null;
		
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		NodeItemMapper nodeMapper = applicationContext.getBean(NodeItemMapper.class);
		
		ConcurrentHashMap<String, NodeItem> nodeMap = nodeMapper.getNodeMap(task.getTaskPhase());
		
		// 选出最合适的node
		if (nodeMap != null && nodeMap.size() != 0) {
			nodeItem = getNodeItem(nodeMap);
			// nodeItem = selectedNodeSet.last();
			if (nodeItem != null) {
				nodeMapper.updateNode(updateNodeItemPayload(nodeItem, 1));
			}
			else {
				task.setTaskStatus(TaskStatus.DISTRIBUTE_FAILED);
				LOGGER.error("没有合适的node给task [ " + task.getTaskId() + " ] " + " ,taskPhase: " + task.getTaskPhase());
				throw new Exception("没有合适的node给task [ " + task.getTaskId() + " ] " + " ,taskPhase: " + task.getTaskPhase());
			}
		}
		else {
			task.setTaskStatus(TaskStatus.DISTRIBUTE_FAILED);
			LOGGER.error("没有合适的node给task [ " + task.getTaskId() + " ] " + " ,taskPhase: " + task.getTaskPhase());
			throw new Exception("没有合适的node给task [ " + task.getTaskId() + " ] " + " ,taskPhase: " + task.getTaskPhase());
		}
		
		if (nodeItem != null) {
			executorId = nodeItem.getNodeId();
		}
		
		nodeItem = null;
		return executorId;
	}
	
	/**
	 * 找出最为适合的nodeItem节点
	 * 即分数最低的nodeItem节点
	 * 同时，忽略所有损坏的nodeItem
	 * @param nodeMap
	 * @return
	 */
	public static NodeItem getNodeItem(ConcurrentHashMap<String, NodeItem> nodeMap)
	{
		NodeItem selectedNodeItem = null;
		int minGrade = Integer.MAX_VALUE;
		
		for(String nodeId : nodeMap.keySet())
		{
			NodeItem nodeItem = nodeMap.get(nodeId);
			//忽略损坏的nodeItem
			if(nodeItem.isBroken() == false)
			{
				if(minGrade > nodeItem.getGrade())
				{
					minGrade = nodeItem.getGrade();
					selectedNodeItem = nodeItem;
				}
			}
		}
		return selectedNodeItem;

	}
	
	
	
	/**
	 * update nodeItem payload
	 * 
	 * @param nodeItem
	 * @param taskNum
	 * @return nodeItem
	 * @throws ResourceException
	 */
	public NodeItem updateNodeItemPayload(NodeItem nodeItem, int taskNum) throws Exception
	{
		nodeItem.setQueueLength(nodeItem.getQueueLength() + taskNum);
		nodeItem.updateGrade();
		return nodeItem;
	}
}
