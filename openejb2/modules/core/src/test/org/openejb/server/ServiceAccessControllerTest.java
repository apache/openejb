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

    public void testWrongExactIPAddressPermission1() throws Exception {
        try {
            ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.123.a");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testWrongExactIPAddressPermission2() throws Exception {
        try {
            ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.123.256");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testExactIPAddressPermission() throws Exception {
        ServiceAccessController.IPAddressPermission permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.123.124");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 124})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 125})));
    }

    public void testWrongStartWithIPAddressPermission1() throws Exception {
        try {
            ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.0.123.0");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testStartWithIPAddressPermission() throws Exception {
        ServiceAccessController.IPAddressPermission permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.0.0");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 124})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {121, 123, 123, 124})));
    }

    public void testFactorizedIPAddressPermission() throws Exception {
        ServiceAccessController.IPAddressPermission permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.123.{1,2,3}");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 1})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 2})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 3})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, 4})));
        
        permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.{1,2,3}");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 1, 1})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 2, 2})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 3, 3})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 4, 3})));
    }
    
    public void testNetmaskIPAddressPermission() throws Exception {
        ServiceAccessController.IPAddressPermission permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.123.254/31");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, (byte) 253})));

        permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.123.254/255.255.255.254");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {121, 122, 123, (byte) 253})));
    }
    
    public void testExactIPv6AddressPermission() throws Exception {
        ServiceAccessController.IPAddressPermission permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("101:102:103:104:105:106:107:108");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 8})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 9})));
    }

    public void testNetmaskIPv6AddressPermission() throws Exception {
        ServiceAccessController.IPAddressPermission permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("101:102:103:104:105:106:107:FFFE/127");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 253})));

        permission = ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("101:102:103:104:105:106:107:FFFE/FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFE");
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 254})));
        assertTrue(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 255})));
        assertFalse(permission.implies(InetAddress.getByAddress(new byte[] {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, (byte) 255, (byte) 253})));
    }

    public void testServiceOKWithConstructor() throws Exception {
        ServiceAccessController.IPAddressPermission[] masks = new ServiceAccessController.IPAddressPermission[] {
                ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.{56,57}")
        };
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(null, mockServerService, masks);
        
        executeTestServiceOK(mockServerService, controller);
    }
    
    public void testServiceNOK() throws Exception {
        ServiceAccessController.IPAddressPermission[] masks = new ServiceAccessController.IPAddressPermission[] {
                ServiceAccessController.IPAddressPermissionFactory.getIPAddressMask("121.122.{56,57}")
        };
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(null, mockServerService, masks);
        
        executeTestServiceNOK(controller);
    }

    public void testServiceOKWithInit() throws Exception {
        Properties properties = new Properties();
        properties.put("only_from", "121.122.{56,57}");
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(mockServerService);
        controller.init(properties);
        
        executeTestServiceOK(mockServerService, controller);
    }

    public void testServiceNOKWithInit() throws Exception {
        Properties properties = new Properties();
        properties.put("only_from", "121.122.{56,57}");
        
        MockServerService mockServerService = new MockServerService();
        ServiceAccessController controller = new ServiceAccessController(mockServerService);
        controller.init(properties);
        
        executeTestServiceOK(mockServerService, controller);
    }

    private void executeTestServiceOK(MockServerService mockServerService, ServiceAccessController controller) throws UnknownHostException, ServiceException, IOException {
        MockSocket mockSocket = new MockSocket(InetAddress.getByAddress(new byte[] {121, 122, 56, 123})); 
        controller.service(mockSocket);
        assertSame(mockSocket, mockServerService.socket);
        
        mockSocket = new MockSocket(InetAddress.getByAddress(new byte[] {121, 122, 57, 123})); 
        controller.service(mockSocket);
        assertSame(mockSocket, mockServerService.socket);
    }

    private void executeTestServiceNOK(ServiceAccessController controller) throws UnknownHostException, ServiceException, IOException {
        MockSocket mockSocket = new MockSocket(InetAddress.getByAddress(new byte[] {121, 122, 58, 123})); 
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