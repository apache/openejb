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
package org.openejb.deployment.entity.cmp.cmr;


import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.axiondb.jdbc.AxionDataSource;
import org.openejb.DeploymentIndexGBean;
import org.openejb.deployment.CmpSchemaBuilder;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.KernelHelper;
import org.openejb.deployment.MockConnectionProxyFactory;
import org.openejb.deployment.CmpBuilder;
import org.openejb.deployment.TranqlCmpSchemaBuilder;
import org.openejb.deployment.XmlBeansHelper;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJBSchema;
import org.tranql.sql.SQLSchema;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractCMRTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final J2eeContext j2eeContext = new J2eeContextImpl(j2eeDomainName, j2eeServerName, NameFactory.NULL, NameFactory.EJB_MODULE, "MockModule", "testapp", NameFactory.J2EE_MODULE);
    protected static final ObjectName CI_NAME = JMXUtil.getObjectName("openejb.server:role=ContainerIndex");
    protected static final ObjectName C_NAME_A;
    protected static final ObjectName C_NAME_B;

    static {
        try {
            C_NAME_A = NameFactory.getEjbComponentName(null, null, null, null, "A", NameFactory.ENTITY_BEAN, j2eeContext);
            C_NAME_B = NameFactory.getEjbComponentName(null, null, null, null, "B", NameFactory.ENTITY_BEAN, j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
    }

//    private Repository repository = null;
    protected Kernel kernel;
    protected DataSource ds;
    protected EJBSchema ejbSchema;
    protected SQLSchema sqlSchema;
    protected GlobalSchema cacheSchema;
    protected Object ahome;
    protected Object bhome;

    protected TransactionContext newTransactionContext() throws Exception {
        return (TransactionContext) kernel.invoke(DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME, "newContainerTransactionContext", null, null);
    }


    protected abstract void buildDBSchema(Connection c) throws Exception;

    protected abstract String getEjbJarDD();

    protected abstract String getOpenEjbJarDD();

    protected abstract EJBClass getA();

    protected abstract EJBClass getB();

    protected void setUp() throws Exception {
        ds = new AxionDataSource("jdbc:axiondb:testdb");
        Connection c = ds.getConnection("root", null);
        buildDBSchema(c);

        kernel = DeploymentHelper.setUpKernelWithTransactionManager();

        File ejbJarFile = new File(basedir, getEjbJarDD());
        File openejbJarFile = new File(basedir, getOpenEjbJarDD());
        EjbJarType ejbJarType= XmlBeansHelper.loadEjbJar(ejbJarFile);
        OpenejbOpenejbJarType openejbJarType = XmlBeansHelper.loadOpenEjbJar(openejbJarFile);

        File tempDir = DeploymentUtil.createTempDir();

        try {
            URI configId = new URI("test");
            EARContext earContext = new EARContext(tempDir,
                    configId,
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID_LIST,
                    kernel,
                    NameFactory.NULL,
                    DeploymentHelper.TRANSACTIONMANAGER_NAME,
                    DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME,
                    DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME,
                    DeploymentHelper.TRANSACTIONALTIMER_NAME,
                    DeploymentHelper.NONTRANSACTIONALTIMER_NAME,
                    null,
                    null);

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            // create module cmp enging GBeanData
            EJBModule ejbModule = new EJBModule(true, configId, null, null, tempDir.getAbsoluteFile().toURI().toString(), ejbJarType, openejbJarType, "");
            CmpSchemaBuilder cmpSchemaBuilder = new TranqlCmpSchemaBuilder();
            cmpSchemaBuilder.addBeans(earContext, j2eeContext, ejbModule, cl);
            ObjectName moduleCmpEngineName = ejbModule.getModuleCmpEngineName();
            GBeanData moduleCmpEngineGBeanData = earContext.getGBeanInstance(moduleCmpEngineName);

            // start the connection factory
            ObjectName connectionProxyFactoryObjectName = (ObjectName) moduleCmpEngineGBeanData.getReferencePatterns("connectionFactory").iterator().next();
            GBeanData connectionProxyFactoryGBean = new GBeanData(connectionProxyFactoryObjectName, MockConnectionProxyFactory.GBEAN_INFO);
            kernel.loadGBean(connectionProxyFactoryGBean, cl);
            kernel.startGBean(connectionProxyFactoryObjectName);
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(connectionProxyFactoryObjectName));

            // start the module cmp engine
            kernel.loadGBean(moduleCmpEngineGBeanData, cl);
            kernel.startGBean(moduleCmpEngineName);
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(moduleCmpEngineName));


            GBeanData containerIndex = new GBeanData(DeploymentIndexGBean.GBEAN_INFO);
            Set patterns = new HashSet();
            patterns.add(C_NAME_A);
            patterns.add(C_NAME_B);
            containerIndex.setReferencePatterns("EjbDeployments", patterns);
            start(CI_NAME, containerIndex);

            setUpContainer("A", getA().bean, getA().home, getA().local, Integer.class, C_NAME_A, moduleCmpEngineName);
            setUpContainer("B", getB().bean, getB().home, getB().local, Integer.class, C_NAME_B, moduleCmpEngineName);

            ahome = kernel.getAttribute(C_NAME_A, "ejbLocalHome");
            bhome = kernel.getAttribute(C_NAME_B, "ejbLocalHome");
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    private void setUpContainer(String ejbName, Class beanClass, Class localHomeClass, Class localClass, Class primaryKeyClass, ObjectName containerName, ObjectName moduleCmpEngineObjectName) throws Exception {
        CmpBuilder builder = new CmpBuilder();
        builder.setContainerId(containerName);
        builder.setEjbName(ejbName);
        builder.setEjbContainerName(DeploymentHelper.CMP_EJB_CONTAINER_NAME);
        builder.setBeanClassName(beanClass.getName());
        builder.setLocalHomeInterfaceName(localHomeClass.getName());
        builder.setLocalInterfaceName(localClass.getName());
        builder.setPrimaryKeyClassName(primaryKeyClass.getName());
        builder.setModuleCmpEngineName(moduleCmpEngineObjectName);
        builder.setCmp2(true);

        GBeanData container = builder.createConfiguration();
        start(containerName, container);
    }

    protected void tearDown() throws Exception {
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

