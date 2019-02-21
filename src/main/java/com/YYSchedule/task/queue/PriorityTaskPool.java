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
import com.YYSchedule.common.pojo.TaskPoolPojo;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
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
	
	@Autowired
	@Qualifier("jedisTemplate")
	public RedisTemplate<String,String> redisTemplate;
	
	private static final int[] nums = {9,6,4,2,0};
	
	private Map<TaskPhase,TaskPoolPojo> map = new ConcurrentHashMap<>();

	//初始化 poolSize和queueSizeMap
    @PostConstruct
    public void init() {
    	LOGGER.info("开始初始化task缓存池");
    	//每个类型的缓存池都要分别建好
    	for(TaskPhase taskPhase : TaskPhase.values())
    	{
    		TaskPoolPojo taskPool = new TaskPoolPojo();
    		//统计每个优先级队列的个数
	    	for(int i = 0; i < nums.length; i++)
	    	{
	    		int size = RedisUtils.size(redisTemplate, taskPhase + "-TaskPool:"+nums[i]);
	    		taskPool.getQueueSizeMap().put(nums[i], new AtomicInteger(size));
	    		taskPool.getPoolSize().addAndGet(size);
	    		if(size != 0)
	    			LOGGER.info("类型[" + taskPhase + "]\t" + "优先级[" + nums[i] + "] 容量为：" + size);
	    	}
	    	map.put(taskPhase, taskPool);
			LOGGER.info("完成对类型[" + taskPhase + "]的初始化！ 总数为：" + taskPool.getPoolSize().get());
    	}
    }
	
	
	public boolean add(Task task)
	{
		String taskJson = JSONObject.toJSONString(task);
		if(RedisUtils.set(redisTemplate, task.getTaskPhase() + "-TaskPool:" + task.getTaskPriority().getValue(), taskJson))
		{
			TaskPoolPojo taskPool = map.get(task.getTaskPhase());
			taskPool.getQueueSizeMap().get(task.getTaskPriority().getValue()).incrementAndGet();
			taskPool.getPoolSize().incrementAndGet();
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
			if(RedisUtils.set(redisTemplate, task.getTaskPhase() + "-TaskPool:" + task.getTaskPriority().getValue(), taskJson))
			{
				ret++;
				TaskPoolPojo taskPool = map.get(task.getTaskPhase());
				taskPool.getQueueSizeMap().get(task.getTaskPriority().getValue()).incrementAndGet();
				taskPool.getPoolSize().incrementAndGet();
			}
		}
		
		return ret;
	}
	
	/**
	 * 如果高优先级队列为空，则不再去请求获取（队列的长度已加载到内存中，获取时间更短），直接尝试访问下一优先级队列
	 * 从池中获取一个task
	 * @return
	 */
	public synchronized Task get(TaskPhase taskPhase)
	{
		TaskPoolPojo taskPool = map.get(taskPhase);
    	for(int i = 0; i < nums.length; i++)
    	{
    		if(taskPool.getQueueSizeMap().get(nums[i]).get() > 0)
    		{
    			String taskJson = RedisUtils.get(redisTemplate, taskPhase + "-TaskPool:" + nums[i]);
    			Task task = JSONObject.parseObject(taskJson,Task.class);
    			taskPool.getQueueSizeMap().get(nums[i]).decrementAndGet();
    			taskPool.getPoolSize().decrementAndGet();
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
	public synchronized List<Task> get(TaskPhase taskPhase,int num)
	{
		List<Task> list = new ArrayList<>();
		TaskPoolPojo taskPool = map.get(taskPhase);
    	for(int i = 0; i < nums.length; i++)
    	{
    		AtomicInteger atomicInteger = taskPool.getQueueSizeMap().get(nums[i]);
    		while(atomicInteger.get() > 0)
    		{
    			if(list.size() == num)
    				break;
    			String taskJson = RedisUtils.get(redisTemplate, taskPhase + "-TaskPool:" + nums[i]);
    			Task task = JSONObject.parseObject(taskJson,Task.class);
    			atomicInteger.decrementAndGet();
    			list.add(task);
    		}
			if(list.size() == num)
				break;
    	}
    	
    	taskPool.getPoolSize().getAndSet(taskPool.getPoolSize().get() - list.size());
    	return list;
	}

	
}
