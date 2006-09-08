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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.common.DeploymentException;
import org.openejb.InstanceContextFactory;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.BasicTimerService;
import org.tranql.cache.FaultHandler;
import org.tranql.identity.IdentityTransform;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
public class CMPInstanceContextFactory implements InstanceContextFactory, Serializable {
    private final Object containerId;
    private final CMP1Bridge cmp1Bridge;
    private final IdentityTransform primaryKeyTransform;
    private final FaultHandler loadFault;
    private final Class beanClass;
    private final Map imap;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private transient final InstanceOperation[] itable;
    private transient final Enhancer enhancer;
    private transient final FastClass beanFastClass;
    private transient EJBProxyFactory proxyFactory;
    private transient Interceptor systemChain;
    private transient SystemMethodIndices systemMethodIndices;
    private transient TransactionContextManager transactionContextManager;
    private transient BasicTimerService timerService;

    public CMPInstanceContextFactory(Object containerId, CMP1Bridge cmp1Bridge, IdentityTransform primaryKeyTransform, FaultHandler loadFault, Class beanClass, Map imap, Set unshareableResources, Set applicationManagedSecurityResources) throws DeploymentException {
        this.containerId = containerId;
        this.cmp1Bridge = cmp1Bridge;
        this.primaryKeyTransform = primaryKeyTransform;
        this.loadFault = loadFault;
        this.beanClass = beanClass;
        this.imap = imap;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;

        if (cmp1Bridge == null) {
            // create a factory to generate concrete subclasses of the abstract cmp implementation class
            enhancer = new Enhancer();
            enhancer.setSuperclass(beanClass);
            enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
            enhancer.setCallbackFilter(FILTER);
            enhancer.setUseFactory(false);
            Class enhancedClass = enhancer.createClass();

            beanFastClass = FastClass.create(enhancedClass);

            itable = new InstanceOperation[beanFastClass.getMaxIndex() + 1];
            for (Iterator iterator = imap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                MethodSignature signature = (MethodSignature) entry.getKey();
                InstanceOperation iop = (InstanceOperation) entry.getValue();
                int index = MethodHelper.getSuperIndex(enhancedClass, signature);
                if(index < 0) {
                    throw new DeploymentException("Based on the EJB configuration I expected to find a method "+beanClass.getName()+"."+signature.toString()+" but no such method was found");
                }
                itable[index] = iop;
            }
        } else {
            enhancer = null;
            itable = null;
            beanFastClass = FastClass.create(beanClass);
        }
    }

    public void setProxyFactory(EJBProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public void setSystemChain(Interceptor systemChain) {
        this.systemChain = systemChain;
    }

    public SystemMethodIndices setSignatures(InterfaceMethodSignature[] signatures) {
        systemMethodIndices = SystemMethodIndices.createSystemMethodIndices(signatures, "setEntityContext", EntityContext.class.getName(), "unsetEntityContext");
        return systemMethodIndices;
    }

    public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
        this.transactionContextManager = transactionContextManager;
    }

    public void setTimerService(BasicTimerService timerService) {
        this.timerService = timerService;
    }

    public synchronized InstanceContext newInstance() throws Exception {
        if (proxyFactory == null) {
            throw new IllegalStateException("ProxyFactory has not been set");
        }
        CMPMethodInterceptor cmpMethodInterceptor = new CMPMethodInterceptor(itable);
        EntityBean instance = createCMPBeanInstance(cmpMethodInterceptor);
        CMPInstanceContext context = new CMPInstanceContext(containerId,
                        proxyFactory,
                        instance,
                        cmp1Bridge,
                        loadFault,
                        primaryKeyTransform,
                        systemChain,
                        systemMethodIndices,
                        unshareableResources,
                        applicationManagedSecurityResources,
                        transactionContextManager,
                        timerService);
        cmpMethodInterceptor.setInstanceContext(context);
        return context;
    }

    private EntityBean createCMPBeanInstance(CMPMethodInterceptor cmpMethodInterceptor) {
        if (cmp1Bridge == null) {
            synchronized (this) {
                enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, cmpMethodInterceptor});
                return (EntityBean) enhancer.create();
            }
        } else {
            try {
                return (EntityBean) beanFastClass.newInstance();
            } catch (InvocationTargetException e) {
                throw new EJBException("Unable to create entity bean instance", e);
            }
        }
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            if (Modifier.isAbstract(method.getModifiers())) {
                return 1;
            }
            return 0;
        }
    };

    private Object readResolve() {
        try {
            return new CMPInstanceContextFactory(containerId, cmp1Bridge, primaryKeyTransform, loadFault, beanClass, imap, unshareableResources, applicationManagedSecurityResources);
        } catch (DeploymentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
