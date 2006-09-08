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
package org.openejb.slsb;

import java.lang.reflect.Method;
import java.util.HashSet;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Timer;

import junit.framework.TestCase;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.SystemMethodIndices;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class InvocationTest extends TestCase {
    private BusinessMethod bizMethod;
    private FastClass fastClass;
    private int index;
    private final static InterfaceMethodSignature[] signatures = new InterfaceMethodSignature[] {
        new InterfaceMethodSignature("ejbActivate", false),
        new InterfaceMethodSignature("ejbLoad", false),
        new InterfaceMethodSignature("ejbPassivate", false),
        new InterfaceMethodSignature("ejbStore", false),
        new InterfaceMethodSignature("ejbCreate", false),
        new InterfaceMethodSignature("ejbRemove", false),
        new InterfaceMethodSignature("ejbTimeout", new Class[] {Timer.class}, false),
        new InterfaceMethodSignature("setSessionContext", new Class[] {SessionContext.class}, false),
    };

    public void testMethodInvoke() throws Exception {
        MockEJB instance = new MockEJB();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000000; i++) {
            instance.intMethod(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Method: "  + ((end - start) * 1000000.0 / 1000000000) + "ns");
    }

    public void testReflectionInvoke() throws Exception {
        Object instance = new MockEJB();
        Object[] args = {new Integer(1)};
        Method m = MockEJB.class.getMethod("intMethod", new Class[]{Integer.TYPE});
        m.invoke(instance, args);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            m.invoke(instance, args);
        }
        long end = System.currentTimeMillis();
        System.out.println("Reflection: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    public void testDirectInvoke() throws Exception {
        Object instance = new MockEJB();
        Object[] args = {new Integer(1)};
        fastClass.invoke(index, instance, args);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            fastClass.invoke(index, instance, args);
        }
        long end = System.currentTimeMillis();
        System.out.println("FastClass: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    public void testDirectInvokeWithResult() throws Exception {
        Object instance = new MockEJB();
        Object[] args = {new Integer(1)};
        fastClass.invoke(index, instance, args);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            new SimpleInvocationResult(true, fastClass.invoke(index, instance, args));
        }
        long end = System.currentTimeMillis();
        System.out.println("FastClass with result: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    public void testBizMethodInvoke() throws Throwable {
        SessionBean instance = new MockEJB();
        Object[] args = {new Integer(1)};
        EJBInvocation invocation = new EJBInvocationImpl(EJBInterfaceType.LOCAL, index, args);
        StatelessInstanceContext ctx = new StatelessInstanceContext(
                "containerID",
                instance,
                null,
//                transactionContextManager,
                null,
                null,
                SystemMethodIndices.createSystemMethodIndices(signatures, "setSessionContext", SessionContext.class.getName(), null),
                null, new HashSet(),
                new HashSet(),
                null);
        invocation.setEJBInstanceContext(ctx);
        bizMethod.execute(invocation);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            bizMethod.execute(invocation);
        }
        long end = System.currentTimeMillis();
        System.out.println("BizMethod: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    protected void setUp() throws Exception {
        super.setUp();
        fastClass = FastClass.create(MockEJB.class);
        index = fastClass.getIndex("intMethod", new Class[]{Integer.TYPE});
        bizMethod = new BusinessMethod(MockEJB.class, new MethodSignature("intMethod", new Class[]{Integer.TYPE}));
    }
}
