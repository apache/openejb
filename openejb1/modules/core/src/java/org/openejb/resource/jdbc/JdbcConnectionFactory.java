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

import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ResourceAllocationException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * As a connection factory the JdbcConnecitonFactory must implement the
 * Serializable and Referenceable methods so that it can be store in a
 * JNDI name space.  The reference itself is an application specific object
 * that can be used to lookup and configure a new ManagedConnectionFactory
 * the JdbcConnecitonFactory is only a store for this reference, its not
 * expected to be functional after it has been serialized into a JNDI
 * namespace.
 * <p/>
 * See section 10.5.3 of the J2EE Connector Architecture 1.0 spec.
 */
public class JdbcConnectionFactory implements javax.sql.DataSource, javax.resource.Referenceable, java.io.Serializable {
    /**
     * A Reference to this ConnectionFactory in JNDI
     */
    private Reference jndiReference;
    
    private final transient ManagedConnectionFactory managedConnectionFactory;
    private final transient ConnectionManager connectionManager;
    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String defaultPassword;
    private final String defaultUserName;
    private transient PrintWriter logWriter;
    private int logTimeout = 0;

    public JdbcConnectionFactory(ManagedConnectionFactory managedConnectionFactory,
                                 ConnectionManager connectionManager, String jdbcUrl,
                                 String jdbcDriver, String defaultPassword, String defaultUserName) throws ResourceException {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
        this.logWriter = managedConnectionFactory.getLogWriter();
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
    }

    /**
     * setReference is called by deployment code
     *
     * @param jndiReference
     */
    public void setReference(Reference jndiReference) {
        this.jndiReference = jndiReference;
    }

    /**
     * getReference is called by JNDI provider during Context.bind
     *
     * @return
     */
    public Reference getReference() {
        return jndiReference;
    }

    public Connection getConnection() throws SQLException {
        return getConnection(defaultUserName, defaultPassword);
    }

    public Connection getConnection(java.lang.String username, java.lang.String password) throws SQLException {
        return getConnection(new JdbcConnectionRequestInfo(username, password, jdbcDriver, jdbcUrl));
    }

    protected Connection getConnection(JdbcConnectionRequestInfo connectionRequestInfo) throws SQLException {
        // TODO: Use ManagedConnection.assocoate() method here if the client has already obtained a physical connection.
        // the previous connection is either shared or invalidated. IT should probably be shared.
        try {
            return (Connection) connectionManager.allocateConnection(managedConnectionFactory, connectionRequestInfo);
        } catch (ApplicationServerInternalException e) {
            throw convertToSQLException(e, "Application error in ContainerManager");
        } catch (javax.resource.spi.SecurityException e) {
            throw convertToSQLException(e, "Authentication error. Invalid credentials");
        } catch (ResourceAdapterInternalException e) {
            throw convertToSQLException(e, "JDBC Connection problem");
        } catch (ResourceAllocationException e) {
            throw convertToSQLException(e, "JDBC Connection could not be obtained");
        } catch (ResourceException e) {
            throw convertToSQLException(e, "JDBC Connection Factory problem");
        }
    }

    private SQLException convertToSQLException(ResourceException e, String error) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            return (SQLException) cause;
        } else {
            String message = ((cause != null) ? cause.getMessage() : "");
            return (SQLException) new SQLException("Error code: " + e.getErrorCode() + error + message).initCause(e);
        }
    }

    public int getLoginTimeout() {
        return logTimeout;
    }

    public java.io.PrintWriter getLogWriter() {
        return logWriter;
    }

    public void setLoginTimeout(int seconds) {
        //TODO: how should log timeout work?
        logTimeout = seconds;
    }

    public void setLogWriter(java.io.PrintWriter out) {
        logWriter = out;
    }
}
