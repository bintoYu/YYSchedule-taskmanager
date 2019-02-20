/**
 * 
 */
package com.YYSchedule.task.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ybt
 *
 * @date 2019年1月23日  
 * @version 1.0  
 */
public class NodeMatcherFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeMatcherFactory.class);
	
	public static NodeMatcher getNodeMatcher(String matcher)
	{
		try
		{
			return (NodeMatcher) Class.forName(matcher).newInstance();
		} catch (InstantiationException e)
		{
			LOGGER.error("Failed in class instantiation : " + e.getMessage(), e);
			throw new RuntimeException("Failed in class instantiation : " + e.getMessage(), e);
		} catch (IllegalAccessException e)
		{
			LOGGER.error("Failed in class illegal access : " + e.getMessage(), e);
			throw new RuntimeException("Failed in class illegal access : " + e.getMessage(), e);
		} catch (ClassNotFoundException e)
		{
			LOGGER.error("Failed in class not found : " + e.getMessage(), e);
			throw new RuntimeException("Failed in class not found : " + e.getMessage(), e);
		}
	}
}
