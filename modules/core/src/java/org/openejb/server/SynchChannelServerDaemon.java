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
import java.net.*;

import org.activeio.SynchChannel;
import org.activeio.SynchChannelServer;
import org.activeio.adapter.SynchChannelToSocketAdapter;
import org.activeio.net.SocketSynchChannelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;

public class SynchChannelServerDaemon implements GBeanLifecycle, Runnable {
    private static final Log log = LogFactory.getLog(SynchChannelServerDaemon.class);

    private final SocketService socketService;
    private InetAddress address;
    private int port;

    private int timeout;
    private String name;
    private URI bindURI;
    private SynchChannelServer server;

    public SynchChannelServerDaemon(SocketService socketService, InetAddress address, int port) {
        this(null, socketService, address, port);
    }

    public SynchChannelServerDaemon(String name, SocketService socketService, InetAddress address, int port) {
        this.name = name;
        if (socketService == null) {
            throw new IllegalArgumentException("socketService is null");
        }
        this.socketService = socketService;
        this.address = address;
        this.port = port;
        try {
            this.bindURI = new URI("uri", null, address.getHostName(), port, null,null,null);
        } catch (URISyntaxException e) {
            throw (IllegalArgumentException) new IllegalArgumentException().initCause(e);
        }
    }

    public void doStart() throws WaitingException, Exception {
        SocketSynchChannelFactory factory = new SocketSynchChannelFactory();
        server = null;

        try {
            server = factory.bindSynchChannel(bindURI);
            port = server.getConnectURI().getPort();
            address = InetAddress.getByName(server.getConnectURI().getHost());
            stopped = false;
//            server.setSoTimeout(timeout);
        } catch (Exception e) {
            throw new ServiceException("Service failed to open socket", e);
        }
        Thread thread = new Thread(this);
        thread.setName("service." + name + "@" + hashCode());
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void doStop() {
        stopped = true;
    }

    public synchronized void doFail() {
        doStop();
        if (server != null){
            server.dispose();
        }
    }

    public void setSoTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
    }

    public int getSoTimeout() throws IOException {
        return timeout;
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

    public void run() {
        while (!shouldStop()) {
            Socket socket = null;
            try {
                SynchChannel channel = (SynchChannel) server.accept(timeout);
                socket = new SynchChannelToSocketAdapter(channel);
                socket.setTcpNoDelay(true);
                if (!shouldStop()) {
                    // the server service is responsible
                    // for closing the socket.
                    this.socketService.service(socket);
                }
            } catch (SocketTimeoutException e) {
                // we don't really care
                log.debug("Socket timed-out", e);
            } catch (Throwable e) {
                log.error("Unexpected error", e);
            } finally {
                log.info("Processed");
            }
        }

        if (server != null) {
            try {
                server.dispose();
            } catch (Exception ioException) {
                log.debug("Error cleaning up socked", ioException);
            }
            server = null;
        }
    }

    private boolean stopped;

    public synchronized void stop() {
        stopped = true;
    }

    private synchronized boolean shouldStop() {
        return stopped;
    }
}

