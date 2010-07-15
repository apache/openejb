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

import java.lang.annotation.Annotation;
import java.util.Properties;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.DeploymentListener;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Assembler;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.BeansDeployer;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.jms.JMSManager;
import org.apache.webbeans.plugins.PluginLoader;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.portable.events.ExtensionLoader;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.proxy.JavassistProxyFactory;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;

public class CdiAppContainer implements ContainerLifecycle, DeploymentListener{

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), CdiAppContainer.class);

    private ContextsService contexsServices;    
    private BeanManager beanManager;
    private final ClassLoader classLoader;
    private final AppInfo appInfo;
    private CdiAppScannerService scannerService;
    
    public CdiAppContainer(ClassLoader classLoader, AppInfo appInfo){
	this.classLoader = classLoader;
	this.appInfo = appInfo;
    }
    
    @Override
    public BeanManager getBeanManager() {
	return this.beanManager;
    }

    @Override
    public ContextsService getContextService() {
	return this.contexsServices;
    }

    @Override
    public void initApplication(Properties properties) {
	//No operation
    }

    @Override
    public void startApplication(Object startupObject) throws Exception {
	long startTime = System.currentTimeMillis();
	ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
	try{
	    if(logger.isInfoEnabled()){
		logger.info("Starting the CDI-OpenWebBeans container for the application location," + ((AppInfo)startupObject).jarPath);
	    }
	    
	    //Set classloader
	    Thread.currentThread().setContextClassLoader(this.classLoader);
	    
	    //Configure our scanner service
	    this.scannerService = (CdiAppScannerService)WebBeansFinder.getSingletonInstance("org.apache.openejb.cdi.CdiAppScannerService", this.classLoader);	    
	    this.scannerService.setLoader(this.classLoader);
	    this.scannerService.setAppInfo(this.appInfo);

	    //Scan the module
	    this.scannerService.scan();
	    
	    //Get deployment BeanManager instance
	    this.beanManager = BeanManagerImpl.getManager();
	    
	    //Start our plugins
	    PluginLoader.getInstance().startUp();
	    
	    //Initialize our contexts
	    this.contexsServices = (CdiAppContextsService)WebBeansFinder.getSingletonInstance("org.apache.openejb.cdi.CdiAppContextsService", this.classLoader);
	    this.contexsServices.init(startupObject);  
	    
	    //Get Plugin
	    CdiPlugin cdiPlugin = (CdiPlugin)PluginLoader.getInstance().getEjbPlugin();
	    cdiPlugin.setAppModule(this.appInfo);
	    cdiPlugin.setClassLoader(this.classLoader);
	    
	    //Configure EJB Deployments
	    cdiPlugin.configureDeployments();
	    
	    //Resournce Injection Service
	    CdiResourceInjectionService injectionService = (CdiResourceInjectionService)WebBeansFinder.getSingletonInstance("org.apache.openejb.cdi.CdiResourceInjectionService", this.classLoader);
	    injectionService.setAppModule(this.appInfo);
	    injectionService.setClassLoader(this.classLoader);
	    
	    //Build injections for managed beans
	    injectionService.buildInjections(this.scannerService.getManagedBeansClasses());
	    
	    //Deploy the beans
	    BeansDeployer deployer = new BeansDeployer(new WebBeansXMLConfigurator());
	    deployer.deploy(this.scannerService);	    
	    
	    if(logger.isInfoEnabled()){
		logger.info("CDI-OpenWebBeans container has started in " + (System.currentTimeMillis() - startTime) + " in ms");
	    }
	    
	}catch(Exception e){
	    String errorMessage = "Error is occured while starting the CDI container, looks error log for further investigation";
	    logger.error(errorMessage, e);
	    throw new OpenEJBException(errorMessage, e);
	}finally{
	    Thread.currentThread().setContextClassLoader(oldCl);
	}
    }

    @Override
    public void stopApplication(Object endObject) {
	ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
	try{	    
	    if(logger.isInfoEnabled()){
		logger.info("Stopping the CDI-OpenWebBeans container for the application location," + ((AppInfo)endObject).jarPath);
	    }
	    
	    //Setting context class loader for cleaning
	    Thread.currentThread().setContextClassLoader(this.classLoader);
	    
            //Fire shut down
            this.beanManager.fireEvent(new BeforeShutdownImpl(), new Annotation[0]);
            
            //Destroys context
            this.contexsServices.destroy(endObject);
            
            //Free all plugin resources
            PluginLoader.getInstance().shutDown();
            
            //Clear extensions
            ExtensionLoader.getInstance().clear();
            
            //Delete Resolutions Cache
            InjectionResolver.getInstance().clearCaches();
            
            //Delte proxies
            JavassistProxyFactory.getInstance().clear();
            
            //Delete AnnotateTypeCache
            AnnotatedElementFactory.getInstance().clear();
            
            //JMs Manager clear
            JMSManager.getInstance().clear();
            
            //Clear the resource injection service
            CdiResourceInjectionService injectionServices = (CdiResourceInjectionService)WebBeansFinder.getSingletonInstance("org.apache.openejb.cdi.CdiResourceInjectionService", this.classLoader);
            injectionServices.clear();

            //ContextFactory cleanup
            ContextFactory.cleanUpContextFactory();            
            
            //Clear singleton list
            WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());
            
            //Remove listener from Assembler
	    org.apache.openejb.assembler.classic.Assembler assembler = (org.apache.openejb.assembler.classic.Assembler) SystemInstance.get().getComponent(Assembler.class);
	    assembler.removeDeploymentListener(this);	    
	    
	    	    
	}catch(Exception e){
	    String errorMessage = "Error is occured while stopping the CDI container for the application jarpath," + this.appInfo.jarPath + "looks error log for further investigation";
	    logger.error(errorMessage, e);
	}finally{
	    Thread.currentThread().setContextClassLoader(oldCl);
	}
	
    }

    @Override
    public void afterApplicationCreated(AppInfo appInfo) {
	if(this.appInfo.jarPath.equals(appInfo.jarPath)){
	    try {
		startApplication(appInfo);
	    } catch (Exception e) {
		//We can do nothing
	    }
	}
    }

    @Override
    public void beforeApplicationDestroyed(AppInfo appInfo) {
	if(this.appInfo.jarPath.equals(appInfo.jarPath)){
	    
	    //Stops the application
	    stopApplication(appInfo);
	    this.beanManager = null;
	    this.contexsServices = null;
	    this.scannerService = null;
	}
    }

}
