/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: MockEJBContainerGBean.java 446239 2006-06-19 21:12:43Z dain $
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
