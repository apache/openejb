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
package org.openejb.server.soap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.codehaus.xfire.MessageContext;
import org.openejb.EJBContainer;
import org.openejb.server.StandardServiceStackGBean;
import org.openejb.server.httpd.HttpListener;
import org.openejb.server.httpd.HttpRequest;
import org.openejb.server.httpd.HttpResponse;
import org.openejb.server.httpd.HttpServerGBean;
import org.openejb.slsb.MockEJBContainer;

public class WSContainerTest extends TestCase {
    WSContainer wsContainer;

//    static {
//        BasicConfigurator.configure();
//    }

    public void testWSContainer() throws Exception {
        EJBContainer ejbContainer = new MockEJBContainer();
        WSContainer container = new WSContainer(ejbContainer, null, null, "urn:testing", "encoded", "rpc");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        out.print("<soap:Envelope\n" +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "xmlns:ns=\"urn:testing\"\n" +
                "soap:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" >\n" +
                "<Body>\n" +
                "<ns:intMethodRequest>\n" +
                "<ns:in0 xsi:type=\"int\">126</ns:in0>\n" +
                "</ns:intMethodRequest>\n" +
                "</Body>\n" +
                "</soap:Envelope>");
        out.flush();
        out.close();

        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        baos.reset();

        baos = new ByteArrayOutputStream();
        MessageContext context = new MessageContext("not-used", null, baos, null, "/test/web/service");
        context.setRequestStream(in);
        container.invoke(context);
        String response = new String(baos.toByteArray());
        assertTrue(response.indexOf(("<out>127</out>")) > 0);
    }

    public void testGBeanWSContainerStack() throws Exception {
        Kernel kernel = new Kernel("wstest");
        kernel.boot();

        ObjectName ejbContainer = MockEJBContainer.addGBean(kernel, "MockEJB");
        ObjectName wsContainer = WSContainerGBean.addGBean(kernel, "HTTPSOAP", ejbContainer, new URI("/test/service"), null, "urn:testing", "encoded", "rpc");
        ObjectName listener = TestSoapHttpListener.addGBean(kernel, "HTTPSOAP", wsContainer);
        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTPSOAP", listener);
        ObjectName stack = StandardServiceStackGBean.addGBean(kernel, "HTTPSOAP", 0, InetAddress.getByName("localhost"), null, 1, 5, null, null, server);

        assertRunning(kernel, ejbContainer);
        assertRunning(kernel, wsContainer);
        assertRunning(kernel, listener);
        assertRunning(kernel, server);
        assertRunning(kernel, stack);

        HttpURLConnection connection = null;

        try {
            kernel.setAttribute(stack, "soTimeout", new Integer(1000));
            int port = ((Integer) kernel.getAttribute(stack, "port")).intValue();
            URL url = new URL("http://localhost:" + port + "/this/should/hit/something");

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/xml");
            PrintStream out = new PrintStream(connection.getOutputStream());
            out.print("<soap:Envelope\n" +
                    "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "xmlns:ns=\"urn:testing\"\n" +
                    "soap:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" >\n" +
                    "<Body>\n" +
                    "<ns:intMethodRequest>\n" +
                    "<ns:in0 xsi:type=\"int\">126</ns:in0>\n" +
                    "</ns:intMethodRequest>\n" +
                    "</Body>\n" +
                    "</soap:Envelope>");
            out.flush();
            out.close();

            byte[] bytes = new byte[connection.getContentLength()];
            DataInputStream in = new DataInputStream(connection.getInputStream());
            in.readFully(bytes);
            String response = new String(bytes);
            assertTrue(response.indexOf(("<out>127</out>")) > 0);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            connection.disconnect();
            kernel.stopGBean(stack);
            kernel.shutdown();
        }
    }

    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        int state = ((Integer) kernel.getAttribute(objectName, "state")).intValue();
        assertEquals("should be running: " + objectName, State.RUNNING_INDEX, state);
    }

    public static class TestSoapHttpListener implements HttpListener {

        private final WSContainer container;

        public TestSoapHttpListener(WSContainer container) {
            this.container = container;
        }

        public void onMessage(HttpRequest req, HttpResponse res) throws IOException {
            try {
                MessageContext context = new MessageContext("not-used", null, res.getOutputStream(), null, req.getURI().toString());
                context.setRequestStream(req.getInputStream());
                res.setContentType("text/xml");
                container.invoke(context);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(TestSoapHttpListener.class);
            infoFactory.addOperation("onMessage", new Class[]{HttpRequest.class, HttpResponse.class});

            infoFactory.setConstructor(new String[]{"WSContainer"});

            infoFactory.addReference("WSContainer", WSContainer.class);

            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }

        public static ObjectName addGBean(Kernel kernel, String name, ObjectName wsContainer) throws GBeanAlreadyExistsException, GBeanNotFoundException {
            ClassLoader classLoader = org.openejb.server.soap.SoapHttpListener.class.getClassLoader();
            ObjectName SERVICE_NAME = JMXUtil.getObjectName("openejb:type=TestSoapHttpListener,name=" + name);

            GBeanData gbean = new GBeanData(SERVICE_NAME, TestSoapHttpListener.GBEAN_INFO);
            gbean.setReferencePattern("WSContainer", wsContainer);
            kernel.loadGBean(gbean, classLoader);
            kernel.startGBean(SERVICE_NAME);
            return SERVICE_NAME;
        }
    }

}