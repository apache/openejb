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
 * $Id$
 */
package org.openejb.slsb;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;


public class MockEJBContainer implements EJBContainer {
    private final ProxyInfo proxyInfo;
    private final Class ejbClass;

    public MockEJBContainer() {
        this.proxyInfo = new ProxyInfo(EJBComponentType.STATELESS, "foo", MockHome.class, MockRemote.class, MockLocalHome.class, MockLocal.class, MockServiceEndpoint.class, null);
        ejbClass = MockEJB.class;
    }

    public MockEJBContainer(URL ejbJarURL, String ejbName, String ejbClass, String home, String remote, String localHome, String local, String serviceEndpoint) {
        ClassLoader cl = new URLClassLoader(new URL[]{ejbJarURL}, MockEJBContainer.class.getClassLoader());
        try {
            this.proxyInfo = new ProxyInfo(EJBComponentType.STATELESS, ejbName, cl.loadClass(home), cl.loadClass(remote), cl.loadClass(localHome), cl.loadClass(local), cl.loadClass(serviceEndpoint), null);
            this.ejbClass = cl.loadClass(ejbClass);
        } catch (ClassNotFoundException e) {
            throw (IllegalStateException) new IllegalStateException("Could not initialize the MockEJBContainer").initCause(e);
        }
    }

    public Object getContainerID() {
        return null;
    }

    public String getEJBName() {
        return null;
    }

    public EJBHome getEJBHome() {
        return null;
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return null;
    }

    public EJBLocalHome getEJBLocalHome() {
        return null;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return null;
    }

    public Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable {
        Method ejbMethod = ejbClass.getMethod(callMethod.getName(), callMethod.getParameterTypes());

        Object ejb = ejbClass.newInstance();
        return ejbMethod.invoke(ejb, args);
    }

    public String[] getJndiNames() {
        return new String[0];
    }

    public String[] getLocalJndiNames() {
        return new String[0];
    }

    public EJBProxyFactory getProxyFactory() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return MockEJBContainer.class.getClassLoader();
    }

    public EJBContainer getUnmanagedReference() {
        return null;
    }

    public InterfaceMethodSignature[] getSignatures() {
        return new InterfaceMethodSignature[0];
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public Subject getDefaultSubject() {
        return null;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(MockEJBContainer.class);

        infoFactory.addOperation("invoke", new Class[]{Invocation.class});
        infoFactory.addOperation("invoke", new Class[]{Method.class, Object[].class, Object.class});
        infoFactory.addAttribute("EJBName", String.class, true);
        infoFactory.addAttribute("ProxyInfo", ProxyInfo.class, true);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public static ObjectName addGBean(Kernel kernel, String name) throws GBeanAlreadyExistsException, GBeanNotFoundException {
        ObjectName gbeanName = JMXUtil.getObjectName("openejb:j2eeType=StatelessSessionBean,name=" + name);

        GBeanData gbean1 = new GBeanData(gbeanName, MockEJBContainer.GBEAN_INFO);

        GBeanData gbean = gbean1;
        kernel.loadGBean(gbean, MockEJBContainer.class.getClassLoader());
        kernel.startGBean(gbean.getName());
        return gbean.getName();
    }

}
