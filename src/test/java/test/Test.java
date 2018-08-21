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
    	task.setTaskId(111L);
    	task.setTaskPhase(TaskPhase.COMMON);
    	
    	System.out.println(task.toString());
    }
    
}
