//package test;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.support.AbstractApplicationContext;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import com.YYSchedule.store.util.RedisUtils;
//import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
//
//
///**
// * @author ybt
// *
// * @date 2018年9月3日  
// * @version 1.0  
// */
//public class RedisTest
//{
//	private AbstractApplicationContext applicationContext;
//
//	@Before
//	public void init() {
//		//创建一个spring容器
//		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
//	}
//	
//	 @Test
//	 public void testSet()
//	 {
//		RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
//		boolean set = RedisUtils.set(redisTemplate,"test", "abc");
//		System.out.println(set);
//	 }
//	 
//	 @Test
//	 public void testGet()
//	 {
//		RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
//		String get = RedisUtils.get(redisTemplate,"CUCKOO");
//		System.out.println(get);
//	 }
//}
