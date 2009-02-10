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
import java.util.Iterator;
import java.util.List;

public class Dependency implements Serializable {
	private String dependsOn;
	private List<MethodCall> path = new ArrayList<MethodCall>();
	
	public String getDependsOn() {
		return dependsOn;
	}
	
	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}
	
	public List<MethodCall> getPath() {
		return path;
	}
	
	public void addMethodCall(MethodCall methodCall) {
		path.add(0, methodCall);
	}

	public boolean touches(String fullyQualifiedName) {
		Iterator<MethodCall> iterator = path.iterator();
		while (iterator.hasNext()) {
			MethodCall methodCall = (MethodCall) iterator.next();
			if (methodCall.getClassName().equals(fullyQualifiedName)) {
				return true;
			}
		}
		
		return false;
	}

	public Iterator<MethodCall> getMethodCalls() {
		return path.iterator();
	}
}
