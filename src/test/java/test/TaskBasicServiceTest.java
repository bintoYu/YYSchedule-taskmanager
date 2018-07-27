package test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.mybatis.pojo.TaskBasic;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.store.service.TaskBasicService;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;


public class TaskBasicServiceTest
{
	private AbstractApplicationContext applicationContext;

	@Before
	public void init() {
		//创建一个spring容器
		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
	}
	
    @Test
    public void testGetTaskBasicById()
    {
        TaskBasicService taskBasicService = applicationContext.getBean(TaskBasicService.class);
    	
        Long taskId = Long.parseLong("1000001010001");

        TaskBasic taskBasic= taskBasicService.getTaskBasicById(taskId);

        if(taskBasic != null)
        {
        	System.out.println(taskBasic.getTaskPhase());
        }
    }
    
    @Test
    public void testGetTaskBasicByIdAndTaskPhase()
    {
    	
        TaskBasicService taskBasicService = applicationContext.getBean(TaskBasicService.class);

        Long taskId = Long.parseLong("1000001010001");

        TaskBasic taskBasic= taskBasicService.getTaskBasicByIdAndTaskPhase(taskId, TaskPhase.COMMON);
        
        if(taskBasic != null)
        {
        	System.out.println(taskBasic.getTaskStatus());
        }
    }
    

}
