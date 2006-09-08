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
package org.openejb.mdb;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import javax.ejb.Timer;

import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.geronimo.management.MessageDrivenBean;
import org.openejb.TwoChains;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.timer.BasicTimerServiceImpl;

/**
 * @version $Revision$ $Date$
 */
public class MDBContainer implements MessageEndpointFactory, GBeanLifecycle, MessageDrivenBean {
    private final ActivationSpecWrapper activationSpecWrapper;
    private final ClassLoader classLoader;
    private final EndpointFactory endpointFactory;
    private final String containerId;
    private final String ejbName;

    private final Interceptor interceptor;
    private final InterfaceMethodSignature[] signatures;
    private final boolean[] deliveryTransacted;
    private final TransactionContextManager transactionContextManager;
    private final Map methodIndexMap;
    private final BasicTimerServiceImpl timerService;
    private final String objectName;

    public MDBContainer(String containerId,
            String ejbName,
            String endpointInterfaceName,
            InterfaceMethodSignature[] signatures,
            boolean[] deliveryTransacted,
            MDBInstanceContextFactory contextFactory,
            MDBInterceptorBuilder interceptorBuilder,
            InstancePool instancePool,
            Map componentContext, UserTransactionImpl userTransaction,
            ActivationSpecWrapper activationSpecWrapper,
            TransactionContextManager transactionContextManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            ClassLoader classLoader,
            ThreadPooledTimer timer,
            String objectName,
            Kernel kernel) throws Exception {

        assert (containerId != null && containerId.length() > 0);
        assert (classLoader != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (signatures != null);
        assert (deliveryTransacted != null);
        assert (signatures.length == deliveryTransacted.length);
        assert (interceptorBuilder != null);
        assert (transactionContextManager != null);
        assert (activationSpecWrapper != null);

        this.classLoader = classLoader;

        this.objectName = objectName;
        this.containerId = containerId;
        this.ejbName = ejbName;
        this.signatures = signatures;
        this.deliveryTransacted = deliveryTransacted;
        this.transactionContextManager = transactionContextManager;
        this.activationSpecWrapper = activationSpecWrapper;
        Class endpointInterface = classLoader.loadClass(endpointInterfaceName);
        endpointFactory = new EndpointFactory(this, endpointInterface, classLoader);

        // create ReadOnlyContext
        Context enc = null;
        if (componentContext != null) {
            for (Iterator iterator = componentContext.values().iterator(); iterator.hasNext();) {
                Object value = iterator.next();
                if (value instanceof KernelAwareReference) {
                    ((KernelAwareReference) value).setKernel(kernel);
                }
                if (value instanceof ClassLoaderAwareReference) {
                    ((ClassLoaderAwareReference) value).setClassLoader(classLoader);
                }
            }
            enc = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext);
        }
        interceptorBuilder.setComponentContext(enc);

        // build the interceptor chain
        interceptorBuilder.setInstancePool(instancePool);
        interceptorBuilder.setTrackedConnectionAssociator(trackedConnectionAssociator);
        TwoChains chains = interceptorBuilder.buildInterceptorChains();
        interceptor = chains.getUserChain();

        SystemMethodIndices systemMethodIndices = contextFactory.setSignatures(getSignatures());

        contextFactory.setSystemChain(chains.getSystemChain());
        contextFactory.setTransactionContextManager(transactionContextManager);
        if (timer != null) {
            timerService = new BasicTimerServiceImpl(systemMethodIndices, interceptor, timer, objectName, kernel.getKernelName(), ObjectName.getInstance(objectName), transactionContextManager, classLoader);
            contextFactory.setTimerService(timerService);
        } else {
            timerService = null;
        }
        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionContextManager, trackedConnectionAssociator);
        }

        // build the legacy map
        Map map = new HashMap();
        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            Method method = signature.getMethod(endpointInterface);
            if (method != null) {
                map.put(method, new Integer(i));
            }
        }
        methodIndexMap = Collections.unmodifiableMap(map);
    }

    public MessageEndpoint createEndpoint(XAResource adapterXAResource) throws UnavailableException {
        NamedXAResource wrapper = adapterXAResource == null? null: new WrapperNamedXAResource(adapterXAResource, containerId);
        return endpointFactory.getMessageEndpoint(wrapper);
    }

    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        Integer methodIndex = (Integer) methodIndexMap.get(method);
        if (methodIndex == null) {
            throw new NoSuchMethodError("Unknown method: " + method);
        }

        return isDeliveryTransacted(methodIndex.intValue());
    }

    public boolean isDeliveryTransacted(int methodIndex) throws NoSuchMethodException {
        return deliveryTransacted[methodIndex];
    }

    public Timer getTimerById(Long id) {
        assert timerService != null;
        return timerService.getTimerById(id);
    }

    public void doStart() throws Exception {
        if (timerService != null) {
            timerService.doStart();
        }
        activationSpecWrapper.activate(this);
    }

    public void doStop() throws PersistenceException {
        activationSpecWrapper.deactivate(this);
        if (timerService != null) {
            timerService.doStop();
        }
    }

    public void doFail() {
        try {
            doStop();
        } catch (PersistenceException e) {
            //todo fix this
            throw new RuntimeException(e);
        }
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return interceptor.invoke(invocation);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getEJBName() {
        return ejbName;
    }

    public EndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    public InterfaceMethodSignature[] getSignatures() {
        // return a copy just to be safe... this method should not be called often
        InterfaceMethodSignature[] copy = new InterfaceMethodSignature[signatures.length];
        System.arraycopy(signatures, 0, copy, 0, signatures.length);
        return copy;
    }

    public TransactionContextManager getTransactionContextManager() {
        return transactionContextManager;
    }

    public Map getMethodIndexMap() {
        return methodIndexMap;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MDBContainer.class, NameFactory.MESSAGE_DRIVEN_BEAN);

        infoFactory.addAttribute("containerId", String.class, true);
        infoFactory.addAttribute("ejbName", String.class, true);
        infoFactory.addAttribute("endpointInterfaceName", String.class, true);
        infoFactory.addAttribute("signatures", InterfaceMethodSignature[].class, true);
        infoFactory.addAttribute("deliveryTransacted", boolean[].class, true);
        infoFactory.addAttribute("contextFactory", MDBInstanceContextFactory.class, true);
        infoFactory.addAttribute("interceptorBuilder", MDBInterceptorBuilder.class, true);
        infoFactory.addAttribute("instancePool", InstancePool.class, true);
        infoFactory.addAttribute("componentContext", Map.class, true);
        infoFactory.addAttribute("userTransaction", UserTransactionImpl.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("ActivationSpecWrapper", ActivationSpecWrapper.class, NameFactory.JCA_ACTIVATION_SPEC);
        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoFactory.addReference("Timer", ThreadPooledTimer.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addOperation("getTimerById", new Class[]{Long.class});

        infoFactory.addInterface(MessageDrivenBean.class);
        
        infoFactory.setConstructor(new String[]{
            "containerId",
            "ejbName",
            "endpointInterfaceName",
            "signatures",
            "deliveryTransacted",
            "contextFactory",
            "interceptorBuilder",
            "instancePool",
            "componentContext",
            "userTransaction",
            "ActivationSpecWrapper",
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "classLoader",
            "Timer",
            "objectName",
            "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
