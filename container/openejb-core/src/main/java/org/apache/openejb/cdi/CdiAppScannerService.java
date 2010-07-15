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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.InterceptorInfo;
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.spi.ScannerService;

public class CdiAppScannerService implements ScannerService {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"),
            CdiAppScannerService.class);

    private AppInfo module;
    private ClassLoader loader;
    private final Set<URL> beansXmls = new HashSet<URL>();
    //Just pojo managed bean classes
    private final Set<Class<?>> managedBeansClasses = new HashSet<Class<?>>();
    //All cdi classes
    private final Set<Class<?>> cdiClasses = new HashSet<Class<?>>();
    public static String BEANS_XML_LOCATION = "META-INF/beans.xml";
    //used in tests
    public static String APPEND_PACKAGE_NAME = null;

    public CdiAppScannerService() {
    }

    public AppInfo getModule() {
        return module;
    }

    public void setAppInfo(AppInfo module) {
        this.module = module;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public Set<Class<?>> getBeanClasses() {
        return this.cdiClasses;
    }

    @Override
    public Set<URL> getBeanXmls() {
        return this.beansXmls;
    }

    @Override
    public void init(Object object) {
        // Do nothing
    }

    public Set<Class<?>> getCdiClasses() {
        return cdiClasses;
    }

    @Override
    public void scan(){

        List<String> candidateClasses = null;
        try {
            // Look for META-INF/beans.xml
            Enumeration<URL> urls = loader.getResources(BEANS_XML_LOCATION);
            Set<URL> urlSet = new HashSet<URL>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                urlSet.add(url);
                this.beansXmls.add(url);
            }
            if (urlSet.isEmpty()){
                return;
            }
            
            candidateClasses = Classes.getClassesFromUrls(urlSet, "beans.xml", APPEND_PACKAGE_NAME);
            for (String clazz : candidateClasses) {
                this.cdiClasses.add(loader.loadClass(clazz));
            }

            // We must remove already scanned classes

            // Remove EJB modules
            List<EjbJarInfo> ejbModules = module.ejbJars;
            if (ejbModules != null) {
                for (EjbJarInfo ejbModule : ejbModules) {
                    List<InterceptorInfo> interceptors = ejbModule.interceptors;
                    for(InterceptorInfo interceptorInfo : interceptors){
                        candidateClasses.remove(interceptorInfo.clazz);
                    }
                    
                    List<EnterpriseBeanInfo> enterpriseBeans = ejbModule.enterpriseBeans;
                    for (EnterpriseBeanInfo beanInfo : enterpriseBeans) {
                        candidateClasses.remove(beanInfo.ejbClass);
                    }                    
                }
            }

            // Remove Client modules
            List<ClientInfo> clients = module.clients;
            for (ClientInfo clientInfo : clients) {
                candidateClasses.remove(clientInfo.mainClass);
            }

            //These are standalone managed bean classes
            for (String clazz : candidateClasses) {
                this.managedBeansClasses.add(loader.loadClass(clazz));
            }            

        } catch (Exception e) {
            logger.error("Error is occured while scanning the CDI-OpenWebBeans managed classes", e);
            throw new RuntimeException(e);
        }

    }

    public Set<Class<?>> getManagedBeansClasses() {
        return managedBeansClasses;
    }

}
