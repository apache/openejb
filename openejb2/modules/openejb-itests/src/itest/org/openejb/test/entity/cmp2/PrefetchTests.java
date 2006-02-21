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
package org.openejb.test.entity.cmp2;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.test.NamedTestCase;
import org.openejb.test.TestManager;
import org.openejb.test.entity.cmp2.prefetch.PrefetchFacadeHome;
import org.openejb.test.entity.cmp2.prefetch.PrefetchFacadeObject;


/**
 * @version $Revision$ $Date$
 */
public class PrefetchTests extends NamedTestCase {
    private InitialContext initialContext;
    private PrefetchFacadeHome ejbHome;

    public PrefetchTests() {
        super("PrefetchTests.");
    }

    public void testDoesNotOverwriteUpdates() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testDoesNotOverwriteUpdates();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testFinderPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testFinderPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testEJBPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testEJBPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testCMPPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testCMPPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testCMRPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testCMRPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);
    }

    protected void tearDown() throws Exception {
    }
}
