/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.TimerService;
import javax.transaction.Status;

import org.openejb.OpenEJB;
import org.openejb.RpcContainer;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.util.proxy.ProxyManager;

/**
* CoreContext is serializable so that it can be serialized if its
* referenced by a stateful bean that is being passivated (written to disk).
*/
public abstract class CoreContext implements java.io.Serializable {

    //==========================
    // method categories
    //

    public final static byte SECURITY_METHOD = (byte)1;

    public final static byte USER_TRANSACTION_METHOD = (byte)2;

    public final static byte ROLLBACK_METHOD = (byte)3;

    public final static byte EJBOBJECT_METHOD = (byte)4;

    public final static byte EJBHOME_METHOD = (byte)5;
    //
    // method categories
    //==========================


    CoreUserTransaction userTransaction;



    public CoreContext() {
        userTransaction = new CoreUserTransaction(OpenEJB.getTransactionManager());
    }


    public abstract void checkBeanState(byte methodCategory) throws IllegalStateException;


    public java.security.Principal getCallerPrincipal() {
        checkBeanState(SECURITY_METHOD);
        Object securityIdentity = ThreadContext.getThreadContext().getSecurityIdentity();
        return(java.security.Principal)OpenEJB.getSecurityService().translateTo(securityIdentity, java.security.Principal.class);
    }

    public boolean isCallerInRole(java.lang.String roleName) {
        checkBeanState(SECURITY_METHOD);
        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)threadContext.getDeploymentInfo();
        String physicalRoles [] = di.getPhysicalRole(roleName);
        Object caller = threadContext.getSecurityIdentity();
        return  OpenEJB.getSecurityService().isCallerAuthorized(caller,physicalRoles);
    }

    public EJBHome getEJBHome() {
        checkBeanState(EJBHOME_METHOD);

        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)threadContext.getDeploymentInfo();

        return di.getEJBHome();
    }

    public javax.ejb.EJBObject getEJBObject() {
        checkBeanState(EJBOBJECT_METHOD);

        //Possible Fix Needed: How to handle reenterant behavior.

        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.DeploymentInfo di = threadContext.getDeploymentInfo();

        EjbObjectProxyHandler handler = newEjbObjectHandler((RpcContainer)di.getContainer(), threadContext.getPrimaryKey(), di.getDeploymentID());
        Object newProxy = null;
        try {
            Class[] interfaces = new Class[]{ di.getRemoteInterface(), org.openejb.core.ivm.IntraVmProxy.class };
            newProxy = ProxyManager.newProxyInstance( interfaces , handler );
        } catch ( IllegalAccessException iae ) {
            throw new RuntimeException("Could not create IVM proxy for "+di.getRemoteInterface()+" interface");
        }
        return(javax.ejb.EJBObject)newProxy;
    }

    public EJBLocalObject getEJBLocalObject() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.DeploymentInfo di = threadContext.getDeploymentInfo();

        EjbObjectProxyHandler handler = newEjbObjectHandler((RpcContainer)di.getContainer(), threadContext.getPrimaryKey(), di.getDeploymentID());
        handler.setLocal(true);
        Object newProxy = null;
        try {
            Class[] interfaces = new Class[]{ di.getLocalInterface(), org.openejb.core.ivm.IntraVmProxy.class };
            newProxy = ProxyManager.newProxyInstance( interfaces , handler );
        } catch ( IllegalAccessException iae ) {
            throw new RuntimeException("Could not create IVM proxy for "+di.getLocalInterface()+" interface");
        }
        return(EJBLocalObject)newProxy;
    }
    
    public EJBLocalHome getEJBLocalHome() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)threadContext.getDeploymentInfo();

        return di.getEJBLocalHome();
    }
    public TimerService getTimerService() {
        return null; //TODO: implement this
    }

    public Object getPrimaryKey( ) {
        /*
        * This method is only declared in the EntityContext interface and is therefor
        * unavailable in the SessionContext and doesn't not require a check for bean kind (Entity vs Session).
        */
        checkBeanState(EJBOBJECT_METHOD);

        // this method is only called on EntityContext interface which is only provided to the entity containers
        ThreadContext threadContext = ThreadContext.getThreadContext();
        return threadContext.getPrimaryKey();
    }

    public boolean getRollbackOnly() {

        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.DeploymentInfo di = threadContext.getDeploymentInfo();
        if ( di.isBeanManagedTransaction() )
            throw new IllegalStateException("bean-managed transaction beans can not access the getRollbackOnly( ) method");

        checkBeanState(ROLLBACK_METHOD);
        try {
            int status = OpenEJB.getTransactionManager().getStatus();
            if ( status == Status.STATUS_MARKED_ROLLBACK || status == Status.STATUS_ROLLEDBACK )
                return true;
            else if ( status == Status.STATUS_NO_TRANSACTION )// this would be true for Supports tx attribute where no tx was propagated
                throw new IllegalStateException("No current transaction");
            else
                return false;
        } catch ( javax.transaction.SystemException se ) {
            throw new RuntimeException("Transaction service has thrown a SystemException");
        }
    }

    public void setRollbackOnly() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.DeploymentInfo di = threadContext.getDeploymentInfo();
        if ( di.isBeanManagedTransaction() )
            throw new IllegalStateException("bean-managed transaction beans can not access the setRollbackOnly( ) method");

        checkBeanState(ROLLBACK_METHOD);

        try {
            OpenEJB.getTransactionManager().setRollbackOnly();
        } catch ( javax.transaction.SystemException se ) {
            throw new RuntimeException("Transaction service has thrown a SystemException");
        }

    }

    public javax.transaction.UserTransaction getUserTransaction() {

        ThreadContext threadContext = ThreadContext.getThreadContext();
        org.openejb.DeploymentInfo di = threadContext.getDeploymentInfo();
        if ( di.isBeanManagedTransaction() ) {
            checkBeanState(USER_TRANSACTION_METHOD);
            return userTransaction;
        } else
            throw new java.lang.IllegalStateException("container-managed transaction beans can not access the UserTransaction");
    }

    /*----------------------------------------------------*/
    /* UNSUPPORTED DEPRICATED METHODS                     */
    /*----------------------------------------------------*/

    public boolean isCallerInRole(java.security.Identity role) {
        throw new java.lang.UnsupportedOperationException();
    }

    public java.security.Identity getCallerIdentity() {
        throw new java.lang.UnsupportedOperationException();
    }

    public java.util.Properties getEnvironment() {
        throw new java.lang.UnsupportedOperationException();
    }

    protected abstract EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID);
}