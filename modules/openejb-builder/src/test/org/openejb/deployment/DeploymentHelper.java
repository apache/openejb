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
package org.openejb.deployment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.connector.ActivationSpecWrapperGBean;
import org.apache.geronimo.connector.ResourceAdapterModuleImplGBean;
import org.apache.geronimo.connector.ResourceAdapterWrapperGBean;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.connector.work.GeronimoWorkManagerGBean;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledNonTransactionalTimer;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledTransactionalTimer;
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;
import org.apache.geronimo.transaction.manager.XidFactoryImplGBean;
import org.openejb.deployment.mdb.mockra.MockActivationSpec;
import org.openejb.deployment.mdb.mockra.MockResourceAdapter;
import org.openejb.slsb.DefaultStatelessEjbContainer;
import org.openejb.sfsb.DefaultStatefulEjbContainer;
import org.openejb.entity.bmp.DefaultBmpEjbContainer;
import org.openejb.entity.cmp.DefaultCmpEjbContainer;
import org.openejb.mdb.DefaultMdbContainer;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class DeploymentHelper {
    //these have to match the domain and server set in KernelHelper mock config store
    public static final String j2eeDomainName = "test";
    public static final String j2eeServerName = "bar";
    private static final String appName = NameFactory.NULL;
    private static final String moduleName = "MockRA";
    //type is random to look for problems.
    private static final J2eeContext raContext = new J2eeContextImpl(j2eeDomainName, j2eeServerName, appName, NameFactory.RESOURCE_ADAPTER_MODULE, moduleName, "xxx", NameFactory.JCA_WORK_MANAGER);
    public static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    public static final ObjectName STATELESS_EJB_CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:name=Stateless,j2eeType=EjbContainer");
    public static final ObjectName STATEFUL_EJB_CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:name=Stateful,j2eeType=EjbContainer");
    public static final ObjectName BMP_EJB_CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:name=Bmp,j2eeType=EjbContainer");
    public static final ObjectName CMP_EJB_CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:name=Cmp,j2eeType=EjbContainer");
    public static final ObjectName MDB_EJB_CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:name=Mdb,j2eeType=EjbContainer");
    public static final ObjectName XIDFACTORY_NAME = JMXUtil.getObjectName(j2eeDomainName + ":type=" + NameFactory.XID_FACTORY);
    public static final ObjectName TRANSACTIONMANAGER_NAME = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    public static final ObjectName TRANSACTIONCONTEXTMANAGER_NAME = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionContextManager");
    public static final ObjectName TRACKEDCONNECTIONASSOCIATOR_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    public static final ObjectName WORKMANAGER_NAME = JMXUtil.getObjectName("geronimo.server:type=WorkManager,name=DefaultWorkManager");

    public static final ObjectName RESOURCE_ADAPTER_MODULE_NAME;
    public static final ObjectName RESOURCE_ADAPTER_NAME;
    public static final ObjectName ACTIVATIONSPEC_NAME;
    public static final ObjectName THREADPOOL_NAME = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPool,name=DefaultThreadPool");
    public static final ObjectName TRANSACTIONALTIMER_NAME = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    public static final ObjectName NONTRANSACTIONALTIMER_NAME = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");
    public static final GBeanData ACTIVATION_SPEC_INFO = new GBeanData(ActivationSpecWrapperGBean.getGBeanInfo());

    static {
        try {
            RESOURCE_ADAPTER_MODULE_NAME = NameFactory.getModuleName(null, null, null, null, null, raContext);
            RESOURCE_ADAPTER_NAME = NameFactory.getComponentName(null, null, null, NameFactory.JCA_RESOURCE, null, "MockRA", NameFactory.JCA_RESOURCE_ADAPTER, raContext);
            ACTIVATIONSPEC_NAME = NameFactory.getComponentName(null, null, null, NameFactory.JCA_RESOURCE, null, "MockRA", NameFactory.JCA_ACTIVATION_SPEC, raContext);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    public static Kernel setUpKernelWithTransactionManager() throws Exception {
        Kernel kernel = KernelHelper.getPreparedKernel();

        GBeanData xidFacGBean = new GBeanData(XIDFACTORY_NAME, XidFactoryImplGBean.GBEAN_INFO);
        xidFacGBean.setAttribute("tmId", "WHAT DO WE CALL IT?".getBytes());
        start(kernel, xidFacGBean);
        
        GBeanData tmGBean = new GBeanData(TRANSACTIONMANAGER_NAME, TransactionManagerImplGBean.GBEAN_INFO);
        Set rmpatterns = new HashSet();
        rmpatterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tmGBean.setAttribute("defaultTransactionTimeoutSeconds", new Integer(100));
        tmGBean.setReferencePattern("XidFactory", XIDFACTORY_NAME);
        tmGBean.setReferencePatterns("ResourceManagers", rmpatterns);
        start(kernel, tmGBean);

        GBeanData tcmGBean = new GBeanData(TRANSACTIONCONTEXTMANAGER_NAME, TransactionContextManagerGBean.GBEAN_INFO);
        tcmGBean.setReferencePattern("TransactionManager", TRANSACTIONMANAGER_NAME);
        tcmGBean.setReferencePattern("XidImporter", TRANSACTIONMANAGER_NAME);
        start(kernel, tcmGBean);

        GBeanData trackedConnectionAssociator = new GBeanData(TRACKEDCONNECTIONASSOCIATOR_NAME, ConnectionTrackingCoordinatorGBean.GBEAN_INFO);
        start(kernel, trackedConnectionAssociator);

        GBeanData threadPoolGBean = new GBeanData(THREADPOOL_NAME, ThreadPool.GBEAN_INFO);
        threadPoolGBean.setAttribute("keepAliveTime", new Long(5000));
        threadPoolGBean.setAttribute("poolSize", new Integer(5));
        threadPoolGBean.setAttribute("poolName", "DefaultThreadPool");
        start(kernel, threadPoolGBean);

        GBeanData transactionalTimerGBean = new GBeanData(TRANSACTIONALTIMER_NAME, VMStoreThreadPooledTransactionalTimer.GBEAN_INFO);
        transactionalTimerGBean.setAttribute("repeatCount", new Integer(5));
        transactionalTimerGBean.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        transactionalTimerGBean.setReferencePattern("ThreadPool", THREADPOOL_NAME);
        start(kernel, transactionalTimerGBean);

        GBeanData nonTransactionalTimerGBean = new GBeanData(NONTRANSACTIONALTIMER_NAME, VMStoreThreadPooledNonTransactionalTimer.GBEAN_INFO);
        nonTransactionalTimerGBean.setReferencePattern("ThreadPool", THREADPOOL_NAME);
        start(kernel, nonTransactionalTimerGBean);

        GBeanData statelessInterceptorStack = new GBeanData(STATELESS_EJB_CONTAINER_NAME, DefaultStatelessEjbContainer.GBEAN_INFO);
        statelessInterceptorStack.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        statelessInterceptorStack.setReferencePattern("TrackedConnectionAssociator", TRACKEDCONNECTIONASSOCIATOR_NAME);
        statelessInterceptorStack.setReferencePattern("TransactedTimer", TRANSACTIONALTIMER_NAME);
        statelessInterceptorStack.setReferencePattern("NontransactedTimer", NONTRANSACTIONALTIMER_NAME);
        start(kernel, statelessInterceptorStack);

        GBeanData statefulInterceptorStack = new GBeanData(STATEFUL_EJB_CONTAINER_NAME, DefaultStatefulEjbContainer.GBEAN_INFO);
        statefulInterceptorStack.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        statefulInterceptorStack.setReferencePattern("TrackedConnectionAssociator", TRACKEDCONNECTIONASSOCIATOR_NAME);
        start(kernel, statefulInterceptorStack);

        GBeanData bmpInterceptorStack = new GBeanData(BMP_EJB_CONTAINER_NAME, DefaultBmpEjbContainer.GBEAN_INFO);
        bmpInterceptorStack.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        bmpInterceptorStack.setReferencePattern("TrackedConnectionAssociator", TRACKEDCONNECTIONASSOCIATOR_NAME);
        bmpInterceptorStack.setReferencePattern("TransactedTimer", TRANSACTIONALTIMER_NAME);
        bmpInterceptorStack.setReferencePattern("NontransactedTimer", NONTRANSACTIONALTIMER_NAME);
        start(kernel, bmpInterceptorStack);

        GBeanData cmpInterceptorStack = new GBeanData(CMP_EJB_CONTAINER_NAME, DefaultCmpEjbContainer.GBEAN_INFO);
        cmpInterceptorStack.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        cmpInterceptorStack.setReferencePattern("TrackedConnectionAssociator", TRACKEDCONNECTIONASSOCIATOR_NAME);
        cmpInterceptorStack.setReferencePattern("TransactedTimer", TRANSACTIONALTIMER_NAME);
        cmpInterceptorStack.setReferencePattern("NontransactedTimer", NONTRANSACTIONALTIMER_NAME);
        start(kernel, cmpInterceptorStack);

        GBeanData mdbInterceptorStack = new GBeanData(MDB_EJB_CONTAINER_NAME, DefaultMdbContainer.GBEAN_INFO);
        mdbInterceptorStack.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        mdbInterceptorStack.setReferencePattern("TrackedConnectionAssociator", TRACKEDCONNECTIONASSOCIATOR_NAME);
        mdbInterceptorStack.setReferencePattern("TransactedTimer", TRANSACTIONALTIMER_NAME);
        mdbInterceptorStack.setReferencePattern("NontransactedTimer", NONTRANSACTIONALTIMER_NAME);
        start(kernel, mdbInterceptorStack);

        return kernel;
    }

    public static void setUpResourceAdapter(Kernel kernel) throws Exception {
        GBeanData geronimoWorkManagerGBean = new GBeanData(WORKMANAGER_NAME, GeronimoWorkManagerGBean.getGBeanInfo());
        geronimoWorkManagerGBean.setAttribute("syncMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setAttribute("startMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setAttribute("scheduledMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        start(kernel, geronimoWorkManagerGBean);

        Map activationSpecInfoMap = new HashMap();
        ACTIVATION_SPEC_INFO.setAttribute("activationSpecClass", MockActivationSpec.class.getName());
        activationSpecInfoMap.put(javax.jms.MessageListener.class.getName(), ACTIVATION_SPEC_INFO);
        GBeanData moduleData = new GBeanData(RESOURCE_ADAPTER_MODULE_NAME, ResourceAdapterModuleImplGBean.GBEAN_INFO);
        moduleData.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
        start(kernel, moduleData);

        GBeanData resourceAdapterGBean = new GBeanData(RESOURCE_ADAPTER_NAME, ResourceAdapterWrapperGBean.getGBeanInfo());
        resourceAdapterGBean.setAttribute("resourceAdapterClass", MockResourceAdapter.class.getName());
        resourceAdapterGBean.setReferencePattern("WorkManager", WORKMANAGER_NAME);
        start(kernel, resourceAdapterGBean);

        GBeanData activationSpecGBean = new GBeanData(ACTIVATIONSPEC_NAME, ActivationSpecWrapperGBean.getGBeanInfo());
        activationSpecGBean.setAttribute("activationSpecClass", MockActivationSpec.class.getName());
        activationSpecGBean.setAttribute("containerId", CONTAINER_NAME.getCanonicalName());
        activationSpecGBean.setReferencePattern("ResourceAdapterWrapper", RESOURCE_ADAPTER_NAME);
        start(kernel, activationSpecGBean);
    }

    private static void start(Kernel kernel, GBeanData gbeanData) throws InternalKernelException, GBeanAlreadyExistsException , GBeanNotFoundException {
        kernel.loadGBean(gbeanData, DeploymentHelper.class.getClassLoader());
        kernel.startGBean(gbeanData.getName());
    }

    public static void tearDownAdapter(Kernel kernel) throws Exception {
        stop(kernel, ACTIVATIONSPEC_NAME);
        stop(kernel, RESOURCE_ADAPTER_NAME);
        stop(kernel, WORKMANAGER_NAME);
    }

    public static void stop(Kernel kernel, ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }
}
