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
package org.apache.openejb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.Properties;

/**
 *
 */
public class SocketConnectionFactory implements ConnectionFactory{

    /**
     * Prepares the ConnectionFactory for use.  Called once right after
     * the ConnectionFactory is instantiated.
     *
     * @param props
     */
    public void init(Properties props) {
    }


    /**
     * Get a connection from the factory
     *
     * @return
     * @exception java.io.IOException
     */
    public Connection getConnection(ServerMetaData server) throws java.io.IOException {
        SocketConnection conn = new SocketConnection();
        conn.open( server );
        return conn;
    }

    /**
     *
     */
    class SocketConnection implements Connection{

        Socket       socket    = null;
        OutputStream socketOut = null;
        InputStream  socketIn  = null;

        protected void open(ServerMetaData server) throws IOException {
            /*-----------------------*/
            /* Open socket to server */
            /*-----------------------*/
            try{
                socket = new Socket(server.address, server.port);
                socket.setTcpNoDelay(true);
            } catch (IOException e){
                throw new IOException("Cannot access server: "+server.address+":"+server.port+" Exception: "+ e.getClass().getName() +" : "+ e.getMessage());

            } catch (SecurityException e){
                throw new IOException("Cannot access server: "+server.address+":"+server.port+" due to security restrictions in the current VM: "+ e.getClass().getName() +" : "+ e.getMessage());

            } catch (Throwable e){
                throw new IOException("Cannot access server: "+server.address+":"+server.port+" due to an unknown exception in the OpenEJB client: "+ e.getClass().getName() +" : "+ e.getMessage());
            }

        }

        public void close() throws IOException {
            try {
                if (socketOut != null) socketOut.close();
                if (socketIn  != null) socketIn.close();
                if (socket    != null) socket.close();
            } catch (Throwable t){
                throw new IOException("Error closing connection with server: "+t.getMessage() );
            }
        }

        public InputStream getInputStream() throws IOException {
            /*----------------------------------*/
            /* Open input streams               */
            /*----------------------------------*/
            try{
                socketIn = socket.getInputStream();
            } catch (StreamCorruptedException e){
                throw new IOException("Cannot open input stream to server, the stream has been corrupted: " + e.getClass().getName() +" : "+ e.getMessage());

            } catch (IOException e){
                throw new IOException("Cannot open input stream to server: " + e.getClass().getName() +" : "+ e.getMessage());

            } catch (Throwable e){
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() +" : "+ e.getMessage());
            }
            return socketIn;
        }

        public OutputStream getOuputStream() throws IOException {
            /*----------------------------------*/
            /* Openning output streams          */
            /*----------------------------------*/
            try{
                socketOut = socket.getOutputStream();
            } catch (IOException e){
                throw new IOException("Cannot open output stream to server: "+ e.getClass().getName() +" : "+ e.getMessage());

            } catch (Throwable e){
                throw new IOException("Cannot open output stream to server: "+ e.getClass().getName() +" : "+ e.getMessage());
            }
            return socketOut;
        }

    }
}

