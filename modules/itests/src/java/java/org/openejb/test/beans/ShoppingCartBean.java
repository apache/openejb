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

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;

public class ShoppingCartBean  implements SessionBean, javax.ejb.SessionSynchronization{
    
    String name;
    SessionContext context;
    Context jndiContext;
    Context envContext;
    Boolean useJdbc = Boolean.FALSE;
    
    public void ejbCreate(String name)throws javax.ejb.CreateException{
        //testAllowedOperations("ejbCreate");
        try{
        
        jndiContext = new InitialContext();
        
        String author = (String)jndiContext.lookup("java:comp/env/author");
     
        Double price = (Double)jndiContext.lookup("java:comp/env/price");
        
        }catch(javax.naming.NamingException re){
            throw new RuntimeException("Using JNDI failed");
        }
        
    }
    public Calculator getCalculator(){
        
        try{
        
        boolean test = context.isCallerInRole("TheMan");
        
        jndiContext = new InitialContext( ); 
        
        CalculatorHome home = (CalculatorHome)jndiContext.lookup("java:comp/env/ejb/calculator");
        Calculator calc = home.create();
        return calc;
        
        }catch(java.rmi.RemoteException re){
            throw new RuntimeException("Getting calulator bean failed");
        }catch(javax.naming.NamingException re){
            throw new RuntimeException("Using JNDI failed");
        }
        
        
    }
    public void doJdbcCall(){

        Connection con = null;
        try{
            
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        con = ds.getConnection();
        
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from Employees");
        while(rs.next())
            System.out.println(rs.getString(2));
   
        Calculator calc = getCalculator();
        calc.add(1, 1);
        calc.sub(1,2);
        
        int i =  1;
        
        }catch(java.rmi.RemoteException re){
            throw new RuntimeException("Accessing Calculator bean failed");
        }catch(javax.naming.NamingException ne){
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
    
    public String getName( ){
 
        return name;
    }
    public void setName(String name){
        //testAllowedOperations("setName");
        this.name = name;
    }
    
    public void setSessionContext(SessionContext cntx){
        context = cntx;        
        //testAllowedOperations("setSessionContext");
    }
    
    public void ejbActivate( ){
        //testAllowedOperations("ejbActivate");
    }
    
    public void ejbPassivate( ){
        //testAllowedOperations("ejbPassivate");
    }
    public void ejbRemove( ){
        //testAllowedOperations("ejbRemove");
    }
    
    public void afterBegin( ){
        // do nothing
    }
    public void beforeCompletion(){
        // do nothing
    }
    public void afterCompletion(boolean commit){
        // do nothing
    }
    private void testAllowedOperations(String methodName){
        System.out.println("******************************************************");
        System.out.println("\nTesting Allowed Operations for "+methodName+"() method\n");
        try{
            context.getEJBObject();
            System.out.println("SessionContext.getEJBObject() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getEJBObject() ... Failed");
        }
        try{
            context.getEJBHome();
            System.out.println("SessionContext.getEJBHome() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getEJBHome() ... Failed");
        }
        try{
            context.getCallerPrincipal();
            System.out.println("SessionContext.getCallerPrincipal() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getCallerPrincipal() ... Failed");
        }
        try{
            context.isCallerInRole("ROLE");
            System.out.println("SessionContext.isCallerInRole() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.isCallerInRole() ... Failed");
        }
        try{
            context.getRollbackOnly();
            System.out.println("SessionContext.getRollbackOnly() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getRollbackOnly() ... Failed");
        }
        try{
            context.setRollbackOnly();
            System.out.println("SessionContext.setRollbackOnly() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.setRollbackOnly() ... Failed");
        }
        try{
            context.getUserTransaction();
            System.out.println("SessionContext.getUserTransaction() ... Allowed");
        }catch(IllegalStateException ise){
            System.out.println("SessionContext.getUserTransaction() ... Failed");
        }
    }
}
    