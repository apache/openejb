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
import java.net.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class ServiceDaemon implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ServiceDaemon.class);

    private final SocketService socketService;
    private final InetAddress address;
    private int port;

    private SocketListener socketListener;
    private int timeout;
    private String name;

    public ServiceDaemon(SocketService socketService, InetAddress address, int port) {
        this(null, socketService, address, port);
    }
    public ServiceDaemon(String name, SocketService socketService, InetAddress address, int port) {
        this.name = name;
        if (socketService == null) {
            throw new IllegalArgumentException("socketService is null");
        }
        this.socketService = socketService;
        this.address = address;
        this.port = port;
    }

    public synchronized void doStart() throws ServiceException {
        // Don't bother if we are already started/starting
        if (socketListener != null) {
            return;
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port, 20, address);
            port = serverSocket.getLocalPort();
            serverSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            throw new ServiceException("Service failed to open socket", e);
        }

        socketListener = new SocketListener(socketService, serverSocket);
        Thread thread = new Thread(socketListener);
        thread.setName("service." + name + "@" + socketListener.hashCode());
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void doStop() {
        if (socketListener != null) {
            socketListener.stop();
            socketListener = null;
        }
    }

    public void doFail() {
        doStop();
    }

    public void setSoTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
        if (socketListener != null){
            socketListener.setSoTimeout(timeout);
        }
    }

    public int getSoTimeout() throws IOException {
        if (socketListener == null) return 0;
        return socketListener.getSoTimeout();
    }

    public String getServiceName() {
        return socketService.getName();
    }

    /**
     * Gets the inetAddress number that the
     * daemon is listening on.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Gets the port number that the
     * daemon is listening on.
     */
    public int getPort() {
        return port;
    }

    private static class SocketListener implements Runnable {
        private SocketService serverService;
        private ServerSocket serverSocket;
        private boolean stopped;

        public SocketListener(SocketService serverService, ServerSocket serverSocket) {
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
                } catch (SocketTimeoutException e) {
                    // we don't really care
                    log.debug("Socket timed-out",e);
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

        public void setSoTimeout(int timeout) throws SocketException {
            serverSocket.setSoTimeout(timeout);
        }

        public int getSoTimeout() throws IOException {
            return serverSocket.getSoTimeout();
        }
    }

    //==== GBEAN METADATA ===================================================//
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServiceDaemon.class);

        infoFactory.addReference("SocketService", SocketService.class);
        infoFactory.addAttribute("inetAddress", InetAddress.class, true);
        infoFactory.addAttribute("port", int.class, true);

        infoFactory.addAttribute("serviceName", String.class, false);

        infoFactory.setConstructor(new String[]{"SocketService", "inetAddress", "port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

