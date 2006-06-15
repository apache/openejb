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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.EJBLocalObject;

import org.openejb.GenericEJBContainer;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class RelationshipSet implements Set {
    private CMPInstanceContext context;
    private int slot;
    private GenericEJBContainer relatedContainer;
    private Collection keys;
    private Class relatedLocalInterface;

    public RelationshipSet(CMPInstanceContext context, int slot, GenericEJBContainer relatedContainer, Collection keys) {
        this.context = context;
        this.slot = slot;
        this.relatedContainer = relatedContainer;
        this.keys = keys;
//        relatedLocalInterface = relatedContainer.getLocalInterface();
    }

    void invalidate() {
        context = null;
        slot = -1;
        relatedContainer = null;
        keys = null;
        relatedLocalInterface = null;
    }

    public int size() {
        return keys.size();
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public boolean contains(Object o) {
        if (relatedLocalInterface.isInstance(o)) {
            EJBLocalObject ejb = (EJBLocalObject) o;
            Object primaryKey = ejb.getPrimaryKey();
            return keys.contains(primaryKey);
        }
        return false;
    }

    public Iterator iterator() {
        return new Iterator() {
            // todo we should drop the reference to the iterator when the set is invalidated
            private Iterator iterator = keys.iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Object next() {
                return relatedContainer.getEjbLocalObject(iterator.next());
            }

            public void remove() {
                iterator.remove();
            }
        };
    }

    public Object[] toArray() {
        return toArray(new Object[keys.size()]);
    }

    public Object[] toArray(Object a[]) {
        if (a.length != keys.size()) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(), keys.size());
        }

        Iterator iterator = keys.iterator();
        for (int i = 0; i < a.length; i++) {
            a[i] = relatedContainer.getEjbLocalObject(iterator.next());
        }
        return a;
    }

    public boolean add(Object o) {
        if (relatedLocalInterface.isInstance(o)) {
            EJBLocalObject ejb = (EJBLocalObject) o;
            Object primaryKey = ejb.getPrimaryKey();
            boolean changed = keys.add(primaryKey);
            if (changed) {
                context.addRelation(slot, primaryKey);
            }
            return changed;
        } else {
            throw new IllegalArgumentException("Object is not an instance of " + relatedLocalInterface.getName());
        }
    }

    public boolean remove(Object o) {
        if (relatedLocalInterface.isInstance(o)) {
            EJBLocalObject ejb = (EJBLocalObject) o;
            Object primaryKey = ejb.getPrimaryKey();
            boolean changed = keys.remove(primaryKey);
            if (changed) {
                context.removeRelation(slot, primaryKey);
            }
            return changed;
        } else {
            throw new IllegalArgumentException("Object is not an instance of " + relatedLocalInterface.getName() +
                    ": " + (o == null ? "null" : o.getClass().getName()));
        }
    }

    public boolean containsAll(Collection c) {
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            if (!contains(iterator.next())) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection c) {
        boolean changed = false;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            if (!add(iterator.next())) {
                changed = true;
            }
        }
        return changed;
    }

    public boolean retainAll(Collection c) {
        Collection inputKeys = new HashSet(c.size());
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            EJBLocalObject ejb = (EJBLocalObject) iterator.next();
            inputKeys.add(ejb.getPrimaryKey());
        }
        boolean changed = false;
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            Object primaryKey = iterator.next();
            if (!inputKeys.contains(primaryKey)) {
                iterator.remove();
                context.removeRelation(slot, primaryKey);
            }
        }
        return changed;
    }

    public boolean removeAll(Collection c) {
        boolean changed = false;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            if (!remove(iterator.next())) {
                changed = true;
            }
        }
        return changed;
    }

    public void clear() {
        keys.clear();
    }
}
