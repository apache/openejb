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

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import junit.framework.TestCase;

import org.openejb.util.BackportExecutorAdapter;

import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceDaemon;
import org.activeio.xnet.StandardServiceStack;
import org.activeio.xnet.SyncChannelServerDaemon;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.pool.ThreadPool;

public class HttpServerTest extends TestCase {

//    static {
//        BasicConfigurator.configure();
//    }

    public void testBareService() throws Exception {
        ServerService service = new HttpServer(new TestHttpListener());
        ServiceDaemon daemon = new ServiceDaemon("HTTP", service, InetAddress.getByName("localhost"), 0);
        HttpURLConnection connection = null;

        try {
            daemon.setSoTimeout(1000);
            daemon.doStart();

            int port = daemon.getPort();
            URL url = new URL("http://localhost:" + port + "/this/should/hit/something");
            connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();
            assertEquals("HTTP response code should be 204", HttpURLConnection.HTTP_NO_CONTENT, responseCode);
        } finally {
            connection.disconnect();
            daemon.doStop();
        }
    }

    public void testBareChannelService() throws Exception {
        ServerService service = new HttpServer(new TestHttpListener());
        SyncChannelServerDaemon daemon = new SyncChannelServerDaemon("HTTP", service, InetAddress.getByName("localhost"), 0);
        HttpURLConnection connection = null;

        try {
            daemon.setSoTimeout(1000);
            daemon.doStart();

            int port = daemon.getPort();
            URL url = new URL("http://localhost:" + port + "/this/should/hit/something");
            connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();
            assertEquals("HTTP response code should be 204", HttpURLConnection.HTTP_NO_CONTENT, responseCode);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            daemon.doStop();
        }
    }


    public void testServiceStack() throws Exception {
        ServerService service = new HttpServer(new TestHttpListener());

        ThreadPool threadPool = new ThreadPool(1, "Test", 1000, getClass().getClassLoader(), "openejb:type=ThreadPool,name=Test");
        BackportExecutorAdapter adapter = new BackportExecutorAdapter(threadPool);
        
        StandardServiceStack serviceStack = new StandardServiceStack("HTTP", 0, "localhost", null, null, null, adapter, service);
        HttpURLConnection connection = null;

        try {
            serviceStack.setSoTimeout(1000);
            serviceStack.doStart();

            int port = serviceStack.getPort();
            URL url = new URL("http://localhost:" + port + "/this/should/hit/something");
            connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();
            assertEquals("HTTP response code should be 204", HttpURLConnection.HTTP_NO_CONTENT, responseCode);
        } finally {
            connection.disconnect();
            serviceStack.doStop();
        }
    }

//    public void testHttpServerGBean() throws Exception {
//        Kernel kernel = KernelFactory.newInstance().createKernel("wstest");
//        kernel.boot();
//
//        ObjectName listener = TestHttpListener.addGBean(kernel, "HTTP");
//        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTP", listener);
//        ServerService service = (ServerService) kernel.getProxyManager().createProxy(server, ServerService.class);
//
//        ThreadPool threadPool = new ThreadPool(1, "Test", 1000, getClass().getClassLoader(), "openejb:type=ThreadPool,name=Test");
//
//        StandardServiceStack serviceStack = new StandardServiceStack("HTTP", 0, "localhost", null, null, null, threadPool, service);
//        HttpURLConnection connection = null;
//
//        try {
//            serviceStack.setSoTimeout(100);
//            serviceStack.doStart();
//
//            int port = serviceStack.getPort();
//            URL url = new URL("http://localhost:" + port + "/this/should/hit/something");
//            connection = (HttpURLConnection) url.openConnection();
//
//            int responseCode = connection.getResponseCode();
//            assertEquals("HTTP response code should be 204", HttpURLConnection.HTTP_NO_CONTENT, responseCode);
//        } finally {
//            connection.disconnect();
//            serviceStack.doStop();
//            kernel.shutdown();
//        }
//    }
//
//    public void testGBeanServiceStack() throws Exception {
//        Kernel kernel = KernelFactory.newInstance().createKernel("wstest");
//        kernel.boot();
//
//        ObjectName listener = TestHttpListener.addGBean(kernel, "HTTP");
//        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTP", listener);
//
//        ClassLoader cl = ThreadPool.class.getClassLoader();
//        ObjectName executor = JMXUtil.getObjectName("openejb:name=ThreadPool");
//        GBeanData gbean = new GBeanData(executor, ThreadPool.GBEAN_INFO);
//        gbean.setAttribute("poolSize", new Integer(1));
//        gbean.setAttribute("poolName", "Test");
//        gbean.setAttribute("keepAliveTime", new Long(1000));
//        kernel.loadGBean(gbean, cl);
//        kernel.startGBean(executor);
//
//        ObjectName stack = StandardServiceStackGBean.addGBean(kernel, "HTTP", 0, "localhost", null, null, null, executor, server);
//
//        assertRunning(kernel, listener);
//        assertRunning(kernel, server);
//        assertRunning(kernel, stack);
//
//        HttpURLConnection connection = null;
//
//        try {
//            kernel.setAttribute(stack, "soTimeout", new Integer(100));
//            int port = ((Integer) kernel.getAttribute(stack, "port")).intValue();
//            URL url = new URL("http://localhost:" + port + "/this/should/hit/something");
//
//            connection = (HttpURLConnection) url.openConnection();
//            int responseCode = connection.getResponseCode();
//            System.out.println("responseCode = " + responseCode);
//            assertEquals("HTTP response code should be 204", responseCode, HttpURLConnection.HTTP_NO_CONTENT);
//        } catch (Exception e) {
//            System.out.println("exception " + e.getMessage());
//        } finally {
//            connection.disconnect();
//            kernel.stopGBean(stack);
//            kernel.shutdown();
//        }
//    }
//
//
//    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
//        assertEquals("should be running: " + objectName, State.RUNNING_INDEX, kernel.getGBeanState(objectName));
//    }

    public static class TestHttpListener implements HttpListener {

        public void onMessage(HttpRequest req, HttpResponse res) {
            System.out.println("HttpServerTest$TestHttpListener.onMessage");
            res.setCode(HttpURLConnection.HTTP_NO_CONTENT);
        }

        public static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(TestHttpListener.class);
            infoFactory.addOperation("onMessage", new Class[]{HttpRequest.class, HttpResponse.class});
            infoFactory.setConstructor(new String[]{});

            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }

//        public static ObjectName addGBean(Kernel kernel, String name) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//            ClassLoader classLoader = TestHttpListener.class.getClassLoader();
//            ObjectName SERVICE_NAME = JMXUtil.getObjectName("openejb:type=TestHttpListener,name=" + name);
//
//            GBeanData gbean = new GBeanData(SERVICE_NAME, TestHttpListener.GBEAN_INFO);
//
//            kernel.loadGBean(gbean, classLoader);
//            kernel.startGBean(SERVICE_NAME);
//            return SERVICE_NAME;
//        }

    }

}