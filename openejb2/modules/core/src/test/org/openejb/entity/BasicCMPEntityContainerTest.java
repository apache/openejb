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
package org.openejb.entity;

import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ObjectNotFoundException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.transaction.TransactionManagerProxy;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.axiondb.jdbc.AxionDataSource;
import org.openejb.MockTransactionManager;
import org.openejb.ContainerIndex;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.deployment.MockConnectionProxyFactory;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.entity.cmp.CMPContainerBuilder;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.sql.sql92.SQL92Schema;
import org.tranql.sql.Table;
import org.tranql.sql.Column;

/**
 * @version $Revision$ $Date$
 */
public class BasicCMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private static final ObjectName TM_NAME = JMXUtil.getObjectName("geronimo.test:role=TransactionManager");
    private static final ObjectName TCA_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    private static final ObjectName CI_NAME = JMXUtil.getObjectName("geronimo.test:role=ContainerIndex");
    private Kernel kernel;
    private GBeanMBean container;

    private DataSource ds;

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "EJBLocalHome");
        assertEquals(2, home.intMethod(1));

        Integer pk = new Integer(33);
        String value = "Thirty-Three";
        int number = 44;

        MockLocal local = home.create(pk, value);
        assertEquals(1 + number + pk.intValue(), local.intMethod(number));
        assertEquals(pk, local.getPrimaryKey());
        assertEquals(value, local.getValue());

        local = home.findByPrimaryKey(pk);
        assertEquals(1 + number + pk.intValue(), local.intMethod(number));
        assertEquals(pk, local.getPrimaryKey());
        assertEquals(value, local.getValue());
    }

    public void testRemoteInvoke() throws Exception {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "EJBHome");
        assertEquals(2, home.intMethod(1));

        Integer pk = new Integer(33);
        String value = "Thirty-Three";
        int number = 44;

        MockRemote remote = home.create(pk, value);
        assertEquals(1 + number + pk.intValue(), remote.intMethod(number));
        assertEquals(pk, remote.getPrimaryKey());
        assertEquals(value, remote.getValue());

        remote = home.findByPrimaryKey(pk);
        assertEquals(1 + number + pk.intValue(), remote.intMethod(number));
        assertEquals(pk, remote.getPrimaryKey());
        assertEquals(value, remote.getValue());
    }

    public void testFields() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "EJBLocalHome");
        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals("Hello", local.getValue());
        local.setValue("World");
        assertEquals("World", local.getValue());

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=1");
        assertTrue(rs.next());
        assertEquals("World", rs.getString(1));
        s.close();
        c.close();

        assertEquals("World", local.getValue());
    }

    public void testLocalLifeCycle() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs;

        // check that it is not there
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
        rs.close();

        // add new
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "EJBLocalHome");
        MockLocal local = home.create(new Integer(2), "Hello");
        rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=2");
        assertTrue(rs.next());
        assertEquals("Hello", rs.getString(1));
        rs.close();

        // find it
        local = home.findByPrimaryKey(new Integer(2));
        assertEquals("Hello", local.getValue());

        // check that it is actually in the database
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertTrue(rs.next());
        rs.close();

        // remove it
        local.remove();

        // verify it is really gone
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());

        try {
            local.intMethod(33);
            fail("Expected NoSuchObjectLocalException, but no exception was thrown");
        } catch(AssertionFailedError e) {
            throw e;
        } catch(NoSuchObjectLocalException e) {
            // expected
        } catch(Throwable e) {
            fail("Expected NoSuchObjectLocalException, but got " + e.getClass().getName());
        }

        try {
            local.getValue();
            fail("Expected NoSuchObjectLocalException, but no exception was thrown");
        } catch(AssertionFailedError e) {
            throw e;
        } catch(NoSuchObjectLocalException e) {
            // expected
        } catch(Throwable e) {
            e.printStackTrace();
            fail("Expected NoSuchObjectLocalException, but got " + e.getClass().getName());
        }

        try {
            local = home.findByPrimaryKey(new Integer(2));
            fail("Expected ObjectNotFoundException, but no exception was thrown");
        } catch(AssertionFailedError e) {
            throw e;
        } catch(ObjectNotFoundException e) {
            // expected
        } catch(Throwable e) {
            fail("Expected ObjectNotFoundException, but got " + e.getClass().getName());
        }

        rs.close();
        s.close();
        c.close();
    }

    public void testRemoteLifeCycle() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs;

        // check that it is not there
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
        rs.close();

        // add new
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "EJBHome");
        MockRemote remote = home.create(new Integer(2), "Hello");
        rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=2");
        assertTrue(rs.next());
        assertEquals("Hello", rs.getString(1));
        rs.close();

        // find it
        remote = home.findByPrimaryKey(new Integer(2));
        assertEquals("Hello", remote.getValue());

        // check that it is actually in the database
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertTrue(rs.next());
        rs.close();

        // remove it
        remote.remove();

        // verify it is really gone
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());

        try {
            remote.intMethod(33);
            fail("Expected NoSuchObjectException, but no exception was thrown");
        } catch(AssertionFailedError e) {
            throw e;
        } catch(NoSuchObjectException e) {
            // expected
        } catch(Throwable e) {
            e.printStackTrace();;
            fail("Expected NoSuchObjectException, but got " + e.getClass().getName());
        }

        try {
            remote.getValue();
            fail("Expected NoSuchObjectException, but no exception was thrown");
        } catch(AssertionFailedError e) {
            throw e;
        } catch(NoSuchObjectException e) {
            // expected
        } catch(Throwable e) {
            e.printStackTrace();
            fail("Expected NoSuchObjectException, but got " + e.getClass().getName());
        }

        try {
            remote = home.findByPrimaryKey(new Integer(2));
            fail("Expected ObjectNotFoundException, but no exception was thrown");
        } catch(AssertionFailedError e) {
            throw e;
        } catch(ObjectNotFoundException e) {
            // expected
        } catch(Throwable e) {
            fail("Expected ObjectNotFoundException, but got " + e.getClass().getName());
        }

        rs.close();
        s.close();
        c.close();
    }

    public void testSelect() throws Exception {
//        Connection c = initDatabase();
//        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);
//
//        assertEquals("Hello", home.singleSelect(new Integer(1)));
//        try {
//            home.singleSelect(new Integer(2));
//            fail("did not get ObjectNotFoundException");
//        } catch (ObjectNotFoundException e) {
             //ok
//        }
//
//        Collection result = home.multiSelect(new Integer(1));
//        assertEquals(1, result.size());
//        assertEquals("Hello", result.iterator().next());
//
//        result = home.multiSelect(new Integer(0));
//        assertEquals(0, result.size());
//
//        result = home.multiObject(new Integer(1));
//        assertEquals(1, result.size());
//        MockLocal local = (MockLocal) result.iterator().next();
//        assertEquals(new Integer(1), local.getPrimaryKey());
//
//        c.close();
    }

    protected void setUp() throws Exception {
        super.setUp();

        // initialize the database
        ds = new AxionDataSource("jdbc:axiondb:testdb");
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        try {
            s.execute("DROP TABLE");
        } catch (SQLException e) {
            // ignore
        }
        s.execute("CREATE TABLE MOCK(ID INTEGER, VALUE VARCHAR(50))");
        s.execute("INSERT INTO MOCK(ID, VALUE) VALUES(1, 'Hello')");
        s.close();
        c.close();

//        SimpleCommandFactory persistenceFactory = new SimpleCommandFactory(ds);
//        ArrayList queries = new ArrayList();
//        MethodSignature signature;
//
//        signature = new MethodSignature("ejbFindByPrimaryKey", new String[]{"java.lang.Object"});
//        persistenceFactory.defineQuery(signature, "SELECT ID FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
//        queries.add(new CMPQuery("Mock", false, signature, false, null));
//        signature = new MethodSignature("ejbLoad", new String[]{});
//        persistenceFactory.defineQuery(signature, "SELECT ID,VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0), new StringBinding(2, 1)});
//        queries.add(new CMPQuery(signature, false, null));
//        signature = new MethodSignature("ejbSelectSingleValue", new String[]{"java.lang.Integer"});
//        persistenceFactory.defineQuery(signature, "SELECT VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new StringBinding(1, 0)});
//        queries.add(new CMPQuery(signature, false, null));
//        signature = new MethodSignature("ejbSelectMultiValue", new String[]{"java.lang.Integer"});
//        persistenceFactory.defineQuery(signature, "SELECT VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new StringBinding(1, 0)});
//        queries.add(new CMPQuery(signature, true, null));
//        signature = new MethodSignature("ejbSelectMultiObject", new String[]{"java.lang.Integer"});
//        persistenceFactory.defineQuery(signature, "SELECT ID FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
//        queries.add(new CMPQuery("Mock", true, signature, true, null));
//
//        signature = new MethodSignature("ejbCreate", new String[]{"java.lang.Integer", "java.lang.String"});
//        persistenceFactory.defineUpdate(signature, "INSERT INTO MOCK(ID, VALUE) VALUES(?,?)", new Binding[]{new IntBinding(1, 0), new StringBinding(2, 1)});
//        signature = new MethodSignature("ejbRemove", new String[0]);
//        persistenceFactory.defineUpdate(signature, "DELETE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)});
//        signature = new MethodSignature("ejbStore", new String[0]);
//        persistenceFactory.defineUpdate(signature, "UPDATE MOCK SET VALUE = ? WHERE ID=?", new Binding[]{new StringBinding(1, 1), new IntBinding(2, 0)});
//
//        CMPConfiguration cmpConfig = new CMPConfiguration();
//        cmpConfig.persistenceFactory = persistenceFactory;
//        cmpConfig.queries = (CMPQuery[]) queries.toArray(new CMPQuery[0]);
//        cmpConfig.cmpFieldNames = new String[]{"id", "value"};
//        cmpConfig.relations = new CMRelation[]{};
//        cmpConfig.schema = "Mock";

        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(CONTAINER_NAME.getCanonicalName());
        builder.setEJBName("MockEJB");
        builder.setBeanClassName(MockCMPEJB.class.getName());
        builder.setHomeInterfaceName(MockHome.class.getName());
        builder.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        builder.setRemoteInterfaceName(MockRemote.class.getName());
        builder.setLocalInterfaceName(MockLocal.class.getName());
        builder.setPrimaryKeyClassName(Integer.class.getName());
        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return ContainerPolicy.Required;
            }
        });
        EJBSchema ejbSchema = new EJBSchema("MOCK");
        SQL92Schema sqlSchema = new SQL92Schema("MOCK", ds);
        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setComponentContext(new ReadOnlyContext());
        builder.setConnectionFactoryName("DefaultDatasource");

        EJBProxyFactory proxyFactory = new EJBProxyFactory(CONTAINER_NAME.getCanonicalName(), false, MockRemote.class, MockHome.class, MockLocal.class, MockLocalHome.class);
        EJB ejb = new EJB("MockEJB", "MOCK", proxyFactory);
        CMPField pkField = new CMPField("id", Integer.class, true);
        ejb.addCMPField(pkField);
        ejb.addCMPField(new CMPField("value", String.class, false));
        ejb.setPrimaryKeyField(pkField);
        ejbSchema.addEJB(ejb);

        Table table = new Table("MockEJB", "MOCK");
        table.addColumn(new Column("id", "ID", Integer.class, true));
        table.addColumn(new Column("value", "VALUE", String.class, false));
        sqlSchema.addTable(table);

        builder.setQueries(new HashMap());

        container = builder.createConfiguration();

        kernel = new Kernel("BeanManagedPersistenceTest");
        kernel.boot();


        kernel = new Kernel("ContainerManagedPersistenceTest");
        kernel.boot();

        GBeanMBean transactionManager = new GBeanMBean(TransactionManagerProxy.GBEAN_INFO);
        transactionManager.setAttribute("Delegate", new MockTransactionManager());
        start(TM_NAME, transactionManager);

        GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(TCA_NAME, trackedConnectionAssociator);

        GBeanMBean containerIndex = new GBeanMBean(ContainerIndex.GBEAN_INFO);
        containerIndex.setReferencePatterns("EJBContainers", Collections.singleton(CONTAINER_NAME));
        start(CI_NAME, containerIndex);

        GBeanMBean connectionProxyFactoryGBean = new GBeanMBean(MockConnectionProxyFactory.GBEAN_INFO);
        ObjectName connectionProxyFactoryObjectName = ObjectName.getInstance(JMXReferenceFactory.BASE_MANAGED_CONNECTION_FACTORY_NAME + "DefaultDatasource");
        kernel.loadGBean(connectionProxyFactoryObjectName, connectionProxyFactoryGBean);
        kernel.startGBean(connectionProxyFactoryObjectName);

        //start the ejb container
        container.setReferencePatterns("TransactionManager", Collections.singleton(TM_NAME));
        container.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(TCA_NAME));
        start(CONTAINER_NAME, container);
    }

    protected void tearDown() throws Exception {
        stop(TM_NAME);
        stop(TCA_NAME);
        stop(CONTAINER_NAME);
        kernel.shutdown();
        java.sql.Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
    }

    private void start(ObjectName name, GBeanMBean instance) throws Exception {
        kernel.loadGBean(name, instance);
        kernel.startGBean(name);
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }
}
