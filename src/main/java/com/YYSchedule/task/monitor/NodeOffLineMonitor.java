package com.YYSchedule.task.monitor;

import java.util.List;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.NodeItemMapper;

/**
 * @author ybt
 * 
 * @date 2018年8月13日
 * @version 1.0
 */
public class NodeOffLineMonitor
{
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private NodeItemMapper nodeMapper;
	
	@Autowired
	private Config config;
	
	/**
	 * 定时扫描nodeMap中的所有node节点 因为node节点可能由于网络问题、down机等因素导致无法与taskmanager进行通信
	 * 所以需要将这些node节点进行清理 清理依据：如果node节点在三个心跳间隔中没有予以心跳信息，便进行清理 具体清理方法见clear()方法
	 */
	public void monitor()
	{
		List<NodeItem> allNode = nodeMapper.getAllNode();
		
		for (NodeItem nodeItem : allNode)
		{
			int time = (int) (System.currentTimeMillis() - nodeItem.getUpdatedTime());
			
			if (time >= config.getOffline_monitor_interval() * 3)
			{
				clear(nodeItem);
			}
		}
	}
	
	/**
	 * 清理的过程包括, 
	 * 1、将mapper里面的node元素进行移除 
	 * 2、将node的队列信息取出来，重新扔到priorityTaskQueue中
	 */
	public boolean clear(NodeItem node)
	{
		boolean isCleared = false;
		String queue = node.getNodeId() + ":distributeTaskQueue";
		String proirityTaskQueue = "";
		boolean isRemoved = nodeMapper.removeNode(node);
		if (isRemoved)
		{
			while (true)
			{
				Task task = new Task();
				try
				{
					task = ActiveMQUtils.receiveTask(jmsTemplate, queue);
					
					if (task == null)
						break;
	
					ActiveMQUtils.sendTask(jmsTemplate, proirityTaskQueue, task);
				} catch (JMSException e)
				{
					e.printStackTrace();
				}
			}
			isCleared = true;
		}
		
		return isCleared;
	}
}