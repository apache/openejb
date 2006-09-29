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
package org.apache.openejb.server.httpd;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.openejb.util.BackportExecutorAdapter;

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