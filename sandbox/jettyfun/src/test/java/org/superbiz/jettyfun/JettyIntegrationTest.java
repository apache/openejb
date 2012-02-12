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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.jettyfun;

import junit.framework.TestCase;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.jee.EjbJar;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @version $Rev$ $Date$
 */
public class JettyIntegrationTest extends TestCase {

    private static final CountDownLatch messageProcessed = new CountDownLatch(1);


    public void test() throws Exception {


        // Construct OpenEJB -- very simple like Jetty
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Deploy and Application
        final AppContext application;
        {
            // Create an EjbModule from Classes (other options available)
            final EjbModule ejbModule = new EjbModule(new EjbJar());
            ejbModule.setFinder(new AnnotationFinder(new ClassesArchive(EnterpriseJavaServlet.class, OrangeQueue.class)));

            // Configure and Assemble the application
            // You can actually split these if you wanted to save the result of configureApplication to disk
            application = assembler.createApplication(config.configureApplication(ejbModule));
        }


        // Begin constructing Jetty
        final Server server = new Server();

        {// set up NIO-based HTTP connector
            final SelectChannelConnector httpConnector;
            httpConnector = new SelectChannelConnector();
            httpConnector.setName("http");
            httpConnector.setPort(8080);
            httpConnector.setHost("localhost");

            server.addConnector(httpConnector);
        }

        {// Setup the thread pool
            final QueuedThreadPool threadPool = new QueuedThreadPool(20);
            threadPool.setMinThreads(1);
            server.setThreadPool(threadPool);
        }

        {// Convert the OpenEJB AppContext into Jetty Servlets
            final ServletContextHandler servletContext = new ServletContextHandler();
            servletContext.setConnectorNames(new String[]{"http"});

            for (BeanContext beanContext : application.getBeanContexts()) {
                if (Servlet.class.isAssignableFrom(beanContext.getBeanClass())) {

                    final Servlet servlet;

                    final boolean proxied = true;
                    if (proxied) {
                        // Interceptors, transactions, CDI scope management all will be enabled
                        servlet = (Servlet) beanContext.getBusinessLocalHome().create();
                    } else {
                        // Here we just take the raw injected instance and use it directly
                        final InstanceContext instanceContext = beanContext.newInstance();
                        servlet = (Servlet) instanceContext.getBean();
                    }

                    ServletHolder servletHolder = new ServletHolder(servlet);
                    servletHolder.setInitParameters(new HashMap());
                    servletContext.addServlet(servletHolder, "/" + beanContext.getEjbName());
                }
            }
            server.setHandler(servletContext);
        }

        // Start the server
        server.start();

        {// Run a test

            final URI uri = new URI("http://localhost:8080/some/path");
            final HttpURLConnection httpConnection = (HttpURLConnection) uri.toURL().openConnection();

            System.out.println("HTTP " + httpConnection.getResponseCode());

            messageProcessed.await();
        }
    }


    @Local
    @Singleton(name = "some/path")
    public static class EnterpriseJavaServlet implements Servlet {

        @Resource(name = "OrangeQueue")
        private Queue queue;

        @Resource
        private ConnectionFactory connectionFactory;

        @Override
        public void init(ServletConfig config) throws ServletException {
        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @TransactionAttribute(REQUIRES_NEW)
        @Override
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            System.out.println("req = " + req);

            Connection connection = null;
            Session session = null;

            try {
                connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(queue);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Create a message
                TextMessage message = session.createTextMessage("Hello Web!");

                // Tell the producer to send the message
                producer.send(message);
            } catch (JMSException e) {
                throw new ServletException(e);
            } finally {
                try {
                    // Clean up
                    if (session != null) session.close();
                    if (connection != null) connection.close();
                } catch (JMSException e) {
                }
            }
        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void destroy() {
        }
    }

    @MessageDriven
    public static class OrangeQueue implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {

                final TextMessage textMessage = (TextMessage) message;
                final String text = textMessage.getText();

                System.out.println(text);

                messageProcessed.countDown();
            } catch (JMSException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
