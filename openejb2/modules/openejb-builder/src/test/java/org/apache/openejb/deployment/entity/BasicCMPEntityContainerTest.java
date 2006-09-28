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
package org.apache.openejb.deployment.entity;

import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ObjectNotFoundException;
import javax.sql.DataSource;

import junit.framework.AssertionFailedError;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.axiondb.jdbc.AxionDataSource;
import org.apache.openejb.deployment.CmpBuilder;
import org.apache.openejb.deployment.DeploymentHelper;
import org.apache.openejb.deployment.MockConnectionProxyFactory;
import org.apache.openejb.entity.cmp.EntitySchema;
import org.apache.openejb.entity.cmp.ModuleSchema;
import org.apache.openejb.entity.cmp.TranqlModuleCmpEngineGBean;

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
//        } catch (Throwable e) {
//            fail("Expected NoSuchObjectLocalException, but got " + e.getClass().getName());
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

        // create a new configuration
        ConfigurationData configurationData = new ConfigurationData(TEST_CONFIGURATION_ID, kernel.getNaming());
        configurationData.getEnvironment().addDependency(new Dependency(BOOTSTRAP_ID, ImportType.ALL));

        // add the datasource
        GBeanData connectionProxyFactoryGBean = configurationData.addGBean("testcf", MockConnectionProxyFactory.GBEAN_INFO);

        // create the cmp schema
        ModuleSchema moduleSchema = new ModuleSchema("MockModule");
        EntitySchema entitySchema = moduleSchema.addEntity("MockEJB");
        entitySchema.setTableName("mock");
        entitySchema.setContainerId(CONTAINER_NAME.toString());
        entitySchema.setEjbClassName(MockCMPEJB.class.getName());
        entitySchema.setHomeInterfaceName(MockHome.class.getName());
        entitySchema.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        entitySchema.setRemoteInterfaceName(MockRemote.class.getName());
        entitySchema.setLocalInterfaceName(MockLocal.class.getName());
        entitySchema.setPkClassName(Integer.class.getName());
        entitySchema.addCmpField("id", "id", Integer.class, true);
        entitySchema.addCmpField("value", "value", String.class, false);

        // create the module cmp engine
        GBeanData moduleCmpEngineBeanData = configurationData.addGBean("ModuleCmpEngine", TranqlModuleCmpEngineGBean.GBEAN_INFO);
        moduleCmpEngineBeanData.setAttribute("moduleSchema", moduleSchema);
        moduleCmpEngineBeanData.setReferencePattern("transactionManager", tmName);
        moduleCmpEngineBeanData.setReferencePattern("connectionFactory", connectionProxyFactoryGBean.getAbstractName());

        // create the cmp bean
        CmpBuilder builder = new CmpBuilder();
        builder.setContainerId(CONTAINER_NAME.toString());
        builder.setEjbName("MockEJB");
        builder.setEjbContainerName(cmpEjbContainerName);
        builder.setBeanClassName(MockCMPEJB.class.getName());
        builder.setHomeInterfaceName(MockHome.class.getName());
        builder.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        builder.setRemoteInterfaceName(MockRemote.class.getName());
        builder.setLocalInterfaceName(MockLocal.class.getName());
        builder.setPrimaryKeyClassName(Integer.class.getName());
        builder.setModuleCmpEngineName(moduleCmpEngineBeanData.getAbstractName());
        builder.setCmp2(true);
        GBeanData deployment = builder.createConfiguration();

        //start the ejb configuration
        configurationData.addGBean(deployment);
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(TEST_CONFIGURATION_ID);
    }

    protected void tearDown() throws Exception {
        Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
        super.tearDown();
    }
}
