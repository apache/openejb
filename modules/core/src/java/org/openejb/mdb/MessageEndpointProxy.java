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

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;

import org.apache.geronimo.transaction.ContainerTransactionContext;
import org.apache.geronimo.transaction.InheritableTransactionContext;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;

/**
 * Base class for MessageEndpoint invocations. Handles operations which can
 * be performed directly by the proxy.
 */
public class MessageEndpointProxy implements MessageEndpoint {
    private final MDBContainer container;
    private ClassLoader adapterClassLoader;
    private TransactionContext adapterTransaction;
    private TransactionContext beanTransaction;

    public MessageEndpointProxy(MDBContainer container) {
        this.container = container;
    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        // todo enforce the invocation ordering
        try {
            // setup the classloader
            Thread currentThread = Thread.currentThread();
            adapterClassLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(container.getClassLoader());

            // setup the transaction
            adapterTransaction = TransactionContext.getContext();
            boolean transactionRequired = container.isDeliveryTransacted(method);

            // if the adapter gave us a transaction and we are reauired, just move on
            if (transactionRequired && adapterTransaction instanceof InheritableTransactionContext) {
                return;
            }

            // suspend what ever we got from the adapter
            if (adapterTransaction != null) {
                adapterTransaction.suspend();
            }

            if (transactionRequired) {
                // start a new container transaction
                beanTransaction = new ContainerTransactionContext(container.getTransactionManager());
            } else {
                // enter an unspecified transaction context
                beanTransaction = new UnspecifiedTransactionContext();
            }

            // start the new context
            TransactionContext.setContext(beanTransaction);
            beanTransaction.begin();
        } catch (Exception e) {
            // todo check if we need to unset the cl and tx on exception
            throw new ResourceException(e);
        }
    }

    public void afterDelivery() throws ResourceException {
        // todo enforce the invocation ordering
        try {
            if (beanTransaction != null) {
                try {
                    // todo check what happens with bean method throws a transactions
                    beanTransaction.commit();
                } catch (Exception e) {
                    throw new ResourceException(e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(adapterClassLoader);
            TransactionContext.setContext(adapterTransaction);
            if (adapterTransaction != null) {
                try {
                    adapterTransaction.resume();
                } catch (Exception e) {
                    throw new ResourceException(e);
                }
            }
        }
    }

    public void release() {
        // todo enforce the invocation ordering
        // todo find out what is supposed to go in here
    }
}
