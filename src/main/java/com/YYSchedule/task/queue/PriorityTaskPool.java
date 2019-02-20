/**
 * 
 */
package com.YYSchedule.task.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.store.util.RedisUtils;
import com.alibaba.fastjson.JSONObject;

/**
 * @author ybt
 *
 * @date 2019年2月7日  10:20 - 11:35
 * @version 1.0  
 */
@Component
@Scope("singleton")
public class PriorityTaskPool
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityTaskPool.class);
	
	private static final int[] nums = {9,6,4,2,0};
	
	//记录池中任务总数量
	private AtomicInteger poolSize = new AtomicInteger(0);		

	//记录池中每个优先级队列的容量
	private Map<Integer,AtomicInteger> queueSizeMap = new ConcurrentHashMap<>();
	
	@Autowired
	@Qualifier("jedisTemplate")
	public RedisTemplate<String,String> redisTemplate;
	

	//初始化 poolSize和queueSizeMap
    @PostConstruct
    public void init() {
    	LOGGER.info("开始初始化task缓存池");
    	for(int i = 0; i < nums.length; i++)
    	{
    		int size = RedisUtils.size(redisTemplate, "taskPool:"+nums[i]);
    		queueSizeMap.put(nums[i], new AtomicInteger(size));
    		poolSize.addAndGet(size);
    		LOGGER.info("优先级[" + nums[i] + "] 容量为：" + size);
    	}
		LOGGER.info("完成对task缓存池的初始化！ 总数为：" + poolSize.get());
    }
	
	
	public boolean add(Task task)
	{
		String taskJson = JSONObject.toJSONString(task);
		if(RedisUtils.set(redisTemplate, "taskPool:" + task.getTaskPriority().getValue(), taskJson))
		{
			queueSizeMap.get(task.getTaskPriority().getValue()).incrementAndGet();
			poolSize.incrementAndGet();
			return true;
		}
		
		return false;
	}
	
	public int add(List<Task> taskList)
	{
		int ret = 0;
		for (Task task : taskList)
		{
			String taskJson = JSONObject.toJSONString(task);
			if(RedisUtils.set(redisTemplate, "taskPool:" + task.getTaskPriority().getValue(), taskJson))
			{
				ret++;
				queueSizeMap.get(task.getTaskPriority().getValue()).incrementAndGet();
			}
		}
		
		poolSize.addAndGet(ret);
		return ret;
	}
	
	/**
	 * 从池中获取一个task
	 * @return
	 */
	public synchronized Task get()
	{
    	for(int i = 0; i < nums.length; i++)
    	{
    		if(queueSizeMap.get(nums[i]).get() > 0)
    		{
    			String taskJson = RedisUtils.get(redisTemplate, "taskPool:" + nums[i]);
    			Task task = JSONObject.parseObject(taskJson,Task.class);
    			queueSizeMap.get(nums[i]).decrementAndGet();
    			poolSize.decrementAndGet();
    			return task;
    		}
    	}
    	
    	return null;
	}
	
	/**
	 * 获取制定数量的task，如果池中数量少于num，则全部给出。
	 * @param num
	 * @return
	 */
	public synchronized List<Task> get(int num)
	{
		List<Task> list = new ArrayList<>();
    	for(int i = 0; i < nums.length; i++)
    	{
    		AtomicInteger atomicInteger = queueSizeMap.get(nums[i]);
    		while(atomicInteger.get() > 0)
    		{
    			if(list.size() == num)
    				break;
    			String taskJson = RedisUtils.get(redisTemplate, "taskPool:" + nums[i]);
    			Task task = JSONObject.parseObject(taskJson,Task.class);
    			atomicInteger.decrementAndGet();
    			list.add(task);
    		}
			if(list.size() == num)
				break;
    	}
    	
    	poolSize.getAndSet(poolSize.get() - list.size());
    	return list;
	}


	public AtomicInteger getPoolSize()
	{
		return poolSize;
	}


	public Map<Integer, AtomicInteger> getQueueSizeMap()
	{
		return queueSizeMap;
	}
	
	
	
}
