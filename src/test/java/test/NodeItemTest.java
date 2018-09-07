///**
// * 
// */
//package test;
//
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentSkipListSet;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.support.AbstractApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import com.YYSchedule.common.pojo.NodeItem;
//import com.YYSchedule.common.rpc.domain.task.TaskPhase;
//import com.YYSchedule.task.mapper.NodeItemMapper;
//
///**
// * @author ybt
// *
// * @date 2018年8月15日  
// * @version 1.0  
// */
//public class NodeItemTest
//{
//	private AbstractApplicationContext applicationContext;
//
//	@Before
//	public void init() {
//		//创建一个spring容器
//		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
//	}
//	
//	
//	@Test
//	public void test()
//	{
//		NodeItemMapper nodeItemMapper = applicationContext.getBean(NodeItemMapper.class);
//		
//		for(int i = 0; i <20 ;i++)
//		{
//			NodeItem nodeItem = new NodeItem(new Random().nextInt(100));
//			nodeItem.setTaskPhase(TaskPhase.COMMON);
//			
//			nodeItemMapper.updateNode(nodeItem);
//		}
//		
//		Map<TaskPhase, ConcurrentHashMap<String, NodeItem>> nodeMap = nodeItemMapper.getNodeMap();
//
//	}
//}
