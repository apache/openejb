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
import java.util.LinkedHashMap;

import org.openejb.AbstractContainerBuilder;
import org.openejb.EJBComponentType;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;

/**
 * @version $Revision$ $Date$
 */
public class StatelessContainerBuilder extends AbstractContainerBuilder {
    protected int getEJBComponentType() {
        return EJBComponentType.STATELESS;
    }

    protected Object buildIt(boolean buildContainer) throws Exception {
        // get the bean class
        ClassLoader classLoader = getClassLoader();
        Class beanClass = classLoader.loadClass(getBeanClassName());

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // create and intitalize the interceptor builder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new StatelessInterceptorBuilder(), signatures, vtable);

        // build the instance factory
        StatelessInstanceContextFactory contextFactory = new StatelessInstanceContextFactory(getContainerId(), beanClass, getUserTransaction(), getUnshareableResources(), getApplicationManagedSecurityResources());
        StatelessInstanceFactory instanceFactory = new StatelessInstanceFactory(getComponentContext(), contextFactory, beanClass);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        if (buildContainer) {
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        } else {
            return createConfiguration(signatures, contextFactory, interceptorBuilder, pool);
        }
    }

    protected LinkedHashMap buildVopMap(Class beanClass) throws Exception {
        LinkedHashMap vopMap = new LinkedHashMap();

        Method setSessionContext = null;
        try {
            Class sessionContextClass = getClassLoader().loadClass("javax.ejb.SessionContext");
            setSessionContext = beanClass.getMethod("setSessionContext", new Class[]{sessionContextClass});
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement setSessionContext(javax.ejb.SessionContext)");
        }

        // add the create method
        vopMap.put(new InterfaceMethodSignature("create", true), new CreateMethod());

        // add the business methods
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }
            if (setSessionContext.equals(beanMethod)) {
                continue;
            }
            String name = beanMethod.getName();
            if (name.startsWith("ejb")) {
                continue;
            }
            MethodSignature signature = new MethodSignature(beanMethod);
            vopMap.put(
                    new InterfaceMethodSignature(signature, false),
                    new BusinessMethod(beanClass, signature));
        }

        return vopMap;
    }
}
