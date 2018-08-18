package test;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.pojo.ResultStatus;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.task.mapper.ResultStatusMapper;

/**
 * @author ybt
 *
 * @date 2018年8月15日  
 * @version 1.0  
 */
public class TestResultStatus
{
	private AbstractApplicationContext applicationContext;

	@Before
	public void init() {
		//创建一个spring容器
		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
	}
	
	@Test
	public void test()
	{
		ResultStatusMapper resultStatusMapper = applicationContext.getBean(ResultStatusMapper.class);
		String nodeId = "test";
		long start = System.currentTimeMillis();
		
		for(int i = 0; i <20 ;i++)
		{
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Result result = new Result();
			result.setTaskId(i);
			result.setFinishedTime(System.currentTimeMillis());
			result.setNodeId(nodeId);
			result.setTaskStatus(TaskStatus.FINISHED);
			
			ResultStatus resultStatus = new ResultStatus(result);
			
			resultStatusMapper.updateResultStatus(resultStatus);
		}
		
		long end = System.currentTimeMillis();
		
		ConcurrentSkipListSet<ResultStatus> set = resultStatusMapper.getResultSet(nodeId);
		for (ResultStatus resultStatus : set) {
			System.out.println(resultStatus);
		}
		
		System.out.println("----------------------------------------------");
		
		NavigableSet<ResultStatus> resultSet = resultStatusMapper.getResultSet(nodeId, start, (start+end)/2);
		for(ResultStatus tmp : resultSet)
		{
			System.out.println(tmp);
		}
	}
}
