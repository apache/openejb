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
package org.openejb.nova.entity.cmp;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.ejb.EntityContext;

import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Callbacks;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;

import org.openejb.nova.dispatch.AbstractOperationFactory;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.entity.BusinessMethod;
import org.openejb.nova.entity.HomeMethod;
import org.openejb.nova.persistence.QueryCommand;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPOperationFactory extends AbstractOperationFactory {
    private final InstanceOperation itable[];

    public static CMPOperationFactory newInstance(CMPEntityContainer container, CMPQuery[] queries, CMPCommandFactory persistenceFactory, String[] fieldNames) {
        Class beanClass = container.getBeanClass();
        Factory factory = Enhancer.create(beanClass, new Class[0], FILTER, new SimpleCallbacks());
        Class enhancedClass = factory.getClass();

        FastClass fastClass = FastClass.create(enhancedClass);

        // get the context set unset method objects
        Method setEntityContext;
        Method unsetEntityContext;
        try {
            setEntityContext = beanClass.getMethod("setEntityContext", new Class[]{EntityContext.class});
            unsetEntityContext = beanClass.getMethod("unsetEntityContext", null);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement javax.ejb.EntityBean");
        }

        // Build the VirtualOperations for business methods defined by the EJB implementation
        Method[] beanMethods = beanClass.getMethods();
        ArrayList sigList = new ArrayList(beanMethods.length);
        ArrayList vopList = new ArrayList(beanMethods.length);
        InstanceOperation[] itable = new InstanceOperation[fastClass.getMaxIndex() + 1];
        HashMap signatureMap = new HashMap(beanMethods.length);
        int vopId = 0;
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
            int index = fastClass.getIndex(name, beanMethod.getParameterTypes());
            VirtualOperation vop;

            if (name.startsWith("ejbCreate")) {
                // ejbCreate vop needs a reference to the ejbPostCreate method
                int postCreateIndex = fastClass.getIndex("ejbPostCreate" + name.substring(9), beanMethod.getParameterTypes());
                vop = new CMPCreateMethod(container, fastClass, index, postCreateIndex, persistenceFactory.getUpdateCommand(signature), fieldNames.length);
            } else if (name.startsWith("ejbHome")) {
                vop = new HomeMethod(fastClass, index);
            } else if (name.equals("ejbRemove")) {
                vop = new CMPRemoveMethod(fastClass, index, persistenceFactory.getUpdateCommand(signature));
            } else if (name.startsWith("ejb")) {
                continue;
            } else {
                vop = new BusinessMethod(fastClass, index);
            }

            sigList.add(signature);
            vopList.add(vop);
            signatureMap.put(signature, new Integer(vopId++));
        }

        for (int i = 0; i < queries.length; i++) {
            CMPQuery query = queries[i];
            MethodSignature signature = query.getSignature();
            if (!(signature.getMethodName().startsWith("ejbFind") || signature.getMethodName().startsWith("ejbSelect"))) {
                continue;
            }
            QueryCommand queryCommand = persistenceFactory.getQueryCommand(signature);
            boolean multiValue = query.isMultiValue();

            String returnSchemaName = query.getReturnSchemaName();
            if (!signature.getMethodName().startsWith("ejbSelect")) {
                VirtualOperation vop = new CMPFinder(persistenceFactory.getContainer(returnSchemaName), queryCommand, multiValue);
                sigList.add(signature);
                vopList.add(vop);
                signatureMap.put(signature, new Integer(vopId++));
            } else {
                int index;
                try {
                    index = MethodHelper.getSuperIndex(fastClass, signature);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Cannot load classes for "+signature);
                }
                CMPEntityContainer queryContainer = (returnSchemaName == null) ? null : persistenceFactory.getContainer(returnSchemaName);
                itable[index] = new CMPSelectMethod(queryContainer, queryCommand, multiValue, query.isLocal());
            }
        }


        MethodSignature[] signatures = (MethodSignature[]) sigList.toArray(new MethodSignature[0]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopList.toArray(new VirtualOperation[0]);

        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            try {
                String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = beanClass.getMethod("get"+baseName, null);
                int index = MethodHelper.getSuperIndex(fastClass, getter);
                itable[index] = new CMPFieldGetter(i);

                Method setter = beanClass.getMethod("set"+baseName, new Class[] {getter.getReturnType()});
                index = MethodHelper.getSuperIndex(fastClass, setter);
                itable[index] = new CMPFieldSetter(i);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing accessor for field "+fieldName);
            }
        }
        CMPInstanceContextFactory contextFactory = new CMPInstanceContextFactory(container, factory);
        return new CMPOperationFactory(vtable, signatures, itable, contextFactory);
    }

    public InstanceOperation[] getITable() {
        return itable;
    }

    private CMPOperationFactory(VirtualOperation[] vtable, MethodSignature[] signatures, InstanceOperation[] itable, CMPInstanceContextFactory contextFactory) {
        super(vtable, signatures);
        this.itable = itable;
        this.contextFactory = contextFactory;
    }

    private final CMPInstanceContextFactory contextFactory;

    public CMPInstanceContextFactory getInstanceContextFactory() {
        return contextFactory;
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            return (Modifier.isAbstract(method.getModifiers())) ? Callbacks.INTERCEPT : Callbacks.NO_OP;
        }
    };
}
