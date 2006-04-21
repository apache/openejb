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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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

import java.io.*;
import java.net.*;
import java.util.*;
import org.openejb.*;
import org.openejb.util.SafeProperties;

/**
 * The Server will call the following methods.
 * 
 * newInstance() init( port, properties) start() stop()
 * 
 * All ServerService implementations must have a no argument constructor.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ServiceDaemon implements ServerService, Runnable {

    ServerService next;

    Properties props;
    String ip;
    int port;
    String name;

    ServerSocket serverSocket;

    /**
	 * We start out in a "stopped" state until someone calls the start method.
	 */
    boolean stop = true;

    public ServiceDaemon(ServerService next) {
        this.next = next;
    }

    /**
	 * Pulls out the access log information
	 * 
	 * @param props
	 * 
	 * @exception ServiceException
	 */
    public void init(Properties props) throws Exception {
        // Do our stuff
        this.props = props;

        String p = props.getProperty("port");
        ip = props.getProperty("bind");
        name = props.getProperty("name");

        port = Integer.parseInt(p);
        // Then call the next guy
        next.init(props);
    }

    public void start() throws ServiceException {
        synchronized (this) {
            // Don't bother if we are already started/starting
            if (!stop)
                return;

            stop = false;
            // Do our stuff
            try {
                serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
                port = serverSocket.getLocalPort();
                ip = serverSocket.getInetAddress().getHostAddress();

                Thread d = new Thread(this);
                d.setName("service." + next.getName() + "@" + d.hashCode());
                d.setDaemon(true);
                d.start();
            } catch (Exception e) {
                throw new ServiceException("Service failed to start.", e);
                //e.printStackTrace();
            }

            // Then call the next guy
            next.start();
        }
    }

    public void stop() throws ServiceException {
        // Do our stuff
        synchronized (this) {
            // Don't bother if we are already stopped/stopping
            if (stop)
                return;

            //System.out.println("[] sending stop signal");
            stop = true;
            try {
                this.notifyAll();
            } catch (Throwable t) {
                t.printStackTrace();
                //logger.error("Unable to notify the server thread to stop.
				// Received exception: "+t.getClass().getName()+" :
				// "+t.getMessage());
            }
            // Then call the next guy
            next.stop();
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    public synchronized void service(final Socket socket)
        throws ServiceException, IOException {
        Thread d = new Thread(new Runnable() {
            public void run() {
                try {
                    next.service(socket);
                } catch (SecurityException e) {
                    //logger.error( "Security error: "+ e.getMessage() );
                } catch (Throwable e) {
                    //logger.error( "Unexpected error", e );

                } finally {
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (Throwable t) {
                        //logger.error("Encountered problem while closing
						// connection with client: "+t.getMessage());
                    }
                }
            }
        });
        d.setDaemon(true);
        d.start();
    }

    /**
	 * Gets the name of the service. Used for display purposes only
	 */
    public String getName() {
        return name;
    }

    /**
	 * Gets the ip number that the daemon is listening on.
	 */
    public String getIP() {
        return ip;
    }

    /**
	 * Gets the port number that the daemon is listening on.
	 */
    public int getPort() {
        return port;
    }

    public void run() {

        Socket socket = null;

        while (!stop) {
            try {
                socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                if (!stop) service(socket);
            } catch (SecurityException e) {
                //logger.error( "Security error: "+ e.getMessage() );
            } catch (Throwable e) {
                //logger.error( "Unexpected error", e );

            } 
        }
    }
}
