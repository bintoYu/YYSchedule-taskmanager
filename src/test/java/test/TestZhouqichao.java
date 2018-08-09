/**
 * 
 */
package test;

import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ybt
 *
 * @date 2018年8月7日  
 * @version 1.0  
 */
public class TestZhouqichao 
{
	public static void main(String[] args)
	{
		AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
		GJZZSuoShuGJService gJZZSuoShuGJService = applicationContext.getBean(GJZZSuoShuGJService.bean);
		List<GJZZSuoShuGJ> list = gJZZSuoShuGJService.selectGJZZSuoShuGJByGJZZId();
		for(GJZZSuoShuGJ tmp : list)
		{
			System.out.println(tmp.toString());
		}
	}
}
