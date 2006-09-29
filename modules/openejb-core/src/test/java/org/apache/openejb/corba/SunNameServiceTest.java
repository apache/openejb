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

import java.util.Properties;
import java.io.File;

import com.sun.corba.se.internal.orbutil.ORBConstants;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * @version $Revision$ $Date$
 */
public class SunNameServiceTest extends TestCase {
    private static final Log log = LogFactory.getLog(SunNameServiceTest.class);
    private ORB orb;
    private SunNameService sunNameService;
    private static final String DB_DIR = "cosnaming.db";
    private File cosNamingDbDir;

    protected void setUp() throws Exception {
        // before we do anything make sure the sun orb is present
        try {
            getClass().getClassLoader().loadClass("com.sun.corba.se.internal.CosNaming.NSORB");
        } catch (ClassNotFoundException e) {
            log.info("Sun orb is not present in this vm, so this test can't run");
            return;
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        cosNamingDbDir = new File(tmpDir, DB_DIR);
        ServerInfo serverInfo = new BasicServerInfo(tmpDir);
        sunNameService = new SunNameService(serverInfo, DB_DIR, 8050);
        sunNameService.doStart();

        // create the ORB
        Properties properties = new Properties();
        properties.put("org.omg.CORBA.ORBInitialPort", "8050");
        orb = ORB.init(new String[0], properties);
        new Thread(new ORBRunner(orb), "ORBRunner").start();
    }

    protected void tearDown() throws Exception {
        if (sunNameService == null) {
            return;
        }
        orb.destroy();
        sunNameService.doStart();
        recursiveDelete(cosNamingDbDir);
    }

    public void testOrb() throws Exception {
        if (sunNameService == null) {
            return;
        }

        NamingContextExt ctx = NamingContextExtHelper.narrow(orb.resolve_initial_references(ORBConstants.PERSISTENT_NAME_SERVICE_NAME));
        NamingContextExt rootNamingContext = ctx;
        NameComponent name[] = ctx.to_name("foo/bar/baz");
        for (int i = 0; i < name.length; i++) {
            NameComponent nameComponent = name[i];
            ctx = NamingContextExtHelper.narrow(ctx.bind_new_context(new NameComponent[] {nameComponent}));
        }
        ctx.rebind(ctx.to_name("plan"), rootNamingContext);
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

    private static void recursiveDelete(File root) {
        if (root == null) {
            return;
        }

        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        recursiveDelete(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        root.delete();
    }
}
