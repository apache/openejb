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

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.openejb.EJBComponentType;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.EJBTimeoutOperation;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.slsb.BusinessMethod;
import org.openejb.slsb.CreateMethod;
import org.openejb.slsb.EJBCreateMethod;
import org.openejb.slsb.HandlerChainConfiguration;
import org.openejb.slsb.RemoveMethod;
import org.openejb.slsb.StatelessInstanceContextFactory;
import org.openejb.slsb.StatelessInstanceFactory;
import org.openejb.slsb.StatelessInterceptorBuilder;
import org.openejb.slsb.dispatch.SetSessionContextOperation;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

/**
 * @version $Revision$ $Date$
 */
public class StatelessContainerBuilder extends AbstractContainerBuilder {

    private static final MethodSignature SET_SESSION_CONTEXT = new MethodSignature("setSessionContext", new String[]{"javax.ejb.SessionContext"});

    private HandlerChainConfiguration handlerChainConfiguration;

    protected int getEJBComponentType() {
        return EJBComponentType.STATELESS;
    }

    public HandlerChainConfiguration getHandlerChainConfiguration() {
        return handlerChainConfiguration;
    }

    public void setHandlerChainConfiguration(HandlerChainConfiguration handlerChainConfiguration) {
        this.handlerChainConfiguration = handlerChainConfiguration;
    }

    protected Object buildIt(GBeanData gbeanData) throws Exception {
        // get the bean class
        ClassLoader classLoader = getClassLoader();
        Class beanClass = classLoader.loadClass(getBeanClassName());

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // create and intitalize the interceptor moduleBuilder
        StatelessInterceptorBuilder interceptorBuilder = (StatelessInterceptorBuilder) initializeInterceptorBuilder(new StatelessInterceptorBuilder(), signatures, vtable);
        interceptorBuilder.setHandlerChainConfiguration(getHandlerChainConfiguration());

        // build the instance factory
        StatelessInstanceContextFactory contextFactory = new StatelessInstanceContextFactory(getContainerId(), beanClass, getUserTransaction(), getUnshareableResources(), getApplicationManagedSecurityResources());
        StatelessInstanceFactory instanceFactory = new StatelessInstanceFactory(contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        AbstractNameQuery timerName = getTimerName(beanClass);

        if (gbeanData == null) {
            //TODO add timer to sig.
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        } else {
            return createConfiguration(gbeanData, classLoader, signatures, contextFactory, interceptorBuilder, pool, timerName);
        }
    }

    protected LinkedHashMap buildVopMap(Class beanClass) throws Exception {
        LinkedHashMap vopMap = new LinkedHashMap();

        // ejbCreate... this is the method called by the pool to create a new instance
        vopMap.put(new InterfaceMethodSignature("ejbCreate", false), new EJBCreateMethod(beanClass));
        // create... this is the method that is called by the user
        vopMap.put(new InterfaceMethodSignature("create", true), new CreateMethod());
        // ejbRemove... this is the method called by the pool to destroy an instance
        vopMap.put(new InterfaceMethodSignature("ejbRemove", false), new RemoveMethod(beanClass));
        // ejbTimeout
        if (TimedObject.class.isAssignableFrom(beanClass)) {
            vopMap.put(new InterfaceMethodSignature("ejbTimeout", new String[]{Timer.class.getName()}, false),
                EJBTimeoutOperation.INSTANCE);
        }

        // add the business methods
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }

            if (beanMethod.getName().startsWith("ejb")) {
                continue;
            }

            MethodSignature signature = new MethodSignature(beanMethod);
            // match set session context sig down here since it can not be easily ignored like ejb* methods
            if (SET_SESSION_CONTEXT.equals(signature)) {
                vopMap.put(new InterfaceMethodSignature("setSessionContext", new String[]{"javax.ejb.SessionContext"}, false),
                        SetSessionContextOperation.INSTANCE);
            } else {
                vopMap.put(new InterfaceMethodSignature(signature, false),
                        new BusinessMethod(beanClass, signature));
            }
        }

        return vopMap;
    }
}
