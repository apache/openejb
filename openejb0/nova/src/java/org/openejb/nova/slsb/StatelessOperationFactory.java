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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import javax.ejb.SessionContext;

import net.sf.cglib.reflect.FastClass;

import org.openejb.nova.dispatch.AbstractOperationFactory;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessOperationFactory extends AbstractOperationFactory {
    public static StatelessOperationFactory newInstance(Class beanClass) {
        FastClass fastClass = FastClass.create(beanClass);

        Method[] methods = beanClass.getMethods();
        Method setSessionContext = null;
        try {
            setSessionContext = beanClass.getMethod("setSessionContext", new Class[]{SessionContext.class});
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement setSessionContext(javax.ejb.SessionContext)");
        }
        ArrayList sigList = new ArrayList(methods.length);
        ArrayList vopList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (Object.class == method.getDeclaringClass()) {
                continue;
            }
            if (setSessionContext.equals(method)) {
                continue;
            }
            String name = method.getName();
            if (name.startsWith("ejb")) {
                continue;
            }
            MethodSignature sig = new MethodSignature(beanClass.getName(), method);
            sigList.add(sig);
            vopList.add(new BusinessMethod(fastClass, fastClass.getIndex(name, method.getParameterTypes())));
        }
        MethodSignature[] signatures = (MethodSignature[]) sigList.toArray(new MethodSignature[0]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopList.toArray(new VirtualOperation[0]);

        return new StatelessOperationFactory(beanClass, vtable, signatures);
    }

    private StatelessOperationFactory(Class beanClass, VirtualOperation[] vtable, MethodSignature[] signatures) {
        super(beanClass, vtable, signatures);
    }

    public Map getHomeMap(Class interfaceClass) {
        throw new UnsupportedOperationException("Cannot get home interface map for Stateless SessionBean");
    }

    public Map getLocalHomeMap(Class interfaceClass) {
        throw new UnsupportedOperationException("Cannot get localhome interface map for Stateless SessionBean");
    }
}
