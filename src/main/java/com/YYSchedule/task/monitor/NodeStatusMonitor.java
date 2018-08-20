/**
 * 
 */
package com.YYSchedule.task.monitor;

import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.mybatis.pojo.Node;
import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.pojo.ResultStatus;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.store.service.NodeService;
import com.YYSchedule.store.util.ActiveMQUtils;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.NodeItemMapper;
import com.YYSchedule.task.mapper.ResultStatusMapper;
import com.YYSchedule.task.queue.PriorityTaskQueue;

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
	private PriorityTaskQueue priorityTaskQueue;
	
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
	 * 查看任务节点在15分钟内的成功率
	 * 如果在15分钟内某个任务节点的成功率突然降到了很低的程度
	 * 我们便可认为该任务节点已损坏
	 */
	public void monitor()
	{
		List<NodeItem> allNode = nodeItemMapper.getAllNode();
		long endTime = System.currentTimeMillis();
		long startTime = endTime - 3 * 60 * config.getStatus_monitor_interval();
		
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
					if (successRate <= config.getNode_success_rate() && sum > 1) {
						nodeItem.setBroken(true);
						nodeItemMapper.updateNode(nodeItem);
						LOGGER.info("将任务节点 [ " + nodeItem.getNodeId() + " ] 标记为已损坏.");
					}
					
					if(nodeItem.isBroken())
					{
						addToPriorityTaskQueue(nodeItem.getNodeId());
					}
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
	
	private void addToPriorityTaskQueue(String nodeId)
	{

		String queue = nodeId + ":distributeTaskQueue";
		Set<Task> taskSet = new HashSet<Task>();
		while (true)
		{
			Task task = new Task();
			try
			{
				task = ActiveMQUtils.receiveTask(jmsTemplate, queue);
				
				if (task == null)
					break;

				taskSet.add(task);
			} catch (JMSException e)
			{
				e.printStackTrace();
			}
		}
		
		priorityTaskQueue.addToPriorityTaskQueue(taskSet);
	}
	
	
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
