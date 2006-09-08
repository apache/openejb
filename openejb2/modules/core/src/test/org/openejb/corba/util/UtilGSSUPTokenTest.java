/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.corba.util;

import java.util.Properties;

import junit.framework.TestCase;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.GSSUP.InitialContextToken;

/**
 * @version $Rev:$ $Date:$
 */
public class UtilGSSUPTokenTest extends TestCase {

    public void testGSSUPTokenEncoding() throws Exception {
        // before we do anything make sure the sun orb is present
        try {
            getClass().getClassLoader().loadClass("com.sun.corba.se.internal.CosNaming.NSORB");
        } catch (ClassNotFoundException e) {
//            log.info("Sun orb is not present in this vm, so this test can't run");
            return;
        }
        // create the ORB
        Properties properties = new Properties();
        properties.put("org.omg.CORBA.ORBInitialPort", "8050");
        ORB orb = ORB.init(new String[0], properties);
        try {
            new Thread(new ORBRunner(orb), "ORBRunner").start();
            Util.setORB(orb);
            Codec codec = Util.getCodec();
            byte[] tokenBytes = Util.encodeGSSUPToken(orb, codec, "user", "password", "target");
            InitialContextToken token = new InitialContextToken();
            if (!Util.decodeGSSUPToken(codec, tokenBytes, token)) {
                fail("could not decode token bytes");
            }
            String userName = new String(token.username, "UTF-8");
            String password = new String(token.password, "UTF-8");
            String target = new String(token.target_name, "UTF-8");
            assertEquals(userName, "user");
            assertEquals(password, "password");
            assertEquals(target, "target");
        } finally {
            orb.destroy();
        }
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
