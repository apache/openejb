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

package org.apache.openejb.mejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Timer;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.naming.Context;
import javax.security.auth.Subject;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.apache.openejb.EJBComponentType;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.security.PermissionManager;
import org.apache.openejb.timer.BasicTimerServiceImpl;
import org.apache.openejb.transaction.TransactionPolicyManager;

/**
 * @version $Rev$ $Date$
 */
public class MEJB extends org.apache.geronimo.j2ee.mejb.MEJB implements RpcEjbDeployment {

    private static final int CREATE_INDEX = -1;
    private static final String DEFAULT_EJB_NAME = "ejb/mgmt/MEJB";

    private final String objectName;
    private final InterfaceMethodSignature[] signatures;
    private final int[] methodMap;
    private final EJBProxyFactory proxyFactory;
    private final ProxyInfo proxyInfo;
    private final FastClass fastClass;
    private final String ejbName;

    // todo remove this as soon as Geronimo supports factory beans
    public MEJB(String objectName, MBeanServerReference mbeanServerReference) {
        this(objectName, mbeanServerReference.getMBeanServer());
    }

    public MEJB(String objectName, MBeanServer mbeanServer) {
        super(objectName, mbeanServer);
        this.objectName = objectName;
        String ejbName;
        try {
            ObjectName oname = ObjectName.getInstance(objectName);
            ejbName = oname.getKeyProperty("name");
            if (ejbName == null) {
                ejbName = DEFAULT_EJB_NAME;
            }
        } catch (MalformedObjectNameException e) {
            ejbName = DEFAULT_EJB_NAME;
        }
        this.ejbName = ejbName;
        fastClass = FastClass.create(MEJB.class);
        LinkedHashMap vopMap = buildSignatures();
        signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        methodMap = new int[signatures.length];
        int i = 0;
        for (Iterator it = vopMap.values().iterator(); it.hasNext();) {
            methodMap[i++] = ((Integer) it.next()).intValue();
        }
        proxyInfo = new ProxyInfo(EJBComponentType.STATELESS, objectName, ManagementHome.class, Management.class, null, null, null, null);
        proxyFactory = new EJBProxyFactory(this);
    }

    private LinkedHashMap buildSignatures() {
        LinkedHashMap vopMap = new LinkedHashMap();
        vopMap.put(new InterfaceMethodSignature("create", true), new Integer(CREATE_INDEX));
        // add the business methods
        Method[] beanMethods = Management.class.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];

            MethodSignature signature = new MethodSignature(beanMethod);
            int methodIndex = fastClass.getIndex(beanMethod.getName(), beanMethod.getParameterTypes());

            vopMap.put(new InterfaceMethodSignature(signature, false),
                    new Integer(methodIndex));
        }

        return vopMap;
    }

    public String getContainerId() {
        return objectName;
    }

    public String getEjbName() {
        return ejbName;
    }

    public EJBHome getEjbHome() {
        return proxyFactory.getEJBHome();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return proxyFactory.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEjbLocalHome() {
        return proxyFactory.getEJBLocalHome();
    }

    public EJBLocalObject getEjbLocalObject(Object primaryKey) {
        return proxyFactory.getEJBLocalObject(primaryKey);
    }

    public Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable {
        throw new EJBException("This invoke style not implemented in MEJB");
    }

    public String[] getJndiNames() {
        return new String[0];
    }

    public String[] getLocalJndiNames() {
        return new String[0];
    }

    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    public EjbDeployment getUnmanagedReference() {
        return this;
    }

    public int getMethodIndex(Method method) {
        return proxyFactory.getMethodIndex(method);
    }

    public InterfaceMethodSignature[] getSignatures() {
        return signatures;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public Subject getDefaultSubject() {
        //TODO this is wrong
        return null;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        int methodIndex = methodMap[ejbInvocation.getMethodIndex()];
        if (methodIndex == CREATE_INDEX) {
            return ejbInvocation.createResult(getEjbObject(null));
        }
        try {
            return ejbInvocation.createResult(fastClass.invoke(methodIndex, this, ejbInvocation.getArguments()));
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                // checked exception - which we simply include in the result
                return ejbInvocation.createExceptionResult((Exception) t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        }
    }

    //TODO It's not clear the following methods should even be here and they need to be implemented!!

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
