/**
 * 
 */
package backup;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.YYSchedule.common.pojo.NodeItem;

/**
 * @author ybt
 *
 * @date 2019年1月23日  
 * @version 1.0  
 */ 
public class RandomMatcher extends NodeMatcher
{
	@Override
	public NodeItem getNodeItem(ConcurrentHashMap<String, NodeItem> nodeMap)
	{
		NodeItem selectedNodeItem = null;
		int size = nodeMap.size();
		int random = new Random().nextInt(size);
		int index = 0;
		for (String key : nodeMap.keySet())
		{
			index++;
			if(index == random)
				return nodeMap.get(key);
		}
		return null;
	}
	
}
