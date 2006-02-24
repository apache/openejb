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
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ServiceAccessController implements ServerService {
    
    ServerService next;
    
    InetAddress[] allowedHosts;

    public ServiceAccessController(ServerService next){
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
        parseAdminIPs(props);

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

    public void service(Socket socket) throws ServiceException, IOException{
        // Do our stuff
        // Check authorization
        //checkHostsAuthorization(socket.getInetAddress(), socket.getLocalAddress());
        // Then call the next guy
        next.service(socket);
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    
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


    public void checkHostsAuthorization(InetAddress client, InetAddress server) throws SecurityException {
        // Authorization flag.  This starts out as unauthorized
        // and will stay that way unless a matching admin ip is
        // found.
        boolean authorized = false;

        // Check the client ip against the server ip. Hosts are
        // allowed to access themselves, so if these ips
        // match, the following for loop will be skipped.
        authorized = client.equals( server );

        for (int i=0; i < allowedHosts.length && !authorized; i++){
            authorized = allowedHosts[i].equals( client );
        }

        if ( !authorized ) {
            throw new SecurityException("Host "+client.getHostAddress()+" is not authorized to access this service.");
        }
    }

    private void parseAdminIPs(Properties props){
        try{

            Vector addresses = new Vector();

            InetAddress[] localIps = InetAddress.getAllByName("localhost");
            for (int i=0; i < localIps.length; i++){
                addresses.add( localIps[i] );
            }

            String ipString = props.getProperty("only_from");
            if (ipString != null) {
                StringTokenizer st = new StringTokenizer(ipString, " ,");
                while (st.hasMoreTokens()) {
                    String address = null;
                    InetAddress ip = null;
                    try{
                        address = st.nextToken();
                        ip = InetAddress.getByName(address);
                        addresses.add( ip );
                    } catch (Exception e){
                        //logger.error("Unable to apply the address ["+address+"] to the list of valid admin hosts: "+e.getMessage());
                    }
                }
            }

            allowedHosts = new InetAddress[ addresses.size() ];
            addresses.copyInto( allowedHosts );

        } catch (Exception e){
            //logger.error("Unable to create the list of valid hosts: "+e.getMessage());
        }
    }

}
