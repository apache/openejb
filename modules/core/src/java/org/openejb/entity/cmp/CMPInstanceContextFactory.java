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

import java.io.InvalidClassException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.EntityBean;

import org.apache.geronimo.transaction.InstanceContext;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.InstanceContextFactory;
import org.openejb.proxy.EJBProxyFactory;
import org.tranql.identity.IdentityTransform;
import org.tranql.cache.CacheTable;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPInstanceContextFactory implements InstanceContextFactory, Serializable {
    private final Object containerId;
    private final EJBProxyFactory proxyFactory;
    private final CacheTable cacheTable;
    private final IdentityTransform primaryKeyTransform;
    private final Class beanClass;
    private final Map imap;
    private final InstanceOperation[] itable;
    private transient final Enhancer enhancer;

    public CMPInstanceContextFactory(Object containerId, EJBProxyFactory proxyFactory, CacheTable cacheTable, IdentityTransform primaryKeyTransform, Class beanClass, Map imap) throws ClassNotFoundException {
        this.containerId = containerId;
        this.proxyFactory = proxyFactory;
        this.cacheTable = cacheTable;
        this.primaryKeyTransform = primaryKeyTransform;
        this.beanClass = beanClass;
        this.imap = imap;

        // create a factory to generate concrete subclasses of the abstract cmp implementation class
        enhancer = new Enhancer();
        enhancer.setSuperclass(beanClass);
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setCallbackFilter(FILTER);
        enhancer.setUseFactory(false);
        Class enhancedClass = enhancer.createClass();

        FastClass fastClass = FastClass.create(enhancedClass);

        itable = new InstanceOperation[fastClass.getMaxIndex() + 1];
        for (Iterator iterator = imap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature signature = (MethodSignature) entry.getKey();
            InstanceOperation iop = (InstanceOperation) entry.getValue();
            itable[MethodHelper.getSuperIndex(enhancedClass, signature)] = iop;
        }
    }

    public synchronized InstanceContext newInstance() throws Exception {
        return new CMPInstanceContext(containerId, proxyFactory, itable, cacheTable, primaryKeyTransform, this);
    }

    public synchronized EntityBean createCMPBeanInstance(CMPInstanceContext instanceContext) {
        enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, instanceContext});
        return (EntityBean) enhancer.create();
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            if (Modifier.isAbstract(method.getModifiers())) {
                return 1;
            }
            return 0;
        }
    };

    private Object readResolve() throws ObjectStreamException {
        try {
            return new CMPInstanceContextFactory(containerId, proxyFactory, cacheTable, primaryKeyTransform, beanClass, imap);
        } catch (ClassNotFoundException e) {
            throw (InvalidClassException) new InvalidClassException("Cound not load method argument class").initCause(e);
        }
    }
}
