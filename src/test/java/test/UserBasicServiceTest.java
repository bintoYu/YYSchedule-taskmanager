//package test;
//
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.support.AbstractApplicationContext;
//
//import com.YYSchedule.common.mybatis.pojo.UserBasic;
//import com.YYSchedule.store.service.UserBasicService;
//import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
//
//
//public class UserBasicServiceTest
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
//    public void testGetUserBasicMapperById()
//    {
//        UserBasicService userBasicService = applicationContext.getBean(UserBasicService.class);
//    	
//        int userId = 3;
//
//        UserBasic userBasic= userBasicService.getUserBasicById(userId);
//
//        if(userBasic != null)
//        {
//        	System.out.println(userBasic.getUsername());
//        }
//    }
//    
//    @Test
//    public void testGetUserBasicList()
//    {
//        UserBasicService userBasicService = applicationContext.getBean(UserBasicService.class);
//
//        List<UserBasic> userBasicList = userBasicService.getUserBasicList();
//
//        for(UserBasic userBasic:userBasicList)
//        {
//        	System.out.println(userBasic.getUsername() + "\t" + userBasic.getPassword() + "\t" + userBasic.getMissionCount());
//        }
//    }
//    
//    @Test
//    public void testUpdateUserBasicMapperById()
//    {
//        UserBasicService userBasicService = applicationContext.getBean(UserBasicService.class);
//    	
//        int userId = 1;
//        
//        UserBasic userBasic = new UserBasic();
//        
//        userBasic.setUserId(userId);
//        userBasic.setMissionCount(-1);
//
//        int result = userBasicService.updateUserBasic(userBasic);
//
//        System.out.println(result);
//    }
//}
