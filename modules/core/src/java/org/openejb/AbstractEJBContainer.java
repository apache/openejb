/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.dispatch.VirtualOperationFactory;
import org.openejb.entity.bmp.BMPOperationFactory;
import org.openejb.entity.cmp.CMPOperationFactory;
import org.openejb.transaction.EJBUserTransaction;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractEJBContainer implements EJBContainer, GBean {
    protected final Object containerID;
    protected final String ejbName;

    protected final TransactionDemarcation transactionDemarcation;
    protected final ReadOnlyContext componentContext;
    protected final EJBUserTransaction userTransaction;
    protected final String contextId;
    protected final Subject runAs;
    protected final boolean setSecurityInterceptor;
    protected final boolean setPolicyContextHandlerDataEJB;
    protected final boolean setIdentity;

    protected final ClassLoader classLoader;
    protected final Class beanClass;

    protected final Class homeInterface;
    protected final Class remoteInterface;

    protected final Class localHomeInterface;
    protected final Class localInterface;

    protected final Class primaryKeyClass;
    
    private Long remoteId;

    private Map homeMethodMap;
    private Map remoteMethodMap;
    private Map localHomeMethodMap;
    private Map localMethodMap;
    

    public AbstractEJBContainer(EJBContainerConfiguration config, TransactionManager transactionManager, TrackedConnectionAssociator trackedConnectionAssociator) throws Exception {
        // copy over all the config stuff
        containerID = config.containerID;
        ejbName = config.ejbName;
        transactionDemarcation = config.txnDemarcation;
        userTransaction = config.userTransaction;
        componentContext = config.componentContext;
        contextId = config.contextId;
        runAs = config.runAs;
        setSecurityInterceptor = config.setSecurityInterceptor;
        setPolicyContextHandlerDataEJB = config.setPolicyContextHandlerDataEJB;
        setIdentity = config.setIdentity;
        
        

        // load all the classes
        classLoader = Thread.currentThread().getContextClassLoader();
        beanClass = classLoader.loadClass(config.beanClassName);
        if (config.homeInterfaceName != null) {
            homeInterface = classLoader.loadClass(config.homeInterfaceName);
            remoteInterface = classLoader.loadClass(config.remoteInterfaceName);
        } else {
            homeInterface = null;
            remoteInterface = null;
        }
        if (config.localHomeInterfaceName != null) {
            localHomeInterface = classLoader.loadClass(config.localHomeInterfaceName);
            localInterface = classLoader.loadClass(config.localInterfaceName);
        } else {
            localHomeInterface = null;
            localInterface = null;
        }
        
        if (config.pkClassName != null){
            primaryKeyClass = classLoader.loadClass(config.pkClassName);
        } else {
            primaryKeyClass = null;
        }
        
        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionManager, trackedConnectionAssociator);
        }
    }
    
    protected abstract EJBProxyFactory getProxyFactory();
    

    public Object invoke(Method method, Object[] args, Object primKey) throws Throwable{
        EJBInterfaceType invocationType = null;

        Class clazz = method.getDeclaringClass();
        Integer index = null;
        if (EJBHome.class.isAssignableFrom(clazz)){
            invocationType = EJBInterfaceType.HOME;
            index = (Integer) homeMethodMap.get(method);
        } else if (EJBObject.class.isAssignableFrom(clazz)){
            invocationType = EJBInterfaceType.REMOTE;
            index = (Integer) remoteMethodMap.get(method);
        } else if (EJBLocalObject.class.isAssignableFrom(clazz)){
            invocationType = EJBInterfaceType.LOCALHOME;
            index = (Integer) localMethodMap.get(method);
        } else if (EJBLocalHome.class.isAssignableFrom(clazz)){
            invocationType = EJBInterfaceType.LOCAL;
            index = (Integer) localHomeMethodMap.get(method);
        }
        
        if(index == null) {
            index = new Integer(-1);
        }
        
        EJBInvocationImpl invocation = new EJBInvocationImpl(invocationType, primKey, index.intValue(), args);

        InvocationResult result = null;
        try {
            result = invoke(invocation);
        } catch (Throwable e) {
            RemoteException re = new RemoteException("The bean encountered a non-application exception. method", e);
            throw new InvalidateReferenceException( re );
        }

        if (result.isException()) {
            throw new org.openejb.ApplicationException(result.getException());
        } else {
            return result.getResult();
        }
    }
    
    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        if (userTransaction != null) {
            userTransaction.setOnline(true);
        }
    }

    public void doStop() throws WaitingException, Exception {
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }
    }

    public void doFail() {
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }
    }

    public String getEJBName() {
        return ejbName;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
    }

    public EJBHome getEJBHome() {
        return getProxyFactory().getEJBHome();
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return getProxyFactory().getEJBObject(primaryKey);
    }

    public EJBLocalHome getEJBLocalHome() {
        return getProxyFactory().getEJBLocalHome();
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return getProxyFactory().getEJBLocalObject(primaryKey);
    }

    public TransactionDemarcation getDemarcation() {
        return transactionDemarcation;
    }

    public EJBUserTransaction getUserTransaction() {
        return userTransaction;
    }
    

    public ReadOnlyContext getComponentContext() {
        return componentContext;
    }

    public Object getContainerID() {
        return containerID;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("OpenEJB EJB Container",
                AbstractEJBContainer.class.getName());
        /**
         * Default constructor; takes onl a EJBContainerConfiguration Object.
         * Any containers that wish to have different constructors (CMPContainer and MDBContainer have multiple argument
         * constructors), must override by setting their own constructor at GBEAN_INFO initialisation.
         */
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"EJBContainerConfiguration", "TransactionManager", "TrackedConnectionAssociator"},
                new Class[]{EJBContainerConfiguration.class, TransactionManager.class, TrackedConnectionAssociator.class}));

        infoFactory.addAttribute(new GAttributeInfo("EJBContainerConfiguration", true));

        infoFactory.addReference(new GReferenceInfo("TransactionManager", TransactionManager.class.getName()));
        infoFactory.addReference(new GReferenceInfo("TrackedConnectionAssociator", TrackedConnectionAssociator.class.getName()));

        /**
         *	TODO: Dain informs me at some point we'll make these attributes, but currently JNDI Referencer can't support it in the way we want.
         */

        infoFactory.addOperation(new GOperationInfo("getComponentContext"));
        infoFactory.addOperation(new GOperationInfo("getDemarcation"));
        infoFactory.addOperation(new GOperationInfo("getEJBHome"));
        infoFactory.addOperation(new GOperationInfo("getEJBLocalHome"));
        infoFactory.addOperation(new GOperationInfo("getEJBLocalObject", new String[]{Object.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getEJBName"));
        infoFactory.addOperation(new GOperationInfo("getEJBObject", new String[]{Object.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getUserTransaction"));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
    protected void buildMethodMap(MethodSignature[] signatures) {
        if (homeInterface != null) {
            homeMethodMap = MethodHelper.getHomeMethodMap(signatures, homeInterface);
            remoteMethodMap = MethodHelper.getObjectMethodMap(signatures, remoteInterface);
        }
        if (localHomeInterface != null) {
            localHomeMethodMap = MethodHelper.getHomeMethodMap(signatures, localHomeInterface);
            localMethodMap = MethodHelper.getObjectMethodMap(signatures, localInterface);
        }
    }

}


