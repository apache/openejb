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
package org.apache.openejb.test.security.slsb;

import javax.rmi.PortableRemoteObject;
import javax.security.auth.Subject;
import java.rmi.AccessException;
import java.security.PrivilegedExceptionAction;


/**
 * @version $Revision$ $Date$
 */
public class BasicStatelessScenario001Tests extends BasicStatelessTestClient {

    public BasicStatelessScenario001Tests() {
        super("BasicStatelessScenario001.");
    }

    public void testUnassigned() throws Exception {
        Object obj = initialContext.lookup("security/001/BasicStatelessHome");
        basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
        basicStateless = basicStatelessHome.create();

        basicStateless.allAccessMethod("FOO");

        basicStateless.allAccessMethod("FOO", "BAR");

        basicStateless.unassignedMethod("FOO");

        basicStateless.unassignedMethod("FOO", "BAR");

        try {
            basicStateless.lowSecurityMethod("FOO");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.lowSecurityMethod("FOO", "BAR");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.mediumSecurityMethod("FOO");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.mediumSecurityMethod("FOO", "BAR");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.highSecurityMethod("FOO");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.highSecurityMethod("FOO", "BAR");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.noAccessMethod("FOO");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        try {
            basicStateless.noAccessMethod("FOO", "BAR");
            fail("Should have thrown an access exception");
        } catch (AccessException e) {
        }

        basicStateless.remove();
    }

    public void testDavidPublic() throws Exception {
        Subject.doAs(this.public_david_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.mediumSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testAlanPublic() throws Exception {
        Subject.doAs(this.public_alan_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testDainPublic() throws Exception {
        Subject.doAs(this.public_dain_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testNoelPublic() throws Exception {
        Subject.doAs(this.public_noel_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.highSecurityMethod("FOO");

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testGeirPublic() throws Exception {
        Subject.doAs(this.public_geir_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.highSecurityMethod("FOO");

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testGeorgePublic() throws Exception {
        Subject.doAs(this.public_george_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testGraciePublic() throws Exception {
        Subject.doAs(this.public_gracie_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.lowSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.mediumSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testDavidBlack() throws Exception {
        Subject.doAs(this.black_david_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.highSecurityMethod("FOO");

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }

    public void testDainBlack() throws Exception {
        Subject.doAs(this.black_dain_subject, new PrivilegedExceptionAction() {
            public Object run() throws Exception {

                Object obj = initialContext.lookup("security/001/BasicStatelessHome");
                basicStatelessHome = (BasicStatelessHome) PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
                basicStateless = basicStatelessHome.create();

                basicStateless.allAccessMethod("FOO");

                basicStateless.allAccessMethod("FOO", "BAR");

                try {
                    basicStateless.unassignedMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.unassignedMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.lowSecurityMethod("FOO");

                try {
                    basicStateless.lowSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.mediumSecurityMethod("FOO");

                try {
                    basicStateless.mediumSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.highSecurityMethod("FOO");

                try {
                    basicStateless.highSecurityMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                try {
                    basicStateless.noAccessMethod("FOO", "BAR");
                    fail("Should have thrown an access exception");
                } catch (AccessException e) {
                }

                basicStateless.remove();

                return null;
            }
        });
    }
}
