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
package org.openejb.test.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DatabaseBean implements javax.ejb.SessionBean {
    
    public SessionContext context;
    public InitialContext jndiContext;
    
    public void ejbCreate( ) throws javax.ejb.CreateException{
        try{        
            jndiContext = new InitialContext();
        } catch (Exception e){
            throw new EJBException(e.getMessage());
        }
    }
    
    public void executeQuery(String statement) throws java.sql.SQLException{
        try{        

        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
        Connection con = ds.getConnection();

        PreparedStatement stmt = con.prepareStatement(statement);
        ResultSet rs = stmt.executeQuery();
        
        con.close();
        } catch (Exception e){
            throw new EJBException("Cannot execute the statement: "+statement+ e.getMessage());
        }
    }
    
    public boolean execute(String statement) throws java.sql.SQLException{
        boolean retval;
        Connection con = null;
        try{        

        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
        con = ds.getConnection();

        Statement stmt = con.createStatement();
        retval = stmt.execute(statement);
        
        } catch (javax.naming.NamingException e){
//        } catch (Exception e){
//            e.printStackTrace();
            //throw new RemoteException("Cannot execute the statement: "+statement, e);
            throw new EJBException("Cannot lookup the Database bean."+e.getMessage());
        } finally {
            if(con!=null) {
                con.close();
            }
        }
        return retval;
    }
    
    public void ejbPassivate( ){
        // never called
    }
    public void ejbActivate(){
        // never called
    }
    public void ejbRemove(){
    }
    
    public void setSessionContext(javax.ejb.SessionContext cntx){
        context = cntx;
    }
} 
   