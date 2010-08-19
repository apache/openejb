/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.atinjecttck;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.atinjecttck.specific.SpecificProducer;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.webbeans.container.BeanManagerImpl;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;

public class AtInjectContainer
{ 
    private static Set<Class<?>> deploymentClasses = null;
    private InitialContext ctx;

    static
    {
        deploymentClasses = new HashSet<Class<?>>();
        deploymentClasses.add(Convertible.class);
        deploymentClasses.add(Seat.class);
        deploymentClasses.add(Tire.class);
        deploymentClasses.add(V8Engine.class);
        deploymentClasses.add(Cupholder.class);
        deploymentClasses.add(FuelTank.class);
        
        //Adding our special producer
        deploymentClasses.add(SpecificProducer.class);
        
    }
    
    public AtInjectContainer()
    {
        
    }
    
    public Test start()
    {
        try
        {
            //deploy(deploymentClasses);
            ConfigurationFactory config = new ConfigurationFactory();
            Assembler assembler = new Assembler();

            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

            EjbJar ejbJar = new EjbJar();
            Beans ejbBeans = new Beans();
            for (Class clazz : deploymentClasses) {
            	ejbBeans.addManagedClass(clazz);
            }

            EjbModule module = new EjbModule(ejbJar);
            module.setBeans(ejbBeans);

            assembler.createApplication(config.configureApplication(module), this.getClass().getClassLoader());  
            
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        	
            final BeanManager manager = BeanManagerImpl.getManager(); 
            Set<Bean<?>> beans = manager.getBeans(Car.class, new Annotation[0]);
            Bean<?> carBean = beans.iterator().next();
            
            Car car = (Car)manager.getReference(carBean , Car.class , manager.createCreationalContext(carBean));
            
            return Tck.testsFor(car, false, true);
            
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    public void stop()
    {
                  

    }
    
}
