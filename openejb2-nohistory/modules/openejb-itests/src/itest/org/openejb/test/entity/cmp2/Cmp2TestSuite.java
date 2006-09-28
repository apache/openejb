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
package org.openejb.test.entity.cmp2;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.test.TestManager;

/**
 * @version $Revision$ $Date$
 */
public class Cmp2TestSuite extends org.openejb.test.TestSuite {

    public Cmp2TestSuite() {
        super();
        this.addTest(new Cmp2JndiTests());
        this.addTest(new Cmp2HomeIntfcTests());
        this.addTest(new Cmp2EjbHomeTests());
        this.addTest(new Cmp2EjbObjectTests());
        this.addTest(new Cmp2RemoteIntfcTests());
        this.addTest(new Cmp2HomeHandleTests());
        this.addTest(new Cmp2HandleTests());
        this.addTest(new Cmp2EjbMetaDataTests());
        this.addTest(new Cmp2AllowedOperationsTests());
        this.addTest(new Cmp2JndiEncTests());
        this.addTest(new Cmp2RmiIiopTests());
        this.addTest(new PrefetchTests());
        this.addTest(new PetstoreTests());
        this.addTest(new StorageTests());
        this.addTest(new CMRMappingTests());
    }

    public static junit.framework.Test suite() {
        return new Cmp2TestSuite();
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        Properties props = TestManager.getServer().getContextEnvironment();
        props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
        new InitialContext(props);
        
        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
        TestManager.getDatabase().createEntityExplicitePKTable();
        TestManager.getDatabase().createCMP2Model();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropEntityTable();
        TestManager.getDatabase().dropEntityExplicitePKTable();
        TestManager.getDatabase().dropCMP2Model();
    }
}
