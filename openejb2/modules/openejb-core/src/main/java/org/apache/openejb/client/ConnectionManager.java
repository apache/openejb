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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: ConnectionManager.java 444943 2004-09-29 03:39:44Z dblevins $
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.util.Properties;


/**
 * @since 11/25/2001
 */
public class ConnectionManager {

    private static ConnectionFactory factory;
    private static Class defaultFactoryClass = SocketConnectionFactory.class;
    private static String factoryName;

    static {
        try {
            installFactory(defaultFactoryClass);
        } catch (Throwable e) {
            throw (IllegalStateException) new IllegalStateException("ConnectionFactory could not be installed").initCause(e);
        }
    }

    public static Connection getConnection(ServerMetaData server) throws IOException {
        return factory.getConnection(server);
    }

    public static void setFactory(String factoryName) throws IOException {
        installFactory(factoryName);
    }

    public static ConnectionFactory getFactory() {
        return factory;
    }

    public static String getFactoryName() {
        return factoryName;
    }

    private static void installFactory(String factoryName) throws IOException {

        Class factoryClass = null;

        try {
            ClassLoader cl = getContextClassLoader();
            factoryClass = Class.forName(factoryName, true, cl);
        } catch (Exception e) {
            throw (IOException) new IOException("No ConnectionFactory Can be installed. Unable to load the class " + factoryName).initCause(e);
        }

        installFactory(factoryClass);

    }

    private static void installFactory(Class factoryClass) throws IOException {
        ConnectionFactory factory;
        try {
            factory = (ConnectionFactory) factoryClass.newInstance();
        } catch (Exception e) {
            throw (IOException) new IOException("No ConnectionFactory Can be installed. Unable to instantiate the class " + factoryName).initCause(e);
        }

        try {
            // TODO:3: At some point we may support a mechanism for
            //         actually specifying properties for the Factories
            factory.init(new Properties());
        } catch (Exception e) {
            throw (IOException) new IOException("No ConnectionFactory Can be installed. Unable to initialize the class " + factoryName).initCause(e);
        }

        ConnectionManager.factory = factory;
        ConnectionManager.factoryName = factoryClass.getName();
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

}
