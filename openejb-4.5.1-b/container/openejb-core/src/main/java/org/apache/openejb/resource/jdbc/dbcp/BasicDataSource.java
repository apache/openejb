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
package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.BasicDataSourceUtil;
import org.apache.openejb.resource.jdbc.IsolationLevels;
import org.apache.openejb.resource.jdbc.cipher.PasswordCipher;
import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.openejb.util.reflection.Reflections;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@SuppressWarnings({"UnusedDeclaration"})
public class BasicDataSource extends org.apache.commons.dbcp.BasicDataSource {

    private static final ReentrantLock lock = new ReentrantLock();

    private Logger logger = null;

    /**
     * The password codec to be used to retrieve the plain text password from a
     * ciphered value.
     * <p/>
     * <em>The default is no codec.</em>. In other words, it means password is
     * not ciphered. The {@link org.apache.openejb.resource.jdbc.cipher.PlainTextPasswordCipher} can also be used.
     */
    private String passwordCipher = null;
    private JMXBasicDataSource jmxDs = null;

    public BasicDataSource() {
        // no-op
    }

    public BasicDataSource(final String name) {
        setName(name);
    }

    private void registerAsMbean(final String name) {
        try {
            jmxDs = new JMXBasicDataSource(name, this);
        } catch (Exception e) {
            jmxDs = null;
        } catch (NoClassDefFoundError ncdfe) { // OSGi
            jmxDs = null;
        }
    }

    /**
     * Returns the password codec class name to use to retrieve plain text
     * password.
     *
     * @return the password codec class
     */
    public String getPasswordCipher() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return this.passwordCipher;
        } finally {
            l.unlock();
        }
    }

    /**
     * <p>
     * Sets the {@link #passwordCipher}.
     * </p>
     *
     * @param passwordCipher password codec value
     */
    public void setPasswordCipher(String passwordCipher) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            this.passwordCipher = passwordCipher;
        } finally {
            l.unlock();
        }
    }


    public String getUserName() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return super.getUsername();
        } finally {
            l.unlock();
        }
    }

    public void setUserName(String string) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setUsername(string);
        } finally {
            l.unlock();
        }
    }

    public String getJdbcDriver() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return super.getDriverClassName();
        } finally {
            l.unlock();
        }
    }

    public void setJdbcDriver(String string) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setDriverClassName(string);
        } finally {
            l.unlock();
        }
    }

    public String getJdbcUrl() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return super.getUrl();
        } finally {
            l.unlock();
        }
    }

    public void setJdbcUrl(String string) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setUrl(string);
        } finally {
            l.unlock();
        }
    }

    public void setDefaultTransactionIsolation(String s) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            if (s == null || s.equals("")) return;
            int level = IsolationLevels.getIsolationLevel(s);
            super.setDefaultTransactionIsolation(level);
        } finally {
            l.unlock();
        }
    }

    public void setMaxWait(final int maxWait) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setMaxWait((long) maxWait);
        } finally {
            l.unlock();
        }
    }

    protected DataSource createDataSource() throws SQLException {
        final ReentrantLock l = lock;
        l.lock();
        try {
            if (super.dataSource != null) {
                return super.dataSource;
            }

            // check password codec if available
            if (null != passwordCipher) {
                PasswordCipher cipher = BasicDataSourceUtil.getPasswordCipher(passwordCipher);
                String plainPwd = cipher.decrypt(password.toCharArray());

                // override previous password value
                super.setPassword(plainPwd);
            }

            // get the plugin
            DataSourcePlugin helper = BasicDataSourceUtil.getDataSourcePlugin(getUrl());

            // configure this
            if (helper != null) {
                final String currentUrl = getUrl();
                final String newUrl = helper.updatedUrl(currentUrl);
                if (!currentUrl.equals(newUrl)) {
                    super.setUrl(newUrl);
                }
            }

            // create the data source
            if (helper == null || !helper.enableUserDirHack()) {
                try {
                    return super.createDataSource();
                } catch (Throwable e) {
                    throw new SQLException("Failed to create DataSource", e);
                }
            } else {
                // wrap super call with code that sets user.dir to openejb.base and then resets it
                Properties systemProperties = System.getProperties();

                String userDir = systemProperties.getProperty("user.dir");
                try {
                    File base = SystemInstance.get().getBase().getDirectory();
                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
                    try {
                        return super.createDataSource();
                    } catch (Throwable e) {
                        throw new SQLException("Failed to create DataSource", e);
                    }
                } finally {
                    systemProperties.setProperty("user.dir", userDir);
                }

            }
        } finally {
            l.unlock();
        }
    }

    public void close() throws SQLException {
        //TODO - Prevent unuathorized call
        final ReentrantLock l = lock;
        l.lock();
        try {
            try {
                unregisterMBean();
            } catch (Exception ignored) {
                // no-op
            }

            super.close();
        } finally {
            l.unlock();
        }
    }

    private void unregisterMBean() {
        if (jmxDs != null) {
            jmxDs.unregister();
        }
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        final ReentrantLock l = lock;
        l.lock();
        try {

            if (null == this.logger) {
                this.logger = (Logger) Reflections.invokeByReflection(super.dataSource, "getParentLogger", new Class<?>[0], null);
            }

            return this.logger;
        } catch (Throwable e) {
            throw new SQLFeatureNotSupportedException();
        } finally {
            l.unlock();
        }
    }

    public void setName(final String name) {
        registerAsMbean(name);
    }
}
