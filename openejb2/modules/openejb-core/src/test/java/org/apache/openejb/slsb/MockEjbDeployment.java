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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import javax.naming.Context;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.openejb.EJBComponentType;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.timer.BasicTimerServiceImpl;
import org.apache.openejb.transaction.TransactionPolicyManager;
import org.apache.openejb.security.PermissionManager;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.proxy.ProxyInfo;


public class MockEjbDeployment implements RpcEjbDeployment {
    private final ProxyInfo proxyInfo;
    private final Class ejbClass;
    private final Method[] methods;

    public MockEjbDeployment() {
        this.proxyInfo = new ProxyInfo(EJBComponentType.STATELESS, "foo", MockHome.class, MockRemote.class, MockLocalHome.class, MockLocal.class, MockServiceEndpoint.class, null);
        ejbClass = MockEJB.class;
        methods = ejbClass.getMethods();
    }

    public MockEjbDeployment(URL ejbJarURL, String ejbName, String ejbClass, String home, String remote, String localHome, String local, String serviceEndpoint) {
        ClassLoader cl = new URLClassLoader(new URL[]{ejbJarURL}, MockEjbDeployment.class.getClassLoader());
        try {
            this.proxyInfo = new ProxyInfo(EJBComponentType.STATELESS, ejbName, cl.loadClass(home), cl.loadClass(remote), cl.loadClass(localHome), cl.loadClass(local), cl.loadClass(serviceEndpoint), null);
            this.ejbClass = cl.loadClass(ejbClass);
            this.methods = this.ejbClass.getMethods();
        } catch (ClassNotFoundException e) {
            throw (IllegalStateException) new IllegalStateException("Could not initialize the MockEJBContainer").initCause(e);
        }
    }

    public int getMethodIndex(Method callMethod) {
        assert callMethod == null: "Method cannot be null";
        Method ejbMethod = null;
        try {
            ejbMethod = ejbClass.getMethod(callMethod.getName(), callMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Method does not exist in bean class: " + callMethod);
        }

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(ejbMethod)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Method does not exist in bean class: " + callMethod);
    }

    public String getContainerId() {
        return null;
    }

    public String getEjbName() {
        return null;
    }

    public EJBHome getEjbHome() {
        return null;
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return null;
    }

    public EJBLocalHome getEjbLocalHome() {
        return null;
    }

    public EJBLocalObject getEjbLocalObject(Object primaryKey) {
        return null;
    }

    public Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable {
        Method ejbMethod = ejbClass.getMethod(callMethod.getName(), callMethod.getParameterTypes());

        Object ejb = ejbClass.newInstance();
        return ejbMethod.invoke(ejb, args);
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        try {
            org.apache.openejb.EjbInvocation i = (org.apache.openejb.EjbInvocation) invocation;
            Object result = invoke(methods[i.getMethodIndex()], i.getArguments(), i.getId());
            return new SimpleInvocationResult(true, result);
        } catch (Throwable throwable) {
            return new SimpleInvocationResult(false, throwable);
        }
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
        return MockEjbDeployment.class.getClassLoader();
    }

    public EjbDeployment getUnmanagedReference() {
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MockEjbDeployment.class);

        infoFactory.addAttribute("ejbName", String.class, true);
        infoFactory.addAttribute("ProxyInfo", ProxyInfo.class, true);

        infoFactory.addOperation("getMethodIndex", new Class[] {Method.class});
        infoFactory.addOperation("getEjbObject", new Class[] {Object.class});
        infoFactory.addOperation("getEjbLocalObject", new Class[] {Object.class});

        infoFactory.addOperation("invoke", new Class[]{Invocation.class});
        infoFactory.addOperation("invoke", new Class[]{Method.class, Object[].class, Object.class});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

//    public static ObjectName addGBean(Kernel kernel, String name) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//        ObjectName gbeanName = JMXUtil.getObjectName("openejb:j2eeType=StatelessSessionBean,name=" + name);
//
//        GBeanData gbean1 = new GBeanData(gbeanName, MockEjbDeployment.GBEAN_INFO);
//
//        GBeanData gbean = gbean1;
//        kernel.loadGBean(gbean, MockEjbDeployment.class.getClassLoader());
//        kernel.startGBean(gbean.getName());
//        return gbean.getName();
//    }
//
//    public static ObjectName addGBean(Kernel kernel, String name, ClassLoader cl) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//        ObjectName gbeanName = JMXUtil.getObjectName("openejb:j2eeType=StatelessSessionBean,name=" + name);
//
//        GBeanData gbean1 = new GBeanData(gbeanName, MockEjbDeployment.GBEAN_INFO);
//
//        GBeanData gbean = gbean1;
//        kernel.loadGBean(gbean, cl);
//        kernel.startGBean(gbean.getName());
//        return gbean.getName();
//    }

    //TODO not sure if the following need implementation

    public Subject getRunAsSubject() {
        return null;
    }

    public Context getComponentContext() {
        return null;
    }

    public void logSystemException(Throwable t) {
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        return null;
    }

    public boolean isSecurityEnabled() {
        return false;
    }

    public String getPolicyContextId() {
        return null;
    }

    public PermissionManager getPermissionManager() {
        return null;
    }

    public TransactionPolicyManager getTransactionPolicyManager() {
        return null;
    }

    public Class getBeanClass() {
        return null;
    }

    public Timer getTimerById(Long id) {
        return null;
    }

    public BasicTimerServiceImpl getTimerService() {
        return null;
    }

    public Set getUnshareableResources() {
        return null;
    }

    public Set getApplicationManagedSecurityResources() {
        return null;
    }
}
