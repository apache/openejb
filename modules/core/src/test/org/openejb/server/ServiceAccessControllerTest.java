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
package org.openejb.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import junit.framework.TestCase;

public class ServiceAccessControllerTest extends TestCase {

    public void testWrongIPAddressMask1() throws Exception {
        try {
            new ServiceAccessController.IPAddressMask("127.0.0.a");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testWrongIPAddressMask2() throws Exception {
        try {
            new ServiceAccessController.IPAddressMask("127.0.0.333");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIPAddressMaskOK() throws Exception {
        ServiceAccessController.IPAddressMask mask = new ServiceAccessController.IPAddressMask("123.*.56.*");
        boolean match = mask.implies(InetAddress.getByAddress(new byte[] {123, 123, 56, 123}));
        assertTrue(match);
    }
    
    public void testIPAddressMaskNOK() throws Exception {
        ServiceAccessController.IPAddressMask mask = new ServiceAccessController.IPAddressMask("123.*.56.*");
        boolean match = mask.implies(InetAddress.getByAddress(new byte[] {123, 123, 57, 123}));
        assertFalse(match);
    }
    
    public void testServiceOKWithConstructor() throws Exception {
        ServiceAccessController.IPAddressMask[] masks = new ServiceAccessController.IPAddressMask[] {
                new ServiceAccessController.IPAddressMask("123.*.56.*"),
                new ServiceAccessController.IPAddressMask("123.*.57.*")
        };
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(null, mockServerService, masks);
        
        executeTestServiceOK(mockServerService, controller);
    }
    
    public void testServiceNOK() throws Exception {
        ServiceAccessController.IPAddressMask[] masks = new ServiceAccessController.IPAddressMask[] {
                new ServiceAccessController.IPAddressMask("123.*.56.*"),
                new ServiceAccessController.IPAddressMask("123.*.57.*")
        };
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(null, mockServerService, masks);
        
        executeTestServiceNOK(controller);
    }

    public void testServiceOKWithInit() throws Exception {
        Properties properties = new Properties();
        properties.put("only_from", "123.*.56.*,123.*.57.*");
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(mockServerService);
        controller.init(properties);
        
        executeTestServiceOK(mockServerService, controller);
    }

    public void testServiceNOKWithInit() throws Exception {
        Properties properties = new Properties();
        properties.put("only_from", "123.*.56.*,123.*.57.*");
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(mockServerService);
        controller.init(properties);
        
        executeTestServiceOK(mockServerService, controller);
    }

    private void executeTestServiceOK(MockServerService mockServerService, ServiceAccessController controller) throws UnknownHostException, ServiceException, IOException {
        MockSocket mockSocket = new MockSocket(InetAddress.getByAddress(new byte[] {123, 123, 56, 123})); 
        controller.service(mockSocket);
        assertSame(mockSocket, mockServerService.socket);
        
        mockSocket = new MockSocket(InetAddress.getByAddress(new byte[] {123, 123, 57, 123})); 
        controller.service(mockSocket);
        assertSame(mockSocket, mockServerService.socket);
    }

    private void executeTestServiceNOK(ServiceAccessController controller) throws UnknownHostException, ServiceException, IOException {
        MockSocket mockSocket = new MockSocket(InetAddress.getByAddress(new byte[] {123, 123, 58, 123})); 
        try {
            controller.service(mockSocket);
            fail();
        } catch (SecurityException e) {
        }
    }

    private static class MockSocket extends Socket {
        private final InetAddress address;
        
        private MockSocket(InetAddress address) {
            this.address = address;
        }
        
        public InetAddress getInetAddress() {
            return address;
        }
    }
    
    private static class MockServerService implements ServerService {
        private Socket socket;
        
        public void init(Properties props) throws Exception {
        }
        
        public void start() throws ServiceException {
            throw new AssertionError();
        }
        
        public void stop() throws ServiceException {
            throw new AssertionError();
        }

        public String getIP() {
            throw new AssertionError();
        }
        
        public int getPort() {
            throw new AssertionError();
        }
        
        public void service(Socket socket) throws ServiceException, IOException {
            this.socket = socket;
        }

        public String getName() {
            throw new AssertionError();
        }
    }
}