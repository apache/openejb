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

import org.openejb.core.EnvProps;
import org.openejb.util.Logger;
import org.openejb.loader.SystemInstance;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.File;
import java.util.Set;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

public class JdbcManagedConnectionFactory implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {

    protected Logger logger = Logger.getInstance("OpenEJB.connector", "org.openejb.alt.util.resources");
    private ManagedConnectionFactory factory;

    public void init(java.util.Properties props) throws javax.resource.spi.ResourceAdapterInternalException {
        String defaultUserName = props.getProperty(EnvProps.USER_NAME);
        String defaultPassword = props.getProperty(EnvProps.PASSWORD);
        String url = props.getProperty(EnvProps.JDBC_URL);
        String driver = props.getProperty(EnvProps.JDBC_DRIVER);

        loadDriver(driver);

        factory = new BasicManagedConnectionFactory(this, driver, url, defaultUserName, defaultPassword);

        if (driver.equals("org.enhydra.instantdb.jdbc.idbDriver")) {
            factory = new ManagedConnectionFactoryPathHack(factory);
        } else if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
            factory = new ManagedConnectionFactoryPathHack(factory);
        } else if (driver.equals("org.hsqldb.jdbcDriver")) {
            factory = new ManagedConnectionFactoryPathHack(factory);
        } else if (url.indexOf("conf/") > 0){
            try {
                String path = url.substring(url.indexOf("conf/"), url.length());
                URI uri = new URI("file:///" + path);
                path = uri.getPath();
                path = path.substring(1, path.length());
                SystemInstance.get().getBase().getFile(path);
                factory = new ManagedConnectionFactoryPathHack(factory);
            } catch (URISyntaxException e) {
            } catch (java.io.FileNotFoundException e) {
            } catch (java.io.IOException e) {
            }
        }

        JdbcConnectionRequestInfo info = new JdbcConnectionRequestInfo(defaultUserName, defaultPassword, driver, url);
        ManagedConnection connection = null;
        try {
            connection = factory.createManagedConnection(null, info);
        } catch (Throwable e) {
            logger.error("Testing driver failed.  " + "[" + url + "]  "
                    + "Could not obtain a physical JDBC connection from the DriverManager."
                    + "\nThe error message was:\n" + e.getMessage() + "\nPossible cause:"
                    + "\n\to JDBC driver classes are not available to OpenEJB"
                    + "\n\to Relative paths are not resolved properly");
        } finally {
            try {
                connection.destroy();
            } catch (ResourceException dontCare) {
            }
        }
    }

    private void loadDriver(String driver) throws ResourceAdapterInternalException {
        try {
            ClassLoader classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
            Class.forName(driver, true, classLoader);
        } catch (ClassNotFoundException cnf) {
            throw new ResourceAdapterInternalException("JDBC Driver class \"" + driver + "\" not found by class loader", ErrorCode.JDBC_0002);
        }
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return factory.createConnectionFactory(connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return factory.createConnectionFactory();
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.createManagedConnection(subject, connectionRequestInfo);
    }

    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.matchManagedConnections(set, subject, connectionRequestInfo);
    }

    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
        factory.setLogWriter(printWriter);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return factory.getLogWriter();
    }

    public int hashCode() {
        return factory.hashCode();
    }

    public boolean equals(Object o) {
        return factory.equals(o);
    }
}