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
package org.openejb.nova.entity.bmp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ejb.EntityBean;

import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;

import org.openejb.nova.EJBContainer;
import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBOperation;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.entity.EntityInstanceContext;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BMPCreateMethod implements VirtualOperation {
    private final Method createMethod;
    private final Method postCreateMethod;

    public BMPCreateMethod(Method createMethod, Method postCreateMethod) {
        this.createMethod = createMethod;
        this.postCreateMethod = postCreateMethod;
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        EntityInstanceContext ctx = (EntityInstanceContext) invocation.getEJBInstanceContext();

        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();

        Object id;
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            id = createMethod.invoke(instance, args);
        } catch (InvocationTargetException e) {
            ctx.setOperation(EJBOperation.INACTIVE);
            // unwrap the exception
            Throwable t = e.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return new SimpleInvocationResult(false, t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        }

        ctx.setId(id);

        try {
            ctx.setOperation(EJBOperation.EJBPOSTCREATE);
            postCreateMethod.invoke(instance, args);
        } catch (InvocationTargetException e) {
            // unwrap the exception
            Throwable t = e.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return new SimpleInvocationResult(false, t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
        }

        EJBInvocationType type = invocation.getType();
        EJBContainer container = ctx.getContainer();
        return new SimpleInvocationResult(true, getReference(type.isRemoteInvocation(), container, id));
    }

    private Object getReference(boolean remote, EJBContainer container, Object id) {
        if (remote) {
            return container.getEJBObject(id);
        } else {
            return container.getEJBLocalObject(id);
        }
    }

    public void start() throws Exception {
    }

    public void stop() {
    }
}
