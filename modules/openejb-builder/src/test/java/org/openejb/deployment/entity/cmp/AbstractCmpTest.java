/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: file,v 1.1 2005/02/18 23:22:00 user Exp $
 */
package org.openejb.deployment.entity.cmp;

import java.sql.Connection;
import java.io.File;
import java.util.Collections;

import javax.sql.DataSource;

import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.XmlBeansHelper;
import org.openejb.deployment.CmpSchemaBuilder;
import org.openejb.deployment.TranqlCmpSchemaBuilder;
import org.openejb.deployment.CmpBuilder;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.axiondb.jdbc.AxionDataSource;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractCmpTest extends DeploymentHelper {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    protected final AbstractName moduleName = naming.createRootName(TEST_CONFIGURATION_ID, "MockModule", NameFactory.EJB_MODULE);
    protected DataSource ds;
    private AbstractName moduleCmpEngineName;
    private ConfigurationData configurationData;

    protected abstract void buildDBSchema(Connection c) throws Exception;

    protected abstract String getEjbJarDD();

    protected abstract String getOpenEjbJarDD();

    protected void initCmpModule() throws Exception {
        ds = new AxionDataSource("jdbc:axiondb:testdb");
        Connection c = ds.getConnection("root", null);
        buildDBSchema(c);

        File ejbJarFile = new File(basedir, getEjbJarDD());
        File openejbJarFile = new File(basedir, getOpenEjbJarDD());

        EjbJarType ejbJarType= XmlBeansHelper.loadEjbJar(ejbJarFile);
        OpenejbOpenejbJarType openejbJarType = XmlBeansHelper.loadOpenEjbJar(openejbJarFile);

        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    null,
                    TEST_ENVIRONMENT,
                    ConfigurationModuleType.EJB,
                    kernel.getNaming(),
                    configurationManager,
                    Collections.EMPTY_SET,
                    new AbstractNameQuery(serverName),
                    moduleName,
                    new AbstractNameQuery(tmName),
                    new AbstractNameQuery(ctcName),
                    new AbstractNameQuery(txTimerName),
                    new AbstractNameQuery(nonTxTimerName),
                    null
            );

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // create module cmp engine GBeanData
            EJBModule ejbModule = new EJBModule(true, moduleName, TEST_ENVIRONMENT, null, tempDir.getAbsoluteFile().toURI().toString(), ejbJarType, openejbJarType, "");
            CmpSchemaBuilder cmpSchemaBuilder = new TranqlCmpSchemaBuilder();
            cmpSchemaBuilder.initContext(earContext, ejbModule, classLoader);
            cmpSchemaBuilder.addBeans(earContext, ejbModule, classLoader);
            moduleCmpEngineName = ejbModule.getModuleCmpEngineName();

            // initialize the contifuation
            configurationData = earContext.getConfigurationData();
            configurationData.getEnvironment().addDependency(new Dependency(BOOTSTRAP_ID, ImportType.ALL));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    protected void addCmpEjb(String ejbName, Class beanClass, Class homeClass, Class remoteClass, Class localHomeClass, Class localClass, Class primaryKeyClass, AbstractName containerName) throws Exception {
        CmpBuilder builder = new CmpBuilder();
        builder.setContainerId(containerName.toString());
        builder.setEjbName(ejbName);
        builder.setEjbContainerName(cmpEjbContainerName);
        builder.setBeanClassName(getName(beanClass));
        builder.setHomeInterfaceName(getName(homeClass));
        builder.setRemoteInterfaceName(getName(remoteClass));
        builder.setLocalHomeInterfaceName(getName(localHomeClass));
        builder.setLocalInterfaceName(getName(localClass));
        builder.setPrimaryKeyClassName(getName(primaryKeyClass));
        builder.setModuleCmpEngineName(moduleCmpEngineName);
        builder.setCmp2(true);

        GBeanData deployment = builder.createConfiguration();
        configurationData.addGBean(deployment);
    }

    private String getName(Class clazz) {
        if (clazz == null) return null;
        return clazz.getName();
    }

    protected void startConfiguration() throws NoSuchConfigException, LifecycleException {
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(TEST_CONFIGURATION_ID);
    }

    protected void tearDown() throws Exception {
        Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
        super.tearDown();
    }
}
