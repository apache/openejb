/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.MarshalledObject;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.jar.JarFile;

import javax.sql.DataSource;

import junit.extensions.TestDecorator;
import junit.framework.Protectable;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.geronimo.axis.builder.AxisBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.openejb.server.axis.WSContainerGBean;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentTestSuite extends TestDecorator implements DeploymentTestContants {
    private DeploymentHelper deploymentHelper = new DeploymentHelper();
    private final String moduleFileString;

    private File tempDir;
    private DataSource dataSource;
    private ClassLoader applicationClassLoader;

    protected DeploymentTestSuite(Class testClass, String moduleFile) {
        super(new TestSuite(testClass));
        this.moduleFileString = moduleFile;
    }

    public Kernel getKernel() {
        return deploymentHelper.kernel;
    }

    public ClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

    public String getName() {
        return "DeploymentTestSuite";
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
        File moduleFile = new File(moduleFileString);
        JarFile jarFile = null;
        try {
            jarFile = DeploymentUtil.createJarFile(moduleFile);
        } catch (IOException e) {
            moduleFile = new File("target/" + moduleFileString);
            jarFile = DeploymentUtil.createJarFile(moduleFile);

        }
        ClassLoader cl = new URLClassLoader(new URL[]{moduleFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        try {
            Environment defaultEnvironment = new Environment(Artifact.create("geronimo/server/1/car"));
            defaultEnvironment.addDependency(DeploymentHelper.BOOTSTRAP_ID, ImportType.ALL);

            WebServiceBuilder webServiceBuilder = new AxisBuilder();
            GBeanData linkData = new GBeanData(WSContainerGBean.GBEAN_INFO);
            OpenEjbModuleBuilder moduleBuilder = new OpenEjbModuleBuilder(deploymentHelper.statelessEjbContainerName, deploymentHelper.statefulEjbContainerName, deploymentHelper.bmpEjbContainerName, deploymentHelper.cmpEjbContainerName, deploymentHelper.mdbEjbContainerName, defaultEnvironment,
                    null,
                    linkData,
                    webServiceBuilder,
                    null, 
                    new GBeanBuilder(null, null),
                    new NamingBuilderCollection(null, null),
                    new MockResourceEnvironmentSetter(),
                    ACTIVATION_SPEC_INFO_LOCATOR,
                    null);

            tempDir = DeploymentUtil.createTempDir();
            EARConfigBuilder earConfigBuilder = new EARConfigBuilder(defaultEnvironment,
                    new AbstractNameQuery(deploymentHelper.tmName),
                    new AbstractNameQuery(deploymentHelper.ctcName),
                    new AbstractNameQuery(deploymentHelper.txTimerName),
                    new AbstractNameQuery(deploymentHelper.nonTxTimerName),
                    null,
                    new AbstractNameQuery(deploymentHelper.serverName),
                    null, // repository
                    moduleBuilder,
                    null,// web
                    null,
                    ACTIVATION_SPEC_INFO_LOCATOR, // connector
                    null, // app client
                    null, null, deploymentHelper.naming
            );

            ConfigurationData configurationData = null;
            DeploymentContext context = null;
            ArtifactManager artifactManager = new DefaultArtifactManager();
            ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, Collections.EMPTY_SET, null);
            try {
                Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile, new ModuleIDBuilder());
                Artifact configurationId = earConfigBuilder.getConfigurationID(plan, jarFile, new ModuleIDBuilder());
                context = earConfigBuilder.buildConfiguration(false, configurationId, plan, jarFile, Collections.singleton(deploymentHelper.configStore), artifactResolver, deploymentHelper.configStore);
                configurationData = context.getConfigurationData();
                configurationData.getEnvironment().addDependency(DeploymentHelper.BOOTSTRAP_ID, ImportType.ALL);

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
