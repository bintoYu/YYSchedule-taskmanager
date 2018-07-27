package test;

import org.springframework.context.support.AbstractApplicationContext;

import com.YYSchedule.task.applicationContext.ApplicationContextHandler;
import com.YYSchedule.task.mapper.MissionMapper;

public class MyThread extends Thread
{
	private AbstractApplicationContext applicationContext;
	
	private MissionMapper missionMapper;
	
    public AbstractApplicationContext getApplicationContext()
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

	public MyThread(AbstractApplicationContext applicationContext){
		this.applicationContext = applicationContext;
		
		missionMapper = applicationContext.getBean(MissionMapper.class);
    }
	
    @Override
    public void run() {
    	missionMapper.generateMissionId(1);
    }
    
    public static void main(String[] args){
    	AbstractApplicationContext applicationContext =	ApplicationContextHandler.getInstance().getApplicationContext();
    	
    	MissionMapper missionMapper = new MyThread(applicationContext).getMissionMapper();
    	
    	missionMapper.initMissionCountMap(1, 0);
    	
        for(int i = 0;i<10;i++){
         new MyThread(applicationContext).start();
        }
     }
}
