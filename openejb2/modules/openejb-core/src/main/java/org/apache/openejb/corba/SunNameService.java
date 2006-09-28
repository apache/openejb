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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: SunNameService.java 446010 2006-02-19 01:46:33Z gdamour $
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
 * <gbean name="NameServer" class="org.openejb.corba.SunNameService">
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
