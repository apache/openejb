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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

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
    private InetAddress[] allowHosts;

    public ServiceAccessController(ServerService next) {
        this.next = next;
    }

    public ServiceAccessController(String name, ServerService next, InetAddress[] allowedHosts) {
        this.next = next;
        this.allowHosts = allowedHosts;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        // Check authorization
        checkHostsAuthorization(socket.getInetAddress(), socket.getLocalAddress());

        next.service(socket);
    }

    public InetAddress[] getAllowHosts() {
        return allowHosts;
    }

    public void setAllowHosts(InetAddress[] allowHosts) {
        this.allowHosts = allowHosts;
    }

    public void checkHostsAuthorization(InetAddress clientAddress, InetAddress serverAddress) throws SecurityException {
        // Authorization flag.  This starts out as unauthorized
        // and will stay that way unless a matching admin ip is
        // found.
        boolean authorized = false;

        // Check the client ip against the server ip. Hosts are
        // allowed to access themselves, so if these ips
        // match, the following for loop will be skipped.
        authorized = clientAddress.equals(serverAddress);

        for (int i = 0; !authorized && i < allowHosts.length; i++) {
            authorized = allowHosts[i].equals(clientAddress);
        }

        if (!authorized) {
            throw new SecurityException("Host " + clientAddress.getHostAddress() + " is not authorized to access this service.");
        }
    }

    private void parseAdminIPs(Properties props) throws ServiceException {
        LinkedList addresses = new LinkedList();

        try {
            InetAddress[] localIps = InetAddress.getAllByName("localhost");
            for (int i = 0; i < localIps.length; i++) {
                addresses.add(localIps[i]);
            }
        } catch (UnknownHostException e) {
            throw new ServiceException("Could not get localhost inet address", e);
        }

        String ipString = props.getProperty("only_from");
        if (ipString != null) {
            StringTokenizer st = new StringTokenizer(ipString, ",");
            while (st.hasMoreTokens()) {
                String address = null;
                InetAddress ip = null;
                try {
                    address = st.nextToken();
                    ip = InetAddress.getByName(address);
                    addresses.add(ip);
                } catch (Exception e) {
                    throw new ServiceException("Error parsing only_from ip addresses");
                }
            }
        }

        allowHosts = (InetAddress[]) addresses.toArray(new InetAddress[addresses.size()]);
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
}
