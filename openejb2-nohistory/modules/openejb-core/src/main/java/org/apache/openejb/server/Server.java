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
 * $Id: Server.java 444993 2004-10-25 09:55:08Z dblevins $
 */
package org.apache.openejb.server;

import java.util.Properties;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

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
public class Server implements org.apache.openejb.spi.Service {

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");
    private Messages _messages = new Messages( "org.openejb.server" );
    private Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server" );

    Properties props;

    static Server server;

    public static Server getServer() {
        if ( server == null ) {
            server = new Server();
        }

        return server;
    }

    public void init(java.util.Properties props) throws Exception {
        this.props = props;

        System.out.println( _messages.message( "ejbdaemon.startup" ) );

        props.put("openejb.nobanner", "true");

        OpenEJB.init(props, new ServerFederation());

//        System.out.println("[init] OpenEJB Remote Server");
        ServiceManager manager = ServiceManager.getManager();
        manager.init();
        manager.start();

    }
}

    



