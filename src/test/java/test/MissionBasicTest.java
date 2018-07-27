package test;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.YYSchedule.common.mybatis.pojo.MissionBasic;
import com.YYSchedule.store.service.MissionBasicService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.queue.PriorityTaskQueueProducer;

public class MissionBasicTest
{
	private AbstractApplicationContext applicationContext;

	@Before
	public void init() {
		//创建一个spring容器
		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
	}
	
	@Test
	public void test()
	{
		MissionBasic missionBasic = new MissionBasic();
		missionBasic.setMissionId(1);
		missionBasic.setMissionName("test");
		missionBasic.setUserId(1);
		missionBasic.setMissionStartTime(System.currentTimeMillis());
		
		MissionBasicService service = applicationContext.getBean(MissionBasicService.class);
		
		service.insertMissionBasic(missionBasic);

		
	}
	
}
