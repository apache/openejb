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
package org.openejb.entity.cmp;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openejb.AbstractContainerBuilder;
import org.openejb.EJBComponentType;
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.entity.BusinessMethod;
import org.openejb.entity.EntityInstanceFactory;
import org.openejb.entity.EntityInterceptorBuilder;
import org.openejb.entity.HomeMethod;
import org.openejb.proxy.EJBProxyFactory;
import org.tranql.cache.CacheTable;
import org.tranql.field.FieldTransform;
import org.tranql.identity.IdentityTransform;
import org.tranql.query.QueryCommand;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPContainerBuilder extends AbstractContainerBuilder {
    private IdentityTransform primaryKeyTransform = null;
    private IdentityTransform localProxyTransform = null;
    private IdentityTransform remoteProxyTransform = null;
    private CacheTable cacheTable = null;
    private QueryCommand[][] queryCommands = null;
    private MethodSignature[] queries = null;

    protected int getEJBComponentType() {
        return EJBComponentType.CMP_ENTITY;
    }

    protected Object buildIt(boolean buildContainer) throws Exception {
        // stuff we still need
        String[] cmpFields = null;
        String[] relations = null;

        // Stuff we must build
        FieldTransform[] cmpFieldTransforms = null;
        FieldTransform[] relationTransforms = null;

        // get the bean class
        Class beanClass = getClassLoader().loadClass(getBeanClassName());

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        EJBProxyFactory proxyFactory = createProxyFactory(signatures);

        // create and intitalize the interceptor builder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new EntityInterceptorBuilder(), signatures, vtable);

        // build the instance factory
        Map instanceMap = buildInstanceMap(beanClass, cmpFields, cmpFieldTransforms, relations, relationTransforms, queries, queryCommands);
        InstanceContextFactory contextFactory = new CMPInstanceContextFactory(getContainerId(), proxyFactory, primaryKeyTransform, beanClass, instanceMap);
        EntityInstanceFactory instanceFactory = new EntityInstanceFactory(getComponentContext(), contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        if (buildContainer) {
            return createContainer(proxyFactory, signatures, interceptorBuilder, pool);
        } else {
            return createConfiguration(proxyFactory, signatures, interceptorBuilder, pool);
        }
    }

    private Map buildInstanceMap(Class beanClass, String[] cmpFields, FieldTransform[] cmpFieldTransforms, String[] relations, FieldTransform[] relationTransforms, MethodSignature[] queries, QueryCommand[][] queryCommands) {
        Map instanceMap;
        instanceMap = new HashMap();

        // add the cmp-field getters and setters to the instance table
        addFieldOperations(instanceMap, beanClass, cmpFields, cmpFieldTransforms);

        // add the cmr getters and setters to the instance table.
        addFieldOperations(instanceMap, beanClass, relations, relationTransforms);


        // add the select methods
        for (int i = 0; i < queries.length; i++) {
            MethodSignature signature = queries[i];
            if (signature.getMethodName().startsWith("ejbSelect")) {
                // add select method to the instance table
                QueryCommand query = queryCommands[i][0];
                instanceMap.put(signature, new CMPSelectMethod(query));
            }
        }
        return instanceMap;
    }

    private void addFieldOperations(Map instanceMap, Class beanClass, String[] fields, FieldTransform[] transforms) {
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i];

            try {
                String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = beanClass.getMethod("get" + baseName, null);
                Method setter = beanClass.getMethod("set" + baseName, new Class[]{getter.getReturnType()});

//                instanceMap.put(new MethodSignature(getter), new CMPGetter(fieldName, transforms[i]));
//                instanceMap.put(new MethodSignature(setter), new CMPSetter(fieldName, transforms[i]));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing accessor for field " + fieldName);
            }
        }
    }

    protected LinkedHashMap buildVopMap(Class beanClass) throws Exception {
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

        // Build the VirtualOperations for business methods defined by the EJB implementation
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];

            // skip the rejects
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }
            if (setEntityContext.equals(beanMethod)) {
                continue;
            }
            if (unsetEntityContext.equals(beanMethod)) {
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
                        new CMPCreateMethod(
                                beanClass,
                                signature,
                                postCreateSignature,
                                cacheTable,
                                localProxyTransform,
                                remoteProxyTransform));
            } else if (name.startsWith("ejbHome")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new HomeMethod(beanClass, signature));
            } else if (name.equals("ejbRemove")) {
                // there are three valid ways to invoke remove on an entity bean

                // ejbObject.remove()
                vopMap.put(
                        new InterfaceMethodSignature("remove", false),
                        new CMPRemoveMethod(beanClass, signature, primaryKeyTransform));

                // ejbHome.remove(primaryKey)
                vopMap.put(
                        new InterfaceMethodSignature("ejbRemove", new Class[]{Object.class}, true),
                        new CMPRemoveMethod(beanClass, signature, primaryKeyTransform));

                // ejbHome.remove(handle)
                Class handleClass = getClassLoader().loadClass("javax.ejb.Handle");
                vopMap.put(
                        new InterfaceMethodSignature("ejbRemove", new Class[]{handleClass}, true),
                        new CMPRemoveMethod(beanClass, signature, primaryKeyTransform));
            } else if (name.startsWith("ejb")) {
                continue;
            } else {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new BusinessMethod(beanClass, signature));
            }
        }

        for (int i = 0; i < queries.length; i++) {
            MethodSignature signature = queries[i];
            if (signature.getMethodName().startsWith("ejbFind")) {
                // add the finder method to the virtual operation table
                QueryCommand localQuery = queryCommands[i][0];
                QueryCommand remoteQuery = queryCommands[i][1];
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new CMPFinder(localQuery, remoteQuery));
            }
        }
        return vopMap;
    }
}
