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
package org.openejb.test.entity.cmp;

import junit.framework.Test;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import org.openejb.test.beans.Database;
import org.openejb.test.beans.DatabaseHome;
import java.util.Properties;
import org.openejb.test.TestServerManager;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpTestSuite extends org.openejb.test.TestSuite{
       
    public static final String CREATE_TABLE = "CREATE TABLE BasicCmpEntities ( EntityID INT PRIMARY KEY, FIRSTNAME CHAR(20), LASTNAME CHAR(20) )";
    //public static final String CREATE_TABLE = "CREATE TABLE BasicCmpEntities ( EntityID INT PRIMARY KEY AUTO INCREMENT, FIRSTNAME CHAR(20), LASTNAME CHAR(20) )";
    public static final String DROP_TABLE = "DROP TABLE BasicCmpEntities";
    
    private Database database;

    public CmpTestSuite(){
        super();
        this.addTest(new CmpJndiTests());
        this.addTest(new CmpHomeIntfcTests());
        this.addTest(new CmpEjbHomeTests());
        this.addTest(new CmpEjbObjectTests());    
        this.addTest(new CmpRemoteIntfcTests());
        this.addTest(new CmpHomeHandleTests());
        this.addTest(new CmpHandleTests());
        this.addTest(new CmpEjbMetaDataTests());
        //this.addTest(new CmpAllowedOperationsTests());
        this.addTest(new CmpJndiEncTests());
        this.addTest(new CmpRmiIiopTests());
        
    }

    public static junit.framework.Test suite() {
        return new CmpTestSuite();
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        Properties props = TestServerManager.getContextEnvironment();
        props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
        InitialContext initialContext = new InitialContext(props);
        
        DatabaseHome databaseHome = (DatabaseHome)initialContext.lookup("client/tools/DatabaseHome");
        database = databaseHome.create();
        database.executeQuery(DROP_TABLE);
        database.executeQuery(CREATE_TABLE);
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        database.executeQuery(DROP_TABLE);
    }
}
