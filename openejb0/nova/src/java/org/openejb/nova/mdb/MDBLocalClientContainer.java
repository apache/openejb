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
package org.openejb.nova.mdb;

import java.lang.reflect.Method;

import javax.ejb.EJBException;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.xa.XAResource;

import net.sf.cglib.proxy.Callbacks;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.method.EJBCallbackFilter;

/**
 * Container for the local interface of a Message Driven Bean.
 * This container owns implementations of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 *
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 *
 * @version $Revision$ $Date$
 */
public class MDBLocalClientContainer {
    private static final Class[] CONSTRUCTOR = new Class[]{MDBLocalClientContainer.class, XAResource.class};
    private static final SimpleCallbacks PROXY_CALLBACK;
    static {
        PROXY_CALLBACK = new SimpleCallbacks();
        PROXY_CALLBACK.setCallback(Callbacks.INTERCEPT, new MDBMessageEndpointCallback());
    }
    
    
    private Interceptor firstInterceptor; // @todo make this final
    private final int[] objectMap;
    private Factory proxyFactory;

    /**
     * Constructor used to initialize the ClientContainer.
     * @param signatures the signatures of the virtual methods
     * @param mdbInterface the class of the MDB's messaging interface (e.g. javax.jmx.MessageListner)
     */
    public MDBLocalClientContainer(MethodSignature[] signatures, Class mdbInterface) {
        SimpleCallbacks callbacks;
        Enhancer enhancer;

        callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new MDBMessageEndpointCallback());
        
        enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[]{mdbInterface, MessageEndpoint.class});
        enhancer.setCallbackFilter(new EJBCallbackFilter(MDBMessageEndpointImpl.class));
        enhancer.setCallbacks(callbacks);
        proxyFactory = enhancer.create(CONSTRUCTOR, new Object[]{this, null});
        
        objectMap = MethodHelper.getObjectMap(signatures, FastClass.create(proxyFactory.getClass()));
    }

    private static Enhancer getEnhancer(Class local, Class baseClass, SimpleCallbacks callbacks) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(baseClass);
        enhancer.setInterfaces(new Class[]{local});
        enhancer.setCallbackFilter(new EJBCallbackFilter(baseClass));
        enhancer.setCallbacks(callbacks);
        return enhancer;
    }

    public void addInterceptor(Interceptor interceptor) {
        if (firstInterceptor == null) {
            firstInterceptor = interceptor;
            return;
        }
        Interceptor parent = firstInterceptor;
        while (parent.getNext() != null) {
            parent = parent.getNext();
        }
        parent.setNext(interceptor);
    }

    public MessageEndpoint getMessageEndpoint(XAResource resource) {
        return (MessageEndpoint)proxyFactory.newInstance(CONSTRUCTOR, new Object[] { resource }, PROXY_CALLBACK);
    }

    /**
     * Base class for MessageEndpoint invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static class MDBMessageEndpointImpl implements MessageEndpoint {

        XAResource resource;
        MDBLocalClientContainer container;
        
        /**
         * @param resource
         */
        public MDBMessageEndpointImpl(MDBLocalClientContainer conatiner, XAResource resource) {
            this.container = conatiner;
            this.resource = resource;
        }
        
        
        /**
         * @see javax.resource.spi.endpoint.MessageEndpoint#beforeDelivery(java.lang.reflect.Method)
         */
        public void beforeDelivery(Method arg0) throws NoSuchMethodException, ResourceException {
            // TODO Auto-generated method stub
            
        }

        /**
         * @see javax.resource.spi.endpoint.MessageEndpoint#afterDelivery()
         */
        public void afterDelivery() throws ResourceException {
            // TODO Auto-generated method stub
            
        }

        /**
         * @see javax.resource.spi.endpoint.MessageEndpoint#release()
         */
        public void release() {
            // TODO Auto-generated method stub
            
        }
    }

    /**
     * Callback handler for EJBLocalHome invocations that cannot be handled
     * directly by the proxy.
     */
    static private class MDBMessageEndpointCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            MDBMessageEndpointImpl mdbEndpoint = ((MDBMessageEndpointImpl)o);
            MDBLocalClientContainer container = mdbEndpoint.container;
            int vopIndex = container.objectMap[methodProxy.getSuperIndex()];
            return container.invoke(new EJBInvocationImpl(EJBInvocationType.LOCAL, null, vopIndex, args));
        }
    }

    
    private Object invoke(EJBInvocation invocation) throws Throwable {
        InvocationResult result;
        try {
            result = firstInterceptor.invoke(invocation);
        } catch (Throwable t) {
            // System exception from interceptor chain - throw as is or wrapped in an EJBException
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                t = new EJBException((Exception) t);
            }
            throw t;
        }
        if (result.isNormal()) {
            return result.getResult();
        } else {
            throw result.getException();
        }
    }
    
}
