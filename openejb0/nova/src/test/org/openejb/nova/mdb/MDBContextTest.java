/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.mdb;

import java.net.URI;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.transaction.EJBUserTransaction;
import org.openejb.nova.util.ServerUtil;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class MDBContextTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=MockEJB");
    private URI uri;
    private MBeanServer mbServer;
    private MDBContainer container;
    private EJBContainerConfiguration config;
    private Session session;
    private static boolean cmt;
    private static boolean setSessionCalled;
    private static boolean ejbCreateCalled;
    private static boolean ejbRemoveCalled;
    private MockResourceAdapter resourceAdapter;

    public void XtestSessionContextCMT() throws Exception {
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.componentContext = new ComponentContextBuilder(null, null).buildContext(session);
        cmt = true;
        resourceAdapter = new MockResourceAdapter();
        container = new MDBContainer(config, new MockActivationSpec(resourceAdapter), "javax.jms.MessageListner");
        mbServer.registerMBean(container, CONTAINER_NAME);
        container.doStart();

        MDBInstanceFactory factory = new MDBInstanceFactory(container);

        resetFlags();
        MDBInstanceContext ctx = (MDBInstanceContext) factory.createInstance();
        assertTrue(setSessionCalled);
        assertTrue(ejbCreateCalled);
        assertFalse(ejbRemoveCalled);

        resetFlags();
        factory.destroyInstance(ctx);
        assertFalse(setSessionCalled);
        assertFalse(ejbCreateCalled);
        assertTrue(ejbRemoveCalled);
    }

    public void XtestSessionContextBMT() throws Exception {
        config.txnDemarcation = TransactionDemarcation.BEAN;
        config.userTransaction = new EJBUserTransaction();
        config.componentContext = new ComponentContextBuilder(null, config.userTransaction).buildContext(session);
        cmt = false;
        resourceAdapter = new MockResourceAdapter();
        container = new MDBContainer(config, new MockActivationSpec(resourceAdapter), "javax.jms.MessageListner");
        mbServer.registerMBean(container, CONTAINER_NAME);
        container.doStart();

        MDBInstanceFactory factory = new MDBInstanceFactory(container);

        resetFlags();
        MDBInstanceContext ctx = (MDBInstanceContext) factory.createInstance();
        assertTrue(setSessionCalled);
        assertTrue(ejbCreateCalled);
        assertFalse(ejbRemoveCalled);

        resetFlags();
        factory.destroyInstance(ctx);
        assertFalse(setSessionCalled);
        assertFalse(ejbCreateCalled);
        assertTrue(ejbRemoveCalled);
    }

    public void testDummy() {
    }

    private void resetFlags() {
        setSessionCalled = false;
        ejbCreateCalled = false;
        ejbRemoveCalled = false;
    }

    protected void setUp() throws Exception {
        super.setUp();

        mbServer = ServerUtil.newLocalServer();
        uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());

        session = new Session();

        config = new EJBContainerConfiguration();
        config.uri = uri;
        config.beanClassName = MockEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
    }

    protected void tearDown() throws Exception {
        ServerUtil.stopLocalServer(mbServer);
        super.tearDown();
    }

    public static interface MockHome extends EJBHome {
        void create() throws RemoteException;
    }

    public static interface MockRemote extends EJBObject {
    }

    public static interface MockLocalHome extends EJBLocalHome {
        void create();
    }

    public static interface MockLocal extends EJBLocalObject {
    }

    public static class MockEJB implements MessageDrivenBean, MessageListener {
        private MessageDrivenContext ctx;

        public void setMessageDrivenContext(MessageDrivenContext mdbContext) throws EJBException {
            ctx = mdbContext;
            assertTrue(ctx.getEJBHome() instanceof MockHome);
            assertTrue(ctx.getEJBLocalHome() instanceof MockLocalHome);
            try {
                ctx.getCallerPrincipal();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.isCallerInRole("");
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.getRollbackOnly();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.setRollbackOnly();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.getUserTransaction();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.getTimerService();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                assertNotNull(new InitialContext().lookup("java:comp/env"));
            } catch (NamingException e) {
                fail("Unable to loookup java:comp/env");
            }

            setSessionCalled = true;
        }

        public void ejbCreate() {
            assertTrue(ctx.getEJBHome() instanceof MockHome);
            assertTrue(ctx.getEJBLocalHome() instanceof MockLocalHome);
            try {
                ctx.getCallerPrincipal();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.isCallerInRole("");
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.getRollbackOnly();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.setRollbackOnly();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            if (cmt) {
                try {
                    ctx.getUserTransaction();
                    fail("Expected IllegalStateException");
                } catch (IllegalStateException e) {
                    // OK
                }
            } else {
                UserTransaction utx = ctx.getUserTransaction();
                try {
                    utx.begin();
                } catch (IllegalStateException e) {
                    // OK
                } catch (Throwable t) {
                    fail("Unexpected Throwable");
                }
            }
            try {
                assertNotNull(new InitialContext().lookup("java:comp/env"));
            } catch (NamingException e) {
                fail("Unable to loookup java:comp/env");
            }

            ejbCreateCalled = true;
        }

        public void ejbActivate() {
            fail("ejbActivate should never be called for a MDB");
        }

        public void ejbPassivate() {
            fail("ejbPassivate should never be called for a MDB");
        }

        public void ejbRemove() {
            assertTrue(ctx.getEJBHome() instanceof MockHome);
            assertTrue(ctx.getEJBLocalHome() instanceof MockLocalHome);
            try {
                ctx.getCallerPrincipal();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.isCallerInRole("");
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.getRollbackOnly();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            try {
                ctx.setRollbackOnly();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // OK
            }
            if (cmt) {
                try {
                    ctx.getUserTransaction();
                    fail("Expected IllegalStateException");
                } catch (IllegalStateException e) {
                    // OK
                }
            } else {
                UserTransaction utx = ctx.getUserTransaction();
                try {
                    utx.begin();
                } catch (IllegalStateException e) {
                    // OK
                } catch (Throwable t) {
                    fail("Unexpected Throwable");
                }
            }
            try {
                assertNotNull(new InitialContext().lookup("java:comp/env"));
            } catch (NamingException e) {
                fail("Unable to loookup java:comp/env");
            }

            ejbRemoveCalled = true;
        }

        /**
         * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
         */
        public void onMessage(Message arg0) {
            // TODO Auto-generated method stub
            
        }
    }
}
