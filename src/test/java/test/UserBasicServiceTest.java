package test;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.YYSchedule.common.mybatis.pojo.UserBasic;
import com.YYSchedule.store.service.UserBasicService;


public class UserBasicServiceTest
{
	private ApplicationContext applicationContext;

	@Before
	public void init() {
		//创建一个spring容器
		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
	}
	
    @Test
    public void testGetUserBasicMapperById()
    {
        UserBasicService userBasicService = applicationContext.getBean(UserBasicService.class);
    	
        int userId = 1;

        UserBasic userBasic= userBasicService.getUserBasicMapperById(userId);

        System.out.println(userBasic.getUsername());
    }
    
    @Test
    public void testUpdateUserBasicMapperById()
    {
        UserBasicService userBasicService = applicationContext.getBean(UserBasicService.class);
    	
        int userId = 1;
        
        UserBasic userBasic = new UserBasic();
        
        userBasic.setUserId(userId);
        userBasic.setMissionCount(-1);

        int result = userBasicService.updateUserBasic(userBasic);

        System.out.println(result);
    }
}
