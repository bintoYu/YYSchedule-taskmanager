/**
 * 
 */
package com.YYSchedule.task.monitor;

import java.util.List;
import java.util.NavigableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.pojo.ResultStatus;
import com.YYSchedule.store.service.NodeService;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.NodeItemMapper;
import com.YYSchedule.task.mapper.ResultStatusMapper;
import com.YYSchedule.task.queue.PriorityTaskPool;

/**
 * @author ybt
 * 
 * @date 2018年8月15日
 * @version 1.0
 */
@Component("NodeStatusMonitor")
public class NodeStatusMonitor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeStatusMonitor.class);
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private PriorityTaskPool taskPool;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private NodeItemMapper nodeItemMapper;
	
	@Autowired
	private ResultStatusMapper statusMapper;
	
	@Autowired
	private Config config;
	
	/**
	 * 每5（配置文件设置）秒监测一次所有任务节点
	 * 一个任务节点损坏，需要满足以下条件：
	 * 1、5分钟内所执行的任务数高于3个
	 * 2、5分钟内所执行的任务成功率低于一定临界值
	 */
	public void monitor()
	{
		List<NodeItem> allNode = nodeItemMapper.getAllNode();
		long endTime = System.currentTimeMillis();
		long startTime = endTime - 60 * config.getStatus_monitor_interval();
		
		for (NodeItem nodeItem : allNode) {
			if (!nodeItem.isBroken()) {
				String nodeId = nodeItem.getNodeId();
				
				// 根据nodeId获得一段时间的执行任务数和成功数
				NavigableSet<ResultStatus> resultSet = statusMapper.getResultSet(nodeId, startTime, endTime);
				if (resultSet != null && resultSet.size() != 0) {
					int success = 0;
					int sum = resultSet.size();
					
					for (ResultStatus resultStatus : resultSet) {
						success += resultStatus.isSuccess() ? 1 : 0;
					}
					
					// 如果成功率过低（配置文件配置）,标记为坏掉的节点
					//如果只执行了任务且失败，我们不认为节点损坏
					double successRate = success / sum;
					if (successRate <= config.getNode_success_rate() && sum > 3) {
						nodeItem.setBroken(true);
						nodeItemMapper.updateNode(nodeItem);
						LOGGER.info("将任务节点 [ " + nodeItem.getNodeId() + " ] 标记为已损坏.");
					}
					
					//将“损坏”的节点中剩余的任务取出，扔回缓存池中
//					if(nodeItem.isBroken())
//					{
//						addToTaskPool(nodeItem.getNodeId());
//					}
					
//					// 保存node,每监听15次存一次库
//					// 当任务节点损坏时，立即保存该任务节点状态
//					if(count % 3 == 0 || nodeItem.isBroken())
//					{
//						Node node = getNode(nodeItem, success, sum);
//						nodeService.insertNode(node);
//					}
				}
			}
			
		}
	}
	
//	private void addToTaskPool(String nodeId)
//	{
//
//		String queue = nodeId + ":distributeTaskQueue";
//		List<Task> taskList = new ArrayList<Task>();
//		while (true)
//		{
//			Task task = new Task();
//			try
//			{
//				task = ActiveMQUtils.receiveTask(jmsTemplate, queue);
//				
//				if (task == null)
//					break;
//
//				taskList.add(task);
//			} catch (JMSException e)
//			{
//				e.printStackTrace();
//			}
//		}
//		
//		taskPool.add(taskList);
//	}
	
	
//	private Node getNode(NodeItem nodeItem, int successNum, int taskNum)
//	{
//		Node node = new Node();
//		node.setNodeId(nodeItem.getNodeId());
//		node.setCpuUsePerc(nodeItem.getCpuUsedPerc());
//		node.setFreeMem(nodeItem.getFreeMem());
//		node.setIsBroken(nodeItem.isBroken());
//		node.setJvmFreeMem(nodeItem.getJvmFreeMem());
//		node.setQueueLength(nodeItem.getQueueLength());
//		node.setSuccessNum(successNum);
//		node.setTaskNum(taskNum);
//		node.setThreadNum(nodeItem.getConsumerThreadNum());
//		node.setTaskPhase(nodeItem.getTaskPhase().toString());
//		node.setUpdateTime(nodeItem.getUpdatedTime());
//		
//		return node;
//	}
	
	
	
}
