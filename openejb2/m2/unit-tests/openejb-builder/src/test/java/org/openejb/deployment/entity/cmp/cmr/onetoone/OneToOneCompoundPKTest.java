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
package org.openejb.deployment.entity.cmp.cmr.onetoone;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.EJBException;

import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.deployment.entity.cmp.cmr.AbstractCMRTest;
import org.openejb.deployment.entity.cmp.cmr.CompoundPK;

/**
 *
 * @version $Revision$ $Date$
 */
public class OneToOneCompoundPKTest extends AbstractCMRTest {
    private ALocalHome ahome;
    private ALocal a;
    private BLocalHome bhome;
    private BLocal b;
    
    public void testAGetBExistingAB() throws Exception {
        TransactionContext ctx = newTransactionContext();
        ALocal a = ahome.findByPrimaryKey(new CompoundPK(new Integer(1), "value1"));
        BLocal b = a.getB();
        assertNotNull(b);
        assertEquals(new Integer(11), b.getField1());
        assertEquals("value11", b.getField2());
        ctx.commit();
    }

    public void testBGetAExistingAB() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new CompoundPK(new Integer(11), "value11"));
        ALocal a = b.getA();
        assertNotNull(a);
        assertEquals(new Integer(1), a.getField1());
        assertEquals("value1", a.getField2());
        ctx.commit();
    }
    
    private void assertStateDropExisting() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1 AND fka2 = 'value1'");
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
        ALocal a = ahome.findByPrimaryKey(new CompoundPK(new Integer(1), "value1"));
        a.setB(null);
        ctx.commit();

        assertStateDropExisting();
    }
    
    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void XtestBSetADropExisting() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new CompoundPK(new Integer(11), "value11"));
        b.setA(null);
        ctx.commit();

        assertStateDropExisting();
    }

    private TransactionContext prepareNewAB() throws Exception {
        CompoundPK pkA = new CompoundPK(new Integer(2), "value2");

        TransactionContext ctx = newTransactionContext();
        a = ahome.create(pkA);
        b = bhome.create(new CompoundPK(new Integer(22), "value22"));
        return ctx;
    }

    private void assertStateNewAB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM A WHERE a1 = 2 AND a2 = 'value2'");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM B WHERE fka1 = 2 AND fka2 = 'value2'");
        assertTrue(rs.next());
        assertEquals(22, rs.getInt(1));
        assertEquals("value22", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBNewAB() throws Exception {
        TransactionContext ctx = prepareNewAB();
        a.setB(b);
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
        CompoundPK pkA = new CompoundPK(new Integer(2), "value2");

        TransactionContext ctx = newTransactionContext();
        a = ahome.create(pkA);
        b = bhome.findByPrimaryKey(new CompoundPK(new Integer(11), "value11"));
        return ctx;
    }
    
    private void assertStateExistingBNewA() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM A WHERE a1 = 2 AND a2 = 'value2'");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM B WHERE fka1 = 2 AND fka2 = 'value2'");
        assertTrue(rs.next());
        assertEquals(11, rs.getInt(1));
        assertEquals("value11", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBExistingBNewA() throws Exception {
        TransactionContext ctx = prepareExistingBNewA();
        a.setB(b);
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
        CompoundPK pkA = new CompoundPK(new Integer(1), "value1");
        
        TransactionContext ctx = newTransactionContext();
        a = ahome.findByPrimaryKey(pkA);
        b = bhome.create(new Integer(22));
        b.setField2("value22");
        return ctx;
    }
    
    private void assertStateExistingANewB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1 AND fka2 = 'value1'");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        rs.close();
        
        rs = s.executeQuery("SELECT b1, b2 FROM B WHERE fka1 = 1 AND fka2 = 'value1'");
        assertTrue(rs.next());
        assertEquals(22, rs.getInt(1));
        assertEquals("value22", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }
    
    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     * @see OneToOneTest for more details.
     */
    public void XtestASetBExistingANewB() throws Exception {
        TransactionContext ctx = prepareExistingANewB();
        a.setB(b);
        ctx.commit();
        
        assertStateExistingANewB();
    }
    
    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     * @see OneToOneTest for more details.
     */
    public void XtestBSetAExistingANewB() throws Exception {
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
        ALocal a = ahome.findByPrimaryKey(new CompoundPK(new Integer(1), "value1"));
        a.remove();
        ctx.commit();

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM B");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        rs = s.executeQuery("SELECT COUNT(*) FROM B WHERE fka1 = 1 AND fka2 = 'value1'");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testCascadeDelete() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new CompoundPK(new Integer(11), "value11"));
        b.remove();
        ctx.commit();

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM A");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testCMPMappedToForeignKeyColumn() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new CompoundPK(new Integer(11), "value11"));

        Integer field3 = b.getField3();
        assertEquals(((CompoundPK) b.getA().getPrimaryKey()).field1, field3);

        String field4 = b.getField4();
        assertEquals(((CompoundPK) b.getA().getPrimaryKey()).field2, field4);
        ctx.commit();
    }
    
    public void testSetCMPMappedToForeignKeyColumn() throws Exception {
        TransactionContext ctx = newTransactionContext();
        BLocal b = bhome.findByPrimaryKey(new CompoundPK(new Integer(11), "value11"));

        try {
            b.setField3(new Integer(13));
            fail("Cannot set the value of a CMP field mapped to a foreign key column.");
        } catch (EJBException e) {
        }
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
        s.execute("CREATE TABLE B(B1 INTEGER, B2 VARCHAR(50), FKA1 INTEGER, FKA2 VARCHAR(50))");
        
        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'value1')");
        s.execute("INSERT INTO B(B1, B2, FKA1, FKA2) VALUES(11, 'value11', 1, 'value1')");
        s.close();
        c.close();
    }

    protected String getEjbJarDD() {
        return "src/test/test-cmp/onetoone/compoundpk/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test/test-cmp/onetoone/compoundpk/openejb-jar.xml";
    }

    protected EJBClass getA() {
        return new EJBClass(ABean.class, ALocalHome.class, ALocal.class);
    }

    protected EJBClass getB() {
        return new EJBClass(BBean.class, BLocalHome.class, BLocal.class);
    }
}
