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
package org.openejb.entity.cmp.cmr;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.transaction.context.ContainerTransactionContext;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.xmlbeans.XmlObject;
import org.axiondb.jdbc.AxionDataSource;
import org.openejb.ContainerIndex;
import org.openejb.DeploymentHelper;
import org.openejb.deployment.CMPEntityBuilderTestUtil;
import org.openejb.deployment.MockConnectionProxyFactory;
import org.openejb.deployment.OpenEJBModuleBuilder;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.entity.cmp.CMPContainerBuilder;
import org.openejb.security.SecurityConfiguration;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.sql.sql92.SQL92Schema;

//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractCMRTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    
    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final J2eeContext j2eeContext = new J2eeContextImpl(j2eeDomainName, j2eeServerName, NameFactory.NULL, "MockModule", "testapp", NameFactory.J2EE_MODULE);
    protected static final ObjectName CI_NAME = JMXUtil.getObjectName("openejb.server:role=ContainerIndex");
    protected static final ObjectName C_NAME_A;
    protected static final ObjectName C_NAME_B;
    static {

        try {
            C_NAME_A = NameFactory.getEjbComponentName(null, null, null,null, "A", NameFactory.ENTITY_BEAN, j2eeContext);
            C_NAME_B = NameFactory.getEjbComponentName(null, null, null,null, "B", NameFactory.ENTITY_BEAN, j2eeContext);
        } catch (DeploymentException e) {
            throw new AssertionError(e);
        }
    }
    protected Kernel kernel;
    protected DataSource ds;
    protected EJBSchema ejbSchema;
    protected SQL92Schema sqlSchema;
    protected GlobalSchema cacheSchema;
    protected Object ahome;
    protected Object bhome;
    
    protected ContainerTransactionContext newTransactionContext() throws Exception {
        return (ContainerTransactionContext) kernel.invoke(DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME, "newContainerTransactionContext", null, null);
    }
    
    protected abstract void buildDBSchema(Connection c) throws Exception;
    protected abstract String getEjbJarDD();
    protected abstract String getOpenEjbJarDD();
    protected abstract EJBClass getA();
    protected abstract EJBClass getB();
    
    protected void setUp() throws Exception {
//        MysqlDataSource mysqlDataSource = new MysqlDataSource();
//        mysqlDataSource.setUser("geronimo");
//        mysqlDataSource.setPassword("geronimo");
//        mysqlDataSource.setURL("jdbc:mysql://localhost/geronimo");
//   
//        ds = mysqlDataSource;
        ds = new AxionDataSource("jdbc:axiondb:testdb");
        Connection c = ds.getConnection("root", null);
        buildDBSchema(c);

        kernel = DeploymentHelper.setUpKernelWithTransactionManager("ContainerManagedPersistenceTest");
        DeploymentHelper.setUpTimer(kernel);

        TransactionManager tm = (TransactionManager) MBeanProxyFactory.getProxy(
            TransactionManager.class, kernel.getMBeanServer(), 
            DeploymentHelper.TRANSACTIONMANAGER_NAME );
        TransactionManagerDelegate tmDelegate =  new TransactionManagerDelegate();
        tmDelegate.setTransactionManager(tm);

        ejbSchema = new EJBSchema("Mock");
        sqlSchema = new SQL92Schema("Mock", ds);
        cacheSchema = new GlobalSchema("Mock");

        File ejbJarFile = new File(basedir, getEjbJarDD());
        File openejbJarFile = new File(basedir, getOpenEjbJarDD());
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();
        
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder();
        CMPEntityBuilderTestUtil builder = new CMPEntityBuilderTestUtil(moduleBuilder);
        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    null,
                    null,
                    j2eeDomainName,
                    j2eeServerName,
                    NameFactory.NULL,
                    null,
                    null,
                    null,
                    null,
                    null);

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            builder.buildCMPSchema(earContext, j2eeContext, ejbJarType, openejbJarType, cl, ejbSchema, sqlSchema, cacheSchema);

            GBeanMBean containerIndex = new GBeanMBean(ContainerIndex.GBEAN_INFO);
            Set patterns = new HashSet();
            patterns.add(C_NAME_A);
            patterns.add(C_NAME_B);
            containerIndex.setReferencePatterns("EJBContainers", patterns);
            start(CI_NAME, containerIndex);

            GBeanMBean connectionProxyFactoryGBean = new GBeanMBean(MockConnectionProxyFactory.GBEAN_INFO);
            ObjectName connectionProxyFactoryObjectName = NameFactory.getResourceComponentName(null, null, null, "jcamodule", "testcf", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            kernel.loadGBean(connectionProxyFactoryObjectName, connectionProxyFactoryGBean);
            kernel.startGBean(connectionProxyFactoryObjectName);

            setUpContainer(ejbSchema.getEJB("A"), getA().bean, getA().home, getA().local, C_NAME_A, tmDelegate);
            setUpContainer(ejbSchema.getEJB("B"), getB().bean, getB().home, getB().local, C_NAME_B, tmDelegate);

            ahome = kernel.getAttribute(C_NAME_A, "ejbLocalHome");
            bhome = kernel.getAttribute(C_NAME_B, "ejbLocalHome");
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }
    
    private void setUpContainer(EJB ejb, Class beanClass, Class homeClass, Class localClass, ObjectName containerName, TransactionManagerDelegate tmDelegate) throws Exception {
        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(containerName.getCanonicalName());
        builder.setEJBName(ejb.getName());
        builder.setBeanClassName(beanClass.getName());
        builder.setHomeInterfaceName(null);
        builder.setLocalHomeInterfaceName(homeClass.getName());
        builder.setRemoteInterfaceName(null);
        builder.setLocalInterfaceName(localClass.getName());
        builder.setPrimaryKeyClassName(ejb.getPrimaryKeyClass().getName());
        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return ContainerPolicy.Required;
            }
        });
        builder.setSecurityConfiguration(new SecurityConfiguration());
        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setGlobalSchema(cacheSchema);
        builder.setComponentContext(new ReadOnlyContext());
//        builder.setConnectionFactoryName("defaultDatasource");
        builder.setTransactionManagerDelegate(tmDelegate);
        builder.setQueries(new HashMap());

        GBeanMBean container = builder.createConfiguration();

        container.setReferencePatterns("TransactionContextManager", Collections.singleton(DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME));
        container.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME));
        container.setReferencePattern("Timer", DeploymentHelper.TRANSACTIONALTIMER_NAME);
        start(containerName, container);
    }
    
    protected void tearDown() throws Exception {
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
