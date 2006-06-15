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

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.openejb.EJBComponentType;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.cache.SimpleInstanceCache;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.sfsb.AfterBegin;
import org.openejb.sfsb.AfterCompletion;
import org.openejb.sfsb.BeforeCompletion;
import org.openejb.sfsb.BusinessMethod;
import org.openejb.sfsb.CreateMethod;
import org.openejb.sfsb.RemoveMethod;
import org.openejb.sfsb.StatefulInstanceContextFactory;
import org.openejb.sfsb.StatefulInstanceFactory;
import org.openejb.sfsb.StatefulInterceptorBuilder;
import org.openejb.slsb.dispatch.EJBActivateOperation;
import org.openejb.slsb.dispatch.EJBPassivateOperation;
import org.openejb.slsb.dispatch.SetSessionContextOperation;
import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Revision$ $Date$
 */
public class StatefulContainerBuilder extends AbstractContainerBuilder {
    protected int getEJBComponentType() {
        return EJBComponentType.STATEFUL;
    }

    protected Object buildIt(GBeanData gbeanData) throws Exception {
        // get the bean class
        ClassLoader classLoader = getClassLoader();
        Class beanClass = classLoader.loadClass(getBeanClassName());

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // build the instance factory
        StatefulInstanceContextFactory contextFactory = new StatefulInstanceContextFactory(getContainerId(), beanClass, getUserTransaction(), getUnshareableResources(), getApplicationManagedSecurityResources());
        StatefulInstanceFactory instanceFactory = new StatefulInstanceFactory(contextFactory);

        // create and intitalize the interceptor moduleBuilder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new StatefulInterceptorBuilder(), signatures, vtable);
        interceptorBuilder.setInstanceFactory(instanceFactory);
        interceptorBuilder.setInstanceCache(new SimpleInstanceCache());

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        if (gbeanData == null) {
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        } else {
            //stateful has no timers
            return createConfiguration(gbeanData, classLoader, signatures, contextFactory, interceptorBuilder, pool, null);
        }
    }

    protected LinkedHashMap buildVopMap(Class beanClass) throws Exception {
        LinkedHashMap vopMap = new LinkedHashMap();

        Method setSessionContext;
        try {
            Class sessionContextClass = getClassLoader().loadClass("javax.ejb.SessionContext");
            setSessionContext = beanClass.getMethod("setSessionContext", new Class[]{sessionContextClass});
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement setSessionContext(javax.ejb.SessionContext)");
        }

        // add the business methods
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }
            // create a VirtualOperation for the method (if the method is understood)
            String name = beanMethod.getName();
            MethodSignature signature = new MethodSignature(beanMethod);

            if (name.startsWith("ejbCreate")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new CreateMethod(beanClass, signature));
            } else if (name.equals("ejbRemove")) {
                // there are two valid ways to invoke remove on a stateful session bean

                // ejbObject.remove()
                vopMap.put(
                        new InterfaceMethodSignature("remove", false),
                        new RemoveMethod(beanClass, signature));

                // ejbHome.remove(handle)
                Class handleClass = getClassLoader().loadClass("javax.ejb.Handle");
                vopMap.put(
                        new InterfaceMethodSignature("remove", new Class[]{handleClass}, true),
                        new RemoveMethod(beanClass, signature));
            } else if (name.equals("ejbActivate")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        EJBActivateOperation.INSTANCE);
            } else if (name.equals("ejbPassivate")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        EJBPassivateOperation.INSTANCE);
            } else if (setSessionContext.equals(beanMethod)) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        SetSessionContextOperation.INSTANCE);
            } else if (name.equals("afterBegin") && signature.getParameterTypes().length == 0) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        AfterBegin.INSTANCE);
            } else if (name.equals("beforeCompletion") && signature.getParameterTypes().length == 0) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        BeforeCompletion.INSTANCE);
            } else if (name.equals("afterCompletion") && signature.getParameterTypes().length == 1 && signature.getParameterTypes()[0].equals(boolean.class.getName())) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        AfterCompletion.INSTANCE);
            } else if (name.startsWith("ejb")) {
                continue;
            } else {
                vopMap.put(
                        new InterfaceMethodSignature(signature, false),
                        new BusinessMethod(beanClass, signature));
            }

        }

        return vopMap;
    }
}
