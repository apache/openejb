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
package org.openejb.core.entity;

import org.openejb.RpcContainer;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbObjectProxyHandler;

/**
 * This class imposes restrictions on what Entity methods can access which EntityContext
 * methods.  This class manages restrictions related to the position of the bean 
 * in its life-cycle to the EntityContext operation being performed.  Restrictions are specified
 * in the EJB specification.  The CoreContext actually fulfills the request, this class just 
 * applies restrictions on access.
 */
public class EntityContext 
extends org.openejb.core.CoreContext implements javax.ejb.EntityContext{

    public void checkBeanState(byte methodCategory) throws IllegalStateException{
        /*  
        The methodCategory will be one of the following constants.
        
        SECURITY_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:
        EJBHOME_METHOD
        
        The super class, CoreContext determines if Context.getUserTransaction( ) method 
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.
        
        The USER_TRANSACTION_METHOD constant will never be a methodCategory 
        because entity beans are not allowed to have "bean managed" transactions.
        
        USER_TRANSACTION_METHOD:
        */
        
        ThreadContext callContext = ThreadContext.getThreadContext();
        org.openejb.DeploymentInfo di = callContext.getDeploymentInfo();
        
        
        switch(callContext.getCurrentOperation()){
            case Operations.OP_SET_CONTEXT:
            case Operations.OP_UNSET_CONTEXT:
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
            case Operations.OP_FIND:
            case Operations.OP_HOME:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                Prohibited Operations:
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                */
                if(methodCategory == EJBOBJECT_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;
            case Operations.OP_ACTIVATE:
            case Operations.OP_PASSIVATE: 
                /* 
                Allowed Operations: 
                    getEJBHome
                    getEJBObject
                    getPrimaryKey
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getUserTransaction
                */
                if(methodCategory != EJBOBJECT_METHOD && methodCategory != EJBHOME_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;    
                    
            case Operations.OP_POST_CREATE:
            case Operations.OP_REMOVE:
            case Operations.OP_LOAD:
            case Operations.OP_STORE:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getEJBObject
                    getPrimaryKey
                Prohibited Operations:
                    getUserTransaction
                */
                break;
            
        }
        
    }
    
    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID){
        return new EntityEjbObjectHandler(container, pk, depID);
    }
    
}