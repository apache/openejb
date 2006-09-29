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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * An Externalizable Enumeration.
 * 
 * Mainly used for returning enumerations from the finder methods in the home interface of entity beans.
 * 
 */
public final class ArrayEnumeration implements Enumeration, Externalizable{
    static final long serialVersionUID = -1194966576855523042L;    

    private Object[] elements;
    private int elementsIndex;

    public ArrayEnumeration(Vector elements){
        this.elements = new Object[elements.size()];
        elements.copyInto(this.elements);
    }

    public ArrayEnumeration(java.util.List list){
        this.elements = new Object[list.size()];
        list.toArray(this.elements);
    }
    
    // This is required for Externalization.
    public ArrayEnumeration() {
    }

    // These methods are borrowed from the List interface
    // They are needed to avoid unnecessary object creation in
    // the finder methods
    public java.lang.Object get(int index) {
        return elements[index];
    }

    public void set(int index, java.lang.Object o) {
        elements[index] = o;
    }

    public int size() {
        return elements.length;
    }
    
    //=========================================
    // java.util.Enumeration interface methods
    //

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    public boolean hasMoreElements(){
        return ( elementsIndex < elements.length );
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return     the next element of this enumeration.
     * @exception  NoSuchElementException  if no more elements exist.
     */
    public Object nextElement(){
        if ( !hasMoreElements()) throw new NoSuchElementException("No more elements exist");
        return elements[elementsIndex++];
    }

    //
    // java.util.Enumeration interface methods
    //=========================================

    //==========================================
    // java.io.Externalizable interface methods
    //

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @serialData Overriding methods should use this tag to describe
     *             the data layout of this Externalizable object.
     *             List the sequence of element types and, if possible,
     *             relate the element to a public/protected field and/or
     *             method of this Externalizable class.
     *
     * @exception IOException Includes any I/O exceptions that may occur
     */
    public void writeExternal(ObjectOutput out) throws IOException{
        out.writeInt(elements.length);
        out.writeInt(elementsIndex);
        for (int i=0; i < elements.length; i++) {
            out.writeObject(elements[i]);
        }
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        elements = new Object[in.readInt()];
        elementsIndex = in.readInt();
        for (int i=0; i < elements.length; i++) {
            elements[i] = in.readObject();
        }
    }

    //
    // java.io.Externalizable interface methods
    //==========================================


}

