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
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

public class ServiceDaemon implements GBean {
    private static final Log log = LogFactory.getLog(ServiceDaemon.class);

    private final SocketService socketService;
    private final InetAddress inetAddress;
    private final int port;

    private SocketDaemon socketDaemon;

    public ServiceDaemon(SocketService socketService, InetAddress inetAddress, int port) {
        if (socketService == null) {
            throw new IllegalArgumentException("socketService is null");
        }
        this.socketService = socketService;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public synchronized void doStart() throws ServiceException {
        // Don't bother if we are already started/starting
        if (socketDaemon != null) {
            return;
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port, 20, inetAddress);
        } catch (Exception e) {
            throw new ServiceException("Service failed to open socket", e);
        }

        socketDaemon = new SocketDaemon(socketService, serverSocket);
        Thread thread = new Thread(socketDaemon);
        thread.setName("service." + getServiceName() + "@" + socketDaemon.hashCode());
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void doStop() {
        if (socketDaemon != null) {
            socketDaemon.stop();
            socketDaemon = null;
        }
    }

    public void doFail() {
        doStop();
    }

    public String getServiceName() {
        return socketService.getName();
    }

    /**
     * Gets the inetAddress number that the
     * daemon is listening on.
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * Gets the port number that the
     * daemon is listening on.
     */
    public int getPort() {
        return port;
    }

    private static class SocketDaemon implements Runnable {
        private SocketService serverService;
        private ServerSocket serverSocket;
        private boolean stopped;

        public SocketDaemon(SocketService serverService, ServerSocket serverSocket) {
            this.serverService = serverService;
            this.serverSocket = serverSocket;
            stopped = false;
        }

        public synchronized void stop() {
            stopped = true;
        }

        private synchronized boolean shouldStop() {
            return stopped;
        }

        public void run() {
            while (!shouldStop()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    if (!shouldStop()) {
                        // the server service is responsible 
                        // for closing the socket.
                        serverService.service(socket);
                    }
                } catch (Throwable e) {
                    log.error("Unexpected error", e);
                }
            }

            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    log.debug("Error cleaning up socked", ioException);
                }
                serverSocket = null;
            }
            serverService = null;
        }
    }

    //==== GBEAN METADATA ===================================================//
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServiceDaemon.class);

        infoFactory.addReference("SocketService", SocketService.class);
        infoFactory.addAttribute("InetAddress", InetAddress.class, true);
        infoFactory.addAttribute("Port", int.class, true);

        infoFactory.addAttribute("ServiceName", String.class, false);

        infoFactory.setConstructor(new String[]{"SocketService", "InetAddress", "Port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

