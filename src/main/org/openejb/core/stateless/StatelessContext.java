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
package org.openejb.core.stateless;

import org.openejb.RpcContainer;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbObjectProxyHandler;

/**
 * This class imposes restrictions on what stateless SessionBean methods can access which SessionContext
 * methods.  While the CoreContext handles restrictions related to container- vs. bean-managed 
 * transaction beans, this class manages restrictions related to the position of the bean 
 * in its life-cycle to the SessionContext operation being performed.  Restrictions are specified
 * in the EJB specification.  The CoreContext actually fulfills the request, this class just 
 * applies restrictions on access.
 */
public class StatelessContext 
extends org.openejb.core.CoreContext implements javax.ejb.SessionContext{
    public void checkBeanState(byte methodCategory) throws IllegalStateException{
        /*  
        SECURITY_METHOD:
        USER_TRANSACTION_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:
        
        The super class, CoreContext determines if Context.getUserTransaction( ) method 
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.
        
        */
        ThreadContext callContext = ThreadContext.getThreadContext();
        DeploymentInfo di = callContext.getDeploymentInfo();
        
        switch(callContext.getCurrentOperation()){
            case Operations.OP_SET_CONTEXT:
                /* 
                Allowed Operations: 
                    getEJBHome
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                */
                if(methodCategory != EJBHOME_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;
            case Operations.OP_CREATE:
            case Operations.OP_REMOVE:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                */
                if(   methodCategory == EJBHOME_METHOD 
                   || methodCategory == EJBOBJECT_METHOD
                   || methodCategory == USER_TRANSACTION_METHOD)
                    break;
                else
                    throw new IllegalStateException("Invalid operation attempted");
            case Operations.OP_BUSINESS:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                Prohibited Operations:
                */
                break;
        }
        
    }
    
    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID){
        return new StatelessEjbObjectHandler(container, pk, depID);
    }
    
}