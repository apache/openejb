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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EntityBean;

import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.EJBInvocation;
import org.openejb.EJBOperation;
import org.openejb.timer.TimerState;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;

import net.sf.cglib.reflect.FastClass;
import org.tranql.cache.CacheRow;
import org.tranql.cache.CacheTable;
import org.tranql.cache.DuplicateIdentityException;
import org.tranql.cache.InTxCache;
import org.tranql.identity.GlobalIdentity;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityTransform;
import org.tranql.pkgenerator.PrimaryKeyGeneratorDelegate;
import org.tranql.pkgenerator.PrimaryKeyGenerator;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPCreateMethod implements VirtualOperation, Serializable {
    private final Class beanClass;
    private final CMP1Bridge cmp1Bridge;
    private final MethodSignature createSignature;
    private final MethodSignature postCreateSignature;
    private final CacheTable cacheTable;
    private final IdentityDefiner identityDefiner;
    private final PrimaryKeyGenerator keyGenerator;
    private final IdentityTransform primaryKeyTransform;
    private final IdentityTransform localProxyTransform;
    private final IdentityTransform remoteProxyTransform;

    private final transient FastClass fastBeanClass;
    private final transient int createIndex;
    private final transient int postCreateIndex;

    public CMPCreateMethod(Class beanClass,
            CMP1Bridge cmp1Bridge,
            MethodSignature createSignature,
            MethodSignature postCreateSignature,
            CacheTable cacheTable,
            IdentityDefiner identityDefiner,
            PrimaryKeyGenerator keyGenerator,
            IdentityTransform primaryKeyTransform,
            IdentityTransform localProxyTransform,
            IdentityTransform remoteProxyTransform) {

        this.beanClass = beanClass;
        this.cmp1Bridge = cmp1Bridge;
        this.createSignature = createSignature;
        this.postCreateSignature = postCreateSignature;
        this.cacheTable = cacheTable;
        this.identityDefiner = identityDefiner;
        this.keyGenerator = keyGenerator;
        this.primaryKeyTransform = primaryKeyTransform;
        this.localProxyTransform = localProxyTransform;
        this.remoteProxyTransform = remoteProxyTransform;

        fastBeanClass = FastClass.create(beanClass);
        Method createMethod = createSignature.getMethod(beanClass);
        if (createMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + createSignature);
        }
        createIndex = fastBeanClass.getIndex(createMethod.getName(), createMethod.getParameterTypes());

        Method postCreateMethod = postCreateSignature.getMethod(beanClass);
        if (postCreateMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement post create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + postCreateSignature);
        }
        postCreateIndex = fastBeanClass.getIndex(postCreateMethod.getName(), postCreateMethod.getParameterTypes());
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        CMPInstanceContext ctx = (CMPInstanceContext) invocation.getEJBInstanceContext();

        // Assign a new row to the context before calling the create method
        CacheRow cacheRow = cacheTable.newRow();
        ctx.setCacheRow(cacheRow);

        // call the create method
        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();
        boolean oldTimerMethodAvailable = ctx.setTimerState(EJBOperation.EJBCREATE);
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            fastBeanClass.invoke(createIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception)t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        if (cmp1Bridge != null) {
            cmp1Bridge.loadCacheRow(ctx, cacheRow);
        }

        GlobalIdentity globalId;
        // Is the primary key auto-generated?
        if ( null != keyGenerator ) {
            Object opaque = keyGenerator.getNextPrimaryKey(cacheRow);
            globalId = primaryKeyTransform.getGlobalIdentity(opaque);
        } else {
            // define identity (may require insert to database)
            globalId = identityDefiner.defineIdentity(cacheRow);
        }


        // ============================================================================
        // FROM HERE ON WE MAY HAVE MODIFIED THE DATABASE SO ERRORS MUST CAUSE ROLLBACK
        // ============================================================================

        // cache insert
        TransactionContext transactionContext = invocation.getTransactionContext();
        InTxCache cache = (InTxCache) transactionContext.getInTxCache();

        try {
            if ( null != keyGenerator ) {
                cacheRow = keyGenerator.updateCache(cache, globalId, cacheRow);

                // CacheRow slots do not define the identity in this case; Inject it.
                identityDefiner.injectIdentity(cacheRow);
            } else {
                // add the row to the cache (returning a new row containing identity)
                cacheRow = cacheTable.addRow(cache, globalId, cacheRow);
            }
        } catch (DuplicateIdentityException e) {
            Object pk = primaryKeyTransform.getDomainIdentity(globalId);
            return invocation.createExceptionResult((Exception)new DuplicateKeyException("ID=" + pk));
        }

        ctx.setCacheRow(cacheRow);
        ctx.setId(primaryKeyTransform.getDomainIdentity(globalId));

        // associate the new cmp instance with the tx context
        ctx.setLoaded(true);
        transactionContext.associate(ctx);

        // call the post create method
        try {
            ctx.setOperation(EJBOperation.EJBPOSTCREATE);
            ctx.setTimerState(EJBOperation.EJBPOSTCREATE);
            fastBeanClass.invoke(postCreateIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                // we do not force rollback, that is up to the application
                return invocation.createExceptionResult((Exception)t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        // return a new proxy
        IdentityTransform transform = invocation.getType().isLocal() ? localProxyTransform : remoteProxyTransform;
        return invocation.createResult(transform.getDomainIdentity(globalId));
    }

    private Object readResolve() {
        return new CMPCreateMethod(beanClass, cmp1Bridge, createSignature, postCreateSignature, cacheTable, identityDefiner, keyGenerator, primaryKeyTransform, localProxyTransform, remoteProxyTransform);
    }
}
