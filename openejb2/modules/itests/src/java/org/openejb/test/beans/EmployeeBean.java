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

import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.naming.InitialContext;

public class EmployeeBean implements javax.ejb.EntityBean {
    int id;
    String lastName;
    String firstName;
    
    EntityContext ejbContext;
    
    public int ejbHomeSum(int one, int two) {
        return one+two;
    }
    public Integer ejbFindByPrimaryKey(Integer primaryKey)
    throws javax.ejb.FinderException{
        boolean found = false;
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        
        PreparedStatement stmt = con.prepareStatement("select * from Employees where EmployeeID = ?");
        stmt.setInt(1, primaryKey.intValue());
        ResultSet rs = stmt.executeQuery();
        found = rs.next();
        con.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new FinderException("FindByPrimaryKey failed");
        }
        
        if(found)
            return primaryKey;
        else
            throw new javax.ejb.ObjectNotFoundException();
        
        
    }
    public java.util.Collection ejbFindAll( ) throws FinderException{
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select EmployeeID from Employees");
        java.util.Vector keys = new java.util.Vector();
        while(rs.next()){
            keys.addElement(new Integer(rs.getInt("EmployeeID")));
        }
        con.close();
        return keys;
        }catch(Exception e){
            e.printStackTrace();
            throw new FinderException("FindAll failed");
        }
    }
    
    public Integer ejbCreate(String fname, String lname)
    throws javax.ejb.CreateException{
        try{
        lastName = lname;
        firstName = fname;
        
        InitialContext jndiContext = new InitialContext( ); 
 
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        
        PreparedStatement stmt = con.prepareStatement("insert into Employees (FirstName, LastName) values (?,?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.executeUpdate();
        
        stmt = con.prepareStatement("select EmployeeID from Employees where FirstName = ? AND LastName = ?");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        ResultSet set = stmt.executeQuery();
        while(set.next())
            id = set.getInt("EmployeeID");
        con.close();
        
        return new Integer(id);
        
        }catch(Exception e){
            e.printStackTrace();
            throw new javax.ejb.CreateException("can't create");
        }
    }
    public String getLastName( ){
        return lastName;
    }
    public String getFirstName( ){
        return firstName;
    }
    public void setLastName(String lname){
        lastName = lname;
    }
    public void setFirstName(String fname){
        firstName = fname;
    }
    
    public void ejbLoad( ){
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        
        PreparedStatement stmt = con.prepareStatement("select * from Employees where EmployeeID = ?");
        Integer primaryKey = (Integer)ejbContext.getPrimaryKey();
        stmt.setInt(1, primaryKey.intValue());
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            lastName = rs.getString("LastName");
            firstName = rs.getString("FirstName");
        }
        con.close();
        
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void ejbStore( ){
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        Connection con = ds.getConnection();
        
        PreparedStatement stmt = con.prepareStatement("update Employees set FirstName = ?, LastName = ? where EmployeeID = ?");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setInt(3, id);
        stmt.execute();
        con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void ejbActivate( ){}
    public void ejbPassivate( ){}
    public void ejbRemove( ){
                
        try{
            InitialContext jndiContext = new InitialContext( ); 
            
            javax.sql.DataSource ds = 
            (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
            
            Connection con = ds.getConnection();
            
            
            PreparedStatement stmt = con.prepareStatement("delete from Employees where EmployeeID = ?");
            Integer primaryKey = (Integer)ejbContext.getPrimaryKey();
            stmt.setInt(1, primaryKey.intValue());
            stmt.executeUpdate();
            con.close();
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void setEntityContext(javax.ejb.EntityContext cntx){
        ejbContext = cntx;
    }
    public void unsetEntityContext(){}
}
    
        