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


import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.Serializable;
import javax.ejb.EJBException;
import javax.ejb.EnterpriseBean;

import org.tranql.cache.CacheRow;
import org.tranql.cache.InTxCache;
import org.tranql.ejb.CMPFieldTransform;

/**
 * @version $Revision$ $Date$
 */
public class CMP1Bridge implements Serializable {
    private final Class beanClass;
    private final LinkedHashMap cmpFieldAccessors;
    private final CMPFieldTransform[] fieldTransforms;
    private final transient Field[] beanFields;

    public CMP1Bridge(Class beanClass, LinkedHashMap cmpFieldAccessors ) {
        this.beanClass = beanClass;
        this.cmpFieldAccessors = cmpFieldAccessors;

        fieldTransforms = new CMPFieldTransform[cmpFieldAccessors.size()];
        beanFields = new Field[cmpFieldAccessors.size()];

        int i = 0;
        for (Iterator iterator = cmpFieldAccessors.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String fieldName = (String) entry.getKey();
            fieldTransforms[i] = (CMPFieldTransform) entry.getValue();

            try {
                beanFields[i] = beanClass.getField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Missing bean field " + fieldName);
            }
            i++;
        }
    }

    public void loadCacheRow(CMPInstanceContext source, CacheRow destination) {
        EnterpriseBean entityBean = source.getInstance();
        InTxCache inTxCache = (InTxCache) source.getTransactionContext().getInTxCache();
        for (int i = 0; i < beanFields.length; i++) {
            Field beanField = beanFields[i];
            CMPFieldTransform fieldTransform = fieldTransforms[i];

            Object value = null;
            try {
                value = beanField.get(entityBean);
            } catch (IllegalAccessException e) {
                throw new EJBException("Could not get the value of cmp a field: " + beanField.getName());
            }
            fieldTransform.set(inTxCache, destination, value);
        }

    }

    public void loadEntityBean(CacheRow source, CMPInstanceContext destination) {
        EnterpriseBean entityBean = destination.getInstance();
        InTxCache inTxCache = (InTxCache) destination.getTransactionContext().getInTxCache();
        for (int i = 0; i < beanFields.length; i++) {
            Field beanField = beanFields[i];
            CMPFieldTransform fieldTransform = fieldTransforms[i];

            Object value = fieldTransform.get(inTxCache, source);
            try {
                beanField.set(entityBean, value);
            } catch (IllegalAccessException e) {
                throw new EJBException("Could not get the value of cmp a field: " + beanField.getName());
            }
        }
    }

    private Object readResolve() {
        return new CMP1Bridge(beanClass, cmpFieldAccessors);
    }
}
