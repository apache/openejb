/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private Messages _messages = new Messages( "org.apache.openejb.server" );
    private Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.apache.openejb.server" );

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

    



