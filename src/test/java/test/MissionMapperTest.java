//package test;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.support.AbstractApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import com.YYSchedule.task.mapper.MissionMapper;
//
//public class MissionMapperTest
//{
//	private AbstractApplicationContext applicationContext;
//
//	@Before
//	public void init() {
//		//创建一个spring容器
//		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
//	}
//	
//	@Test
//	public void test()
//	{
//		int userId = 1;
//		
//		MissionMapper missionMapper = applicationContext.getBean(MissionMapper.class);
//		
//		missionMapper.generateMissionId(userId);
//	}
//	
//}
