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

import java.util.Collection;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.NotSerializableException;
import java.io.WriteAbortedException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.*;
import org.openejb.client.*;
import org.openejb.server.EjbDaemon;
import org.openejb.client.proxy.*;
import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.EnvProps;
import org.openejb.spi.SecurityService;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class HttpDaemon implements Runnable{

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");

    Logger logger = new Logger( "OpenEJB" );

    Vector           clientSockets  = new Vector();
    ServerSocket     serverSocket   = null;

    // The EJB Server Port
    int    port = 4202;
    String ip   = "127.0.0.1";
    Properties props;
    EjbDaemon ejbd;

    public HttpDaemon(EjbDaemon ejbd) {
        this.ejbd = ejbd;
    }

    public void init(Properties props) throws Exception{

        props.putAll(System.getProperties());

        //SafeProperties safeProps = toolkit.getSafeProperties(props);

        //port = safeProps.getPropertyAsInt("openejb.server.port");
        //ip   = safeProps.getProperty("openejb.server.ip");
        
        try{
            serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
        } catch (Exception e){
            System.out.println("Cannot bind to the ip: "+ip+" and port: "+port+".  Received exception: "+ e.getClass().getName()+":"+ e.getMessage());
            System.exit(1);
        }
    }

    // This class doesn't use its own namespace, it uses the
    // jndi context of OpenEJB
    boolean stop = false;


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
                Thread.currentThread().setName(clientIP.getHostAddress());

                in  = socket.getInputStream();
                out = socket.getOutputStream();

                processRequest(in, out); 

                // Exceptions should not be thrown from these methods
                // They should handle their own exceptions and clean
                // things up with the client accordingly.
            } catch ( Throwable e ) {
                logger.error( "Unexpected error", e );
                //System.out.println("ERROR: "+clienntIP.getHostAddress()+": " +e.getMessage());
            } finally {
                try {
                    if ( out != null ) {
			out.flush();
			out.close();
		    }
                    if ( in != null ) in.close();
                    if ( socket != null ) socket.close();
                } catch ( Throwable t ){
                    logger.error("Encountered problem while closing connection with client: "+t.getMessage());
                }
            }
        }

    }
    
    private void replyWithFatalError(OutputStream out, Throwable error, String message){
//      logger.fatal(message, error);
//      RemoteException re = new RemoteException
//          ("The server has encountered a fatal error: "+message+" "+error);
//      EJBResponse res = new EJBResponse();
//      res.setResponse(EJB_ERROR, re);
//      try
//      {
//          res.writeExternal(out);
//      }
//      catch (java.io.IOException ie)
//      {
//          logger.error("Failed to write to EJBResponse", ie);
//      }
    }

    public void processRequest(InputStream in, OutputStream out) {

        HttpRequest req = new HttpRequest();
        HttpResponse res = new HttpResponse();

        try {
            req.readExternal( in );
        } catch (Throwable t) {
	    //replyWithFatalError(out, t, "Error caught during request processing");
            t.printStackTrace();
            return;
        }
        
        java.io.PrintWriter body = res.getPrintWriter();

        body.println("<html>");
        body.println("<body>");
        body.println("<br><br><br><br>");
        body.println("<h1>Hello World</h1>");
        body.println("</body>");
        body.println("</html>");
        
        try {
            res.writeExternal( out );
        } catch (Throwable t) {
	    //replyWithFatalError(out, t, "Error caught during request processing");
            t.printStackTrace();
            return;
        }
    }
}
