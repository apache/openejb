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
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.httpd;

import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.openejb.ContainerIndex;
import org.openejb.server.*;

public class HttpServerTest extends TestCase {

    public void testBareService() throws Exception {
        ServerService service = new HttpServer();
        ServiceDaemon daemon = new ServiceDaemon("HTTP", service, InetAddress.getByName("localhost"), 0);
        HttpURLConnection connection = null;

        try {
            daemon.setSoTimeout(100);
            daemon.doStart();

            int port = daemon.getPort();
            URL url = new URL("http://localhost:"+port+"/this/should/hit/something");
            connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();
            assertEquals("HTTP response code should be 500", responseCode, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            connection.disconnect();
            daemon.doStop();
        }

    }

    public void testServiceStack() throws Exception {
        ServerService service = new HttpServer();
        StandardServiceStack serviceStack = new StandardServiceStack("HTTP", 0, InetAddress.getByName("localhost"), null, 1,5, null, null, service);
        HttpURLConnection connection = null;

        try {
            serviceStack.doStart();
            int port = serviceStack.getPort();

            URL url = new URL("http://localhost:"+port+"/this/should/hit/something");

            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            assertEquals("HTTP response code should be 500", responseCode, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            connection.disconnect();
            serviceStack.doStop();
        }
    }

}