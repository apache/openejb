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
package org.openejb.nova;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;

import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.transaction.EJBUserTransaction;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractEJBContainer
        implements EJBContainer, GeronimoMBeanTarget {

    private GeronimoMBeanContext context;

    protected final URI uri;
    protected final String ejbClassName;
    protected final String homeClassName;
    protected final String localHomeClassName;
    protected final String remoteClassName;
    protected final String localClassName;
    protected final TransactionDemarcation txnDemarcation;
    protected TransactionManager txnManager;         //not final until Endpoints can be Constructor args.
    protected final ReadOnlyContext componentContext;
    protected final EJBUserTransaction userTransaction;

    protected ClassLoader classLoader;
    protected Class beanClass;
    protected VirtualOperation[] vtable;

    protected EJBRemoteClientContainer remoteClientContainer;
    protected Class homeInterface;
    protected Class remoteInterface;

    protected EJBLocalClientContainer localClientContainer;
    protected Class localHomeInterface;
    protected Class localInterface;

    protected InstancePool pool;
    private Long remoteId;
    private Map homeMethodMap;
    private Map remoteMethodMap;
    private Map localHomeMethodMap;
    private Map localMethodMap;

    public AbstractEJBContainer(EJBContainerConfiguration config) {
        uri = config.uri;
        ejbClassName = config.beanClassName;
        homeClassName = config.homeInterfaceName;
        remoteClassName = config.remoteInterfaceName;
        localHomeClassName = config.localHomeInterfaceName;
        localClassName = config.localInterfaceName;
        txnDemarcation = config.txnDemarcation;
        txnManager = config.txnManager;
        userTransaction = config.userTransaction;
        componentContext = config.componentContext;
    }

    public void setTransactionManager(TransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    public boolean canStart() {
        return true;
    }

    /* Start the Component
     * @see org.apache.geronimo.core.service.AbstractManagedObject#doStart()
     */
    public void doStart() {
        //super.doStart();
        classLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("classloader="+classLoader);
        try {
            if (userTransaction != null) {
                userTransaction.setTransactionManager(txnManager);
            }
            beanClass = classLoader.loadClass(ejbClassName);

            if (homeClassName != null) {
                homeInterface = classLoader.loadClass(homeClassName);
                remoteInterface = classLoader.loadClass(remoteClassName);
            } else {
                homeInterface = null;
                remoteInterface = null;
            }
            if (localHomeClassName != null) {
                localHomeInterface = classLoader.loadClass(localHomeClassName);
                localInterface = classLoader.loadClass(localClassName);
            } else {
                localHomeInterface = null;
                localInterface = null;
            }
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public boolean canStop() {
        return true;
    }

    /* Stop the Component
     * @see org.apache.geronimo.core.service.AbstractManagedObject#doStop()
     */
    public void doStop() {
        homeInterface = null;
        remoteInterface = null;
        localHomeInterface = null;
        localInterface = null;
        beanClass = null;
        if (userTransaction != null) {
            userTransaction.setTransactionManager(null);
        }
        //super.doStop();
    }

    public void doFail() {
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
        return remoteClientContainer.getEJBHome();
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return remoteClientContainer.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEJBLocalHome() {
        return localClientContainer.getEJBLocalHome();
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return localClientContainer.getEJBLocalObject(primaryKey);
    }

    public TransactionDemarcation getDemarcation() {
        return txnDemarcation;
    }

    public EJBUserTransaction getUserTransaction() {
        return userTransaction;
    }

    public ReadOnlyContext getComponentContext() {
        return componentContext;
    }

    /**
     * Return the name of this EJB's implementation class
     * @return the name of this EJB's implementation class
     * @jmx.managed-attribute
     */
    public String getBeanClassName() {
        return ejbClassName;
    }

    /**
     * Return the name of this EJB's home interface class
     * @return the name of this EJB's home interface class
     * @jmx.managed-attribute
     */
    public String getHomeClassName() {
        return homeClassName;
    }

    /**
     * Return the name of this EJB's remote component interface class
     * @return the name of this EJB's remote component interface class
     * @jmx.managed-attribute
     */
    public String getRemoteClassName() {
        return remoteClassName;
    }

    /**
     * Return the name of this EJB's local home class
     * @return the name of this EJB's local home class
     * @jmx.managed-attribute
     */
    public String getLocalHomeClassName() {
        return localHomeClassName;
    }

    /**
     * Return the name of this EJB's local component interface class
     * @return the name of this EJB's local component interface class
     * @jmx.managed-attribute
     */
    public String getLocalClassName() {
        return localClassName;
    }

    protected URI startServerRemoting(Interceptor firstInterceptor) {
        // set up server side remoting endpoint
        DeMarshalingInterceptor demarshaller = new DeMarshalingInterceptor();
        demarshaller.setClassloader(classLoader);
        demarshaller.setNext(firstInterceptor);
        remoteId = InterceptorRegistry.instance.register(demarshaller);
        return uri.resolve("#" + remoteId);
    }

    protected void stopServerRemoting() {
        InterceptorRegistry.instance.unregister(remoteId);
    }

    private Interceptor firstInterceptor;
    private LinkedList interceptors = new LinkedList();

    public final void addInterceptor(Interceptor interceptor) {
        if (firstInterceptor == null) {
            firstInterceptor = interceptor;
            interceptors.addLast(interceptor);
        } else {
            Interceptor lastInterceptor = (Interceptor) interceptors.getLast();
            lastInterceptor.setNext(interceptor);
            interceptors.addLast(interceptor);
        }
    }

    public void clearInterceptors() {
        interceptors.clear();
        firstInterceptor = null;
    }

    protected void buildMethodMap(MethodSignature[] signatures) {
        if (homeInterface != null) {
            homeMethodMap = MethodHelper.getHomeMethodMap(signatures, homeInterface);
            remoteMethodMap = MethodHelper.getHomeMethodMap(signatures, remoteInterface);
        }
        if (localHomeInterface != null) {
            localHomeMethodMap = MethodHelper.getHomeMethodMap(signatures, localHomeInterface);
            localMethodMap = MethodHelper.getHomeMethodMap(signatures, localInterface);
        }
    }

    public int getMethodIndex(Method method, EJBInvocationType invocationType) {
        Integer index = null;
        if (invocationType == EJBInvocationType.HOME) {
            index = (Integer) homeMethodMap.get(method);
        } else if (invocationType == EJBInvocationType.REMOTE) {
            index = (Integer) remoteMethodMap.get(method);
        } else if (invocationType == EJBInvocationType.LOCALHOME) {
            index = (Integer) localHomeMethodMap.get(method);
        } else if (invocationType == EJBInvocationType.LOCAL) {
            index = (Integer) localMethodMap.get(method);
        }
        if(index != null) {
            return index.intValue();
        }
        return -1;
    }
}


