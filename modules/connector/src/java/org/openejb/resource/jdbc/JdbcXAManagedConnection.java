/*
 * Created on 05/04/2004
 *
 */
package org.openejb.resource.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

/**
 * Holds an XAConnection to exposes its
 * Connection and XAResource
 */
public class JdbcXAManagedConnection extends JdbcManagedConnection {
    protected final XAConnection xaConnection;
    private final XAResource xaResource;

    /**
     * Pending
     *
     * @param factory
     * @param xaConnection
     * @param connection
     * @param rxInfo
     */
    public JdbcXAManagedConnection(
            JdbcXAManagedConnectionFactory factory, XAConnection xaConnection,
            Connection connection, JdbcConnectionRequestInfo rxInfo)
            throws ResourceAdapterInternalException {
        super(factory, connection, rxInfo);
        this.xaConnection = xaConnection;
        try {
            this.xaResource = xaConnection.getXAResource();
        } catch (SQLException e) {
            throw new ResourceAdapterInternalException("Could not get XAResource from alleged XAConnection", e);
        }
    }

    /**
     * Pending
     *
     * @see javax.resource.spi.ManagedConnection#getXAResource()
     */
    public XAResource getXAResource() throws ResourceException {
        return xaResource;
    }

}
