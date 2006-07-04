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
 *    please contact info@openejb.org.
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
package org.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;

import org.activeio.xnet.ServiceException;
import org.activeio.xnet.ServiceException;

/**
 */
public class TelnetServer implements org.activeio.xnet.ServerService {


    public void init(Properties props) throws Exception {
    }
    
    public void service(Socket socket) throws ServiceException, IOException {
        InputStream telnetIn = null;
        PrintStream telnetOut = null;
        
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            
            telnetIn = new TelnetInputStream(in, out);
            telnetOut = new TelnetPrintStream(out);
            
            telnetOut.println("OpenEJB Remote Server Console");
            telnetOut.println("type \'help\' for a list of commands");

            // TODO:1: Login
            //...need a security service first

            TextConsole shell = new TextConsole();
            shell.exec(telnetIn, telnetOut);
            
        } catch (Throwable t) {
            // TODO: log this
            //logger.error(t.getMessage());
            //t.printStackTrace();
        } finally {
            if (telnetIn != null)
                telnetIn.close();
            if (telnetOut != null)
                telnetOut.close();
            if (socket != null) socket.close();
            
        }
       }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "telnet";
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

}
