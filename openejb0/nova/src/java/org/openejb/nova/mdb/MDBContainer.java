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

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;
import org.openejb.nova.AbstractEJBContainer;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.dispatch.DispatchInterceptor;
import org.openejb.nova.transaction.TransactionContextInterceptor;
import org.openejb.nova.util.SoftLimitedInstancePool;

/**
 *
 * @version $Revision$ $Date$
 */
public class MDBContainer extends AbstractEJBContainer implements MessageEndpointFactory {
    
    private ActivationSpec activationSpec;
    private ResourceAdapter adapter;
    private String interfaceType;
    private Class mdbInterface;
    private MDBLocalClientContainer messageClientContainer;
    
    public MDBContainer(EJBContainerConfiguration config, ActivationSpec activationSpec, String interfaceType) {
        super(config);
        this.interfaceType = interfaceType;
        this.activationSpec = activationSpec;
        adapter = activationSpec.getResourceAdapter();        
    }        
    
    public void doStart() {
        super.doStart();

        
        try {
            mdbInterface = Thread.currentThread().getContextClassLoader().loadClass(interfaceType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load MDB interface class: "+interfaceType, e);
        } 
        
        MDBOperationFactory vopFactory = MDBOperationFactory.newInstance(beanClass);
        vtable = vopFactory.getVTable();

        pool = new SoftLimitedInstancePool(new MDBInstanceFactory(this), 1);

        // set up server side interceptors
        Interceptor firstInterceptor = new ComponentContextInterceptor(componentContext);
        addInterceptor(firstInterceptor);
        addInterceptor(new MDBInstanceInterceptor(pool));
        addInterceptor(new TransactionContextInterceptor(txnManager));
        addInterceptor(new DispatchInterceptor(vtable));

        // set up client containers
        MDBClientContainerFactory clientFactory = new MDBClientContainerFactory(vopFactory, firstInterceptor, mdbInterface);
        messageClientContainer = clientFactory.getMessageClientContainer();
        buildMethodMap(vopFactory.getSignatures());
        
        try {
            // Setup the endpoint.        
            adapter.endpointActivation(this, activationSpec);            
        } catch (ResourceException e) {
            throw new RuntimeException("The resource adpater did not accept the activation of the MDB endpoint", e);
        }
    }

    public void doStop() {
        // Deactivate the endpoint.        
        adapter.endpointDeactivation(this, activationSpec);            

        localClientContainer = null;
        clearInterceptors();
        pool = null;
        super.doStop();
    }

    /**
     * @see javax.resource.spi.endpoint.MessageEndpointFactory#createEndpoint(javax.transaction.xa.XAResource)
     */
    public MessageEndpoint createEndpoint(XAResource resource) throws UnavailableException {
        return messageClientContainer.getMessageEndpoint(resource);
    }

    /**
     * @see javax.resource.spi.endpoint.MessageEndpointFactory#isDeliveryTransacted(java.lang.reflect.Method)
     */
    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        // TODO: need to see if the method is Supports or Required.
        return MDBContainer.this.txnDemarcation == TransactionDemarcation.CONTAINER; 
            
    }

}
