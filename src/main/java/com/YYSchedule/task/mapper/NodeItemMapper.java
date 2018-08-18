package com.YYSchedule.task.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;

/**
 * NodeMapper.java
 * 
 * @author ybt
 * @date 2018-7-2
 * @description
 */
@Component("NodeMapper")
@Scope("singleton")
public class NodeItemMapper {


	/**
	 * Map<TaskPhase,Map<nodeId,NodeItem>>
	 */
	private Map<TaskPhase, ConcurrentHashMap<String,NodeItem>> map = new ConcurrentHashMap<TaskPhase, ConcurrentHashMap<String,NodeItem>>();

	private NodeItemMapper() {
	}

	public synchronized Map<TaskPhase, ConcurrentHashMap<String,NodeItem>> getNodeMap() {
		return map;
	}

	/**
	 * init node nodeMap with taskPhase
	 * 
	 * @param TaskPhase
	 * 
	 */
	public synchronized ConcurrentHashMap<String,NodeItem> initNodeMapWithTaskPhase(TaskPhase taskPhase) {
		if (map.get(taskPhase) == null) {
			ConcurrentHashMap<String,NodeItem> nodeMap = new ConcurrentHashMap<String,NodeItem>();
			map.put(taskPhase, nodeMap);
			return nodeMap;
		} else {
			return null;
		}
	}

	/**
	 * init general node nodeMap with node type and node item
	 * 
	 * @param nodeTypeList
	 * @param nodeItem
	 */
	public synchronized void initNodeMapWithNodeItem(NodeItem nodeItem) {

		ConcurrentHashMap<String,NodeItem> nodeMap = map.get(nodeItem.getTaskPhase());
		if (nodeMap == null) {
			initNodeMapWithTaskPhase(nodeItem.getTaskPhase());
		} else if (nodeMap.get(nodeItem.getNodeId()) == null ) {
			nodeMap.put(nodeItem.getNodeId(), nodeItem);
		}
		
	}
	

	/**
	 * 更新任务节点信息nodeItem
	 * 
	 * @param nodeItem
	 * @return nodeSet
	 */
	public synchronized void updateNode(NodeItem nodeItem) {
		ConcurrentHashMap<String,NodeItem> nodeMap = map.get(nodeItem.getTaskPhase());
		String nodeId = nodeItem.getNodeId();
		if (nodeMap == null) 
		{
			nodeMap = initNodeMapWithTaskPhase(nodeItem.getTaskPhase());
		}
		else if(nodeMap.get(nodeId) != null)
		{
				nodeMap.remove(nodeId);
		}
		
		nodeMap.put(nodeId, nodeItem);
	}
	
	public synchronized ConcurrentHashMap<String,NodeItem> getNodeMap(TaskPhase taskPhase) {
		return map.get(taskPhase);
	}

	public synchronized boolean removeNode(NodeItem nodeItem)
	{
		ConcurrentHashMap<String,NodeItem> nodeMap = map.get(nodeItem.getTaskPhase());
		
		if (nodeMap != null) {
			nodeMap.remove(nodeItem.getNodeId());
		}
		
		return true;
	}
	
	public synchronized NodeItem getNode(NodeItem nodeItem)
	{
		ConcurrentHashMap<String, NodeItem> concurrentHashMap = map.get(nodeItem.getTaskPhase());
		if(concurrentHashMap == null)
		{
			return null;
		}
		else
		{
			return concurrentHashMap.get(nodeItem.getNodeId());
		}
		
	}
	
	public synchronized List<NodeItem> getAllNode()
	{
		List<NodeItem> nodeItemList = new ArrayList<>();
		
		/*************************遍历map************************/
		for(TaskPhase taskPhase : map.keySet())
		{
			ConcurrentHashMap<String,NodeItem> nodeMap = map.get(taskPhase);

			for (String nodeId : nodeMap.keySet()) {
				nodeItemList.add(nodeMap.get(nodeId));
			}
		}
		
		return nodeItemList;
	}
}
