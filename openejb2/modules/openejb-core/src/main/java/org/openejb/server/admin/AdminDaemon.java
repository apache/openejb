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

import java.io.*;
import java.net.*;
import java.util.*;
import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceException;
import org.openejb.server.ServiceManager;
import org.activeio.xnet.ServiceException;
import org.openejb.client.RequestMethods;
import org.openejb.DeploymentIndex;

/**
 * This is the base class for orcistrating the other daemons 
 * which actually accept and react to calls coming in from 
 * different protocols or channels.
 * 
 * To perform this task, this class will
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 * 
 * 
 */
public class AdminDaemon implements ServerService {

    public void init(Properties props) throws Exception {
    }

    public AdminDaemon(DeploymentIndex index) {
    }

    public void service(Socket socket) throws ServiceException,IOException {
        InputStream in = null;
        InetAddress clientIP = null;
        
        try {
            in = socket.getInputStream();
            clientIP = socket.getInetAddress();


            byte requestType = (byte)in.read();
            
            if (requestType == -1) {return;}
            
            switch (requestType) {
                case RequestMethods.STOP_REQUEST_Quit:
                case RequestMethods.STOP_REQUEST_quit:
                case RequestMethods.STOP_REQUEST_Stop:
                case RequestMethods.STOP_REQUEST_stop:
                    ServiceManager.getManager().stop();
                    //stop(clientIP, serverSocket.getInetAddress());
                    
            }

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
        } catch ( SecurityException e ) {
            //logger.error( "Security error: "+ e.getMessage() );
        } catch ( Throwable e ) {
            //logger.error( "Unexpected error", e );
            //System.out.println("ERROR: "+clienntIP.getHostAddress()+": " +e.getMessage());
        } finally {
            try {
                if ( in     != null ) in.close();
                if ( socket != null ) socket.close();
            } catch ( Throwable t ){
                //logger.error("Encountered problem while closing connection with client: "+t.getMessage());
            }
        }
    }
    
    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    /**
     * Gets the port number that the
     * daemon is listening on.
     */
    public int getPort() {
        return 0;
    }
    
    public String getIP() {
        return "";
    }
    
    public String getName() {
        return "admin thread";
    }

}
