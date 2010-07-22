/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Options;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.xbean.finder.UrlSet;

/**
 * @version $Rev$ $Date$
*/
public class AppContext extends DeploymentContext {
    private final SystemInstance systemInstance;
    private final ClassLoader classLoader;
    private BeanManager beanManager;

    public AppContext(String id, SystemInstance systemInstance, ClassLoader classLoader) {
        super(id, systemInstance.getOptions());
        this.classLoader = classLoader;
        this.systemInstance = systemInstance;
        
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public SystemInstance getSystemInstance() {
        return systemInstance;
    }
    
    public static void main(String[] args) throws Exception{
        Enumeration<URL> en = AppContext.class.getClassLoader().getResources("META-INF/openwebbeans/openwebbeans-default.properties");
        while(en.hasMoreElements()){
            URL u = en.nextElement();
            System.out.println(u.toExternalForm());
        }
    }

    public BeanManager getBeanManager() {
        return beanManager;
    }

    public void setBeanManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }
}
