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
package org.openejb.server.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.admin.web.HttpHome;
import org.openejb.admin.web.HttpObject;
import org.openejb.server.EjbDaemon;
import org.openejb.util.Logger;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;

/** This is the main class for the web administration.  It takes care of the
 * processing from the browser, sockets and threading.
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 * @since 11/25/2001
 */
public class HttpDaemon implements Runnable{

    /** the tool kit for this HttpDaemon */
    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");
    Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.server.util.resources" );
    Vector           clientSockets  = new Vector();
    ServerSocket     serverSocket   = null;

    // The EJB Server Port
    int    listenPort = 4203;
    String ip   = "127.0.0.1";
    Properties props;
    EjbDaemon ejbd;
    InitialContext jndiContext;

    /** This creates a new instance of the HttpDaemon
     * @param ejbd The EjbDaemon to connect to
     */
    public HttpDaemon(EjbDaemon ejbd) {
        this.ejbd = ejbd;
    }

    /** Initalizes this instance and takes care of starting things up
     * @param props a properties instance for system properties
     * @throws Exception if an exeption is thrown
     */
    public void init(Properties props) throws Exception{

        props.putAll(System.getProperties());

        Properties properties = new Properties();
        properties.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "org.openejb.core.ivm.naming.InitContextFactory");
        jndiContext = new InitialContext(properties);

        SafeProperties safeProps = new SafeProperties(System.getProperties(), "HTTP Server");

        try{
            serverSocket = new ServerSocket(listenPort);
            //serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
        } catch (Exception e){
            System.out.println(
                "Cannot bind to the ip: "
                    + ip
                    + " and port: "
                    + listenPort
                    + ".  Received exception: "
                    + e.getClass().getName()
                    + ":"
                    + e.getMessage());
        }
    }

    // This class doesn't use its own namespace, it uses the
    // jndi context of OpenEJB
    boolean stop = false;

    /** Starts the HttpDaemon thread and does most of the work */
    public void run( ) {

        Socket socket = null;

        /**
         * The ObjectInputStream used to receive incoming messages from the client.
         */
        InputStream in = null;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        OutputStream out = null;

        InetAddress clientIP = null;
        while ( !stop ) {
            try {
                socket = serverSocket.accept();

                clientIP = socket.getInetAddress();
                InetAddress serverIP = serverSocket.getInetAddress();

                Thread.currentThread().setName(clientIP.getHostAddress());

                in  = socket.getInputStream();
                out = socket.getOutputStream();

                try {
                    EjbDaemon.checkHostsAdminAuthorization(clientIP, serverIP);

                    // This will not get called if a SecurityException was thrown
                processRequest(in, out);
                } catch (SecurityException e) {
                    HttpResponseImpl res =
                        HttpResponseImpl.createForbidden(clientIP.getHostAddress());
                    try {
                        res.writeMessage(out);
                    } catch (Throwable t2) {
                        t2.printStackTrace();
                    }
                    try {
                        out.close();
                        socket.close();
                    } catch (Exception dontCare) {}
                }

                // Exceptions should not be thrown from these methods
                // They should handle their own exceptions and clean
                // things up with the client accordingly.
            } catch ( Throwable e ) {
                logger.error( "Unexpected error", e );
                System.out.println("ERROR: " + clientIP.getHostAddress() + ": " + e.getMessage());
            } finally {
                try {
                    if ( out != null ) {
                        out.flush();
                        out.close();
                    }
                    if (in != null)
                        in.close();
                    if (socket != null)
                        socket.close();
                } catch ( Throwable t ){
                    logger.error(
                        "Encountered problem while closing connection with client: "
                            + t.getMessage());
                }
            }
        }
    }

    /** takes care of processing requests and creating the webadmin ejb's
     * @param in the input stream from the browser
     * @param out the output stream to the browser
     */
    public void processRequest(InputStream in, OutputStream out) {

        
        HttpRequestImpl req = new HttpRequestImpl();
        HttpResponseImpl res = new HttpResponseImpl();
        

        try {
            req.readMessage(in);
            res.setRequest(req);
        } catch (Throwable t) {
            t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Could not read the request.\n" + t.getClass().getName() + ":\n" + t.getMessage(),
                    t);
            try {
                res.writeMessage(out);
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
            return;
        }

        //System.out.println("[] read");
        URL uri = null;
        String file = null;

        try {
            uri = req.getURI();
            file = uri.getFile();
            int querry = file.indexOf("?");
            if (querry != -1) {
                file = file.substring(0, querry);
            }

            //System.out.println("[] file="+file);

        } catch (Throwable t) {
            t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Could not determine the module "
                        + file
                        + "\n"
                        + t.getClass().getName()
                        + ":\n"
                        + t.getMessage());
            try {
                res.writeMessage(out);
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
            return;
        }

        HttpObject httpObject = null;

        try {
            httpObject = getHttpObject(file);
            //System.out.println("[] module="+httpObject);
        } catch (Throwable t) {
            t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Could not load the module "
                        + file
                        + "\n"
                        + t.getClass().getName()
                        + ":\n"
                        + t.getMessage(),
                    t);
            //System.out.println("[] res="+res);
            try {
                res.writeMessage(out);
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
            return;
        }

        try {
            httpObject.onMessage(req, res);
        } catch (Throwable t) {
            t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Error occurred while executing the module "
                        + file
                        + "\n"
                        + t.getClass().getName()
                        + ":\n"
                        + t.getMessage(),
                    t);
            try {
                res.writeMessage(out);
            } catch (Throwable t2) {
                t2.printStackTrace();
            }

            return;
        }

        try {
            res.writeMessage(out);
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
    }

    /** gets an ejb object reference for use in <code>processRequest</code>
     * @param beanName the name of the ejb to look up
     * @throws IOException if an exception is thrown
     * @return an object reference of the ejb
     */
    protected HttpObject getHttpObject(String beanName) throws IOException {
        Object obj = null;

        //check for no name, add something here later
        if (beanName.equals("/")) {
            try {
                obj = jndiContext.lookup("Webadmin/Home");
            } catch (javax.naming.NamingException ne) {
                throw new IOException(ne.getMessage());
            }
        } else {
            try {
                obj = jndiContext.lookup(beanName);
            } catch (javax.naming.NameNotFoundException e) {
                try {
                    obj = jndiContext.lookup("httpd/DefaultBean");
                } catch (javax.naming.NamingException ne) {
                    throw new IOException(ne.getMessage());
                }
            } catch (javax.naming.NamingException e) {
                throw new IOException(e.getMessage());
            }
        }

        HttpHome ejbHome = (HttpHome) obj;
        HttpObject httpObject = null;

        try {
            httpObject = ejbHome.create();

            // 
            obj = org.openejb.util.proxy.ProxyManager.getInvocationHandler(httpObject);
            org.openejb.core.ivm.BaseEjbProxyHandler handler = null;
            handler = (org.openejb.core.ivm.BaseEjbProxyHandler) obj;
            handler.setIntraVmCopyMode(false);
        } catch (javax.ejb.CreateException cre) {
            throw new IOException(cre.getMessage());
        }

        return httpObject;
    }
    
    
    /**
     * @return Returns the listenPort.
     */
    public int getListenPort() {
        return listenPort;
    }

    /**
     * @param listenPort The listenPort to set.
     */
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }
}
