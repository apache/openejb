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
package org.openejb.core.stateful;

import org.openejb.RpcContainer;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbObjectProxyHandler;

/**
 * This class imposes restrictions on what stateful SessionBean methods can access which SessionContext
 * methods.  While the CoreContext handles restrictions related to container- vs. bean-managed 
 * transaction beans, this class manages restrictions related to the position of the bean 
 * in its life-cycle to the SessionContext operation being performed.  Restrictions are specified
 * in the EJB specification.  The CoreContext actually fulfills the request, this class just 
 * applies restrictions on access.
 */
public class StatefulContext 
extends org.openejb.core.CoreContext implements javax.ejb.SessionContext{
    public void checkBeanState(byte methodCategory) throws IllegalStateException{
        /*  
        The methodCategory will be one of the following constants.
        
        SECURITY_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:
        EJBHOME_METHOD
        USER_TRANSACTION_METHOD:
        
        The super class, CoreContext determines if Context.getUserTransaction( ) method 
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.
        
        
        The USER_TRANSACTION_METHOD will never be passed as a methodCategory in the SessionSynchronization 
        interface methods. The CoreContext won't allow it.
        
        */
        ThreadContext callContext = ThreadContext.getThreadContext();
        
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
            case Operations.OP_ACTIVATE:
            case Operations.OP_PASSIVATE: 
            case Operations.OP_AFTER_COMPLETION:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    isCallerInRole
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction (not allowed in OP_AFTER_COMPLETION)
                Prohibited Operations:
                    getRollbackOnly,
                    setRollbackOnly
                */
                if(methodCategory == ROLLBACK_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                else
                    break;
            case Operations.OP_BUSINESS:
            case Operations.OP_AFTER_BEGIN:
            case Operations.OP_BEFORE_COMPLETION:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    isCallerInRole
                    getEJBObject
                    getPrimaryKey
                    getRollbackOnly,
                    setRollbackOnly
                    getUserTransaction (business methods only)
                Prohibited Operations:
                */
                break;
        }
        
    }
    
    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID){
        return new StatefulEjbObjectHandler(container, pk, depID);
    }
    
}