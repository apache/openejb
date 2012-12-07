/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.router.Router;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.reflection.Reflections;

public class RoutedDataSource implements DataSource {
    private static final String OPENEJB_RESOURCE_PREFIX = "openejb:Resource/";

    private Router delegate;

    public RoutedDataSource() {
        // no-op
    }

    public RoutedDataSource(String router) {
        setRouter(router);
    }

    public void setRouter(String router) {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        Object o = null;
        Context ctx = containerSystem.getJNDIContext();
        try {
            o = ctx.lookup(OPENEJB_RESOURCE_PREFIX + router);

        } catch (NamingException e) {
            throw new IllegalArgumentException("Can't find router [" + router + "]", e);
        }

        if (o instanceof Router) {
            delegate = (Router) o;

        } else {
            throw new IllegalArgumentException(o + " is not a router");
        }
    }

    public PrintWriter getLogWriter() throws SQLException {
        if (getTargetDataSource() == null) {
            return null;
        }
        return getTargetDataSource().getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        if (getTargetDataSource() != null) {
            getTargetDataSource().setLogWriter(out);
        }
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        if (getTargetDataSource() != null) {
            getTargetDataSource().setLoginTimeout(seconds);
        }
    }

    public int getLoginTimeout() throws SQLException {
        if (getTargetDataSource() == null) {
            return -1;
        }
        return getTargetDataSource().getLoginTimeout();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (getTargetDataSource() == null) {
            return null;
        }
        return (T) Reflections.invokeByReflection(getTargetDataSource(), "unwrap",
                new Class<?>[]{Class.class}, new Object[]{iface});
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if (getTargetDataSource() == null) {
            return null;
        }
        return (Logger) Reflections.invokeByReflection(getTargetDataSource(), "getParentLogger", new Class<?>[0], null);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (getTargetDataSource() == null) {
            return false;
        }
        return (Boolean) Reflections.invokeByReflection(getTargetDataSource(), "isWrapperFor",
                new Class<?>[]{Class.class}, new Object[]{iface});
    }

    public Connection getConnection() throws SQLException {
        return getTargetDataSource().getConnection();
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        return getTargetDataSource().getConnection(username, password);
    }

    public Router getDelegate() {
        if (delegate == null) {
            throw new IllegalStateException("a router has to be defined");
        }
        return delegate;
    }

    private DataSource getTargetDataSource() {
        return getDelegate().getDataSource();
    }
}
