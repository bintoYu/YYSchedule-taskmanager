package test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.job.JobPriority;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.distributor.TaskDistributor;
import com.YYSchedule.task.distributor.TaskDistributorThread;
import com.YYSchedule.task.queue.PriorityTaskQueueProducer;
import com.YYSchedule.task.queue.TaskQueue;

/**
 * 编写类 启动DistributeTaskQueueProducer
 * @author Administrator
 *
 */
public class DisTributeTaskQueuePoolTest
{
	private static AbstractApplicationContext applicationContext;
	
	public void start()
	{
		List<Task> taskList = new ArrayList<>();
		for(JobPriority jobPriority : JobPriority.values())
		{
			Task task = new Task();
			task.setTaskId((long)jobPriority.getValue());
			task.setTaskPriority(jobPriority);
			task.setLoadedTime(System.currentTimeMillis());
			taskList.add(task);
			
		}
		
		TaskQueue taskQueue = applicationContext.getBean(TaskQueue.class);
		PriorityTaskQueueProducer globalTaskQueueProducer = new PriorityTaskQueueProducer(taskQueue, taskList);
		Thread globalTaskQueueRunner = new Thread(globalTaskQueueProducer);
		globalTaskQueueRunner.start();
	}
	
	public void startDis()
	{
		applicationContext.getBean(TaskDistributor.class).startThreadPool();
	}
	
	public static void main(String[] args)
	{
		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		
		DisTributeTaskQueuePoolTest globalTaskQueueTest = new DisTributeTaskQueuePoolTest();
		globalTaskQueueTest.start();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		globalTaskQueueTest.startDis();
	}
}
