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
package org.apache.openejb.slsb;

import java.lang.reflect.Method;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.openejb.proxy.ProxyInfo;

public class MockEJBContainerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MockEJBContainerGBean.class, MockEjbDeployment.class);

        infoFactory.addAttribute("ejbJarURL", URL.class, true);
        infoFactory.addAttribute("ejbName", String.class, true);
        infoFactory.addAttribute("ejbClass", String.class, true);
        infoFactory.addAttribute("home", String.class, true);
        infoFactory.addAttribute("remote", String.class, true);
        infoFactory.addAttribute("localHome", String.class, true);
        infoFactory.addAttribute("local", String.class, true);
        infoFactory.addAttribute("serviceEndpoint", String.class, true);
        infoFactory.addOperation("getMethodIndex", new Class[] {Method.class});
        infoFactory.addOperation("invoke", new Class[]{Invocation.class});
        infoFactory.addOperation("invoke", new Class[]{Method.class, Object[].class, Object.class});
        infoFactory.addAttribute("EJBName", String.class, true);
        infoFactory.addAttribute("ProxyInfo", ProxyInfo.class, true);

        infoFactory.setConstructor(new String[]{
            "ejbJarURL",
            "ejbName",
            "ejbClass",
            "home",
            "remote",
            "localHome",
            "local",
            "serviceEndpoint"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

//    public static ObjectName addGBean(Kernel kernel, URL ejbJarURL, String ejbName, String ejbClass, String home, String remote, String localHome, String local, String serviceEndpoint) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//        GBeanData gbean = createGBean(ejbJarURL, ejbName, ejbClass, home, remote, localHome, local, serviceEndpoint);
//        kernel.loadGBean(gbean, MockEjbDeployment.class.getClassLoader());
//        kernel.startGBean(gbean.getName());
//        return gbean.getName();
//    }
//
//    public static ObjectName addGBean(Kernel kernel, URL ejbJarURL, String ejbName, String ejbClass, String home, String remote, String localHome, String local, String serviceEndpoint, ClassLoader cl) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//        GBeanData gbean = createGBean(ejbJarURL, ejbName, ejbClass, home, remote, localHome, local, serviceEndpoint);
//        kernel.loadGBean(gbean, cl);
//        kernel.startGBean(gbean.getName());
//        return gbean.getName();
//    }
//
//    public static GBeanData createGBean(URL ejbJarURL, String ejbName, String ejbClass, String home, String remote, String localHome, String local, String serviceEndpoint) {
//        ObjectName gbeanName = JMXUtil.getObjectName("openejb:j2eeType=StatelessSessionBean,name=" + ejbName);
//
//        GBeanData gbean = new GBeanData(gbeanName, MockEJBContainerGBean.GBEAN_INFO);
//        gbean.setAttribute("ejbJarURL", ejbJarURL);
//        gbean.setAttribute("ejbName", ejbName);
//        gbean.setAttribute("ejbClass", ejbClass);
//        gbean.setAttribute("home", home);
//        gbean.setAttribute("remote", remote);
//        gbean.setAttribute("localHome", localHome);
//        gbean.setAttribute("local", local);
//        gbean.setAttribute("serviceEndpoint", serviceEndpoint);
//
//        return gbean;
//    }
}
