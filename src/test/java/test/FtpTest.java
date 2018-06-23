package test;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.YYSchedule.store.ftp.FtpConnFactory;
import com.YYSchedule.store.ftp.FtpUtils;
import com.YYSchedule.task.applicationContext.ApplicationContextHandler;

public class FtpTest
{
	private ApplicationContext applicationContext;
	
	@Before
	public void init()
	{
		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
	}
	
    @Test
    public void testFtpUpload()
    {
    	String file = "D:\\ApkVerifyLog.txt";
    	
    	FtpConnFactory ftpConnFactory = applicationContext.getBean(FtpConnFactory.class);
    	
    	FTPClient ftpClient = ftpConnFactory.connect();
    	FtpUtils.upload(ftpClient, file, "/test/");
    	
    }
}
