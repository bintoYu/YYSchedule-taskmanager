//package test;
//
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.support.AbstractApplicationContext;
//
//import com.YYSchedule.common.mybatis.pojo.TaskResult;
//import com.YYSchedule.common.rpc.domain.task.TaskPhase;
//import com.YYSchedule.store.service.TaskResultService;
//import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
//
//
//public class TaskResultServiceTest
//{
//	private AbstractApplicationContext applicationContext;
//
//	@Before
//	public void init() {
//		//创建一个spring容器
//		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
//	}
//	
//    @Test
//    public void testUpdateTaskResultById()
//    {
//        TaskResultService taskResultService = applicationContext.getBean(TaskResultService.class);
//    	
//        Long taskId = Long.parseLong("1000001010001");
//        
//        TaskResult result = new TaskResult();
//        result.setTaskId(taskId);
//
//        taskResultService.updateTaskResult(result);
//
//    }    
//
//    
//
//}
