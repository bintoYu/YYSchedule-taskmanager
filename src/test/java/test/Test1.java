package test;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.mybatis.pojo.MissionBasic;
import com.YYSchedule.common.mybatis.pojo.TaskResult;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.PathUtils;
import com.YYSchedule.store.service.MissionBasicService;
import com.YYSchedule.store.service.TaskResultService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;

public class Test1
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
		TaskResult taskResult = new TaskResult();
		taskResult.setTaskId(1111L);
		taskResult.setLogger("1111111111111111111111111111111111111111111111111111111111111111111111111");
		
		
		TaskResultService taskResultService = applicationContext.getBean(TaskResultService.class);
		
		int i = taskResultService.updateTaskResult(taskResult);

		
		System.out.println(i);
	}
    
}
