/**
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
package org.apache.openejb.server;

import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@Managed
public class ServiceLogger extends ServerServiceFilter {

    private Logger logger;
    private boolean debug = false;

    public ServiceLogger(final ServerService next) {
        super(next);
    }

    @Override
    public void init(final Properties props) throws Exception {

        this.logger = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("service." + getName()), "org.apache.openejb.server.util.resources");
        this.debug = this.logger.isDebugEnabled();

        super.init(props);
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    private static Method MDBput = null;

    static {
        try {
            final Class<?> MDC = ServiceLogger.class.getClassLoader().loadClass("org.apache.log4j.MDC");
            MDBput = MDC.getMethod("put", String.class, String.class);
        } catch (Exception e) { // no need to log it with a higher level
            Logger.getInstance(LogCategory.OPENEJB, ServiceLogger.class.getName())
                  .debug("can't find log4j MDC class");
        }
    }

    public static void MDCput(final String key, final String value) {
        if (MDBput != null) {
            try {
                MDBput.invoke(null, key, value);
            } catch (Exception e) {
                // ignored
            }
        }
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {

        final InetAddress client = socket.getInetAddress();
        final String address = client.getHostAddress();
        final String name = this.getName();

        MDCput("HOST", address);
        MDCput("SERVER", name);

        try {
            final long start = System.nanoTime();
            super.service(socket);

            if (this.debug) {
                this.logger.debug("[request] for '" + name + "' by '" + address + "' took " + (System.nanoTime() - start) + "ns");
            }
        } catch (Exception e) {
            final String msg = "[Request failed] for '" + name + "' by '" + address + "' : " + e.getMessage();
            if (this.debug) {
                this.logger.error(msg, e);
            } else {
                this.logger.error(msg + " - Debug for StackTrace");
            }
        }
    }
}
