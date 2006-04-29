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
package org.openejb.deployment;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.jar.JarFile;
import java.util.Collections;
import java.rmi.MarshalledObject;

import javax.sql.DataSource;

import junit.extensions.TestDecorator;
import junit.framework.Protectable;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.geronimo.axis.builder.AxisBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.openejb.server.axis.WSContainerGBean;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentTestSuite extends TestDecorator implements DeploymentTestContants {

    private DeploymentHelper deploymentHelper = new DeploymentHelper();
    private final File moduleFile;

    private File tempDir;
    private DataSource dataSource;
    private ClassLoader applicationClassLoader;

    protected DeploymentTestSuite(Class testClass, File moduleFile) {
        super(new TestSuite(testClass));
        this.moduleFile = moduleFile;
    }

    public Kernel getKernel() {
        return deploymentHelper.kernel;
    }

    public ClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

    public void run(final TestResult result) {
        Protectable p = new Protectable() {
            public void protect() throws Exception {
                try {
                    setUp();
                    basicRun(result);
                } finally {
                    tearDown();
                }
            }
        };
        result.runProtected(this, p);
    }

    private void setUp() throws Exception {
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            str = str + ":org.apache.geronimo.naming";
        }
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);

        deploymentHelper.setUp();

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{moduleFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        try {
            WebServiceBuilder webServiceBuilder = new AxisBuilder();
            GBeanData linkData = new GBeanData(WSContainerGBean.GBEAN_INFO);
            OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(DeploymentHelper.DEFAULT_ENVIRONMENT, new AbstractNameQuery(deploymentHelper.listenerName), linkData, webServiceBuilder, deploymentHelper.kernel);
            OpenEJBReferenceBuilder ejbReferenceBuilder = new OpenEJBReferenceBuilder();

            tempDir = DeploymentUtil.createTempDir();
            EARConfigBuilder earConfigBuilder = new EARConfigBuilder(DeploymentHelper.DEFAULT_ENVIRONMENT,
                    new AbstractNameQuery(deploymentHelper.tcmName),
                    new AbstractNameQuery(deploymentHelper.ctcName),
                    new AbstractNameQuery(deploymentHelper.txTimerName),
                    new AbstractNameQuery(deploymentHelper.nonTxTimerName),
                    null,
                    new AbstractNameQuery(deploymentHelper.serverName),
                    null, // repository
                    moduleBuilder,
                    ejbReferenceBuilder,
                    null,// web
                    null,
                    resourceReferenceBuilder, // connector
                    null, // app client
                    serviceReferenceBuilder,
                    deploymentHelper.naming
            );

            JarFile jarFile = null;
            ConfigurationData configurationData = null;
            DeploymentContext context = null;
            ArtifactManager artifactManager = new DefaultArtifactManager();
            ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, Collections.EMPTY_SET, null);
            try {
                jarFile = DeploymentUtil.createJarFile(moduleFile);
                Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile, new ModuleIDBuilder());
                context = earConfigBuilder.buildConfiguration(false, earConfigBuilder.getConfigurationID(plan, jarFile, new ModuleIDBuilder()), plan, jarFile, Collections.singleton(deploymentHelper.configStore), artifactResolver, deploymentHelper.configStore);
                configurationData = context.getConfigurationData();
                // copy the configuration to force gbeans to serialize
                configurationData = (ConfigurationData) new MarshalledObject(configurationData).get();
                configurationData.setConfigurationStore(deploymentHelper.configStore);
            } finally {
                if (context != null) {
                    context.close();
                }
                if (jarFile != null) {
                    jarFile.close();
                }
            }

            dataSource = (DataSource) deploymentHelper.kernel.invoke(deploymentHelper.dataSourceName, "$getResource");
            Connection connection = null;
            Statement statement = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                statement.execute("CREATE TABLE SIMPLECMP(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE SEQUENCE PKGENCMP4_SEQ");
                statement.execute("CREATE SEQUENCE PKGENCMP5_SEQ");
                statement.execute("CREATE TABLE PKGENCMP(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE TABLE PKGENCMP2(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE TABLE PKGENCMP3(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE TABLE PKGENCMP4(ID INTEGER DEFAULT PKGENCMP4_SEQ.NEXTVAL, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE TABLE PKGENCMP5(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                // First two sequence rows initialized by OpenEJB wrappers around PK generators
                statement.execute("CREATE TABLE PKGENCMP_SEQ(NAME VARCHAR(50), VALUE INTEGER)");
                statement.execute("INSERT INTO PKGENCMP_SEQ VALUES('PKGENCMP3', 100)");
            } finally {
                JDBCUtil.close(statement);
                JDBCUtil.close(connection);
            }


            // start the configuration
            deploymentHelper.configurationManager.loadConfiguration(configurationData);
            deploymentHelper.configurationManager.startConfiguration(configurationData.getId());

            // get the configuration classloader
            applicationClassLoader = deploymentHelper.configurationManager.getConfiguration(configurationData.getId()).getConfigurationClassLoader();
        } catch (Error e) {
            DeploymentUtil.recursiveDelete(tempDir);
            throw e;
        } catch (Exception e) {
            DeploymentUtil.recursiveDelete(tempDir);
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void tearDown() throws Exception {
        DeploymentUtil.recursiveDelete(tempDir);

        try {
            deploymentHelper.tearDown();
        } catch (Exception ignored) {
            //we tried
        }

        if (dataSource != null) {
            Connection connection = null;
            Statement statement = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                statement.execute("SHUTDOWN");
            } finally {
                JDBCUtil.close(statement);
                JDBCUtil.close(connection);
                dataSource = null;
            }
        }
    }

}
