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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.ActivationSpecInfo;
import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledTransactionalTimer;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledNonTransactionalTimer;
import org.openejb.mdb.mockra.MockActivationSpec;
import org.openejb.mdb.mockra.MockResourceAdapter;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class DeploymentHelper {
    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    public static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    public static final ObjectName TRANSACTIONMANAGER_NAME = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    public static final ObjectName TRANSACTIONCONTEXTMANAGER_NAME = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionContextManager");
    public static final ObjectName TRACKEDCONNECTIONASSOCIATOR_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    public static final ObjectName WORKMANAGER_NAME = JMXUtil.getObjectName("geronimo.server:type=WorkManager,name=DefaultWorkManager");
    public static final ObjectName RESOURCE_ADAPTER_NAME = JMXUtil.getObjectName("openejb.server:j2eeType=ResourceAdapter,J2EEServer=TestOpenEJBServer,name=MockRA");
    public static final ObjectName ACTIVATIONSPEC_NAME = JMXUtil.getObjectName("geronimo.server:j2eeType=ActivationSpec,name=MockMDB");
    public static final ObjectName THREADPOOL_NAME = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPool,name=DefaultThreadPool");
    public static final ObjectName TRANSACTIONALTIMER_NAME = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    public static final ObjectName NONTRANSACTIONALTIMER_NAME = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");

    public static Kernel setUpKernelWithTransactionManager(String kernelName) throws Exception {
        Kernel kernel = new Kernel(kernelName);
        kernel.boot();
        GBeanMBean tmGBean = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
        Set rmpatterns = new HashSet();
        rmpatterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tmGBean.setReferencePatterns("ResourceManagers", rmpatterns);
        start(kernel, TRANSACTIONMANAGER_NAME, tmGBean);
        GBeanMBean tcmGBean = new GBeanMBean(TransactionContextManager.GBEAN_INFO);
        tcmGBean.setReferencePattern("TransactionManager", TRANSACTIONMANAGER_NAME);
        tcmGBean.setReferencePattern("XidImporter", TRANSACTIONMANAGER_NAME);
        tcmGBean.setReferencePattern("Recovery", TRANSACTIONMANAGER_NAME);
        start(kernel, TRANSACTIONCONTEXTMANAGER_NAME, tcmGBean);
        GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        DeploymentHelper.start(kernel, TRACKEDCONNECTIONASSOCIATOR_NAME, trackedConnectionAssociator);
        return kernel;
    }

    public static void setUpTimer(Kernel kernel) throws Exception {
        GBeanMBean threadPoolGBean = new GBeanMBean(ThreadPool.GBEAN_INFO);
        threadPoolGBean.setAttribute("keepAliveTime", new Integer(5000));
        threadPoolGBean.setAttribute("poolSize", new Integer(5));
        threadPoolGBean.setAttribute("poolName", "DefaultThreadPool");
        start(kernel, THREADPOOL_NAME, threadPoolGBean);
        GBeanMBean transactionalTimerGBean = new GBeanMBean(VMStoreThreadPooledTransactionalTimer.GBEAN_INFO);
        transactionalTimerGBean.setAttribute("repeatCount", new Integer(5));
        transactionalTimerGBean.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        transactionalTimerGBean.setReferencePattern("ThreadPool", THREADPOOL_NAME);
        start(kernel, TRANSACTIONALTIMER_NAME, transactionalTimerGBean);
        GBeanMBean nonTransactionalTimerGBean = new GBeanMBean(VMStoreThreadPooledNonTransactionalTimer.GBEAN_INFO);
        nonTransactionalTimerGBean.setReferencePattern("ThreadPool", THREADPOOL_NAME);
        start(kernel, NONTRANSACTIONALTIMER_NAME, nonTransactionalTimerGBean);
    }

    public static void setUpResourceAdapter(Kernel kernel) throws Exception {
        GBeanMBean geronimoWorkManagerGBean = new GBeanMBean(GeronimoWorkManager.getGBeanInfo());
        geronimoWorkManagerGBean.setAttribute("syncMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setAttribute("startMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setAttribute("scheduledMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setReferencePattern("TransactionContextManager", TRANSACTIONCONTEXTMANAGER_NAME);
        start(kernel, WORKMANAGER_NAME, geronimoWorkManagerGBean);

        GBeanMBean resourceAdapterGBean = new GBeanMBean(ResourceAdapterWrapper.getGBeanInfo());
        Map activationSpecInfoMap = new HashMap();
        ActivationSpecInfo activationSpecInfo = new ActivationSpecInfo(MockActivationSpec.class, ActivationSpecWrapper.getGBeanInfo());
        activationSpecInfoMap.put(MockActivationSpec.class.getName(), activationSpecInfo);
        resourceAdapterGBean.setAttribute("resourceAdapterClass", MockResourceAdapter.class);
        resourceAdapterGBean.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
        resourceAdapterGBean.setReferencePattern("WorkManager", WORKMANAGER_NAME);
        start(kernel, RESOURCE_ADAPTER_NAME, resourceAdapterGBean);

        GBeanMBean activationSpecGBean = new GBeanMBean(ActivationSpecWrapper.getGBeanInfo());
        activationSpecGBean.setAttribute("activationSpecClass", MockActivationSpec.class);
        activationSpecGBean.setAttribute("containerId", CONTAINER_NAME.getCanonicalName());
        activationSpecGBean.setReferencePattern("ResourceAdapterWrapper", RESOURCE_ADAPTER_NAME);
        start(kernel, ACTIVATIONSPEC_NAME, activationSpecGBean);
    }

    public static void start(Kernel kernel, ObjectName name, GBeanMBean instance) throws Exception {
        kernel.loadGBean(name, instance);
        kernel.startGBean(name);
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
