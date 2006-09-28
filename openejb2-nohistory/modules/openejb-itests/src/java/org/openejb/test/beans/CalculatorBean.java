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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
public class CalculatorBean implements javax.ejb.SessionBean {
    
    public SessionContext context;
    public InitialContext jndiContext;
    boolean testCreate, testAdd, testSub, testSetSessionContext, testRemove;
    
    public void ejbCreate( ){
    }
    
    public int add(int a, int b){
       return a+b;
    }
    protected void doJdbcCall(){

        Connection con = null;
        try{
            
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/mydb");
        
        con = ds.getConnection();
        
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from Employees");
        while(rs.next())
            System.out.println(rs.getString(2));
        
        }catch(javax.naming.NamingException re){
            throw new RuntimeException("Using JNDI failed");
        }catch(java.sql.SQLException se){
            throw new RuntimeException("Getting JDBC data source failed");
        }finally{
            if(con!=null){
                try{
                con.close();
                }catch(SQLException se){se.printStackTrace();}
            }
        }
        
    }
    
    public int sub(int a, int b){
        return a-b;
    }
    
    public void ejbPassivate( ){
        // never called
    }
    public void ejbActivate(){
        // never called
    }
    public void ejbRemove(){
        if (testRemove) testAllowedOperations("ejbRemove");
    }
    public void setSessionContext(javax.ejb.SessionContext cntx){
        context = cntx;
        if (testSetSessionContext) testAllowedOperations("setSessionContext");
        
    }
    
    private void testAllowedOperations(String methodName){
        System.out.println("******************************************************");
        System.out.println("\nTesting Allowed Operations for "+methodName+"() method\n");
        try{
            context.getEJBObject();
            System.out.println("SessionContext.getEJBObject() ......... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getEJBObject() ......... Failed");
        }
        try{
            context.getEJBHome();
            System.out.println("SessionContext.getEJBHome() ........... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getEJBHome() ........... Failed");
        }
        try{
            context.getCallerPrincipal();
            System.out.println("SessionContext.getCallerPrincipal() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getCallerPrincipal() ... Failed");
        }
        try{
            context.isCallerInRole("ROLE");
            System.out.println("SessionContext.isCallerInRole() ....... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.isCallerInRole() ....... Failed");
        }
        try{
            context.getRollbackOnly();
            System.out.println("SessionContext.getRollbackOnly() ...... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getRollbackOnly() ...... Failed");
        }
        try{
            context.setRollbackOnly();
            System.out.println("SessionContext.setRollbackOnly() ...... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.setRollbackOnly() ...... Failed");
        }
        try{
            context.getUserTransaction();
            System.out.println("SessionContext.getUserTransaction() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getUserTransaction() ... Failed");
        }
    }
    
} 
   