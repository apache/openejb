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
package org.openejb.server.axis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.axis.builder.AxisServiceBuilder;
import org.apache.axis.description.JavaServiceDesc;
import org.openejb.server.StandardServiceStackGBean;
import org.openejb.server.soap.SoapHttpListenerGBean;
import org.openejb.server.httpd.HttpServerGBean;
import org.openejb.slsb.MockEJBContainer;
import org.openejb.slsb.MockEJBContainerGBean;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WSContainerTest extends TestCase {

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

        URI wsdlURI = new URI("META-INF/wsdl/test-ejb.wsdl");
        JarFile jarFile = new JarFile("target/test-ejb-jar.jar");
        String ejbName = "SimpleStatelessSession";
        ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:target/test-ejb-jar.jar")}, getClass().getClassLoader());
        
        JavaServiceDesc serviceDesc = AxisServiceBuilder.createEJBServiceDesc(jarFile, ejbName, classLoader);

        ObjectName ejbContainer = MockEJBContainer.addGBean(kernel, "MockEJB", classLoader);
        ObjectName listener = SoapHttpListenerGBean.addGBean(kernel, "HTTPSOAP");
        ObjectName wsContainer = WSContainerGBean.addGBean(kernel, "HTTPSOAP", ejbContainer, listener, new URI("/test/service"), wsdlURI, serviceDesc);
        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTPSOAP", listener);
        ObjectName executor = buildExecutor(kernel);
        ObjectName stack = StandardServiceStackGBean.addGBean(kernel, "HTTPSOAP", 0, InetAddress.getByName("localhost"), null, null, null, executor, server);

        assertRunning(kernel, ejbContainer);
        assertRunning(kernel, wsContainer);
        assertRunning(kernel, listener);
        assertRunning(kernel, executor);
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
            in.close();
            assertNotNull(definition);
            
            // Check to see if the WSDL address was updated.
            Service service = (Service) definition.getServices().values().iterator().next();
            assertNotNull(service);
            Port port2 = service.getPort("SimplePort");
            assertNotNull(port2);
            SOAPAddress address = (SOAPAddress) port2.getExtensibilityElements().get(0);
            assertNotNull(address);
            assertEquals("http://localhost:" + port + "/test/service", address.getLocationURI());

        } finally {
            kernel.stopGBean(stack);
            kernel.shutdown();
        }
    }

    public void testAxisStyleMessage() throws Exception {
        Kernel kernel = new Kernel("wstest");
        kernel.boot();

        URI wsdlURI = new URI("META-INF/wsdl/test-ejb.wsdl");
        JarFile jarFile = new JarFile("target/test-ejb-jar.jar");
        String ejbName = "SimpleStatelessSession";
        ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:target/test-ejb-jar.jar")}, getClass().getClassLoader());

        JavaServiceDesc serviceDesc = AxisServiceBuilder.createEJBServiceDesc(jarFile, ejbName, classLoader);

        ObjectName ejbContainer = MockEJBContainerGBean.addGBean(kernel, new File("target/test-ejb-jar").toURL(), "SimpleEJB", "org.openejb.test.simple.slsb.SimpleStatelessSessionEJB", "org.openejb.test.simple.slsb.SimpleStatelessSessionHome", "org.openejb.test.simple.slsb.SimpleStatelessSession", "org.openejb.test.simple.slsb.SimpleStatelessSessionLocalHome", "org.openejb.test.simple.slsb.SimpleStatelessSessionLocal", "org.openejb.test.simple.slsb.SimpleStatelessSessionEndpoint", classLoader);
        ObjectName listener = SoapHttpListenerGBean.addGBean(kernel, "HTTPSOAP");
        ObjectName wsContainer = WSContainerGBean.addGBean(kernel, "HTTPSOAP", ejbContainer, listener, new URI("/services/Simple"), wsdlURI, serviceDesc);
        ObjectName server = HttpServerGBean.addGBean(kernel, "HTTPSOAP", listener);
        ObjectName executor = buildExecutor(kernel);
        ObjectName stack = StandardServiceStackGBean.addGBean(kernel, "HTTPSOAP", 0, InetAddress.getByName("localhost"), null, null, null, executor, server);

        assertRunning(kernel, ejbContainer);
        assertRunning(kernel, wsContainer);
        assertRunning(kernel, listener);
        assertRunning(kernel, executor);
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

    public void testFixme(){
        
    }

    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        int state = ((Integer) kernel.getAttribute(objectName, "state")).intValue();
        assertEquals("should be running: " + objectName, State.RUNNING_INDEX, state);
    }

    private String getResult(InputStream responseStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        TestHandler handler = new TestHandler();
        saxParser.parse(responseStream, handler);
        return handler.result;
    }

    private ObjectName buildExecutor(Kernel kernel) throws Exception {
        ClassLoader cl = ThreadPool.class.getClassLoader();
        ObjectName executor = JMXUtil.getObjectName("openejb:name=ThreadPool");
        GBeanData gbean = new GBeanData(executor, ThreadPool.GBEAN_INFO);
        gbean.setAttribute("poolSize", new Integer(1));
        gbean.setAttribute("poolName", "Test");
        gbean.setAttribute("keepAliveTime", new Long(1000));
        gbean.setAttribute("classLoader", cl);
        kernel.loadGBean(gbean, cl);
        kernel.startGBean(executor);
        return executor;
    }
    
    private static class TestHandler extends DefaultHandler {
        String result;
        boolean found;
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("result")){
                found = true;
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if (!found){
                result = new String(ch, start, length);
            }
        }
    }
}
