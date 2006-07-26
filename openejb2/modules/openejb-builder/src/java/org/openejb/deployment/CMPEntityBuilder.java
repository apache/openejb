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

import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbNameType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.openejb.deployment.pkgen.PKGenBuilder;
import org.openejb.entity.cmp.CMPEJBContainer;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.pkgen.EjbKeyGeneratorType;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJBProxyFactory;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.intertxcache.FrontEndCacheDelegate;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.ql.QueryException;
import org.tranql.sql.SQLSchema;


/**
 * During the process of deploying an EJB JAR, this class is called to deploy
 * each CMP entity bean in the JAR.  It is called from OpenEJBModuleBuilder.
 *
 * @version $Revision$ $Date$
 */
class CMPEntityBuilder extends EntityBuilder {

    public CMPEntityBuilder(OpenEJBModuleBuilder builder) {
        super(builder);
    }

    protected String getBeanType() {
        return "Container";
    }

    protected GBeanData getGBeanData(AbstractName entityObjectName) {
        return new GBeanData(entityObjectName, CMPEJBContainer.GBEAN_INFO);
    }

    /**
     * Create GBeans for all the CMP entity beans in the EJB JAR.
     */
    public void buildBeans(EARContext earContext, AbstractName moduleBaseName, ClassLoader cl, EJBModule ejbModule, EJBSchema ejbSchema, SQLSchema sqlSchema, GlobalSchema globalSchema, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, TransactionManagerDelegate tmDelegate, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        FrontEndCacheDelegate cacheDelegate = new FrontEndCacheDelegate();
        
        // CMP Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            if (!"Container".equals(getString(entityBean.getPersistenceType()))) {
                continue;
            }

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(getString(entityBean.getEjbName()));
            AbstractName entityObjectName = super.createEJBObjectName(earContext, moduleBaseName, entityBean);

//            GBeanData entityGBean =
            createBean(earContext, ejbModule, entityObjectName, entityBean, openejbEntityBean, ejbSchema, sqlSchema, globalSchema, transactionPolicyHelper, cl, tmDelegate, cacheDelegate, componentPermissions, policyContextID);

//            try {
//                earContext.addGBean(entityGBean);
//            } catch (GBeanAlreadyExistsException e) {
//                throw new DeploymentException("Could not add cmp entity bean to context", e);
//            }
        }
    }

    /**
     * Process the deployment information for all the CMP entity beans in the
     * EJB JAR.  This includes setting up the database mappings, key
     * generation, etc.
     */
    public Schemata buildSchemata(final EARContext earContext, final AbstractName moduleJ2eeContext, String moduleName, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, final PKGenBuilder pkGen, final DataSource dataSource, final TransactionManager tm) throws DeploymentException {
        SchemataBuilder builder = new SchemataBuilder() {
            protected EJBProxyFactory buildEJBProxyFactory(EntityBeanType entityBean, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) throws DeploymentException {
                AbstractName entityAbstractName = createEJBObjectName(earContext, moduleJ2eeContext, entityBean);
                //TODO configid need canonical form
                return (EJBProxyFactory) getModuleBuilder().createEJBProxyFactory(entityAbstractName.toURI().toString(),
                        false,
                        remoteInterfaceName,
                        homeInterfaceName,
                        localInterfaceName,
                        localHomeInterfaceName,
                        cl);
            }

            protected PrimaryKeyGenerator buildPKGenerator(EjbKeyGeneratorType config, Class pkClass) throws DeploymentException, QueryException {
                return pkGen.configurePKGenerator(config, tm, dataSource, pkClass, earContext);
            }
        };
        return builder.buildSchemata(moduleName, ejbJar, openejbEjbJar, dataSource, cl);
    }

    private static boolean isCMP2(EntityBeanType entityBean) throws DeploymentException {
        if (!entityBean.isSetCmpVersion()) {
            return true;
        } else {
            String version = getString(entityBean.getCmpVersion());
            if ("1.x".equals(version)) {
                return false;
            } else if ("2.x".equals(version)) {
                return true;
            } else {
                throw new DeploymentException("cmp-version must be either 1.x or 2.x, but was " + version);
            }
        }
    }


    public GBeanData createBean(EARContext earContext, EJBModule ejbModule, AbstractName containerAbstractName, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, EJBSchema ejbSchema, SQLSchema sqlSchema, GlobalSchema globalSchema, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl, TransactionManagerDelegate tmDelegate, FrontEndCacheDelegate cacheDelegate, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        String ejbName = getString(entityBean.getEjbName());
        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(cl);
        //TODO configid need canonical form
        builder.setContainerId(containerAbstractName.toURI().toString());
        builder.setEJBName(ejbName);
        builder.setBeanClassName(getString(entityBean.getEjbClass()));
        builder.setHomeInterfaceName(getString(entityBean.getHome()));
        builder.setRemoteInterfaceName(getString(entityBean.getRemote()));
        builder.setLocalHomeInterfaceName(getString(entityBean.getLocalHome()));
        builder.setLocalInterfaceName(getString(entityBean.getLocal()));
        builder.setPrimaryKeyClassName(getString(entityBean.getPrimKeyClass()));
        builder.setCMP2(isCMP2(entityBean));
        TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
        builder.setTransactionPolicySource(transactionPolicySource);
        builder.setTransactionImportPolicyBuilder(getModuleBuilder().getTransactionImportPolicyBuilder());
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
        builder.setReentrant(entityBean.getReentrant().getBooleanValue());

        addSecurity(earContext, ejbName, builder, cl, ejbModule, entityBean, componentPermissions, policyContextID);

        processEnvironmentRefs(builder, earContext, ejbModule, entityBean, openejbEntityBean, null, cl);

        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setGlobalSchema(globalSchema);
        builder.setTransactionManager(tmDelegate);
        builder.setFrontEndCacheDelegate(cacheDelegate);

        try {
            AbstractNameQuery tssBeanObjectName = getTssBeanQuery(openejbEntityBean, ejbModule, earContext, entityBean);
            if(tssBeanObjectName != null && openejbEntityBean.getJndiNameArray().length == 0) {
                throw new DeploymentException("Cannot expose an entity bean via CORBA unless a JNDI name is set (that's also used as the CORBA naming service name)");
            }
            if(tssBeanObjectName != null && (!entityBean.isSetRemote() || !entityBean.isSetHome())) {
                throw new DeploymentException("An entity bean without a remote interface cannot be exposed via CORBA");
            }
            GBeanData gbeanData = earContext.getGBeanInstance(containerAbstractName);
            return builder.createConfiguration(earContext.getTransactionContextManagerObjectName(), earContext.getConnectionTrackerObjectName(), tssBeanObjectName, gbeanData);
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJB named '" + ejbName + "': "+e.getMessage(), e);
        }
    }

    private static String getString(org.apache.geronimo.xbeans.j2ee.String value) {
        if (value == null) {
            return null;
        }
        return value.getStringValue().trim();
    }

    private String getString(EjbNameType value) {
        if (value == null) {
            return null;
        }
        return value.getStringValue().trim();
    }
}