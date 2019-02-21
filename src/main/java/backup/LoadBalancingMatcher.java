package backup;

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
 * @date 2019年1月23日
 * @version 2.0
 */
public class LoadBalancingMatcher extends NodeMatcher
{
	/**
	 * 找出最为适合的nodeItem节点
	 * 即分数最低的nodeItem节点
	 * 同时，忽略所有损坏的nodeItem
	 * @param nodeMap
	 * @return
	 */
	@Override
	public NodeItem getNodeItem(ConcurrentHashMap<String, NodeItem> nodeMap)
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
}
