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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.openejb.server.StandardServiceStackGBean;
import org.openejb.server.httpd.HttpListener;
import org.openejb.server.httpd.HttpRequest;
import org.openejb.server.httpd.HttpResponse;
import org.openejb.server.httpd.HttpServerGBean;
import org.openejb.slsb.MockEJBContainer;
import org.openejb.slsb.MockEJBContainerGBean;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WSContainerTest extends TestCase {
    WSContainer wsContainer;

//    static {
//        org.apache.log4j.BasicConfigurator.configure();
//    }


    private Definition getDefinition(URL wsdlURL) throws Exception {
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        return wsdlReader.readWSDL(wsdlURL.toExternalForm());
    }

    public void testGetWSDL() throws Exception {
        Kernel kernel = new Kernel("wstest");
        kernel.boot();

        URL wsdlURL = new File("target/test-ejb-jar/META-INF/wsdl/test-ejb.wsdl").toURL();


        ObjectName ejbContainer = MockEJBContainer.addGBean(kernel, "MockEJB");
        ObjectName wsContainer = WSContainerGBean.addGBean(kernel, "HTTPSOAP", ejbContainer, getDefinition(wsdlURL), new URI("/test/service"), wsdlURL, "urn:testing", "encoded", "rpc");
        ObjectName index = WSContainerIndexGBean.addGBean(kernel, "HTTPSOAP", wsContainer);
        ObjectName listener = SoapHttpListenerGBean.addGBean(kernel, "HTTPSOAP", index);
        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTPSOAP", listener);
        ObjectName stack = StandardServiceStackGBean.addGBean(kernel, "HTTPSOAP", 0, InetAddress.getByName("localhost"), null, 1, 5, null, null, server);

        assertRunning(kernel, ejbContainer);
        assertRunning(kernel, wsContainer);
        assertRunning(kernel, index);
        assertRunning(kernel, listener);
        assertRunning(kernel, server);
        assertRunning(kernel, stack);

        InputStream in = null;
        try {
            kernel.setAttribute(stack, "soTimeout", new Integer(1000));
            int port = ((Integer) kernel.getAttribute(stack, "port")).intValue();
            URL url = new URL("http://localhost:" + port + "/test/service?wsdl");
            in = url.openStream();

            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            Definition definition = wsdlReader.readWSDL(null, new InputSource(in));

            assertNotNull(definition);

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            in.close();
            kernel.stopGBean(stack);
            kernel.shutdown();
        }
    }

    public void testAxisStyleMessage() throws Exception {
        Kernel kernel = new Kernel("wstest");
        kernel.boot();

        URL wsdlURL = new File("target/test-ejb-jar/META-INF/wsdl/test-ejb.wsdl").toURL();

        ObjectName ejbContainer = MockEJBContainerGBean.addGBean(kernel, new File("target/test-ejb-jar").toURL(), "SimpleEJB", "org.openejb.test.simple.slsb.SimpleStatelessSessionEJB", "org.openejb.test.simple.slsb.SimpleStatelessSessionHome", "org.openejb.test.simple.slsb.SimpleStatelessSession", "org.openejb.test.simple.slsb.SimpleStatelessSessionLocalHome", "org.openejb.test.simple.slsb.SimpleStatelessSessionLocal", "org.openejb.test.simple.slsb.SimpleStatelessSessionEndpoint");
        ObjectName wsContainer = WSContainerGBean.addGBean(kernel, "HTTPSOAP", ejbContainer, getDefinition(wsdlURL), new URI("/services/Simple"), wsdlURL, "urn:testing", "encoded", "rpc");
        ObjectName index = WSContainerIndexGBean.addGBean(kernel, "HTTPSOAP", wsContainer);
        ObjectName listener = SoapHttpListenerGBean.addGBean(kernel, "HTTPSOAP", index);
        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTPSOAP", listener);
        ObjectName stack = StandardServiceStackGBean.addGBean(kernel, "HTTPSOAP", 0, InetAddress.getByName("localhost"), null, 1, 5, null, null, server);

        assertRunning(kernel, ejbContainer);
        assertRunning(kernel, wsContainer);
        assertRunning(kernel, index);
        assertRunning(kernel, listener);
        assertRunning(kernel, server);
        assertRunning(kernel, stack);

        HttpURLConnection connection = null;

        try {
            kernel.setAttribute(stack, "soTimeout", new Integer(1000));
            int port = ((Integer) kernel.getAttribute(stack, "port")).intValue();
            URL url = new URL("http://localhost:" + port + "/services/Simple");

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/xml");
            PrintStream out = new PrintStream(connection.getOutputStream());
            out.print("<soapenv:Envelope\n" +
                    "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "<soapenv:Body>\n" +
                    "<ns1:echo soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                    "          xmlns:ns1=\"http://openejb.org/test-ejb-jar\">\n" +
                    "<String_1 xsi:type=\"xsd:string\">hello</String_1>\n" +
                    "</ns1:echo>\n" +
                    "</soapenv:Body>\n" +
                    "</soapenv:Envelope>");
            out.flush();
            out.close();

            String result = getResult(connection.getInputStream());

            assertEquals("hello", result);

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

    private String getResult(InputStream responseStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        TestHandler handler = new TestHandler();
        saxParser.parse(responseStream, handler);
        return handler.result;
    }

    private static class TestHandler extends DefaultHandler {
        String result;

        public void characters(char ch[], int start, int length) throws SAXException {
            result = new String(ch, start, length);
        }
    }
}