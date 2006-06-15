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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.openejb.EJBInstanceContext;
import org.openejb.EJBInvocation;
import org.openejb.EJBOperation;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;

/**
 * @version $Revision$ $Date$
 */
public class EJBCreateMethod implements VirtualOperation, Serializable {
    private static final InvocationResult NULL_RESULT = new SimpleInvocationResult(true, null);
    private static final MethodSignature CREATE_SIG = new MethodSignature("ejbCreate");
    private final Class beanClass;

    private final transient FastClass fastClass;
    private final transient int createIndex;

    public EJBCreateMethod(Class beanClass) {
        this.beanClass = beanClass;

        fastClass = FastClass.create(beanClass);
        Method javaMethod = CREATE_SIG.getMethod(beanClass);
        if (javaMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + CREATE_SIG);
        }
        createIndex = fastClass.getIndex(javaMethod.getName(), javaMethod.getParameterTypes());
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();

        // call create
        Object instance = ctx.getInstance();
        Object[] args = invocation.getArguments();
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            fastClass.invoke(createIndex, instance, args);
            return NULL_RESULT;
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception)t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
        }
    }

    private Object readResolve() {
        return new EJBCreateMethod(beanClass);
    }
}
