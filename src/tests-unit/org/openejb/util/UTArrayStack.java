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


import java.util.EmptyStackException;

import junit.framework.Assert;

import org.openejb.test.NamedTestCase;

/**
 *
 * @author <a href="mailto:manchoon@yahoo.com">manchoon</a>
 */
public class UTArrayStack extends NamedTestCase {

    private ArrayStack m_arrayStack = null;
    private String m_test = null;

    public UTArrayStack() {
        super( "org.openejb.util.ArrayStack." );
    }

    public void setUp() throws Exception {
        m_arrayStack = new ArrayStack();
        m_test = "test";
	}

    public void tearDown() throws Exception {
        m_arrayStack = null;
        m_test = null;
    }

    public void test01_constructor()
    {
        ArrayStack tmp = new ArrayStack();
    }

    /**
     * Trim the array size.
     */
    public void test02_setSize() {
        try {
            for ( int i = 0; i < 10; i++ )
            {
                m_arrayStack.add( "object " + i );
            }
            m_arrayStack.setSize( 5 );
            int expectedSize = 5;
            int actualSize = m_arrayStack.size();
            assertEquals( "The array's size is not setted correctly.",
                          expectedSize, actualSize );
        } catch( Exception e ) {
            Assert.assert( "Received Exception " + e.getClass() + " : " +
                           e.getMessage(), false );
        }
    }

    public void test03_push() {
        try {
            m_arrayStack.clear();
            Object expectedObject = m_test;
            Object actualObject = m_arrayStack.push( m_test );
            int expectedSize = 1;
            int actualSize = m_arrayStack.size();
            assertEquals( "The array's pushed object is not equal.",
                          expectedObject, actualObject );
            assertEquals( "The array's size is not increased by one correctly.",
                          expectedSize, actualSize );
        } catch( Exception e ) {
            Assert.assert( "Received Exception " + e.getClass() + " : " +
                           e.getMessage(), false );
        }
    }

    public void test04_pop1() {
        try {
            Object expectedObject = m_test;
            Object actualObject = m_arrayStack.pop();
            int expectedSize = 0;
            int actualSize = m_arrayStack.size();
            assertEquals( "The array's poped object is not equal.",
                          expectedObject, actualObject );
            assertEquals( "The array's size is not decreased by one correctly.",
                          expectedSize, actualSize );
        } catch( Exception e ) {
            Assert.assert( "Received Exception " + e.getClass() + " : " +
                           e.getMessage(), false );
        }
    }

    // test for EmptyStackException
    public void test05_pop2() {
        try {
            m_arrayStack.pop();
            fail( "Should raise an EmptyStackException!" );
        } catch ( EmptyStackException ese ) {
        }
    }

    // test for EmptyStackException
    public void test06_peek1() {
        try {
            m_arrayStack.peek();
            fail( "Should raise an EmptyStackException" );
        } catch ( EmptyStackException ese ) {
        }
    }

    public void test07_peek2() {
        try {
            m_arrayStack.push( m_test );
            Object expectedObject = m_test;
            Object actualObject = m_arrayStack.peek();
            int expectedSize = 1;
            int actualSize = m_arrayStack.size();
            assertEquals( "The array's peeked object is not equal.",
                          expectedObject, actualObject );
            assertEquals( "The array's size is changed.",
                          expectedSize, actualSize );
        } catch( Exception e ) {
            Assert.assert( "Received Exception " + e.getClass() + " : " +
                           e.getMessage(), false );
        }
    }
}
