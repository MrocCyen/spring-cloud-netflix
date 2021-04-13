/*
 * Copyright 2016-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.ribbon;

import java.util.HashMap;
import java.util.Map;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.netflix.ribbon.SpringClientFactory.NAMESPACE;

/**
 * @author Spencer Gibb
 */

/**
 * 属性工厂，用户自定义的，todo
 */
public class PropertiesFactory {

	@Autowired
	private Environment environment;

	/**
	 * class到属性的映射
	 */
	private Map<Class, String> classToProperty = new HashMap<>();

	public PropertiesFactory() {
		//负载均衡器
		classToProperty.put(ILoadBalancer.class, "NFLoadBalancerClassName");
		//ping
		classToProperty.put(IPing.class, "NFLoadBalancerPingClassName");
		//负载规则
		classToProperty.put(IRule.class, "NFLoadBalancerRuleClassName");
		//服务列表
		classToProperty.put(ServerList.class, "NIWSServerListClassName");
		//服务列表过滤器
		classToProperty.put(ServerListFilter.class, "NIWSServerListFilterClassName");
	}

	/**
	 * 是否设置了属性
	 *
	 * @param clazz 属性类型
	 * @param name  客户端名称
	 * @return true，设置过
	 */
	public boolean isSet(Class clazz, String name) {
		return StringUtils.hasText(getClassName(clazz, name));
	}

	public String getClassName(Class clazz, String name) {
		//包含这个类
		if (this.classToProperty.containsKey(clazz)) {
			String classNameProperty = this.classToProperty.get(clazz);
			//从环境中获取，<name>.ribbon.NFLoadBalancerClassName
			return environment.getProperty(name + "." + NAMESPACE + "." + classNameProperty);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <C> C get(Class<C> clazz, IClientConfig config, String name) {
		String className = getClassName(clazz, name);
		if (StringUtils.hasText(className)) {
			try {
				Class<?> toInstantiate = Class.forName(className);
				return (C) SpringClientFactory.instantiateWithConfig(toInstantiate, config);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Unknown class to load " + className
						+ " for class " + clazz + " named " + name);
			}
		}
		return null;
	}

}
