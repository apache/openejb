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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBLocalObject;
import javax.ejb.TimerService;
import javax.ejb.Timer;

import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.EJBInvocation;
import org.openejb.EJBOperation;
import org.openejb.timer.TimerState;
import org.openejb.dispatch.AbstractMethodOperation;
import org.openejb.dispatch.MethodSignature;
import org.tranql.cache.CacheTable;
import org.tranql.cache.InTxCache;
import org.tranql.cache.CacheRow;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.schema.AssociationEnd;

/**
 * Virtual operation handling removal of an instance.
 *
 * @version $Revision$ $Date$
 */
public class CMPRemoveMethod extends AbstractMethodOperation {
    private final CMPFieldTransform[] cascadeOneDeleteFields;
    private final CMPFieldTransform[] cascadeManyDeleteFields;
    private final CMPFieldTransform[] cmrOneFields;
    private final CMPFieldTransform[] cmrManyFields;

    public CMPRemoveMethod(Class beanClass, MethodSignature signature, CacheTable cacheTable, Map cmrAccessors) {
        super(beanClass, signature);
        List cascadeOneDeleteFieldsList = new ArrayList();
        List cascadeManyDeleteFieldsList = new ArrayList();
        List cmrOneFieldsList = new ArrayList();
        List cmrManyFieldsList = new ArrayList();
        for (Iterator iter = cacheTable.getAssociationEnds().iterator(); iter.hasNext();) {
            AssociationEnd end = (AssociationEnd) iter.next();
            CMPFieldTransform accessor = (CMPFieldTransform) cmrAccessors.get(end.getName());
            if ( null == accessor ) {
                throw new IllegalArgumentException("No CMR accessor for association end " + end.getName());
            }
            if ( end.isSingle() ) {
                cmrOneFieldsList.add(accessor);
                if ( end.isCascadeDelete() ) {
                    cascadeOneDeleteFieldsList.add(accessor);
                }
            } else {
                cmrManyFieldsList.add(accessor);
                if ( end.isCascadeDelete() ) {
                    cascadeManyDeleteFieldsList.add(accessor);
                }
            }
        }

        cascadeOneDeleteFields = (CMPFieldTransform[]) cascadeOneDeleteFieldsList.toArray(new CMPFieldTransform[0]);
        cascadeManyDeleteFields = (CMPFieldTransform[]) cascadeManyDeleteFieldsList.toArray(new CMPFieldTransform[0]);
        cmrOneFields = (CMPFieldTransform[]) cmrOneFieldsList.toArray(new CMPFieldTransform[0]);
        cmrManyFields = (CMPFieldTransform[]) cmrManyFieldsList.toArray(new CMPFieldTransform[0]);
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        CMPInstanceContext ctx = (CMPInstanceContext) invocation.getEJBInstanceContext();
        InvocationResult result = invoke(invocation, EJBOperation.EJBREMOVE);

        if (result.isNormal()) {
            //cancel timers
            TimerService timerService = ctx.getTimerService();
            if (timerService != null) {
                boolean oldTimerMethodAvailable = TimerState.getTimerState();
                ctx.setTimerServiceAvailable(true);
                TimerState.setTimerState(true);
                try {
                    Collection timers = timerService.getTimers();
                    for (Iterator iterator = timers.iterator(); iterator.hasNext();) {
                        Timer timer = (Timer) iterator.next();
                        timer.cancel();
                    }
                } finally {
                    ctx.setTimerServiceAvailable(false);
                    TimerState.setTimerState(oldTimerMethodAvailable);
                }
            }

            InTxCache cache = (InTxCache) invocation.getTransactionContext().getInTxCache();
            CacheRow cacheRow = ctx.getCacheRow();

            // get the entities to be deleted as part of a cascade delete
            Collection cascadeDeleteEntities = new ArrayList();
            for (int i = 0; i < cascadeOneDeleteFields.length; i++) {
                EJBLocalObject entity = (EJBLocalObject) cascadeOneDeleteFields[i].get(cache, cacheRow);
                if ( null != entity ) {
                    cascadeDeleteEntities.add(entity);
                }
            }
            for (int i = 0; i < cascadeManyDeleteFields.length; i++) {
                Collection entities = (Collection) cascadeManyDeleteFields[i].get(cache, cacheRow);
                cascadeDeleteEntities.addAll(entities);
            }

            // delete this row in the persistence engine
            cacheRow.markRemoved();
            cache.remove(cacheRow);

            // remove entity from all relationships
            for (int i = 0; i < cmrOneFields.length; i++) {
                cmrOneFields[i].set(cache, cacheRow, null);
            }
            for (int i = 0; i < cmrManyFields.length; i++) {
                cmrManyFields[i].set(cache, cacheRow, Collections.EMPTY_SET);
            }


            // clear id and row data from the instance
            ctx.setId(null);
            ctx.setCacheRow(null);

            // cascade delete
            for (Iterator iter = cascadeDeleteEntities.iterator(); iter.hasNext();) {
                EJBLocalObject entity = (EJBLocalObject) iter.next();
                entity.remove();
            }
        }
        return result;
    }
}
