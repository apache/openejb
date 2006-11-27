/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class SingletonCollection implements Collection {
    private final Object element;

    public SingletonCollection(Object element) {
        this.element = element;
    }

    public Iterator iterator() {
        return new Iterator() {
            boolean done;

            public boolean hasNext() {
                return !done;
            }

            public Object next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                done = true;
                return element;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object o) {
        return (element == null) ? o == null : element.equals(o);
    }

    public boolean containsAll(Collection c) {
        if (c.size() == 1) {
            return contains(c.iterator().next());
        }
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return 1;
    }

    public Object[] toArray() {
        return new Object[]{element};
    }

    public Object[] toArray(Object a[]) {
        if (a.length == 0) {
            a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass(), 1);
        } else if (a.length > 1) {
            a[1] = null;
        }
        a[0] = element;
        return a;
    }
}
