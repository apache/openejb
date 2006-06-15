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
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.EJBTimeoutOperation;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.entity.BusinessMethod;
import org.openejb.entity.EntityInstanceFactory;
import org.openejb.entity.HomeMethod;
import org.openejb.entity.bmp.BMPCreateMethod;
import org.openejb.entity.bmp.BMPEntityInterceptorBuilder;
import org.openejb.entity.bmp.BMPFinderMethod;
import org.openejb.entity.bmp.BMPInstanceContextFactory;
import org.openejb.entity.bmp.BMPRemoveMethod;
import org.openejb.entity.dispatch.EJBActivateOperation;
import org.openejb.entity.dispatch.EJBLoadOperation;
import org.openejb.entity.dispatch.EJBPassivateOperation;
import org.openejb.entity.dispatch.EJBStoreOperation;
import org.openejb.entity.dispatch.SetEntityContextOperation;
import org.openejb.entity.dispatch.UnsetEntityContextOperation;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

/**
 * @version $Revision$ $Date$
 */
public class BMPContainerBuilder extends AbstractContainerBuilder {
    private boolean reentrant;

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    protected int getEJBComponentType() {
        return EJBComponentType.BMP_ENTITY;
    }

    protected InterceptorBuilder initializeInterceptorBuilder(BMPEntityInterceptorBuilder interceptorBuilder, InterfaceMethodSignature[] signatures, VirtualOperation[] vtable) {
        super.initializeInterceptorBuilder(interceptorBuilder, signatures, vtable);
        interceptorBuilder.setReentrant(reentrant);
        return interceptorBuilder;
    }

    protected Object buildIt(GBeanData gbeanData) throws Exception {
        // get the bean class
        ClassLoader classLoader = getClassLoader();
        Class beanClass = classLoader.loadClass(getBeanClassName());

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[])vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // create and intitalize the interceptor moduleBuilder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new BMPEntityInterceptorBuilder(), signatures, vtable);

        // build the context factory
        InstanceContextFactory contextFactory = new BMPInstanceContextFactory(getContainerId(), beanClass, getUnshareableResources(), getApplicationManagedSecurityResources());
        EntityInstanceFactory instanceFactory = new EntityInstanceFactory(contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        AbstractNameQuery timerName = getTimerName(beanClass);

        if (gbeanData == null) {
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        } else {
            return createConfiguration(gbeanData, classLoader, signatures, contextFactory, interceptorBuilder, pool, timerName);
        }
    }

   protected LinkedHashMap buildVopMap(final Class beanClass) throws Exception{
        LinkedHashMap vopMap = new LinkedHashMap();

        // get the context set unset method objects
        Method setEntityContext;
        Method unsetEntityContext;
        try {
            Class entityContextClass = getClassLoader().loadClass("javax.ejb.EntityContext");
            setEntityContext = beanClass.getMethod("setEntityContext", new Class[]{entityContextClass});
            unsetEntityContext = beanClass.getMethod("unsetEntityContext", null);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement javax.ejb.EntityBean");
        }

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            MethodSignature signature = new MethodSignature("ejbTimeout", new Class[]{Timer.class});
            vopMap.put(
                    MethodHelper.translateToInterface(signature)
                    , EJBTimeoutOperation.INSTANCE);
        }

        // Build the VirtualOperations for business methods defined by the EJB implementation
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];

            // skip the rejects
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }

            // create a VirtualOperation for the method (if the method is understood)
            String name = beanMethod.getName();

            MethodSignature signature = new MethodSignature(beanMethod);
            if (name.startsWith("ejbCreate")) {
                // ejbCreate vop needs a reference to the ejbPostCreate method
                MethodSignature postCreateSignature = new MethodSignature(
                        "ejbPostCreate" + name.substring(9),
                        beanMethod.getParameterTypes());
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new BMPCreateMethod(beanClass, signature, postCreateSignature));
            } else if (name.startsWith("ejbHome")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new HomeMethod(beanClass, signature));
            } else if (name.equals("ejbRemove")) {
                // there are three valid ways to invoke remove on an entity bean

                // ejbObject.remove()
                vopMap.put(
                        new InterfaceMethodSignature("remove", false),
                        new BMPRemoveMethod(beanClass, signature));

                // ejbHome.remove(primaryKey)
                vopMap.put(
                        new InterfaceMethodSignature("remove", new Class[]{Object.class}, true),
                        new BMPRemoveMethod(beanClass, signature));

                // ejbHome.remove(handle)
                Class handleClass = getClassLoader().loadClass("javax.ejb.Handle");
                vopMap.put(
                        new InterfaceMethodSignature("remove", new Class[]{handleClass}, true),
                        new BMPRemoveMethod(beanClass, signature));
            } else if (signature.getMethodName().startsWith("ejbFind")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new BMPFinderMethod(beanClass, signature));
            } else if (name.equals("ejbActivate")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature)
                        , EJBActivateOperation.INSTANCE);
            } else if (name.equals("ejbLoad")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature)
                        , EJBLoadOperation.INSTANCE);
            } else if (name.equals("ejbPassivate")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature)
                        , EJBPassivateOperation.INSTANCE);
            } else if (name.equals("ejbStore")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature)
                        , EJBStoreOperation.INSTANCE);
            } else if (setEntityContext.equals(beanMethod)) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature)
                        , SetEntityContextOperation.INSTANCE);
            } else if (unsetEntityContext.equals(beanMethod)) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature)
                        , UnsetEntityContextOperation.INSTANCE);
            } else if (name.startsWith("ejb")) {
                //TODO this shouldn't happen?
                continue;
            } else {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new BusinessMethod(beanClass, signature));
            }
        }
        return vopMap;
    }
}
