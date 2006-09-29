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
package org.apache.openejb.corba;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.omg.CORBA.ORB;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Properties;


/**
 * Starts the Sun transient cos naming service using NSORB.  This only not run
 * on a Java VM containing the Sun ORB classes.  Add the following to your plan
 * to use this service:
 * <p/>
 * <gbean name="NameServer" class="org.apache.openejb.corba.SunNameService">
 * <reference name="ServerInfo">
 * <module>geronimo/j2ee-system/${geronimo_version}/car</module>
 * <name>ServerInfo</name>
 * </reference>
 * <attribute name="dbDir">var/cosnaming.db</attribute>
 * <attribute name="port">2809</attribute>
 * </gbean>
 *
 * @version $Revision$ $Date$
 */
public class SunNameService implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(SunNameService.class);
    private final ORB orb;
    private final int port;

    protected SunNameService() {
        orb = null;
        port = -1;
    }

    public SunNameService(ServerInfo serverInfo, String dbDir, int port) throws Exception {
        this.port = port;

        File dir = serverInfo.resolveServer(dbDir);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        // This must be a system property, the Sun BootStrapActivation class only looks at the
        // system properties for this value
        System.setProperty("com.sun.CORBA.activation.DbDir", dir.getAbsolutePath());

        Properties properties = new Properties();

        // the transient name service is automatically started by the Sun NSORB
        properties.put("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.CosNaming.NSORB");

        String portString = Integer.toString(port);

        // causes the Sun orb to immedately activate and start the activation services
        properties.put("com.sun.CORBA.POA.ORBPersistentServerPort", portString);

        // this port must match the above entry so the orb can find its own name server
        properties.put("org.omg.CORBA.ORBInitialPort", portString);

        // create the orb
        orb = ORB.init(new String[0], properties);
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getAddress() {
        return new InetSocketAddress("0.0.0.0", getPort());
    }

    public void doStart() throws Exception {
        new Thread(new ORBRunner(orb), "ORBRunner").start();
        log.debug("Started transient CORBA name service on port " + port);
    }

    public void doStop() throws Exception {
        orb.destroy();
        log.debug("Stopped transient CORBA name service on port " + port);
    }

    public void doFail() {
        orb.destroy();
        log.warn("Failed transient CORBA name service on port " + port);
    }

    private static final class ORBRunner implements Runnable {
        private final ORB orb;

        public ORBRunner(ORB orb) {
            this.orb = orb;
        }

        public void run() {
            orb.run();
        }
    }

}
