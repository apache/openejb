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
package org.openejb.deployment.entity.cmp.ejbql;


import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.xmlbeans.XmlObject;
import org.axiondb.jdbc.AxionDataSource;
import org.openejb.ContainerIndex;
import org.openejb.deployment.CMPContainerBuilder;
import org.openejb.deployment.CMPEntityBuilderTestUtil;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.KernelHelper;
import org.openejb.deployment.MockConnectionProxyFactory;
import org.openejb.deployment.OpenEJBModuleBuilder;
import org.openejb.deployment.Schemata;
import org.openejb.deployment.pkgen.TranQLPKGenBuilder;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.sql.SQLSchema;

/**
 * @version $Revision$ $Date$
 */
public class EJBQLTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final J2eeContext j2eeContext = new J2eeContextImpl(j2eeDomainName, j2eeServerName, NameFactory.NULL, NameFactory.EJB_MODULE, "MockModule", "testapp", NameFactory.J2EE_MODULE);
    protected static final ObjectName CI_NAME = JMXUtil.getObjectName("openejb.server:role=ContainerIndex");
    protected static final ObjectName C_NAME_A;

    static {
        try {
            C_NAME_A = NameFactory.getEjbComponentName(null, null, null, null, "A", NameFactory.ENTITY_BEAN, j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
    }

    private Repository repository = null;
    private Kernel kernel;
    private DataSource ds;
    private EJBSchema ejbSchema;
    private SQLSchema sqlSchema;
    private GlobalSchema cacheSchema;
    private AHome aHome;
    private ALocalHome aLocalHome;
    private TransactionManager tm;

    public void testHomeFindTest() throws Exception {
        ARemote a = aHome.findTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    public void testLocalHomeFindTest() throws Exception {
        ALocal a = aLocalHome.findTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    public void testSelectTest() throws Exception {
        ALocal a = aLocalHome.selectTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    private void buildDBSchema(Connection c) throws Exception {
        Statement s = c.createStatement();
        try {
            s.execute("DROP TABLE A");
        } catch (SQLException e) {
            // ignore
        }

        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");

        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'test')");
        s.close();
        c.close();
    }

    private String getEjbJarDD() {
        return "src/test/test-cmp/ejb-ql/ejb-jar.xml";
    }

    private String getOpenEjbJarDD() {
        return "src/test/test-cmp/ejb-ql/openejb-jar.xml";
    }

    protected void setUp() throws Exception {
        ds = new AxionDataSource("jdbc:axiondb:testdb");

        Connection c = ds.getConnection("root", null);
        buildDBSchema(c);

        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
        DeploymentHelper.setUpTimer(kernel);

        tm = (TransactionManager) kernel.getProxyManager().createProxy(DeploymentHelper.TRANSACTIONMANAGER_NAME, TransactionManager.class);
        TransactionManagerDelegate tmDelegate = new TransactionManagerDelegate();

        tmDelegate.setTransactionManager(tm);

        File ejbJarFile = new File(basedir, getEjbJarDD());
        File openejbJarFile = new File(basedir, getOpenEjbJarDD());
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(null, null, null, null, repository, kernel);
        CMPEntityBuilderTestUtil builder = new CMPEntityBuilderTestUtil(moduleBuilder);
        TranQLPKGenBuilder pkGen = new TranQLPKGenBuilder();
        File tempDir = DeploymentUtil.createTempDir();

        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID_LIST,
                    kernel,
                    NameFactory.NULL,
                    null,
                    null,
                    null,
                    null,
                    null, null);

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Schemata schemata = builder.buildCMPSchema(earContext, j2eeContext, ejbJarType, openejbJarType, cl, pkGen, ds);

            ejbSchema = schemata.getEjbSchema();
            sqlSchema = schemata.getSqlSchema();
            cacheSchema = schemata.getGlobalSchema();
            
            GBeanData containerIndex = new GBeanData(ContainerIndex.GBEAN_INFO);
            Set patterns = new HashSet();
            patterns.add(C_NAME_A);
            containerIndex.setReferencePatterns("EJBContainers", patterns);
            start(CI_NAME, containerIndex);

            ObjectName connectionProxyFactoryObjectName = NameFactory.getComponentName(null, null, null, NameFactory.JCA_RESOURCE, "jcamodule", "testcf", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            GBeanData connectionProxyFactoryGBean = new GBeanData(connectionProxyFactoryObjectName, MockConnectionProxyFactory.GBEAN_INFO);
            kernel.loadGBean(connectionProxyFactoryGBean, this.getClass().getClassLoader());
            kernel.startGBean(connectionProxyFactoryObjectName);

            setUpContainer(ejbSchema.getEJB("A"), ABean.class, AHome.class, ARemote.class, ALocalHome.class, ALocal.class, C_NAME_A, tmDelegate);

            aLocalHome = (ALocalHome) kernel.getAttribute(C_NAME_A, "ejbLocalHome");
            aHome = (AHome) kernel.getAttribute(C_NAME_A, "ejbHome");
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }


    private void setUpContainer(EJB ejb, Class beanClass, Class homeClass, Class remoteClass, Class localHomeClass, Class localClass, ObjectName containerName, TransactionManagerDelegate tmDelegate) throws Exception {
        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(containerName.getCanonicalName());
        builder.setEJBName(ejb.getName());
        builder.setBeanClassName(beanClass.getName());
        builder.setHomeInterfaceName(homeClass.getName());
        builder.setLocalHomeInterfaceName(localHomeClass.getName());
        builder.setRemoteInterfaceName(remoteClass.getName());
        builder.setLocalInterfaceName(localHomeClass.getName());
        builder.setPrimaryKeyClassName(ejb.getPrimaryKeyClass().getName());

        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicyType getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return TransactionPolicyType.Required;
            }
        });

        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setGlobalSchema(cacheSchema);
        builder.setComponentContext(new HashMap());
        builder.setTransactionManagerDelegate(tmDelegate);

        GBeanData container = builder.createConfiguration(containerName, DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME, DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME, null);

        container.setReferencePattern("Timer", DeploymentHelper.TRANSACTIONALTIMER_NAME);
        start(containerName, container);
    }

    protected void tearDown() throws Exception {
        kernel.getProxyManager().destroyProxy(tm);
        kernel.shutdown();
        java.sql.Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
    }

    private void start(ObjectName name, GBeanData instance) throws Exception {
        instance.setName(name);
        kernel.loadGBean(instance, this.getClass().getClassLoader());
        kernel.startGBean(name);
    }

    protected class EJBClass {
        public EJBClass(Class bean, Class home, Class local) {
            this.bean = bean;
            this.home = home;
            this.local = local;
        };
        public Class bean;
        public Class home;
        public Class local;
    }
}