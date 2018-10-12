//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.ApplicationContext;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import com.YYSchedule.store.util.RedisUtils;
//import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
//
///**
// * 
// */
//
///**
// * @author Administrator
// *
// * @date 2018年9月26日  
// * @version 1.0  
// */
//public class TestRedis
//{
//	private ApplicationContext applicationContext;
//	
//	@Before
//	public void get()
//	{
//		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
//	}
//	
//	@Test
//	public void test()
//	{
//		RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
//		String string = RedisUtils.get(redisTemplate, "VIRUSTOTAL");
//		System.out.println(string);
//	}
//}
