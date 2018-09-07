package com.YYSchedule.task.splitter;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.job.Job;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.utils.PathUtils;

public class JobSplitter
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JobSplitter.class);
	
	/**
	 * split job into task
	 * @param job
	 * @return
	 */
	public static List<Task> split(Job job,List<String> fileList,int userId) {
		
		LOGGER.info("开始将job切分成task, jobId: " + job.getJobId());
		
		List<Task> taskList = new ArrayList<Task>();
		
		AtomicInteger taskCount = new AtomicInteger(0);
		
		for(String file : fileList)
		{
			Task task = new Task();
			// FIXME if parameter size multiple phase size is larger than 9999, then failed
			long taskId = Long.parseLong(String.valueOf(job.getJobId()) + new DecimalFormat("0000").format(taskCount.incrementAndGet() % 10000));
			task.setTaskId(taskId);
			task.setTaskPriority(job.getJobPriority());
			task.setTaskPhase(job.getTaskPhase());
			task.setTaskParameter(job.getJobParameter());
			task.setTaskStatus(TaskStatus.COMMITTED);
			task.setLoadedTime(System.currentTimeMillis());
			task.setTimeout(job.getTimeout());
			
			task.setFileName(file);
			
			taskList.add(task);
		}
		
		return taskList;
	}
}
