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
package org.openejb.nova.dispatch;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.openejb.nova.method.EJBInterfaceMethods;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class AbstractOperationFactory implements VirtualOperationFactory {
    private final Class beanClass;
    private final MethodSignature[] signatures;
    private final Map sigMap;
    private final VirtualOperation[] vtable;

    protected AbstractOperationFactory(Class beanClass, VirtualOperation[] vtable, MethodSignature[] signatures) {
        this.beanClass = beanClass;
        this.vtable = vtable;
        this.signatures = signatures;

        this.sigMap = new HashMap(signatures.length);
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            sigMap.put(signature, new Integer(i));
        }
    }

    public MethodSignature[] getSignatures() {
        return signatures;
    }

    public VirtualOperation[] getVTable() {
        return vtable;
    }

    public Map getHomeMap(Class interfaceClass) {
        assert (interfaceClass.isInterface());
        assert (EJBHome.class.isAssignableFrom(interfaceClass)) : interfaceClass.getName() + " does not extend EJBHome";

        return mapHomeInterface(interfaceClass, EJBInterfaceMethods.HOME_METHODS);
    }

    public Map getLocalHomeMap(Class interfaceClass) {
        assert (interfaceClass.isInterface());
        assert (EJBLocalHome.class.isAssignableFrom(interfaceClass)) : interfaceClass.getName() + " does not extend EJBLocalHome";

        return mapHomeInterface(interfaceClass, EJBInterfaceMethods.LOCALHOME_METHODS);
    }

    public Map getObjectMap(Class interfaceClass) {
        assert (interfaceClass.isInterface());
        assert (EJBObject.class.isAssignableFrom(interfaceClass)) : interfaceClass.getName() + " does not extend EJBObject";

        return mapObjectInterface(interfaceClass, EJBInterfaceMethods.OBJECT_METHODS);
    }

    public Map getLocalObjectMap(Class interfaceClass) {
        assert (interfaceClass.isInterface());
        assert (EJBLocalObject.class.isAssignableFrom(interfaceClass)) : interfaceClass.getName() + " does not extend EJBLocalObject";

        return mapObjectInterface(interfaceClass, EJBInterfaceMethods.LOCALOBJECT_METHODS);
    }

    private Map mapObjectInterface(Class interfaceClass, Set excludes) {
        Method[] ifMethods = interfaceClass.getMethods();
        Map map = new HashMap(ifMethods.length);
        for (int i = 0; i < ifMethods.length; i++) {
            Method ifMethod = ifMethods[i];
            if (excludes.contains(ifMethod)) {
                continue;
            }
            MethodSignature sig = new MethodSignature(beanClass.getName(), ifMethod);
            Integer index = (Integer) sigMap.get(sig);
            map.put(ifMethod, index);
        }
        return map;
    }

    private Map mapHomeInterface(Class interfaceClass, Set excludes) {
        Method[] ifMethods = interfaceClass.getMethods();
        Map map = new HashMap(ifMethods.length);
        for (int i = 0; i < ifMethods.length; i++) {
            Method ifMethod = ifMethods[i];
            if (excludes.contains(ifMethod)) {
                continue;
            }
            String methodName = ifMethod.getName();
            if (methodName.startsWith("create")) {
                methodName = "ejbCreate" + methodName.substring(6);
            } else if (methodName.startsWith("find")) {
                methodName = "ejbFind" + methodName.substring(4);
            } else {
                methodName = "ejbHome" + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
            }
            MethodSignature sig = new MethodSignature(beanClass.getName(), methodName, ifMethod.getParameterTypes());
            Integer index = (Integer) sigMap.get(sig);
            map.put(ifMethod, index);
        }
        return map;
    }
}
