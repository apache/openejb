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
package org.openejb.deployment.entity.cmp.cmr.onetomany;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.deployment.entity.cmp.cmr.AbstractCMRTest;

/**
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
            if (b.getField1().equals(new Integer(11))) {
                assertEquals("value11", b.getField2());
            } else if (b.getField1().equals(new Integer(22))) {
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
        return "src/test-cmp/onetomany/simplepk/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test-cmp/onetomany/simplepk/openejb-jar.xml";
    }

    protected EJBClass getA() {
        return new EJBClass(ABean.class, ALocalHome.class, ALocal.class);
    }

    protected EJBClass getB() {
        return new EJBClass(BBean.class, BLocalHome.class, BLocal.class);
    }

}

