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
package org.openejb.nova.entity;

import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import junit.framework.TestCase;
import org.hsqldb.jdbcDataSource;

import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.entity.cmp.CMPEntityContainer;
import org.openejb.nova.entity.cmp.CMPQuery;
import org.openejb.nova.entity.cmp.SimpleCommandFactory;
import org.openejb.nova.persistence.jdbc.Binding;
import org.openejb.nova.persistence.jdbc.binding.IntBinding;
import org.openejb.nova.util.ServerUtil;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BasicCMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private MBeanServer mbServer;
    private EntityContainerConfiguration config;
    private CMPEntityContainer container;
    private final jdbcDataSource ds = new jdbcDataSource();
    private Connection c;

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
    }

    public void testLifeCycle() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();

        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());

        MockLocal local = home.create(new Integer(2));
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertTrue(rs.next());

        local = home.findByPrimaryKey(new Integer(2));

        local.remove();
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
    }

    public void XtestFields() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals("Hello", local.getString());
    }

    protected void setUp() throws Exception {
        super.setUp();

        ds.setDatabase(".");
        ds.setUser("sa");
        ds.setPassword("");
        c = ds.getConnection();
        Statement s = c.createStatement();
        s.execute("CREATE TABLE MOCK(ID INTEGER, VALUE VARCHAR(50))");
        s.execute("INSERT INTO MOCK(ID, VALUE) VALUES(1, 'Hello')");
        s.close();

        mbServer = ServerUtil.newRemoteServer();

        config = new EntityContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockCMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.txnManager = new MockTransactionManager();
        config.pkClassName = Integer.class.getName();

        SimpleCommandFactory persistenceFactory = new SimpleCommandFactory(ds);
        ArrayList queries = new ArrayList();
        MethodSignature signature;

        signature = new MethodSignature(MockCMPEJB.class.getName(), "ejbFindByPrimaryKey", new String[]{"java.lang.Object"});
        persistenceFactory.defineQuery(signature, "SELECT ID FROM Mock WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
        queries.add(new CMPQuery(signature, false, null));

        signature = new MethodSignature(MockCMPEJB.class.getName(), "ejbCreate", new String[]{"java.lang.Integer"});
        persistenceFactory.defineUpdate(signature, "INSERT INTO MOCK(ID) VALUES(?)", new Binding[]{new IntBinding(1, 0)});
        signature = new MethodSignature(MockCMPEJB.class.getName(), "ejbRemove", new String[0]);
        persistenceFactory.defineUpdate(signature, "DELETE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)});

        container = new CMPEntityContainer(config, persistenceFactory, (CMPQuery[]) queries.toArray(new CMPQuery[0]));
        mbServer.registerMBean(container, CONTAINER_NAME);
        container.start();
    }

    protected void tearDown() throws Exception {
        c.close();

        container.stop();
        ServerUtil.stopRemoteServer(mbServer);
        super.tearDown();
    }
}
