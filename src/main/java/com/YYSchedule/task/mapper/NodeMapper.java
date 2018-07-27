package com.YYSchedule.task.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;

/**
 * NodeMapper.java
 * 
 * @author yubingtao
 * @date 2018-7-2
 * @description
 */
@Component("NodeMapper")
@Scope("singleton")
public class NodeMapper {


	/**
	 * Map<TaskPhase, List<NodeItem>>
	 */
	private Map<TaskPhase, ConcurrentSkipListSet<NodeItem>> nodeMap = new ConcurrentHashMap<TaskPhase, ConcurrentSkipListSet<NodeItem>>();

	private NodeMapper() {
	}

	public synchronized Map<TaskPhase, ConcurrentSkipListSet<NodeItem>> getNodeMap() {
		return nodeMap;
	}

	/**
	 * init node map with taskPhase
	 * 
	 * @param TaskPhase
	 * 
	 */
	public synchronized ConcurrentSkipListSet<NodeItem> initNodeMapWithTaskPhase(TaskPhase taskPhase) {
		if (nodeMap.get(taskPhase) == null) {
			ConcurrentSkipListSet<NodeItem> nodeSet = new ConcurrentSkipListSet<NodeItem>();
			nodeMap.put(taskPhase, nodeSet);
			return nodeSet;
		} else {
			return null;
		}
	}

	/**
	 * init general node map with node type and node item
	 * 
	 * @param nodeTypeList
	 * @param nodeItem
	 */
	public synchronized void initNodeMapWithNodeItem(NodeItem nodeItem) {

		ConcurrentSkipListSet<NodeItem> nodeSet = nodeMap.get(nodeItem.getTaskPhase());
		if (nodeSet == null) {
			initNodeMapWithTaskPhase(nodeItem.getTaskPhase());
		} else if (!nodeSet.contains(nodeItem)) {
			nodeSet.add(nodeItem);
		}
		
	}
	

	/**
	 * update general node when received heart beat
	 * 
	 * @param nodeItem
	 * @return nodeSet
	 */
	public synchronized void updateNode(NodeItem nodeItem) {
		ConcurrentSkipListSet<NodeItem> nodeSet = nodeMap.get(nodeItem.getTaskPhase());
		if (nodeSet == null) {
			nodeSet = initNodeMapWithTaskPhase(nodeItem.getTaskPhase());
		} else if (nodeSet.contains(nodeItem)) {
			nodeSet.remove(nodeItem);
		} 
	
		if (nodeSet != null) {
			nodeSet.add(nodeItem);
		}
	}
	
	public synchronized ConcurrentSkipListSet<NodeItem> getNodeSet(TaskPhase taskPhase) {
		return nodeMap.get(taskPhase);
	}

	
}
