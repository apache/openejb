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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  The Server will call the following methods.
 *
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 *
 * All Daemon implementations must have a no argument
 * constructor.
 *
 */
public class ServiceAccessController implements ServerService {
    private final ServerService next;
    private IPAddressMask[] allowHosts;

    public ServiceAccessController(ServerService next) {
        this.next = next;
    }

    public ServiceAccessController(String name, ServerService next, IPAddressMask[] ipAddressMasks) {
        this.next = next;
        this.allowHosts = ipAddressMasks;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        // Check authorization
        checkHostsAuthorization(socket.getInetAddress(), socket.getLocalAddress());

        next.service(socket);
    }

    public IPAddressMask[] getAllowHosts() {
        return allowHosts;
    }

    public void setAllowHosts(IPAddressMask[] ipAddressMasks) {
        this.allowHosts = ipAddressMasks;
    }

    public void checkHostsAuthorization(InetAddress clientAddress, InetAddress serverAddress) throws SecurityException {
        // Check the client ip against the server ip. Hosts are
        // allowed to access themselves, so if these ips
        // match, the following for loop will be skipped.
        if (clientAddress.equals(serverAddress)) {
            return;
        }

        for (int i = 0; i < allowHosts.length; i++) {
            if (allowHosts[i].implies(clientAddress)) {
                return;
            }
        }

        throw new SecurityException("Host " + clientAddress.getHostAddress() + " is not authorized to access this service.");
    }

    private void parseAdminIPs(Properties props) throws ServiceException {
        LinkedList ipAddressMasksList = new LinkedList();

        try {
            InetAddress[] localIps = InetAddress.getAllByName("localhost");
            for (int i = 0; i < localIps.length; i++) {
                if (localIps[i] instanceof Inet4Address) {
                    ipAddressMasksList.add(new IPAddressMask(localIps[i].getHostAddress()));
                }
            }
        } catch (UnknownHostException e) {
            throw new ServiceException("Could not get localhost inet address", e);
        }

        String ipString = props.getProperty("only_from");
        if (ipString != null) {
            StringTokenizer st = new StringTokenizer(ipString, ",");
            while (st.hasMoreTokens()) {
                String mask = st.nextToken();
                ipAddressMasksList.add(new IPAddressMask(mask));
            }
        }

        allowHosts = (IPAddressMask[]) ipAddressMasksList.toArray(new IPAddressMask[ipAddressMasksList.size()]);
    }

    public void init(Properties props) throws Exception {
        parseAdminIPs(props);
        next.init(props);
    }

    public void start() throws ServiceException {
        next.start();
    }

    public void stop() throws ServiceException {
        next.stop();
    }

    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return next.getIP();
    }

    public int getPort() {
        return next.getPort();
    }
    
    public static class IPAddressMask implements Serializable {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^(\\*|\\d{1,3})\\.(\\*|\\d{1,3})\\.(\\*|\\d{1,3})\\.(\\*|\\d{1,3})$");

        private final String mask;
        private final byte[] byteMask;
        private final boolean[] definedBytes;
        
        public IPAddressMask(String mask) {
            this.mask = mask;
            
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }
            
            byteMask = new byte[4];
            definedBytes = new boolean[4];
            for (int i = 1; i < 5; i++) {
                String group = matcher.group(i);
                if (false == group.equals("*")) {
                    int value = Integer.parseInt(group);
                    if (value < 0 || 255 < value) {
                        throw new IllegalArgumentException("byte #" + i + " is not valid.");
                    }
                    byteMask[i - 1] = (byte) value;
                    definedBytes[i - 1] = true;
                }
            }
        }
        
        public String getMask() {
            return mask;
        }
        
        public boolean implies(InetAddress address) {
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < 4; i++) {
                if (definedBytes[i] && byteAddress[i] != byteMask[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public static class IPAddressMaskEditor extends PropertyEditorSupport {
        private IPAddressMask addressMask;
        
        public void setAsText(String text) throws IllegalArgumentException {
            addressMask = new IPAddressMask(text);
        }

        public Object getValue() {
            return addressMask;
        }
    }
}
