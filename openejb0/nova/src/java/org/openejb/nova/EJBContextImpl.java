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

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.TimerService;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.openejb.nova.transaction.ContainerTransactionContext;
import org.openejb.nova.transaction.TransactionContext;

/**
 * Implementation of EJBContext that uses the State pattern to determine
 * which operations can be performed for a given EJB.
 *
 * @version $Revision$ $Date$
 */
public abstract class EJBContextImpl {
    protected final EJBInstanceContext context;
    protected EJBContextState state;

    public EJBContextImpl(EJBInstanceContext context) {
        this.context = context;
    }

    public EJBHome getEJBHome() {
        return state.getEJBHome(context);
    }

    public EJBLocalHome getEJBLocalHome() {
        return state.getEJBLocalHome(context);
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        return state.getEJBObject(context);
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        return state.getEJBLocalObject(context);
    }

    public Principal getCallerPrincipal() {
        return state.getCallerPrincipal();
    }

    public boolean isCallerInRole(String s) {
        return state.isCallerInRole(s);
    }

    public UserTransaction getUserTransaction() {
        return state.getUserTransaction(context);
    }

    public void setRollbackOnly() {
        state.setRollbackOnly(context);
    }

    public boolean getRollbackOnly() {
        return state.getRollbackOnly(context);
    }

    public TimerService getTimerService() {
        return state.getTimerService();
    }

    public Properties getEnvironment() {
        throw new EJBException("getEnvironment is no longer supported; use JNDI instead");
    }

    public Identity getCallerIdentity() {
        throw new EJBException("getCallerIdentity is no longer supported; use getCallerPrincipal instead");
    }

    public boolean isCallerInRole(Identity identity) {
        throw new EJBException("isCallerInRole(Identity role) is no longer supported; use isCallerInRole(String roleName) instead");
    }

    public abstract static class EJBContextState {
        public EJBHome getEJBHome(EJBInstanceContext context) {
            EJBHome home = context.getContainer().getEJBHome();
            if (home == null) {
                throw new IllegalStateException("getEJBHome is not allowed if no home interface is defined");
            }
            return home;
        }

        public EJBLocalHome getEJBLocalHome(EJBInstanceContext context) {
            EJBLocalHome localHome = context.getContainer().getEJBLocalHome();
            if (localHome == null) {
                throw new IllegalStateException("getEJBLocalHome is not allowed if no local localHome interface is defined");
            }
            return localHome;
        }

        public EJBObject getEJBObject(EJBInstanceContext context) {
            EJBObject remote = context.getContainer().getEJBObject(context.getId());
            if (remote == null) {
                throw new IllegalStateException("getEJBObject is not allowed if no remote interface is defined");
            }
            return remote;
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            EJBLocalObject local = context.getContainer().getEJBLocalObject(context.getId());
            if (local == null) {
                throw new IllegalStateException("getEJBLocalObject is not allowed if no local interface is defined");
            }
            return local;
        }

        public Principal getCallerPrincipal() {
            return null;
        }

        public boolean isCallerInRole(String s) {
            return false;
        }

        public UserTransaction getUserTransaction(EJBInstanceContext context) {
            EJBContainer container = context.getContainer();
            TransactionDemarcation demarcation = container.getDemarcation();
            if (demarcation.isContainer()) {
                throw new IllegalStateException("getUserTransaction is not allowed when using Container Managed Transactions");
            }
            return container.getUserTransaction();
        }

        public void setRollbackOnly(EJBInstanceContext context) {
            TransactionDemarcation demarcation = context.getContainer().getDemarcation();
            if (demarcation.isContainer()) {
                TransactionContext ctx = TransactionContext.getContext();
                if (ctx instanceof ContainerTransactionContext) {
                    ContainerTransactionContext containerContext = (ContainerTransactionContext) ctx;
                    try {
                        containerContext.setRollbackOnly();
                    } catch (SystemException e) {
                        throw new EJBException(e);
                    }
                } else {
                    throw new IllegalStateException("There is no transaction in progess.");
                }
               
            } else {
                throw new IllegalStateException("Calls to setRollbackOnly are not allowed for SessionBeans with bean-managed transaction demarcation");
            }
        }

        public boolean getRollbackOnly(EJBInstanceContext context) {
            TransactionDemarcation demarcation = context.getContainer().getDemarcation();
            if (demarcation.isContainer()) {
                TransactionContext ctx = TransactionContext.getContext();
                if (ctx instanceof ContainerTransactionContext) {
                    ContainerTransactionContext containerContext = (ContainerTransactionContext) ctx;
                    try {
                        return containerContext.getRollbackOnly();
                    } catch (SystemException e) {
                        throw new EJBException(e);
                    }
                } else {
                    throw new IllegalStateException("There is no transaction in progess.");
                }
               
            } else {
                throw new IllegalStateException("Calls to getRollbackOnly are not allowed for SessionBeans with bean-managed transaction demarcation");
            }
        }

        public TimerService getTimerService() {
            throw new UnsupportedOperationException();
        }
    }
}
