package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.PathUtils;

public class Test
{

    public static void main(String[] args) {
    	Task task = new Task();
    	task.setTaskStatus(TaskStatus.COMMITTED);
    	
    	Test1 test1 = new Test1();
    	test1.test(task);
    	
    	System.out.println(task.getTaskStatus().toString());
    }
    
}
