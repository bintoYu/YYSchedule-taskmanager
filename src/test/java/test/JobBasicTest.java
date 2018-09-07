//package test;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.support.AbstractApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import com.YYSchedule.common.mybatis.pojo.JobBasic;
//import com.YYSchedule.common.rpc.domain.job.Job;
//import com.YYSchedule.common.rpc.domain.job.JobPriority;
//import com.YYSchedule.common.rpc.domain.parameter.JobParameter;
//import com.YYSchedule.common.rpc.domain.task.TaskPhase;
//import com.YYSchedule.common.utils.Bean2BeanUtils;
//import com.YYSchedule.store.service.JobBasicService;
//import com.YYSchedule.task.mapper.JobMapper;
//
//public class JobBasicTest
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
//		Job job = new Job();
//		JobParameter jobParameter = new JobParameter();
//		job.setJobParameter(jobParameter);
//		job.setJobPriority(JobPriority.HIGHER);
//		job.setTaskPhase(TaskPhase.COMMON);
//		job.setTimeout(1000);
//
//		JobMapper jobMapper = applicationContext.getBean(JobMapper.class);
//		JobBasicService jobBasicService = applicationContext.getBean(JobBasicService.class);
//		jobMapper.initJobCountMap(1, 0);
//		long jobId = jobMapper.generateJobId(1);
//		job.setJobId(jobId);
//		
//		JobBasic jobBasic = Bean2BeanUtils.Job2JobBasic(job, 1);
//		jobBasicService.insertJobBasic(jobBasic);
//
//	}
//	
//}
