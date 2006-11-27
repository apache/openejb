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
package org.openejb.deployment.entity.cmp.cmr.onetomany;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.EJBException;

import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.deployment.entity.cmp.cmr.AbstractCMRTest;

/**
 *
 * @version $Revision$ $Date$
 */
public class OneToManyTest extends AbstractCMRTest {
    private ALocalHome ahome;
    private ALocal a;
    private BLocalHome bhome;
    private BLocal b;

    public void testAGetBExistingAB() throws Exception {
        TransactionContext ctx = newTransactionContext();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        Set bSet = a.getB();
        assertEquals(2, bSet.size());
        for (Iterator iter = bSet.iterator(); iter.hasNext();) {
            BLocal b = (BLocal) iter.next();
            if ( b.getField1().equals(new Integer(11)) ) {
                assertEquals("value11", b.getField2());
            } else if ( b.getField1().equals(new Integer(22)) ) {
                assertEquals("value22", b.getField2());
            } else {
                fail();
            }
        }
        ctx.commit();
    }
    
    public void testBGetAExistingAB() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));
        ALocal a = b.getA();
        assertNotNull(a);
        assertEquals(new Integer(1), a.getField1());
        assertEquals("value1", a.getField2());

        b = bhome.findByPrimaryKey(new Integer(22));
        a = b.getA();
        assertNotNull(a);
        assertEquals(new Integer(1), a.getField1());
        assertEquals("value1", a.getField2());
        ctx.commit();
    }

    private void assertStateDropExisting() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void XtestASetBDropExisting() throws Exception {
        TransactionContext ctx = newTransactionContext();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        a.setB(new HashSet());
        ctx.commit();

        assertStateDropExisting();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void XtestBSetADropExisting() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));
        b.setA(null);
        b = bhome.findByPrimaryKey(new Integer(22));
        b.setA(null);
        ctx.commit();

        assertStateDropExisting();
    }
    
    private TransactionContext prepareNewAB() throws Exception {
        TransactionContext ctx = newTransactionContext();
        a = ahome.create(new Integer(2));
        a.setField2("value2");
        b = bhome.create(new Integer(22));
        b.setField2("value22");
        return ctx;
    }

    private void assertStateNewAB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM A WHERE a1 = 2");
        assertTrue(rs.next());
        assertEquals("value2", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM B WHERE fka1 = 2");
        assertTrue(rs.next());
        assertEquals(22, rs.getInt(1));
        assertEquals("value22", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBNewAB() throws Exception {
        TransactionContext ctx = prepareNewAB();
        Set bSet = new HashSet();
        bSet.add(b);
        a.setB(bSet);
        ctx.commit();
        
        assertStateNewAB();
    }

    public void testBSetANewAB() throws Exception {
        TransactionContext ctx = prepareNewAB();
        b.setA(a);
        ctx.commit();
        
        assertStateNewAB();
    }
    
    private TransactionContext prepareExistingBNewA() throws Exception {
        TransactionContext ctx = newTransactionContext();
        a = ahome.create(new Integer(2));
        a.setField2("value2");
        b = bhome.findByPrimaryKey(new Integer(11));
        return ctx;
    }

    private void assertStateExistingBNewA() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM A WHERE a1 = 2");
        assertTrue(rs.next());
        assertEquals("value2", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM B WHERE fka1 = 2");
        assertTrue(rs.next());
        assertEquals(11, rs.getInt(1));
        assertEquals("value11", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBExistingBNewA() throws Exception {
        TransactionContext ctx = prepareExistingBNewA();
        Set bSet = a.getB();
        bSet.add(b);
        ctx.commit();
        
        assertStateExistingBNewA();
    }

    public void testBSetAExistingBNewA() throws Exception {
        TransactionContext ctx = prepareExistingBNewA();
        b.setA(a);
        ctx.commit();
        
        assertStateExistingBNewA();
    }
    
    private TransactionContext prepareExistingANewB() throws Exception {
        TransactionContext ctx = newTransactionContext();
        a = ahome.findByPrimaryKey(new Integer(1));
        b = bhome.create(new Integer(33));
        b.setField2("value33");
        return ctx;
    }
    
    private void assertStateExistingANewB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        
        rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1 AND b1 = 33 AND b2 = 'value33'");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBExistingANewB() throws Exception {
        TransactionContext ctx = prepareExistingANewB();
        Set bSet = a.getB();
        bSet.add(b);
        ctx.commit();
        
        assertStateExistingANewB();
    }

    public void testBSetAExistingANewB() throws Exception {
        TransactionContext ctx = prepareExistingANewB();
        b.setA(a);
        ctx.commit();
        
        assertStateExistingANewB();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void XtestRemoveRelationships() throws Exception {
        TransactionContext ctx = newTransactionContext();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        a.remove();
        ctx.commit();

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM B");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        rs.close();
        rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testCMPMappedToForeignKeyColumn() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));

        Integer field3 = b.getField3();
        assertEquals(b.getA().getPrimaryKey(), field3);
        ctx.commit();
    }
    
    public void testSetCMPMappedToForeignKeyColumn() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));

        b.setField3(new Integer(2));
        
        ALocal a = b.getA();
        assertEquals(new Integer(2), a.getField1());
        assertEquals("value2", a.getField2());
        
        ctx.commit();
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        ahome = (ALocalHome) super.ahome;
        bhome = (BLocalHome) super.bhome;
    }
    
    protected void buildDBSchema(Connection c) throws Exception {
        Statement s = c.createStatement();
        try {
            s.execute("DROP TABLE A");
        } catch (SQLException e) {
            // ignore
        }
        try {
            s.execute("DROP TABLE B");
        } catch (SQLException e) {
            // ignore
        }
        
        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");
        s.execute("CREATE TABLE B(B1 INTEGER, B2 VARCHAR(50), FKA1 INTEGER)");
        
        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'value1')");
        s.execute("INSERT INTO A(A1, A2) VALUES(2, 'value2')");
        s.execute("INSERT INTO B(B1, B2, FKA1) VALUES(11, 'value11', 1)");
        s.execute("INSERT INTO B(B1, B2, FKA1) VALUES(22, 'value22', 1)");
        s.close();
        c.close();
    }

    protected String getEjbJarDD() {
        return "src/test/test-cmp/onetomany/simplepk/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test/test-cmp/onetomany/simplepk/openejb-jar.xml";
    }

    protected EJBClass getA() {
        return new EJBClass(ABean.class, ALocalHome.class, ALocal.class);
    }

    protected EJBClass getB() {
        return new EJBClass(BBean.class, BLocalHome.class, BLocal.class);
    }
    
}
