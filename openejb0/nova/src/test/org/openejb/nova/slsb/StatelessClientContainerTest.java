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
package org.openejb.nova.slsb;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.core.service.AbstractInterceptor;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import junit.framework.TestCase;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBLocalClientContainer;
import org.openejb.nova.EJBRemoteClientContainer;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessClientContainerTest extends TestCase {
    private EJBRemoteClientContainer remoteContainer;
    private EJBLocalClientContainer localContainer;

    public void XtestMetadata() throws Exception {
        EJBMetaData metaData = remoteContainer.getEJBHome().getEJBMetaData();
        assertTrue(metaData.isSession());
        assertTrue(metaData.isStatelessSession());
        assertEquals(MockHome.class, metaData.getHomeInterfaceClass());
        assertEquals(MockRemote.class, metaData.getRemoteInterfaceClass());
        EJBHome home = metaData.getEJBHome();
        assertTrue(home instanceof MockHome);
        try {
            PortableRemoteObject.narrow(home, MockHome.class);
        } catch (ClassCastException e) {
            fail("Unable to narrow home interface");
        }
        try {
            metaData.getPrimaryKeyClass();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // OK
        } catch (Throwable t) {
            fail("Expected IllegalStateException");
        }
    }

    public void XtestHomeInterface() throws Exception {
        MockHome home = (MockHome) remoteContainer.getEJBHome();
        assertTrue(home.create() instanceof MockRemote);
        try {
            home.remove(new Integer(1));
            fail("Expected RemoveException");
        } catch (RemoveException e) {
            // OK
        } catch (Throwable t) {
            fail("Expected RemoveException");
        }
        try {
            home.remove(new Handle() {
                public EJBObject getEJBObject() throws RemoteException {
                    return null;
                }
            });
            fail("Expected RemoteException");
        } catch (RemoteException e) {
            // OK
        } catch (Throwable t) {
            fail("Expected RemoteException");
        }
    }

    public void testLocalHomeInterface() {
        MockLocalHome localHome = (MockLocalHome) localContainer.getEJBLocalHome();
        try {
            localHome.remove(new Integer(1));
            fail("Expected RemoveException");
        } catch (RemoveException e) {
            // OK
        } catch (Throwable t) {
            fail("Expected RemoveException");
        }
    }

    public void XtestObjectInterface() throws Exception {
        MockHome home = (MockHome) remoteContainer.getEJBHome();
        MockRemote remote = home.create();
        assertTrue(home == remote.getEJBHome());
        assertTrue(remote.isIdentical(remote));
        assertTrue(remote.isIdentical(home.create()));
        try {
            remote.getPrimaryKey();
            fail("Expected RemoteException");
        } catch (RemoteException e) {
            // OK
        } catch (Throwable t) {
            fail("Expected RemoteException");
        }
        remote.remove();
    }

    public void testLocalInterface() throws Exception {
        MockLocalHome localHome = (MockLocalHome) localContainer.getEJBLocalHome();
        MockLocal local = localHome.create();
        assertTrue(localHome == local.getEJBLocalHome());
        assertTrue(local.isIdentical(local));
        assertTrue(local.isIdentical(localHome.create()));
        try {
            local.getPrimaryKey();
            fail("Expected EJBException");
        } catch (EJBException e) {
            // OK
        } catch (Throwable t) {
            fail("Expected EJBException");
        }
        local.remove();
    }

    public void XtestInvocation() throws Exception {
        MockHome home = (MockHome) remoteContainer.getEJBHome();
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
        try {
            remote.appException();
            fail("Expected AppException");
        } catch (AppException e) {
            // OK
        }
        try {
            remote.sysException();
            fail("Expected RemoteException");
        } catch (RemoteException e) {
            // OK
        }
    }

    public void XtestProxySpeed() throws Exception {
        Map map = new HashMap();
        map.put(MockLocal.class.getMethod("intMethod", new Class[]{Integer.TYPE}), new Integer(0));
        StatelessLocalClientContainer localContainer = new StatelessLocalClientContainer(MockLocalHome.class, map, MockLocal.class);
        localContainer.addInterceptor(new Interceptor() {
            public Interceptor getNext() {
                return null;
            }

            public InvocationResult invoke(Invocation invocation) throws Throwable {
                return new SimpleInvocationResult(true, new Integer(1));
            }

            public void setNext(Interceptor interceptor) throws IllegalStateException {
            }
        });
        MockLocalHome localHome = (MockLocalHome) localContainer.getEJBLocalHome();
        MockLocal local = localHome.create();
        assertEquals(1, local.intMethod(1));
        for (int i = 0; i < 1000000; i++) {
            local.intMethod(1);
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            local.intMethod(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Proxy: " + (end - start) / 100);
    }

    protected void setUp() throws Exception {
        super.setUp();
        URI uri = new URI("async://localhost:3434#1234");
        StatelessOperationFactory vopFactory = StatelessOperationFactory.newInstance(MockEJB.class);
        StatelessClientContainerFactory clientFactory = new StatelessClientContainerFactory(vopFactory, uri, MockHome.class, MockRemote.class, null, MockLocalHome.class, MockLocal.class);
        remoteContainer = clientFactory.getRemoteClient();
        localContainer = clientFactory.getLocalClient();
    }

    private class MockInterceptor extends AbstractInterceptor {
        public InvocationResult invoke(Invocation invocation) throws Throwable {
            EJBInvocation ejbInvocation = (EJBInvocation) invocation;
            int index = ejbInvocation.getMethodIndex();
            if (index == 1) {
                return new SimpleInvocationResult(true, new Integer(2));
            } else if (index == 2) {
                return new SimpleInvocationResult(false, new AppException("App Exception"));
            } else {
                throw new RuntimeException();
            }
        }
    }
}
