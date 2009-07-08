/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProjectModel implements Serializable {
	private static final long serialVersionUID = -8772710294450537711L;
	
	private List<Dependency> dependencies = new ArrayList<Dependency>();
	private String projectName;
	private Map<String, List<String>> ifaces;
	
	public ProjectModel(String projectName) {
		super();
		this.projectName = projectName;
		ifaces = new HashMap<String, List<String>>();
	}

	public List<Dependency> getDependencies(String beanName) {
		List<Dependency> result = new ArrayList<Dependency>();
		
		for (Dependency dependency : dependencies) {
			if (dependency.getDependsOn().equals(beanName)) {
				result.add(dependency);
			}
		}
		
		return result;
	}
	
	public List<String> getBeansDependentOn(String typeName) {
		List<String> beans = new ArrayList<String>();
		List<Dependency> deps = getDependencies();
		
		for (Dependency dep : deps) {
			if (dep.touches(typeName)) {
				Iterator<MethodCall> iterator = dep.getMethodCalls();
				while (iterator.hasNext()) {
					MethodCall methodCall = (MethodCall) iterator.next();
					String className = methodCall.getClassName();

					if (ifaces.keySet().contains(className)) {
						if (! beans.contains(className)) {
							beans.add(className);
						}
					}
				}
			}
		}
		
		return beans;
	}

	public void clear() {
		dependencies.clear();
	}

	public void addDependencies(List<Dependency> dependencyList) {
		for (Dependency dependency : dependencyList) {
			if (! dependencies.contains(dependency)) {
				dependencies.add(dependency);
			}
		}
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setSingletonBeanInterfaces(String singleton, List<String> interfaces) {
		ifaces.put(singleton, interfaces);
	}

	public List<String> getInterfacesToSearchFor(String singleton) {
		List<String> result = new ArrayList<String>();
		
		Iterator<String> iterator = ifaces.keySet().iterator();
		while (iterator.hasNext()) {
			String singletonBean = (String) iterator.next();
			if (! singletonBean.equals(singleton)) {
				List<String> interfaces = ifaces.get(singletonBean);
				for (String iface : interfaces) {
					if (! result.contains(iface)) {
						result.add(iface);
					}
				}
			}
		}
		
		return result;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getBeanForInterface(String qualifiedName) {
		Iterator<String> iterator = ifaces.keySet().iterator();
		while (iterator.hasNext()) {
			String bean = (String) iterator.next();
			if (qualifiedName.equals(bean)) {
				return bean;
			}
			
			List<String> ifaceList = ifaces.get(bean);
			if (ifaceList.contains(qualifiedName)) {
				return bean;
			}
		}
		
		return null;
	}
}
