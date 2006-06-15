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
package org.openejb.deployment;

import java.util.Map;
import javax.ejb.SessionContext;

import junit.framework.TestCase;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.deployment.slsb.MockEJB;
import org.openejb.EJBInstanceContext;

/**
 * @version $Revision$ $Date$
 */
public class StatelessSystemMethodIndicesTest extends TestCase {

    public void testSystemMethodIndices() throws Exception {
        StatelessContainerBuilder builder = new StatelessContainerBuilder();
        builder.setClassLoader(MockEJB.class.getClassLoader());
        Map vopMap = builder.buildVopMap(MockEJB.class);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        SystemMethodIndices systemMethodIndices = SystemMethodIndices.createSystemMethodIndices(signatures, "setSessionContext", new String(SessionContext.class.getName()), null);
        EJBInstanceContext ctx = MockEJBInstanceContext.INSTANCE;
        try {
            assertTrue(systemMethodIndices.getEjbActivateInvocation(ctx).getMethodIndex() == -1);
        } catch (AssertionError e) {
            //expected
        }
        try {
            assertTrue(systemMethodIndices.getEjbLoadInvocation(ctx).getMethodIndex() == -1);
        } catch (AssertionError e) {
            //expected
        }
        try {
            assertTrue(systemMethodIndices.getEjbPassivateInvocation(ctx).getMethodIndex() == -1);
        } catch (AssertionError e) {
            //expected
        }
        try {
            assertTrue(systemMethodIndices.getEjbStoreInvocation(ctx).getMethodIndex() == -1);
        } catch (AssertionError e) {
            //expected
        }
        assertTrue(systemMethodIndices.getSetContextInvocation(ctx, null).getMethodIndex() != -1);
        try {
            assertTrue(systemMethodIndices.getUnsetContextInvocation(ctx).getMethodIndex() == -1);
        } catch (AssertionError e) {
            //expected
        }
    }
}
