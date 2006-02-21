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
package org.openejb.test.stateful;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.naming.InitialContext;

import org.openejb.test.ApplicationException;
import org.openejb.test.object.OperationsPolicy;

/**
 * 
 */
public class BasicStatefulBean implements javax.ejb.SessionBean, SessionSynchronization {
    private String name;
    private SessionContext ejbContext;
    private Hashtable allowedOperationsTable = new Hashtable();

    public String getName() {
        return name;
    }
    
    //=============================
    // Home interface methods
    //    
    /**
     * Maps to BasicStatefulHome.create
     */
    public void ejbCreate(String name) throws javax.ejb.CreateException{
        testAllowedOperations("ejbCreate");
        this.name = name;
    }
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //

    public void doNothing() {
        testAllowedOperations("businessMethod");
    }

    /**
     * Maps to BasicStatefulObject.businessMethod
     */
    public String businessMethod(String text){
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    /**
     * Throws an ApplicationException when invoked
     * 
     */
    public void throwApplicationException() throws ApplicationException{
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }
    
    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the 
     * destruction of the instance and invalidation of the
     * remote reference.
     * 
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }
    
    /**
     * Maps to BasicStatefulObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicStatefulObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }
    
    //    
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) {
        ejbContext = ctx;
        testAllowedOperations("setSessionContext");
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() {
        testAllowedOperations("ejbRemove");
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() {
        testAllowedOperations("ejbActivate");
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() {
        testAllowedOperations("ejbPassivate");
    }
    //    
    // SessionBean interface methods
    //==================================
    
    
    //============================================
    // SessionSynchronization interface methods
    //
    /**
     * The afterBegin method notifies a session Bean instance that a new
     * transaction has started, and that the subsequent business methods on the
     * instance will be invoked in the context of the transaction.
     */
    public void afterBegin() throws EJBException,RemoteException {
        testAllowedOperations("afterBegin");
    }
    /**
     * The beforeCompletion method notifies a session Bean instance that
     * a transaction is about to be committed. The instance can use this
     * method, for example, to write any cached data to a database.
     */
    public void beforeCompletion() throws EJBException,RemoteException {
        testAllowedOperations("beforeCompletion");
    }
    /**
     * The afterCompletion method notifies a session Bean instance that a
     * transaction commit protocol has completed, and tells the instance
     * whether the transaction has been committed or rolled back.
     */
    public void afterCompletion(boolean committed) throws EJBException,RemoteException {
        testAllowedOperations("afterCompletion");
    }
    //    
    // SessionSynchronization interface methods
    //============================================

    protected void testAllowedOperations(String methodName){
        OperationsPolicy policy = new OperationsPolicy();
        
        /*[1] Test getEJBHome /////////////////*/ 
        try{
            ejbContext.getEJBHome();
            policy.allow(OperationsPolicy.Context_getEJBHome);
        }catch(IllegalStateException ise){}
        
        /*[2] Test getCallerPrincipal /////////*/ 
        try{
            ejbContext.getCallerPrincipal();
            policy.allow( OperationsPolicy.Context_getCallerPrincipal );
        }catch(IllegalStateException ise){}
        
        /*[3] Test isCallerInRole /////////////*/ 
        try{
            ejbContext.isCallerInRole("ROLE");
            policy.allow( OperationsPolicy.Context_isCallerInRole );
        }catch(IllegalStateException ise){}
        
        /*[4] Test getRollbackOnly ////////////*/ 
        try{
            ejbContext.getRollbackOnly();
            policy.allow( OperationsPolicy.Context_getRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[5] Test setRollbackOnly ////////////*/
        //
        // this can not be effectively tested right now
        //
        //try{
        //    ejbContext.setRollbackOnly();
        //    policy.allow( OperationsPolicy.Context_setRollbackOnly );
        //}catch(IllegalStateException ise){}
        
        /*[6] Test getUserTransaction /////////*/ 
        try{
            ejbContext.getUserTransaction();
            policy.allow( OperationsPolicy.Context_getUserTransaction );
        }catch(IllegalStateException ise){}
        
        /*[7] Test getEJBObject ///////////////*/ 
        try{
            ejbContext.getEJBObject();
            policy.allow( OperationsPolicy.Context_getEJBObject );
        }catch(IllegalStateException ise){}
         
        /*[8] Test JNDI_access_to_java_comp_env ///////////////*/
        try {
            InitialContext jndiContext = new InitialContext();

            jndiContext.lookup("java:comp/env/stateful/references/JNDI_access_to_java_comp_env");

            policy.allow( OperationsPolicy.JNDI_access_to_java_comp_env );
        } catch (IllegalStateException ise) {
        } catch (javax.naming.NamingException ne) {
        }

        /*[9] Test Resource_manager_access ///////////////*/
        try {
            InitialContext jndiContext = new InitialContext( );

            jndiContext.lookup("java:comp/env/stateful/references/Resource_manager_access");

            policy.allow( OperationsPolicy.Resource_manager_access );
        } catch (IllegalStateException ise) {
        } catch (javax.naming.NamingException ne) {
        }

        /*[10] Test Enterprise_bean_access ///////////////*/
        try {
            InitialContext jndiContext = new InitialContext( );

            jndiContext.lookup("java:comp/env/stateful/beanReferences/Enterprise_bean_access");

            policy.allow( OperationsPolicy.Enterprise_bean_access );
        } catch (IllegalStateException ise) {
        } catch (javax.naming.NamingException ne) {
        }

        allowedOperationsTable.put(methodName, policy);
    }

}
