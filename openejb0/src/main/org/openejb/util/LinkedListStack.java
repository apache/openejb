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
package org.openejb.util;


/**
 * A First In First Out (FIFO) queue, also known as a Stack.
 * 
 * Note: This is an implementation of org.openejb.util.Stack
 * not to be confused with java.util.Stack
 */
public class LinkedListStack implements Stack {
    
    /**
     * Entries that contain data as the result of the <code>push</code> method.  
     * The data in these entries is removed and returned when the <code>pop</code> method is called.
     */
    private LinkedEntry occupiedEntries;

    /**
     * Entries that contain null values as the result of the <code>pop</code> method.
     * These entries will be populated with data when the <code>push</code> method is called.
     */
    private LinkedEntry vacantEntries;

    /**
     * the number of elements on the stack
     */
    private int size;
    
    /**
     * Constructs this LinkedListStack with the specified number of LinkedEntry 
     * objects all sequentially linked together.
     * 
     * @param initialSize
     */
    public LinkedListStack(int initialSize) {
        for ( int i = 0; i < initialSize; i++ )
            vacantEntries = new LinkedEntry( null, vacantEntries );
    }

    public synchronized Object push( Object object ) {
        /* Take an entry from the vacant list and move it to the occupied list. */

        if ( vacantEntries == null )
            occupiedEntries = new LinkedEntry( object, occupiedEntries );
        else {
            //  Take the top vacant entry
            LinkedEntry entry = vacantEntries;

            //  Shrink the vacant entries list by one
            vacantEntries = vacantEntries.next;

            //  Assign the entry a value and put it at the top of the 
            //  occupied entries list
            occupiedEntries = entry.set(object, occupiedEntries);
        }
        ++size;
        return object;
    }


    public synchronized Object pop() throws java.util.EmptyStackException {
        /* Take an entry from the occupied list and move it to the vacant list. */

        //  Take the top occupied entry
        LinkedEntry entry = occupiedEntries;
        if ( entry == null ) return null;
        
        //  Shrink the occupied entries list by one
        occupiedEntries = occupiedEntries.next;
        
        //  Assign the entry a null value and put it at the top of the 
        //  vacant entries list
        Object value = entry.value;
        vacantEntries = entry.set(null ,vacantEntries);
        --size;
        return value;
    }

    public synchronized int size() {
        return size;
    }
    //======================================================
    // Inner class to represent entries in the linked list
    //
    
    static class LinkedEntry {

        LinkedEntry  next;
        Object value;

        LinkedEntry( Object value, LinkedEntry next ) {
            set(value,next);
        }

        LinkedEntry set( Object value, LinkedEntry next ) {
            this.next = next;
            this.value = value;
            return this;
        }
    }
    
    //
    // Inner class to represent entries in the linked list
    //======================================================
}