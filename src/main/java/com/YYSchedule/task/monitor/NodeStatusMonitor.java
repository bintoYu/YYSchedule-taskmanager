/**
 * 
 */
package com.YYSchedule.task.monitor;

import java.util.List;
import java.util.NavigableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.mybatis.pojo.Node;
import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.pojo.ResultStatus;
import com.YYSchedule.store.service.NodeService;
import com.YYSchedule.task.config.Config;
import com.YYSchedule.task.mapper.NodeItemMapper;
import com.YYSchedule.task.mapper.ResultStatusMapper;

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
	private NodeService nodeService;
	
	@Autowired
	private NodeItemMapper nodeItemMapper;
	
	@Autowired
	private ResultStatusMapper statusMapper;
	
	@Autowired
	private Config config;
	
	private int count;
	
	public void monitor()
	{
		count++;
		
		LOGGER.info("监听任务节点信息.............");
		List<NodeItem> allNode = nodeItemMapper.getAllNode();
		long endTime = System.currentTimeMillis();
		long startTime = endTime - config.getStatus_monitor_interval();
		
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
					double successRate = success / sum;
					if (successRate <= config.getNode_success_rate()) {
						nodeItem.setBroken(true);
						nodeItemMapper.updateNode(nodeItem);
						LOGGER.info("将任务节点 [ " + nodeItem.getNodeId() + " ] 标记为已损坏.");
					}
					
//					// 保存node,每监听15次存一次库
//					// 当任务节点损坏时，立即保存该任务节点状态
//					//TODO 现在这种情况只是选取了最新的任务节点状态，应该弄成平均值（例如cpu占有率）
//					if(count % 3 == 0 || nodeItem.isBroken())
//					{
//						Node node = getNode(nodeItem, success, sum);
//						nodeService.insertNode(node);
//					}
				}
			}
			
		}
	}
	
	private Node getNode(NodeItem nodeItem, int successNum, int taskNum)
	{
		Node node = new Node();
		node.setNodeId(nodeItem.getNodeId());
		node.setCpuUsePerc(nodeItem.getCpuUsedPerc());
		node.setFreeMem(nodeItem.getFreeMem());
		node.setIsBroken(nodeItem.isBroken());
		node.setJvmFreeMem(nodeItem.getJvmFreeMem());
		node.setQueueLength(nodeItem.getQueueLength());
		node.setSuccessNum(successNum);
		node.setTaskNum(taskNum);
		node.setThreadNum(nodeItem.getConsumerThreadNum());
		node.setTaskPhase(nodeItem.getTaskPhase().toString());
		node.setUpdateTime(nodeItem.getUpdatedTime());
		
		return node;
	}
	
}
