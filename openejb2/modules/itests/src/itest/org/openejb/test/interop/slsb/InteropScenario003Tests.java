/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.openejb.test.interop.slsb;

import javax.rmi.PortableRemoteObject;

import com.sun.corba.se.internal.util.JDKBridge;
import org.openorb.orb.rmi.DefaultORB;


/**
 * @version $Revision$ $Date$
 */
public class InteropScenario003Tests extends InteropTestClient {

    public InteropScenario003Tests() {
        super("InteropScenario003.");
    }

    public void testUnassigned() throws Exception {
        Object obj = initialContext.lookup("interop/003/InteropHome");

//        Object o = DefaultORB.getORB();
//        org.openorb.orb.core.ORB orb = (org.openorb.orb.core.ORB) DefaultORB.getORB();
//        org.apache.avalon.framework.logger.Logger log = (orb).getLogger().getChildLogger("ud");


//        String className = System.getProperty("javax.rmi.CORBA.UtilClass", "com.sun.corba.se.internal.POA.ShutdownUtilDelegate");
//        try {
//            JDKBridge.loadClass(className, null, null).newInstance();
//        } catch (ClassNotFoundException ex) {
//            fail("ClassNotFoundException: " + ex.toString());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            fail("Exception: " + ex.toString());
//        }
        interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
        interop = interopHome.create();

        assertEquals("FOO", interop.callRemote("FOO"));

        interop.remove();
    }

//    public void testDavidPublic() throws Exception {
//        Subject.doAs(this.public_david_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testAlanPublic() throws Exception {
//        Subject.doAs(this.public_alan_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testDainPublic() throws Exception {
//        Subject.doAs(this.public_dain_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testNoelPublic() throws Exception {
//        Subject.doAs(this.public_noel_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testGeirPublic() throws Exception {
//        Subject.doAs(this.public_geir_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testGeorgePublic() throws Exception {
//        Subject.doAs(this.public_george_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testGraciePublic() throws Exception {
//        Subject.doAs(this.public_gracie_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testDavidBlack() throws Exception {
//        Subject.doAs(this.black_david_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
//
//    public void testDainBlack() throws Exception {
//        Subject.doAs(this.black_dain_subject, new PrivilegedExceptionAction() {
//            public Object run() throws Exception {
//
//                Object obj = initialContext.lookup("interop/003/InteropHome");
//                interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
//                interop = interopHome.create();
//
//                assertEquals("FOO", interop.callRemote("FOO"));
//
//                interop.remove();
//
//                return null;
//            }
//        });
//    }
}
