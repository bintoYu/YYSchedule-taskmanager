/**
 * 
 */
package backup;

import java.util.concurrent.ConcurrentHashMap;

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
 * @author ybt
 *
 * @date 2019年1月23日  
 * @version 1.0  
 */
public abstract class NodeMatcher
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeMatcher.class);
	
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
	
	public abstract NodeItem getNodeItem(ConcurrentHashMap<String, NodeItem> nodeMap);
	
	public NodeItem updateNodeItemPayload(NodeItem nodeItem, int taskNum) throws Exception
	{
		nodeItem.setQueueLength(nodeItem.getQueueLength() + taskNum);
		nodeItem.updateGrade();
		return nodeItem;
	};
}
