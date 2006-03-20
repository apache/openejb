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

import junit.framework.TestCase;
import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceDaemon;
import org.activeio.xnet.StandardServiceStack;
import org.activeio.xnet.StandardServiceStackGBean;
import org.activeio.xnet.SyncChannelServerDaemon;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.pool.ThreadPool;

import javax.management.ObjectName;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

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

        StandardServiceStack serviceStack = new StandardServiceStack("HTTP", 0, "localhost", null, null, null, threadPool, service);
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
//        ConfigurationData configurationData = new ConfigurationData(new Artifact("openejb", "test", "", "car"), kernel.getNming());
//        configurationData.addGBean("ConfigurationManager", KernelConfigurationManager.GBEAN_INFO);
//
//        AbstractName listener = TestHttpListener.addGBean(configurationData, "HTTP");
//        AbstractName server = HttpServerGBean.addGBean(configurationData, "HTTP", listener);
//
//        GBeanData threadPoolGBean = configurationData.addGBean("ThreadPool", ThreadPool.GBEAN_INFO);
//        threadPoolGBean.setAttribute("poolSize", new Integer(1));
//        threadPoolGBean.setAttribute("poolName", "Test");
//        threadPoolGBean.setAttribute("keepAliveTime", new Long(1000));
//
//        ObjectName stack = StandardServiceStackGBean.addGBean(configurationData, "HTTP", 0, "localhost", null, null, null, threadPoolGBean.getAbstractName(), server);
//
//        ConfigurationUtil.loadBootstrapConfiguration(kernel, configurationData, getClass().getClassLoader());
//
//        HttpURLConnection connection = null;
//
//        try {
//            StandardServiceStack serviceStack = (StandardServiceStack) kernel.getProxyManager().createProxy(stack, StandardServiceStack.class);
//
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

//        public static AbstractName addGBean(ConfigurationData configuration, String name) {
//            GBeanData gbean = configuration.addGBean(name, TestHttpListener.GBEAN_INFO);
//            return gbean.getAbstractName();
//        }
    }
}