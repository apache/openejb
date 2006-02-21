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
import org.openejb.util.*;

/**
 *  The Server will call the following methods.
 * 
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 * 
 * All ServerService implementations must have a no argument 
 * constructor.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ServiceLogger implements ServerService {
    
    Messages messages = new Messages( "org.openejb.server.util.resources" );
    Logger logger;

    boolean logOnSuccess;
    boolean logOnFailure;

    ServerService next;

    public ServiceLogger(ServerService next){
        this.next = next;
    }

    /**
     * Pulls out the access log information
     * 
     * @param props
     * 
     * @exception ServiceException
     */
    public void init(Properties props) throws Exception{
        // Do our stuff
        String logCategory = "OpenEJB.server.service."+getName();

        logger = Logger.getInstance( logCategory, "org.openejb.server.util.resources" );
        // Then call the next guy
        next.init(props);
    }
    
    public void start() throws ServiceException{
        // Do our stuff
        
        // Then call the next guy
        next.start();
    }
    
    public void stop() throws ServiceException{
        // Do our stuff
        
        // Then call the next guy
        next.stop();
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    /**
     * log_on_success 
     * -----------------
     * Different information can be logged when a server starts: 
     * 
     * PID : the server's PID (if it's an internal xinetd service, the PID has then a value of 0) ; 
     * HOST : the client address ; 
     * USERID : the identity of the remote user, according to RFC1413 defining identification protocol; 
     * EXIT : the process exit status; 
     * DURATION : the session duration.  
     * 
     * log_on_failure 
     * ------------------
     * Here again, xinetd can log a lot of information when a server can't start, either by lack of resources or because of access rules: 
     * HOST, USERID : like above mentioned ; 
     * ATTEMPT : logs an access attempt. This an automatic option as soon as another value is provided; 
     * RECORD : logs every information available on the client. 
     * 
     * @param socket
     * 
     * @exception ServiceException
     * @exception IOException
     */
    public void service(Socket socket) throws ServiceException, IOException{
        // Fill this in more deeply later.
        InetAddress client = socket.getInetAddress();
        org.apache.log4j.MDC.put("HOST", client.getHostName());
        org.apache.log4j.MDC.put("SERVER", getName());

        try{
            //logIncoming();
//            logger.info("[request] "+socket.getPort()+" - "+client.getHostName());
            next.service(socket);
//            logSuccess();
        } catch (Exception e){
            logger.error("[failure] "+socket.getPort()+" - "+client.getHostName()+": "+e.getMessage());
            //logFailure(e);
            e.printStackTrace();
        }
    }

    private void logIncoming(){
        logger.info("incomming request");
    }

    private void logSuccess(){
        logger.info("successful request");
    }
    
    private void logFailure(Exception e){
        logger.error(e.getMessage());
    }

    /**
     * Gets the name of the service.
     * Used for display purposes only
     */ 
    public String getName(){
        return next.getName();
    }

    /**
     * Gets the ip number that the 
     * daemon is listening on.
     */
    public String getIP(){
        return next.getIP();
    }
    
    /**
     * Gets the port number that the 
     * daemon is listening on.
     */
    public int getPort(){
        return next.getPort();
    }

}
