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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Permissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.openejb.dispatch.MethodSignature;
import org.openejb.entity.cmp.CMPContainerBuilder;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbQueryType;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.schema.Schema;
import org.tranql.sql.Column;
import org.tranql.sql.Table;
import org.tranql.sql.sql92.SQL92Schema;


class CMPEntityBuilder extends EntityBuilder {
	
	private OpenEJBModuleBuilder builder;

	public CMPEntityBuilder(OpenEJBModuleBuilder builder, OpenEJBModuleBuilder moduleBuilder) {
		super(builder, moduleBuilder);
		this.builder = builder;
	}

	protected void buildBeans(EARContext earContext, Module module, ClassLoader cl, EJBModule ejbModule, String connectionFactoryName, EJBSchema ejbSchema, SQL92Schema sqlSchema, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, Security security, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
		// CMP Entity Beans
	    EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
	    for (int i = 0; i < entityBeans.length; i++) {
	        EntityBeanType entityBean = entityBeans[i];
	
	        if (!"Container".equals(entityBean.getPersistenceType().getStringValue())) {
	        	continue;
	        }
	        
	        OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(entityBean.getEjbName().getStringValue());
	        ObjectName entityObjectName = super.createEJBObjectName(earContext, module.getName(), entityBean);
	
	        GBeanMBean entityGBean = createBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, ejbSchema, sqlSchema, connectionFactoryName, transactionPolicyHelper, security, cl);
	        
	        earContext.addGBean(entityObjectName, entityGBean);
	    }
	}

	public void buildCMPSchema(EARContext earContext, String ejbModuleName, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQL92Schema sqlSchema) throws DeploymentException {
	    EntityBeanType[] entityBeans = ejbJar.getEnterpriseBeans().getEntityArray();
	
	    for (int i = 0; i < entityBeans.length; i++) {
	        EntityBeanType entityBean = entityBeans[i];
	        if ("Container".equals(entityBean.getPersistenceType().getStringValue())) {
	            String ejbName = entityBean.getEjbName().getStringValue();
	            String abstractSchemaName = entityBean.getAbstractSchemaName().getStringValue();
	
	            ObjectName entityObjectName = super.createEJBObjectName(earContext, ejbModuleName, entityBean);
	
	            EJBProxyFactory proxyFactory = (EJBProxyFactory) this.builder.createEJBProxyFactory(entityObjectName.getCanonicalName(),
	                                                                                   false,
	                                                                                   OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getRemote()),
	                                                                                   OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getHome()),
	                                                                                   OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocal()),
	                                                                                   OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocalHome()),
	                                                                                   cl);
	
	            Class ejbClass;
	            try {
	                ejbClass = cl.loadClass(entityBean.getEjbClass().getStringValue());
	            } catch (ClassNotFoundException e) {
	                throw new DeploymentException("Could not load cmp bean class: ejbName=" + ejbName + " ejbClass=" + entityBean.getEjbClass().getStringValue());
	            }

                Class pkClass;
                try {
                    pkClass = cl.loadClass(entityBean.getPrimKeyClass().getStringValue());
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load cmp primary key class: ejbName=" + ejbName + " pkClass=" + entityBean.getPrimKeyClass().getStringValue());
                }

	            EJB ejb = new EJB(ejbName, abstractSchemaName, pkClass, proxyFactory);
	            Table table = new Table(ejbName, abstractSchemaName);

                Set pkFieldNames;
                if (entityBean.getPrimkeyField() == null) {
                    // no field name specified, must be a compound pk so get the field names from the public fields
                    Field[] fields = pkClass.getFields();
                    pkFieldNames = new HashSet(fields.length);
                    for (int j = 0; j < fields.length; j++) {
                        Field field = fields[j];
                        pkFieldNames.add(field.getName());
                    }
                } else {
                    // specific field is primary key
                    pkFieldNames = new HashSet(1);
                    pkFieldNames.add(entityBean.getPrimkeyField().getStringValue());
                }

                CmpFieldType[] cmpFieldTypes = entityBean.getCmpFieldArray();
                for (int cmpFieldIndex = 0; cmpFieldIndex < cmpFieldTypes.length; cmpFieldIndex++) {
                    CmpFieldType cmpFieldType = cmpFieldTypes[cmpFieldIndex];
                    String fieldName = cmpFieldType.getFieldName().getStringValue();
                    Class fieldType = getCMPFieldType(fieldName, ejbClass);
                    boolean isPKField = pkFieldNames.contains(fieldName);
                    ejb.addCMPField(new CMPField(fieldName, fieldType, isPKField));
                    table.addColumn(new Column(fieldName, fieldType, isPKField));
                    if (isPKField) {
                        pkFieldNames.remove(fieldName);
                    }
                }
                if (!pkFieldNames.isEmpty()) {
                    StringBuffer fields = new StringBuffer();
                    fields.append("Could not find cmp fields for following pk fields:");
                    for (Iterator iterator = pkFieldNames.iterator(); iterator.hasNext();) {
                        fields.append(' ');
                        fields.append(iterator.next());
                    }
                    throw new DeploymentException(fields.toString());
                }

	            ejbSchema.addEJB(ejb);
	            sqlSchema.addTable(table);
	        }
	    }
	}

	private Class getCMPFieldType(String fieldName, Class beanClass) throws DeploymentException {
	    try {
	        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	        Method getter = beanClass.getMethod(getterName, null);
	        return getter.getReturnType();
	    } catch (Exception e) {
	        throw new DeploymentException("Getter for CMP field not found: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
	    }
	}

	public GBeanMBean createBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, EJBSchema ejbSchema, Schema sqlSchema, String connectionFactoryName, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl) throws DeploymentException {
	    String ejbName = entityBean.getEjbName().getStringValue();
	
	    CMPContainerBuilder builder = new CMPContainerBuilder();
	    builder.setClassLoader(cl);
	    builder.setContainerId(containerId);
	    builder.setEJBName(ejbName);
	    builder.setBeanClassName(entityBean.getEjbClass().getStringValue());
	    builder.setHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getHome()));
	    builder.setRemoteInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getRemote()));
	    builder.setLocalHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocalHome()));
	    builder.setLocalInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocal()));
	    builder.setPrimaryKeyClassName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getPrimKeyClass()));
	    TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
	    builder.setTransactionPolicySource(transactionPolicySource);
	    builder.setTransactedTimerName(earContext.getTransactedTimerName());
	    builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
	
	    Permissions toBeChecked = new Permissions();
	    securityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
	    securityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
	    securityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
	    securityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
	    securityBuilder.fillContainerBuilderSecurity(builder,
	                                 toBeChecked,
	                                 security,
	                                 ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
	                                 entityBean.getEjbName().getStringValue(),
	                                 entityBean.getSecurityIdentity(),
	                                 entityBean.getSecurityRoleRefArray());
	
	    try {
	        ReadOnlyContext compContext = super.buildComponentContext(earContext, ejbModule, entityBean, openejbEntityBean, null, cl);
	        builder.setComponentContext(compContext);
	    } catch (Exception e) {
	        throw new DeploymentException("Unable to create EJB jndi environment: ejbName=" + ejbName, e);
	    }
	
	    if (openejbEntityBean != null) {
	        setResourceEnvironment(builder, entityBean.getResourceRefArray(), openejbEntityBean.getResourceRefArray());
	        builder.setJndiNames(openejbEntityBean.getJndiNameArray());
	        builder.setLocalJndiNames(openejbEntityBean.getLocalJndiNameArray());
	    } else {
	        builder.setJndiNames(new String[]{ejbName});
	        builder.setLocalJndiNames(new String[]{"local/" + ejbName});
	    }
	
	    builder.setEJBSchema(ejbSchema);
	    builder.setSQLSchema(sqlSchema);
	    builder.setConnectionFactoryName(connectionFactoryName);
	
	    Map queries = new HashMap();
	    if (openejbEntityBean != null) {
	        OpenejbQueryType[] queryTypes = openejbEntityBean.getQueryArray();
	        for (int i = 0; i < queryTypes.length; i++) {
	            OpenejbQueryType queryType = queryTypes[i];
	            MethodSignature signature = new MethodSignature(queryType.getQueryMethod().getMethodName(),
	                                                            queryType.getQueryMethod().getMethodParams().getMethodParamArray());
	            String sql = queryType.getSql();
	            queries.put(signature, sql);
	        }
	    }
	    builder.setQueries(queries);
	
	    try {
	        GBeanMBean gbean = builder.createConfiguration();
	        gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
	        gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
	        return gbean;
	    } catch (Throwable e) {
	        throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName=" + ejbName, e);
	    }
	}
}