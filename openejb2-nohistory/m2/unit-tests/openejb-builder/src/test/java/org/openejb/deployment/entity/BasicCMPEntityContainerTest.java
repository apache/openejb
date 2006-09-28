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
package org.openejb.deployment.entity;

import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ObjectNotFoundException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.axiondb.jdbc.AxionDataSource;
import org.openejb.ContainerIndex;
import org.openejb.deployment.CMPContainerBuilder;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.MockConnectionProxyFactory;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;
import org.tranql.cache.CacheSlot;
import org.tranql.cache.CacheTable;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBQueryBuilder;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejbqlcompiler.DerbyEJBQLCompilerFactory;
import org.tranql.identity.IdentityDefinerBuilder;
import org.tranql.query.SchemaMapper;
import org.tranql.query.UpdateCommand;
import org.tranql.sql.Column;
import org.tranql.sql.Table;
import org.tranql.sql.sql92.SQL92Schema;

/**
 * @version $Revision$ $Date$
 */
public class BasicCMPEntityContainerTest extends TestCase {
    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private J2eeContext j2eeContext = new J2eeContextImpl(j2eeDomainName, j2eeServerName, "testapp", NameFactory.EJB_MODULE, "testejbmodule", "testapp", NameFactory.J2EE_APPLICATION);
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("openejb.server:ejb=Mock");
    private static final ObjectName CI_NAME = JMXUtil.getObjectName("openejb.server:role=ContainerIndex");
    private Kernel kernel;
    private GBeanData container;

    private DataSource ds;

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
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
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
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

    public void testTimeout() throws Exception {
        MockLocalHome localHome = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal local = localHome.create(new Integer(1), null);
        local.startTimer();
        Thread.sleep(400L);
        int timeoutCount = local.getTimeoutCount();
        assertEquals(1, timeoutCount);
    }

    public void testFields() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
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
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
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
        } catch (AssertionFailedError e) {
            throw e;
        } catch (NoSuchObjectLocalException e) {
            // expected
        } catch (Throwable e) {
            fail("Expected NoSuchObjectLocalException, but got " + e.getClass().getName());
        }

        try {
            local.getValue();
            fail("Expected NoSuchObjectLocalException, but no exception was thrown");
        } catch (AssertionFailedError e) {
            throw e;
        } catch (NoSuchObjectLocalException e) {
            // expected
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Expected NoSuchObjectLocalException, but got " + e.getClass().getName());
        }

        try {
            local = home.findByPrimaryKey(new Integer(2));
            fail("Expected ObjectNotFoundException, but no exception was thrown");
        } catch (AssertionFailedError e) {
            throw e;
        } catch (ObjectNotFoundException e) {
            // expected
        } catch (Throwable e) {
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
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
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
        } catch (AssertionFailedError e) {
            throw e;
        } catch (NoSuchObjectException e) {
            // expected
        } catch (Throwable e) {
            e.printStackTrace();
            ;
            fail("Expected NoSuchObjectException, but got " + e.getClass().getName());
        }

        try {
            remote.getValue();
            fail("Expected NoSuchObjectException, but no exception was thrown");
        } catch (AssertionFailedError e) {
            throw e;
        } catch (NoSuchObjectException e) {
            // expected
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Expected NoSuchObjectException, but got " + e.getClass().getName());
        }

        try {
            remote = home.findByPrimaryKey(new Integer(2));
            fail("Expected ObjectNotFoundException, but no exception was thrown");
        } catch (AssertionFailedError e) {
            throw e;
        } catch (ObjectNotFoundException e) {
            // expected
        } catch (Throwable e) {
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

        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
        DeploymentHelper.setUpTimer(kernel);


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
        builder.setCMP2(true);
        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicyType getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return TransactionPolicyType.Required;
            }
        });
        EJBSchema ejbSchema = new EJBSchema("MOCK");
        SQL92Schema sqlSchema = new SQL92Schema("MOCK", ds, new DerbyEJBQLCompilerFactory());
        GlobalSchema globalSchema = new GlobalSchema("MOCK");
        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setGlobalSchema(globalSchema);
        builder.setComponentContext(new HashMap());
//        builder.setConnectionFactoryName("defaultDatasource");

        EJBProxyFactory proxyFactory = new EJBProxyFactory(CONTAINER_NAME.getCanonicalName(), false, MockRemote.class, MockHome.class, MockLocal.class, MockLocalHome.class);
        EJB ejb = new EJB("MockEJB", "MOCK", Integer.class, proxyFactory, null, false);
        CMPField pkField = new CMPField("id", Integer.class, true);
        ejb.addCMPField(pkField);
        ejb.addCMPField(new CMPField("value", String.class, false));
        ejbSchema.addEJB(ejb);

        Table table = new Table("MockEJB", "MOCK");
        table.addColumn(new Column("id", "id", Integer.class, true));
        table.addColumn(new Column("value", "VALUE", String.class, false));
        sqlSchema.addTable(table);

        SchemaMapper mapper = new SchemaMapper(sqlSchema);
        EJBQueryBuilder queryBuilder = new EJBQueryBuilder(new IdentityDefinerBuilder(ejbSchema, globalSchema), sqlSchema);
        UpdateCommand createCommand = mapper.transform(queryBuilder.buildCreate("MockEJB"));
        UpdateCommand storeCommand = mapper.transform(queryBuilder.buildStore("MockEJB"));
        UpdateCommand removeCommand = mapper.transform(queryBuilder.buildRemove("MockEJB"));
        CacheSlot slots[] = new CacheSlot[2];
        slots[0] = new CacheSlot("id", Integer.class, null);
        slots[1] = new CacheSlot("value", String.class, null);
        CacheTable cacheTable = new CacheTable("MockEJB", slots, null, createCommand, storeCommand, removeCommand);
        globalSchema.addCacheTable(cacheTable);

        container = builder.createConfiguration(CONTAINER_NAME, DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME, DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME, null);

        GBeanData containerIndex = new GBeanData(ContainerIndex.GBEAN_INFO);
        containerIndex.setReferencePatterns("EJBContainers", Collections.singleton(CONTAINER_NAME));
        start(CI_NAME, containerIndex);

        ObjectName connectionProxyFactoryObjectName = NameFactory.getComponentName(null, null, null, NameFactory.JCA_RESOURCE, "jcamodule", "testcf", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
        GBeanData connectionProxyFactoryGBean = new GBeanData(connectionProxyFactoryObjectName, MockConnectionProxyFactory.GBEAN_INFO);
        kernel.loadGBean(connectionProxyFactoryGBean, this.getClass().getClassLoader());
        kernel.startGBean(connectionProxyFactoryObjectName);

        //start the ejb container
        container.setReferencePattern("Timer", DeploymentHelper.TRANSACTIONALTIMER_NAME);

        start(CONTAINER_NAME, container);
    }

    protected void tearDown() throws Exception {
        stop(CONTAINER_NAME);
        kernel.shutdown();
        java.sql.Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
    }

    private void start(ObjectName name, GBeanData instance) throws Exception {
        instance.setName(name);
        kernel.loadGBean(instance, this.getClass().getClassLoader());
        kernel.startGBean(name);
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }
}
