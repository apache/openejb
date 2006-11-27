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
package org.apache.openejb;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.apache.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public class MethodMap extends AbstractMap {

    private final MethodMetadata[] methodIndex;
    private final LinkedHashMap signatureIndex;
    private final MethodMapEntrySet entrySet;
    private MethodMapList methodMapList;

    public MethodMap(InterfaceMethodSignature[] methods) {
        methodIndex = new MethodMetadata[methods.length];
        signatureIndex = new LinkedHashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            InterfaceMethodSignature method = methods[i];
            methodIndex[i] = new MethodMetadata(method, null);
            signatureIndex.put(method, new Integer(i));
        }

        entrySet = new MethodMapEntrySet();
    }

    public List valuesList() {
        if (methodMapList == null) {
            methodMapList = new MethodMapList();
        }
        return methodMapList;
    }

    public Collection values() {
        return valuesList();
    }

    public Set entrySet() {
        return entrySet;
    }

    public Object get(int index) {
        if (index < 0 || index >= methodIndex.length) throw new IndexOutOfBoundsException("" + index);
        return methodIndex[index].getValue();
    }

    public Object set(int index, Object value) {
        if (index < 0 || index >= methodIndex.length) throw new IndexOutOfBoundsException("" + index);
        MethodMetadata methodMetadata = methodIndex[index];
        Object oldValue = methodMetadata.getValue();
        methodMetadata.setValue(value);
        return oldValue;
    }

    public Object put(Object key, Object value) {
        if (!(key instanceof InterfaceMethodSignature)) {
            throw new IllegalArgumentException("Key is not an instance of InterfaceMethodSignature");
        }

        InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) key;
        int i = indexOf(methodSignature);
        if (i < 0) {
            throw new IllegalArgumentException("MethodMap does not contain this method and new entries can not be added: " + methodSignature);
        }

        MethodMetadata methodMetadata = methodIndex[i];
        Object oldValue = methodMetadata.getValue();
        methodMetadata.setValue(value);
        return oldValue;
    }

    public boolean containsKey(Object key) {
        return signatureIndex.containsKey(key);
    }

    public int indexOf(InterfaceMethodSignature methodSignature) {
        Integer index = (Integer) signatureIndex.get(methodSignature);
        if (index == null) {
            return -1;
        }
        int i = index.intValue();
        return i;
    }

    public Object get(Object key) {
        if (!(key instanceof InterfaceMethodSignature)) {
            return null;
        }

        InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) key;
        int i = indexOf(methodSignature);
        if (i < 0) {
            return null;
        }

        Object value = methodIndex[i].getValue();
        return value;
    }

    public Iterator iterator() {
        return new MethodMapIterator();
    }

    public ListIterator listIterator() {
        return new MethodMapListIterator(0);
    }

    public ListIterator listIterator(int index) {
        if (index < 0 || index >= methodIndex.length) throw new IndexOutOfBoundsException("" + index);
        return new MethodMapListIterator(index);
    }

    public Object[] toArray() {
        return toArray(new Object[methodIndex.length]);
    }

    public Object[] toArray(Object values[]) {
        if (values.length < methodIndex.length) {
            values = (Object[]) Array.newInstance(values.getClass().getComponentType(), methodIndex.length);
        }

        for (int i = 0; i < methodIndex.length; i++) {
            MethodMetadata methodMetadata = methodIndex[i];
            values[i] = methodMetadata.getValue();
        }
        return values;
    }

    private static class MethodMetadata implements Map.Entry {
        private final InterfaceMethodSignature method;
        private Object value;

        private MethodMetadata(InterfaceMethodSignature method, Object value) {
            this.method = method;
            this.value = value;
        }

        public InterfaceMethodSignature getMethod() {
            return method;
        }

        public Object getKey() {
            return method;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    private class MethodMapEntrySet extends AbstractSet {
        public Iterator iterator() {
            return new Iterator() {
                private int index = 0;
                public boolean hasNext() {
                    return index < methodIndex.length;
                }

                public Object next() {
                    return methodIndex[index++];
                }

                public void remove() {
                    throw new UnsupportedOperationException("MethodMap entries can not be removed");
                }
            };
        }

        public int size() {
            return methodIndex.length;
        }
    }

    private class MethodMapList extends AbstractList {
        public Object get(int index) {
            return MethodMap.this.get(index);
        }

        public Object set(int index, Object element) {
            return MethodMap.this.set(index, element);
        }

        public int size() {
            return MethodMap.this.size();
        }
    }

    private class MethodMapIterator implements Iterator {
        protected int index;

        public boolean hasNext() {
            return index < methodIndex.length;
        }

        public Object next() {
            return methodIndex[index++].getValue();
        }

        public void remove() {
            throw new UnsupportedOperationException("MethodMap entries can not be removed");
        }
    }

    private class MethodMapListIterator extends MethodMapIterator implements ListIterator {
        public MethodMapListIterator(int index) {
            this.index = index;
        }

        public int nextIndex() {
            return index;
        }

        public boolean hasPrevious() {
            return index > 0;
        }

        public Object previous() {
            return methodIndex[--index].getValue();
        }

        public int previousIndex() {
            return index - 1;
        }

        public void set(Object o) {
            methodIndex[index].setValue(o);
        }

        public void add(Object o) {
            throw new UnsupportedOperationException("Entries can not be added to a MethodMap");
        }

    }
}
