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

import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ObjectNotFoundException;
import javax.sql.DataSource;

import junit.framework.AssertionFailedError;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.axiondb.jdbc.AxionDataSource;
import org.openejb.deployment.CMPContainerBuilder;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.entity.cmp.CMPEJBContainer;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;
import org.tranql.builder.EJBQueryBuilder;
import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.cache.CacheSlot;
import org.tranql.cache.CacheTable;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejbqlcompiler.DerbyEJBQLCompilerFactory;
import org.tranql.intertxcache.FrontEndCacheDelegate;
import org.tranql.query.SchemaMapper;
import org.tranql.query.UpdateCommand;
import org.tranql.sql.Column;
import org.tranql.sql.Table;
import org.tranql.sql.sql92.SQL92Schema;

/**
 * @version $Revision$ $Date$
 */
public class BasicCMPEntityContainerTest extends DeploymentHelper {

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
        ds = new WrapperDataSource();
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

//        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
//        DeploymentHelper.setUpTimer(kernel);


        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(CONTAINER_NAME.toURI().toString());
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
        builder.setFrontEndCacheDelegate(new FrontEndCacheDelegate());
//        builder.setConnectionFactoryName("defaultDatasource");

        EJBProxyFactory proxyFactory = new EJBProxyFactory(CONTAINER_NAME.toURI().toString(), false, MockRemote.class, MockHome.class, MockLocal.class, MockLocalHome.class);
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
        EJBQueryBuilder queryBuilder = new EJBQueryBuilder(new IdentityDefinerBuilder(ejbSchema, globalSchema));
        UpdateCommand createCommand = mapper.transform(queryBuilder.buildCreate("MockEJB"));
        UpdateCommand storeCommand = mapper.transform(queryBuilder.buildStore("MockEJB"));
        UpdateCommand removeCommand = mapper.transform(queryBuilder.buildRemove("MockEJB"));
        CacheSlot slots[] = new CacheSlot[2];
        slots[0] = new CacheSlot("id", Integer.class, null);
        slots[1] = new CacheSlot("value", String.class, null);
        CacheTable cacheTable = new CacheTable("MockEJB", slots, null, createCommand, storeCommand, removeCommand);
        globalSchema.addCacheTable(cacheTable);

        GBeanData container = new GBeanData(CONTAINER_NAME, CMPEJBContainer.GBEAN_INFO);
                builder.createConfiguration(
                new AbstractNameQuery(tcmName),
                new AbstractNameQuery(ctcName),
                null, container);

        container.setReferencePattern("Timer", txTimerName);
        ConfigurationData configurationData = new ConfigurationData(testConfigurationArtifact, kernel.getNaming());
        configurationData.getEnvironment().addDependency(new Dependency(baseId, ImportType.ALL));
        configurationData.addGBean(container);
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(testConfigurationArtifact);
    }

    protected void tearDown() throws Exception {
        java.sql.Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
        super.tearDown();
    }

    private static class WrapperDataSource implements DataSource, Serializable {
        private static final long serialVersionUID = -1035588858939680910L;
        private transient DataSource ds;

        public Connection getConnection() throws SQLException {
            return getDs().getConnection();
        }

        public Connection getConnection(String string, String string1) throws SQLException {
            return getDs().getConnection(string, string1);
        }

        public PrintWriter getLogWriter() throws SQLException {
            return getDs().getLogWriter();
        }

        public void setLogWriter(PrintWriter printWriter) throws SQLException {
            getDs().setLogWriter(printWriter);
        }

        public void setLoginTimeout(int i) throws SQLException {
            getDs().setLoginTimeout(i);
        }

        public int getLoginTimeout() throws SQLException {
            return getDs().getLoginTimeout();
        }

        private DataSource getDs() {
            if (ds == null) {
                ds = new AxionDataSource("jdbc:axiondb:testdb");
            }
            return ds;
        }
    }
}
