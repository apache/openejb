/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.apache.openejb.deployment.entity.cmp.cmr.manytomany;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.transaction.Transaction;

import org.apache.openejb.deployment.entity.cmp.cmr.AbstractCMRTest;

/**
 *
 * @version $Revision$ $Date$
 */
public class ManyToManyTest extends AbstractCMRTest {
    private ALocalHome ahome;
    private ALocal a;
    private BLocalHome bhome;
    private BLocal b;

    public void testAGetBExistingAB() throws Exception {
        Transaction ctx = newTransaction();
        a = ahome.findByPrimaryKey(new Integer(1));
        Set bSet = a.getB();
        assertEquals(2, bSet.size());
        for (Iterator iter = bSet.iterator(); iter.hasNext();) {
            b = (BLocal) iter.next();
            if ( b.getField1().equals(new Integer(11)) ) {
                assertEquals("value11", b.getField2());
            } else if ( b.getField1().equals(new Integer(22)) ) {
                assertEquals("value22", b.getField2());
            } else {
                fail();
            }
        }
        completeTransaction(ctx);
    }
    
    public void testBGetAExistingAB() throws Exception {
        Transaction ctx = newTransaction();
        BLocal b = bhome.findByPrimaryKey(new Integer(22));
        Set aSet = b.getA();
        assertEquals(3, aSet.size());
        for (Iterator iter = aSet.iterator(); iter.hasNext();) {
            a = (ALocal) iter.next();
            if ( a.getField1().equals(new Integer(1)) ) {
                assertEquals("value1", a.getField2());
            } else if ( a.getField1().equals(new Integer(2)) ) {
                assertEquals("value2", a.getField2());
            } else if ( a.getField1().equals(new Integer(3)) ) {
                assertEquals("value3", a.getField2());
            } else {
                fail();
            }
        }
        completeTransaction(ctx);
    }

    private void assertStateDropExisting() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBDropExisting() throws Exception {
        Transaction ctx = newTransaction();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        a.setB(new HashSet());
        a = ahome.findByPrimaryKey(new Integer(2));
        a.setB(new HashSet());
        a = ahome.findByPrimaryKey(new Integer(3));
        a.setB(new HashSet());
        completeTransaction(ctx);

        assertStateDropExisting();
    }

    public void testBSetADropExisting() throws Exception {
        Transaction ctx = newTransaction();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));
        b.setA(new HashSet());
        b = bhome.findByPrimaryKey(new Integer(22));
        b.setA(new HashSet());
        completeTransaction(ctx);

        assertStateDropExisting();
    }

    private Transaction prepareNewAB() throws Exception {
        Transaction ctx = newTransaction();
        a = ahome.create(new Integer(4));
        a.setField2("value4");
        b = bhome.create(new Integer(33));
        b.setField2("value33");
        return ctx;
    }

    private void assertStateNewAB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 4 AND fkb1 = 33");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT a2 FROM A WHERE a1 = 4");
        assertTrue(rs.next());
        assertEquals("value4", rs.getString(1));

        rs = s.executeQuery("SELECT b2 FROM B WHERE b1 = 33");
        assertTrue(rs.next());
        assertEquals("value33", rs.getString(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBNewAB() throws Exception {
        Transaction ctx = prepareNewAB();
        Set bSet = a.getB();
        bSet.add(b);
        completeTransaction(ctx);
        
        assertStateNewAB();
    }

    public void testBSetANewAB() throws Exception {
        Transaction ctx = prepareNewAB();
        Set aSet = b.getA();
        aSet.add(a);
        completeTransaction(ctx);
        
        assertStateNewAB();
    }

    private Transaction prepareExistingBNewA() throws Exception {
        Transaction ctx = newTransaction();
        a = ahome.create(new Integer(4));
        a.setField2("value4");
        b = bhome.findByPrimaryKey(new Integer(11));
        return ctx;
    }

    private void assertStateExistingBNewA() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM A WHERE a1 = 4");
        assertTrue(rs.next());
        assertEquals("value4", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 4 AND fkb1 = 11");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBExistingBNewA() throws Exception {
        Transaction ctx = prepareExistingBNewA();
        Set bSet = a.getB();
        bSet.add(b);
        completeTransaction(ctx);
        
        assertStateExistingBNewA();
    }

    public void testBSetAExistingBNewA() throws Exception {
        Transaction ctx = prepareExistingBNewA();
        Set aSet = b.getA();
        aSet.add(a);
        completeTransaction(ctx);
        
        assertStateExistingBNewA();
    }

    private Transaction prepareExistingANewB() throws Exception {
        Transaction ctx = newTransaction();
        a = ahome.findByPrimaryKey(new Integer(1));
        b = bhome.create(new Integer(33));
        b.setField2("value33");
        return ctx;
    }
    
    private void assertStateExistingANewB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT b2 FROM B WHERE b1 = 33");
        assertTrue(rs.next());
        assertEquals("value33", rs.getString(1));
        rs.close();
        
        rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 1 AND fkb1 = 33");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBExistingANewB() throws Exception {
        Transaction ctx = prepareExistingANewB();
        Set bSet = a.getB();
        bSet.add(b);
        completeTransaction(ctx);
        
        assertStateExistingANewB();
    }

    public void testBSetAExistingANewB() throws Exception {
        Transaction ctx = prepareExistingANewB();
        Set aSet = b.getA();
        aSet.add(a);
        completeTransaction(ctx);
        
        assertStateExistingANewB();
    }

    public void testRemoveRelationships() throws Exception {
        Transaction ctx = newTransaction();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        a.remove();
        completeTransaction(ctx);

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
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
        try {
            s.execute("DROP TABLE MTM");
        } catch (SQLException e) {
            // ignore
        }
        
        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");
        s.execute("CREATE TABLE B(B1 INTEGER, B2 VARCHAR(50), FKA1 INTEGER)");
        s.execute("CREATE TABLE MTM(FKA1 INTEGER, FKB1 INTEGER)");
        
        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'value1')");
        s.execute("INSERT INTO A(A1, A2) VALUES(2, 'value2')");
        s.execute("INSERT INTO A(A1, A2) VALUES(3, 'value3')");
        s.execute("INSERT INTO B(B1, B2) VALUES(11, 'value11')");
        s.execute("INSERT INTO B(B1, B2) VALUES(22, 'value22')");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(1, 11)");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(1, 22)");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(2, 22)");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(3, 22)");        
        s.close();
        c.close();
    }

    protected String getEjbJarDD() {
        return "src/test-cmp/manytomany/simplepk/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test-cmp/manytomany/simplepk/openejb-jar.xml";
    }

    protected EJBClass getA() {
        return new EJBClass(ABean.class, ALocalHome.class, ALocal.class);
    }

    protected EJBClass getB() {
        return new EJBClass(BBean.class, BLocalHome.class, BLocal.class);
    }
    
}
