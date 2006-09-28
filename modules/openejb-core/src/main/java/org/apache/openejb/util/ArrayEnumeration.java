/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
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

