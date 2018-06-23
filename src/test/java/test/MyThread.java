package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.MissionMapper;

public class MyThread extends Thread
{
	private ApplicationContext applicationContext;
	
	private MissionMapper missionMapper;
	
    public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	public MissionMapper getMissionMapper()
	{
		return missionMapper;
	}

	public void setMissionMapper(MissionMapper missionMapper)
	{
		this.missionMapper = missionMapper;
	}

	public MyThread(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
		
		missionMapper = applicationContext.getBean(MissionMapper.class);
    }
	
    @Override
    public void run() {
    	missionMapper.generateMissionId(1);
    }
    
    public static void main(String[] args){
    	ApplicationContext applicationContext =	ApplicationContextHandler.getInstance().getApplicationContext();
    	
    	MissionMapper missionMapper = new MyThread(applicationContext).getMissionMapper();
    	
    	missionMapper.initMissionCountMap(1, 0);
    	
        for(int i = 0;i<10;i++){
         new MyThread(applicationContext).start();
        }
     }
}
