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
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test.stateless;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import org.openejb.test.object.Account;

/**
 * 
 */
public class ContainerTxStatelessBean implements javax.ejb.SessionBean{

    
    private String name;
    private SessionContext ejbContext;
    private InitialContext jndiContext;
    public final String jndiDatabaseEntry = "jdbc/stateless/containerManagedTransaction/database";


    
    //=============================
    // Home interface methods
    //    
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    public String txMandatoryMethod(String message) {
        return message;
    }
    
    public String txNeverMethod(String message) {
        return message;
    }
    
    public String txNotSupportedMethod(String message) {
        return message;
    }
    
    public String txRequiredMethod(String message) {
        return message;
    }
    
    public String txRequiresNewMethod(String message) {
        return message;
    }
    
    public String txSupportsMethod(String message) {
        return message;
    }

    public void openAccount(Account acct, Boolean rollback) throws RollbackException{
        
        try{
            DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
            Connection con = ds.getConnection();
            
            /*[2] Update the table */
            PreparedStatement stmt = con.prepareStatement("insert into Account (SSN, First_name, Last_name, Balance) values (?,?,?,?)");
            stmt.setString(1, acct.getSsn());
            stmt.setString(2, acct.getFirstName());
            stmt.setString(3, acct.getLastName());
            stmt.setInt(4, acct.getBalance());
            stmt.executeUpdate();

            /*[4] Clean up */
            stmt.close();
            con.close();
        } catch (Exception e){
            //throw new RemoteException("[Bean] "+e.getClass().getName()+" : "+e.getMessage());
        }
    }

    public Account retreiveAccount(String ssn) {
        Account acct = new Account();
        try{
            DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/database");
            Connection con = ds.getConnection();

            PreparedStatement stmt = con.prepareStatement("select * from Account where SSN = ?");
            stmt.setString(1, ssn);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            
            acct.setSsn( rs.getString(1) );
            acct.setFirstName( rs.getString(2) );
            acct.setLastName( rs.getString(3) );
            acct.setBalance( rs.getInt(4) );

            stmt.close();
            con.close();
        } catch (Exception e){
            //throw new RemoteException("[Bean] "+e.getClass().getName()+" : "+e.getMessage());
        }
        return acct;
    }


    //    
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    /**
     * 
     * @param name
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate() throws javax.ejb.CreateException{
        try {
            jndiContext = new InitialContext(); 
        } catch (Exception e){
            throw new CreateException("Can not get the initial context: "+e.getMessage());
        }
    }
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================
    
}
