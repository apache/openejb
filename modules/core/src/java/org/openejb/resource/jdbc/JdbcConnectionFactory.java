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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.resource.jdbc;

import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

/*
* As a connection factory the JdbcConnecitonFactory must implement the Serializable and 
* Referenceable methods so that it can be store in a JNDI name space.  The referenc itself
* is an application specific object that can be used to lookup and configure a new ManagedConnectionFactory
* the JdbcConnecitonFactory is only a store for this reference, its not expected to be functional after 
* it has been serialized into a JNDI name space.  See section 10.5.3 of the Connector API spec.
*/
public class JdbcConnectionFactory 
implements 
javax.sql.DataSource, 
javax.resource.Referenceable, 
java.io.Serializable {
    
    protected transient ManagedConnectionFactory mngdCxFactory;
    protected transient ConnectionManager cxManager;
    protected transient java.io.PrintWriter logWriter;
    protected int logTimeout = 0;
    
    // Reference to this ConnectionFactory
    javax.naming.Reference jndiReference;
    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String defaultPassword;
    private final String defaultUserName;

    // setReference is called by deployment code
    public void setReference(javax.naming.Reference ref) {
        jndiReference = ref;
    }
    // getReference is called by JNDI provider during Context.bind
    public javax.naming.Reference getReference() {
        return jndiReference;
    }
    
    public JdbcConnectionFactory(ManagedConnectionFactory mngdCxFactory, ConnectionManager cxManager, String jdbcUrl, String jdbcDriver, String defaultPassword, String defaultUserName)
    throws ResourceException{
        this.mngdCxFactory = mngdCxFactory;
        this.cxManager = cxManager;
        this.logWriter = mngdCxFactory.getLogWriter();
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
    }
        
    public java.sql.Connection getConnection() throws SQLException{
        return getConnection(defaultUserName, defaultPassword);
    }

    public java.sql.Connection getConnection(java.lang.String username, java.lang.String password)throws SQLException{
        return getConnection(new JdbcConnectionRequestInfo(username, password, jdbcDriver, jdbcUrl));
    }
    protected java.sql.Connection getConnection(JdbcConnectionRequestInfo conInfo) throws SQLException{
        try{
            // FIXME: Use ManagedConnection.assocoate() method here if the client has already obtained a physical connection.
            // the previous connection is either shared or invalidated. IT should probably be shared.
            return (java.sql.Connection)cxManager.allocateConnection(mngdCxFactory, conInfo);
        }catch(javax.resource.spi.ApplicationServerInternalException asi){
            // Application problem with the ConnectionManager. May be a SQLException
            if(asi.getLinkedException() instanceof SQLException)
                throw (SQLException)asi.getLinkedException();
            else
                throw new SQLException("Error code: "+asi.getErrorCode()+"\nApplication error in ContainerManager"+((asi.getLinkedException()!=null)?asi.getLinkedException().getMessage():""));
        }catch(javax.resource.spi.SecurityException se){
            // The username/password in the conInfo is invalid. Should be a nested SQLException.
            if(se.getLinkedException() instanceof SQLException)
                throw (SQLException)se.getLinkedException();
            else
                throw new SQLException("Error code: "+se.getErrorCode()+"\nAuthentication error. Invalid credentials"+((se.getLinkedException()!=null)?se.getLinkedException().getMessage():""));
        }catch(javax.resource.spi.ResourceAdapterInternalException rai){
            // some kind of connection problem. Should be a nested SQLException.
            if(rai.getLinkedException() instanceof SQLException)
                throw (SQLException)rai.getLinkedException();
            else
                throw new SQLException("Error code: "+rai.getErrorCode()+"\nJDBC Connection problem"+((rai.getLinkedException()!=null)?rai.getLinkedException().getMessage():""));
        }catch(javax.resource.spi.ResourceAllocationException rae){
            // a connection could not be obtained from the driver or ConnectionManager.  May be a SQLException
            if(rae.getLinkedException() instanceof SQLException)
                throw (SQLException)rae.getLinkedException();
            else
                throw new SQLException("Error code: "+rae.getErrorCode()+"\nJDBC Connection could not be obtained"+((rae.getLinkedException()!=null)?rae.getLinkedException().getMessage():""));
        }catch(javax.resource.ResourceException re){
            // Unknown cause of exception.  May be a SQLException
            if(re.getLinkedException() instanceof SQLException)
                throw (SQLException)re.getLinkedException();
            else
                throw new SQLException("Error code: "+re.getErrorCode()+"\nJDBC Connection Factory problem"+((re.getLinkedException()!=null)?re.getLinkedException().getMessage():""));
        }
    }
    public int getLoginTimeout(){
        return logTimeout;
    } 
       
    public java.io.PrintWriter getLogWriter(){
        return logWriter;
    }
    public void setLoginTimeout(int seconds){
        //FIXME: how should log timeout work?
        logTimeout = seconds;
    } 
       
    public void setLogWriter(java.io.PrintWriter out){
        logWriter = out;
    }
}
