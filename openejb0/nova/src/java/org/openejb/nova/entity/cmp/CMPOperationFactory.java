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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.ejb.EntityContext;

import net.sf.cglib.reflect.FastClass;

import org.openejb.nova.dispatch.AbstractOperationFactory;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.entity.BusinessMethod;
import org.openejb.nova.entity.HomeMethod;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPOperationFactory extends AbstractOperationFactory {
    public static CMPOperationFactory newInstance(CMPEntityContainer container, CMPQuery[] queries, CMPCommandFactory persistenceFactory) {
        Class beanClass = container.getBeanClass();
        FastClass fastClass = FastClass.create(beanClass);
        String beanClassName = beanClass.getName();

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
        Integer remove = null;
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
            MethodSignature signature = new MethodSignature(beanClassName, beanMethod);
            int index = fastClass.getIndex(name, beanMethod.getParameterTypes());
            VirtualOperation vop;

            if (name.startsWith("ejbCreate")) {
                try {
                    // ejbCreate vop needs a reference to the ejbPostCreate method
                    Method postCreate = beanClass.getMethod("ejbPostCreate" + name.substring(9), beanMethod.getParameterTypes());
                    vop = new CMPCreateMethod(beanMethod, postCreate);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("No ejbPostCreate method found matching " + beanMethod);
                }
            } else if (name.startsWith("ejbHome")) {
                vop = new HomeMethod(fastClass, index);
            } else if (name.equals("ejbRemove")) {
                vop = new CMPRemoveMethod(fastClass, index);
                remove = new Integer(sigList.size());
            } else if (name.startsWith("ejb")) {
                continue;
            } else {
                vop = new BusinessMethod(fastClass, index);
            }

            sigList.add(signature);
            vopList.add(vop);
        }

        for (int i = 0; i < queries.length; i++) {
            CMPQuery query = queries[i];
            MethodSignature signature = query.getSignature();
            VirtualOperation vop = new CMPFinder(container, persistenceFactory.getFinder(signature), query.isMultiValue());
            sigList.add(signature);
            vopList.add(vop);
        }


        MethodSignature[] signatures = (MethodSignature[]) sigList.toArray(new MethodSignature[0]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopList.toArray(new VirtualOperation[0]);

        return new CMPOperationFactory(beanClass, vtable, signatures, remove);
    }

    /**
     * Index of the remove method in the vop table.  This gets special handling in entity, because
     * the parent class ignores the remove method.
     */
    private final Integer remove;

    private CMPOperationFactory(Class beanClass, VirtualOperation[] vtable, MethodSignature[] signatures, Integer remove) {
        super(beanClass, vtable, signatures);
        this.remove = remove;
    }

    /**
     * Builds a map from java.lang.reflect.Method vop index (Integer) for the Remote interface
     * @param interfaceClass the class to build the map for
     * @return the map from Method to Integer index
     */
    public Map getObjectMap(Class interfaceClass) {
        Map map = super.getObjectMap(interfaceClass);
        try {
            map.put(interfaceClass.getMethod("remove", null), remove);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bean does not define ejbRemove");
        }
        return map;
    }

    /**
     * Builds a map from java.lang.reflect.Method vop index (Integer) for the Local interface
     * @param interfaceClass the class to build the map for
     * @return the map from Method to Integer index
     */
    public Map getLocalObjectMap(Class interfaceClass) {
        Map map = super.getLocalObjectMap(interfaceClass);
        try {
            map.put(interfaceClass.getMethod("remove", null), remove);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bean does not define ejbRemove");
        }
        return map;
    }
}
