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
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.cache.InstancePool;

/**
 * @version $Revision$ $Date$
 */
public class MDBContainer implements MessageEndpointFactory, GBeanLifecycle {
    private final ActivationSpecWrapper activationSpecWrapper;
    private final ClassLoader classLoader;
    private final EndpointFactory endpointFactory;
    private final String containerId;
    private final String ejbName;

    private final Interceptor interceptor;
    private final InterfaceMethodSignature[] signatures;
    private final boolean[] deliveryTransacted;
    private final TransactionManager transactionManager;
    private final Map methodIndexMap;

    public MDBContainer(String containerId,
            String ejbName,
            String endpointInterfaceName,
            InterfaceMethodSignature[] signatures,
            boolean[] deliveryTransacted,
            MDBInterceptorBuilder interceptorBuilder,
            InstancePool instancePool, UserTransactionImpl userTransaction,
            ActivationSpecWrapper activationSpecWrapper,
            TransactionManager transactionManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            ClassLoader classLoader) throws Exception {

        assert (containerId != null && containerId.length() > 0);
        assert (classLoader != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (signatures != null);
        assert (deliveryTransacted != null);
        assert (signatures.length == deliveryTransacted.length);
        assert (interceptorBuilder != null);
        assert (transactionManager != null);
        assert (activationSpecWrapper != null);

        this.classLoader = classLoader;

        this.containerId = containerId;
        this.ejbName = ejbName;
        this.signatures = signatures;
        this.deliveryTransacted = deliveryTransacted;
        this.transactionManager = transactionManager;
        this.activationSpecWrapper = activationSpecWrapper;
        Class endpointInterface = classLoader.loadClass(endpointInterfaceName);
        endpointFactory = new EndpointFactory(this, endpointInterface, classLoader);

        // build the interceptor chain
        interceptorBuilder.setInstancePool(instancePool);
        interceptorBuilder.setTrackedConnectionAssociator(trackedConnectionAssociator);
        interceptor = interceptorBuilder.buildInterceptorChain();

        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionManager, trackedConnectionAssociator);
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
        return endpointFactory.getMessageEndpoint(new WrapperNamedXAResource(adapterXAResource, containerId));
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

    public void doStart() throws ResourceException {
        activationSpecWrapper.activate(this);
    }

    public void doStop() {
        activationSpecWrapper.deactivate(this);
    }

    public void doFail() {
        doStop();
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

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public Map getMethodIndexMap() {
        return methodIndexMap;
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(MDBContainer.class);

        infoFactory.addAttribute("containerId", String.class, true);
        infoFactory.addAttribute("ejbName", String.class, true);
        infoFactory.addAttribute("endpointInterfaceName", String.class, true);
        infoFactory.addAttribute("signatures", InterfaceMethodSignature[].class, true);
        infoFactory.addAttribute("deliveryTransacted", boolean[].class, true);
        infoFactory.addAttribute("interceptorBuilder", MDBInterceptorBuilder.class, true);
        infoFactory.addAttribute("instancePool", InstancePool.class, true);
        infoFactory.addAttribute("userTransaction", UserTransactionImpl.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("activationSpecWrapper", ActivationSpecWrapper.class);
        infoFactory.addReference("transactionManager", TransactionManager.class);
        infoFactory.addReference("trackedConnectionAssociator", TrackedConnectionAssociator.class);

        infoFactory.setConstructor(new String[]{
            "containerId",
            "ejbName",
            "endpointInterfaceName",
            "signatures",
            "deliveryTransacted",
            "interceptorBuilder",
            "instancePool",
            "userTransaction",
            "activationSpecWrapper",
            "transactionManager",
            "trackedConnectionAssociator",
            "classLoader",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
