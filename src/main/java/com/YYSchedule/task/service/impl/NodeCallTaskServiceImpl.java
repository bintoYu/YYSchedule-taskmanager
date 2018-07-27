/**
 * 
 */
package com.YYSchedule.task.service.impl;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.rpc.domain.node.NodePayload;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.NodeMapper;

/**
 * @author ybt
 *
 * @date 2018年7月18日  
 * @version 1.0  
 */
public class NodeCallTaskServiceImpl implements NodeCallTaskService.Iface
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NodeCallTaskServiceImpl.class);
	
	
	/* (non-Javadoc)
	 * @see com.YYSchedule.common.rpc.service.task.NodeCallTaskService.Iface#registerNode(com.YYSchedule.common.rpc.domain.node.NodeInfo)
	 */
	@Override
	public int registerNode(NodePayload nodePayload) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException
	{
		int retValue = -1;

		retValue = reportHeartbeat(nodePayload);
		if (retValue >= 0) {
			LOGGER.info("success to register node : " + nodePayload.getNodeId());
		}
		else
		{
			LOGGER.error("failed to register node : " + nodePayload.getNodeId());
		}
		return retValue;
	}

	/* (non-Javadoc)
	 * @see com.YYSchedule.common.rpc.service.task.NodeCallTaskService.Iface#reportHeartbeat(com.YYSchedule.common.rpc.domain.node.NodePayload)
	 */
	@Override
	public int reportHeartbeat(NodePayload nodePayload)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException
	{
		int retCode = -1;
		if (nodePayload == null || nodePayload.getNodeRuntime() == null) {
			LOGGER.error("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
			throw new InvalidRequestException("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
		}
		LOGGER.debug("Node [ " + nodePayload.getNodeId() + " ] payload info received.");
		NodeItem nodeItem = new NodeItem(nodePayload);
		nodeItem.setUpdatedTime(System.currentTimeMillis());
		
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		NodeMapper nodeMapper = applicationContext.getBean(NodeMapper.class);
		nodeMapper.updateNode(nodeItem);
		retCode = 0;
		return retCode;
	}

	/* (non-Javadoc)
	 * @see com.YYSchedule.common.rpc.service.task.NodeCallTaskService.Iface#reportTaskExecutionStatus(java.lang.String, long, com.YYSchedule.common.rpc.domain.task.TaskPhase, com.YYSchedule.common.rpc.domain.task.TaskStatus)
	 */
	@Override
	public int reportTaskExecutionStatus(String nodeId, long taskId,
			TaskPhase taskPhase, TaskStatus taskStatus)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.YYSchedule.common.rpc.service.task.NodeCallTaskService.Iface#submitResult(long, java.lang.String, java.nio.ByteBuffer)
	 */
	@Override
	public int submitResult(long taskId, String name, ByteBuffer content)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
}
