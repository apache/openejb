/*
 * Created on 05/04/2004
 *
 */
package org.openejb.resource.jdbc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * Handles Transactional XA Data sources.
 */
public class JdbcXAManagedConnectionFactory
        extends JdbcManagedConnectionFactory
        implements ManagedConnectionFactory, Serializable {
    protected String xadatasourceClassName;

    protected Class xadsclass;

    public void init(java.util.Properties props)
            throws javax.resource.spi.ResourceAdapterInternalException {
        setXADataSourceClass(props.getProperty("JdbcXADataSource"));
        super.init(props);
    }

    protected void testConnection() {
        XAConnection connection = null;

        try {
            XADataSource ds = createXADataSource();
            connection = ds.getXAConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException ignores) {
            }
        }
    }

    /**
     * Pending
     *
     * @return
     */
    public String getXADataSourceClass() {
        return xadatasourceClassName;
    }

    /**
     * Pending
     *
     * @param value
     */
    public void setXADataSourceClass(String value)
            throws javax.resource.spi.ResourceAdapterInternalException {
        xadatasourceClassName = value;

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            xadsclass = Class.forName(xadatasourceClassName, true, cl);
        } catch (ClassNotFoundException cnf) {
            ResourceAdapterInternalException raie = new ResourceAdapterInternalException(
                    "XADataSource class \"" + xadatasourceClassName + "\" not found by class loader", ErrorCode.JDBC_0002);
            throw raie;
        }
    }

    /**
     * Pending
     *
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new JdbcConnectionFactory(this, cxManager);
    }

    /**
     * Pending
     *
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
            throws ResourceException {
        JdbcConnectionRequestInfo rxInfo = (JdbcConnectionRequestInfo) cxRequestInfo;
        XAConnection xaConnection = null;
        Connection connection = null;
        try {
            XADataSource ds = createXADataSource();
            xaConnection = ds.getXAConnection(rxInfo.getUserName(), rxInfo.getPassword());
            connection = xaConnection.getConnection();
        } catch (java.sql.SQLException sqlE) {
            EISSystemException eisse = new EISSystemException("Could not obtain a physical JDBC connection from the DriverManager");
            eisse.setLinkedException(sqlE);
            throw eisse;
        }
        return new JdbcXAManagedConnection(this, xaConnection, connection, rxInfo);
    }

    protected XADataSource createXADataSource() throws ResourceAdapterInternalException {
        try {
            Object instance = xadsclass.newInstance();

            // Try to set up username and password
            // This is very import as Oracle (for instance)
            // won't carry username/password on URL

            setupInstance(instance);

            return (XADataSource) instance;
        } catch (Exception ex) {
            ResourceAdapterInternalException raie =
                    new ResourceAdapterInternalException("Could not instantiate XADataSource class", ex);

            throw raie;
        }
    }

    protected void setupInstance(Object instance) {
        Method[] methods = xadsclass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();

            if (!name.startsWith("set")) {
                continue;
            }
            name = name.substring(3);

            if (name.equalsIgnoreCase("username") ||
                    name.equalsIgnoreCase("defaultusername") ||
                    name.equalsIgnoreCase("user")) {
                invokeMethod(method, instance, super.defaultUserName);
            } else if (name.equalsIgnoreCase("password") ||
                    name.equalsIgnoreCase("defaultpassword") ||
                    name.equalsIgnoreCase("pass")) {
                invokeMethod(method, instance, super.defaultPassword);
            } else if (name.equalsIgnoreCase("url") ||
                    name.equalsIgnoreCase("defaulturl")) {
                invokeMethod(method, instance, super.jdbcUrl);
            }
        }
    }

    /**
     * Pending
     *
     * @param method
     * @param instance
     * @param value
     */
    private void invokeMethod(Method method, Object instance, String value) {
        try {
            method.invoke(instance, new Object[]{value});
        } catch (Exception ignores) {
        }
    }

    public boolean equals(Object other) {
        if (other instanceof JdbcXAManagedConnectionFactory) {
            JdbcXAManagedConnectionFactory otherMCF = (JdbcXAManagedConnectionFactory) other;
            if (xadatasourceClassName.equals(otherMCF.xadatasourceClassName) && jdbcUrl.equals(otherMCF.jdbcUrl) &&
                    defaultUserName.equals(otherMCF.defaultUserName) && defaultPassword.equals(otherMCF.defaultPassword)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (hashCode != 0) return hashCode;
        hashCode = xadatasourceClassName.hashCode() ^ jdbcUrl.hashCode() ^ defaultUserName.hashCode() ^ defaultPassword.hashCode();
        return hashCode;
    }

}
