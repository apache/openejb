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
import java.lang.reflect.Modifier;
import java.security.Permissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbNameType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationshipRoleType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.JavaTypeType;
import org.apache.geronimo.xbeans.j2ee.QueryType;
import org.openejb.entity.cmp.PrimaryKeyGeneratorWrapper;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.xbeans.ejbjar.OpenejbEjbRelationType;
import org.openejb.xbeans.ejbjar.OpenejbEjbRelationshipRoleType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbQueryType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.AutomaticKeyGeneration;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.CmpFieldMapping;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.GlobalSchemaLoader;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.FKField;
import org.tranql.ejb.FinderEJBQLQuery;
import org.tranql.ejb.Relationship;
import org.tranql.ejb.SelectEJBQLQuery;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.pkgenerator.PrimaryKeyGeneratorDelegate;
import org.tranql.schema.Association.JoinDefinition;
import org.tranql.sql.Column;
import org.tranql.sql.EndTable;
import org.tranql.sql.FKColumn;
import org.tranql.sql.JoinTable;
import org.tranql.sql.SQLSchema;
import org.tranql.sql.Table;
import org.tranql.sql.TypeConverter;
import org.tranql.sql.jdbc.SQLTypeLoader;


/**
 * @version $Revision$ $Date$
 */
class CMPEntityBuilder extends EntityBuilder {
    public CMPEntityBuilder(OpenEJBModuleBuilder builder) {
        super(builder);
    }

    protected void buildBeans(EARContext earContext, J2eeContext moduleJ2eeContext, ClassLoader cl, EJBModule ejbModule, EJBSchema ejbSchema, SQLSchema sqlSchema, GlobalSchema globalSchema, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, Security security, EnterpriseBeansType enterpriseBeans, TransactionManagerDelegate tmDelegate) throws DeploymentException {
        // CMP Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            if (!"Container".equals(getString(entityBean.getPersistenceType()))) {
                continue;
            }

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(getString(entityBean.getEjbName()));
            ObjectName entityObjectName = super.createEJBObjectName(moduleJ2eeContext, entityBean);

            GBeanData entityGBean = createBean(earContext, ejbModule, entityObjectName, entityBean, openejbEntityBean, ejbSchema, sqlSchema, globalSchema, transactionPolicyHelper, security, cl, tmDelegate);

            earContext.addGBean(entityGBean);
        }
    }
    

    public void buildCMPSchema(EARContext earContext, J2eeContext moduleJ2eeContext, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQLSchema sqlSchema, GlobalSchema globalSchema) throws DeploymentException {
        try {
            processEnterpriseBeans(earContext, moduleJ2eeContext, ejbJar, openejbEjbJar, cl, ejbSchema, sqlSchema);
            processRelationships(ejbJar, openejbEjbJar, ejbSchema, sqlSchema);
            GlobalSchemaLoader.populateGlobalSchema(globalSchema, ejbSchema, sqlSchema);
        } catch (Exception e) {
            throw new DeploymentException("Module [" + moduleJ2eeContext.getJ2eeModuleName() + "]", e);
        }
    }

    private void processEnterpriseBeans(EARContext earContext, J2eeContext moduleJ2eeContext, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQLSchema sqlSchema) throws DeploymentException {
        Map openEjbEntities = new HashMap();
        OpenejbEntityBeanType[] openEJBEntities = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openEJBEntities.length; i++) {
            OpenejbEntityBeanType entity = openEJBEntities[i];
            openEjbEntities.put(entity.getEjbName(), entity);
        }

        Map keyGenerators = new HashMap();
        EntityBeanType[] entityBeans = ejbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];
            if ( false == "Container".equals(getString(entityBean.getPersistenceType()))) {
                continue;
            }
            
            String ejbName = getString(entityBean.getEjbName());
            boolean cmp2 = isCMP2(entityBean);

            String abstractSchemaName;
            if (cmp2) {
                abstractSchemaName = getString(entityBean.getAbstractSchemaName());
            } else {
                abstractSchemaName = ejbName;
            }

            OpenejbEntityBeanType openEjbEntity = (OpenejbEntityBeanType) openEjbEntities.get(ejbName);
            if (null == openEjbEntity) {
                throw new DeploymentException("EJB [" + ejbName + "] is misconfigured: no CMP mapping defined by OpenEJB DD.");
            }
                
            ObjectName entityObjectName = super.createEJBObjectName(moduleJ2eeContext, entityBean);
    
            EJBProxyFactory proxyFactory = (EJBProxyFactory) getModuleBuilder().createEJBProxyFactory(entityObjectName.getCanonicalName(),
                    false,
                    getString(entityBean.getRemote()),
                    getString(entityBean.getHome()),
                    getString(entityBean.getLocal()),
                    getString(entityBean.getLocalHome()),
                    cl);

            Class ejbClass;
            try {
                ejbClass = cl.loadClass(getString(entityBean.getEjbClass()));
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load cmp bean class: ejbName=" + ejbName + " ejbClass=" + getString(entityBean.getEjbClass()));
            }

            boolean isUnknownPK = false;
            Class pkClass;
            try {
                String pkClassName = getString(entityBean.getPrimKeyClass());
                if ( pkClassName.equals("java.lang.Object") ) {
                    isUnknownPK = true;
                    if ( false == openEjbEntity.isSetAutomaticKeyGeneration() ) {
                        throw new DeploymentException("Automatic key generation is not defined: ejbName=" + ejbName);
                    }
                    AutomaticKeyGeneration keyGeneration = openEjbEntity.getAutomaticKeyGeneration();
                    pkClassName = keyGeneration.getPrimaryKeyClass();
                }
                pkClass = cl.loadClass(pkClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load cmp primary key class: ejbName=" + ejbName + " pkClass=" + getString(entityBean.getPrimKeyClass()));
            }

            EJB ejb;
            if ( openEjbEntity.isSetAutomaticKeyGeneration() ) {
                AutomaticKeyGeneration keyGeneration = openEjbEntity.getAutomaticKeyGeneration();
                String generatorName = keyGeneration.getGeneratorName();
                PrimaryKeyGeneratorDelegate keyGeneratorDelegate = (PrimaryKeyGeneratorDelegate) keyGenerators.get(generatorName);
                if ( null == keyGeneratorDelegate ) {
                    keyGeneratorDelegate = new PrimaryKeyGeneratorDelegate();
                    GBeanData keyGenerator;
                    try {
                        ObjectName generatorObjectName = new ObjectName(generatorName);
                        ObjectName wrapperGeneratorObjectName = new ObjectName(generatorName + ",isWrapper=true");
                        keyGenerator = new GBeanData(wrapperGeneratorObjectName, PrimaryKeyGeneratorWrapper.GBEAN_INFO);
                        keyGenerator.setReferencePattern("PrimaryKeyGenerator", generatorObjectName);
                        keyGenerator.setAttribute("primaryKeyGeneratorDelegate", keyGeneratorDelegate);
                    } catch (Exception e) {
                        throw new DeploymentException("Unable to initialize PrimaryKeyGeneratorWrapper GBean", e);
                    }
                    earContext.addGBean(keyGenerator);
                    
                    keyGenerators.put(generatorName, keyGeneratorDelegate);
                }
                ejb = new EJB(ejbName, abstractSchemaName, pkClass, proxyFactory, keyGeneratorDelegate);
            } else {
                ejb = new EJB(ejbName, abstractSchemaName, pkClass, proxyFactory);
            }

            Table table = new Table(ejbName, openEjbEntity.getTableName());

            Set pkFieldNames;
            if ( isUnknownPK && openEjbEntity.isSetPrimkeyField() ) {
                pkFieldNames = new HashSet(1);
                pkFieldNames.add(openEjbEntity.getPrimkeyField());
            } else if ( false == entityBean.isSetPrimkeyField() ) {
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
                pkFieldNames.add(getString(entityBean.getPrimkeyField()));
            }

            Map cmpFieldToMapping = new HashMap();
            CmpFieldMapping mappings[] = openEjbEntity.getCmpFieldMappingArray();
            for (int j = 0; j < mappings.length; j++) {
                CmpFieldMapping mapping = mappings[j];
                cmpFieldToMapping.put(mapping.getCmpFieldName(), mapping);
            }
                
            CmpFieldType[] cmpFieldTypes = entityBean.getCmpFieldArray();
            for (int cmpFieldIndex = 0; cmpFieldIndex < cmpFieldTypes.length; cmpFieldIndex++) {
                CmpFieldType cmpFieldType = cmpFieldTypes[cmpFieldIndex];
                String fieldName = getString(cmpFieldType.getFieldName());
                CmpFieldMapping mapping = (CmpFieldMapping) cmpFieldToMapping.remove(fieldName);
                if ( null == mapping ) {
                    throw new DeploymentException("EJB [" + ejbName + "] is misconfigured: CMP field [" + fieldName + "] not mapped by OpenEJB DD.");
                }
                Class fieldType = getCMPFieldType(cmp2, fieldName, ejbClass);
                boolean isPKField = pkFieldNames.contains(fieldName);
                ejb.addCMPField(new CMPField(fieldName, fieldName, fieldType, isPKField));
                Column column = new Column(fieldName, mapping.getTableColumn(), fieldType, isPKField);
                if (mapping.isSetSqlType()) {
                    column.setSQLType(SQLTypeLoader.getSQLType(mapping.getSqlType()));
                }
                if (mapping.isSetTypeConverter()) {
                    TypeConverter typeConverter;
                    try {
                        Class typeConverterClass = cl.loadClass(mapping.getTypeConverter());
                        typeConverter = (TypeConverter) typeConverterClass.newInstance();
                    } catch (Exception e) {
                        throw new DeploymentException("Can not create type converter " + mapping.getTypeConverter(), e);
                    }
                    column.setTypeConverter(typeConverter);
                }
                table.addColumn(column);
                if (isPKField) {
                    pkFieldNames.remove(fieldName);
                }
            }
            
            for (Iterator iter = cmpFieldToMapping.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                CmpFieldMapping mapping = (CmpFieldMapping) entry.getValue();
                String fieldName = mapping.getCmpFieldName();
                if ( false == mapping.isSetCmpFieldClass() ) {
                    throw new DeploymentException("Class must be defined for an automatic primary key field: ejbName=" + ejbName + " field=" + fieldName);
                }
                String fieldClass = mapping.getCmpFieldClass();
                Class fieldType;
                try {
                    fieldType = cl.loadClass(fieldClass);
                } catch (ClassNotFoundException e1) {
                    throw new DeploymentException("Could not load automatic primary field: ejbName=" + ejbName + " field=" + fieldName);
                }
                boolean isPKField = pkFieldNames.contains(fieldName);
                ejb.addVirtualCMPField(new CMPField(fieldName, fieldName, fieldType, isPKField));
                table.addColumn(new Column(fieldName, mapping.getTableColumn(), fieldType, isPKField));
                if (isPKField) {
                    pkFieldNames.remove(fieldName);
                }
            }
            
            if (!pkFieldNames.isEmpty()) {
                StringBuffer fields = new StringBuffer();
                fields.append("EJB [" + ejbName + "] is misconfigured: could not find CMP fields for following pk fields:");
                for (Iterator iterator = pkFieldNames.iterator(); iterator.hasNext();) {
                    fields.append(" [");
                    fields.append(iterator.next());
                    fields.append("]");
                }
                throw new DeploymentException(fields.toString());
            }
            
            processQuery(ejb, entityBean, openEjbEntity, cl);
            
            ejbSchema.addEJB(ejb);
            sqlSchema.addTable(table);
        }
    }

    private void processQuery(EJB ejb, EntityBeanType entityBean, OpenejbEntityBeanType openEjbEntity, ClassLoader cl) throws DeploymentException {
        Map queries = new HashMap();
        
        QueryType[] queryTypes = entityBean.getQueryArray();
        if (null != queryTypes) {
            for (int i = 0; i < queryTypes.length; i++) {
                QueryType queryType = queryTypes[i];
                String methodName = getString(queryType.getQueryMethod().getMethodName());
                Class[] parameterTypes = null;
                JavaTypeType[] javaTypeTypes = queryType.getQueryMethod().getMethodParams().getMethodParamArray();
                if (null != javaTypeTypes) {
                    parameterTypes = new Class[javaTypeTypes.length];
                    for (int j = 0; j < javaTypeTypes.length; j++) {
                        String paramType = getString(javaTypeTypes[j]);
                        try {
                            parameterTypes[j] = ClassLoading.loadClass(paramType, cl);
                        } catch (ClassNotFoundException e) {
                            throw new DeploymentException("Can not load parameter type " + paramType +
                                    " defined by method " + methodName);
                        }
                    }
                }
                String ejbQL = queryType.getEjbQl().getStringValue();
                if (methodName.startsWith("find")) {
                    FinderEJBQLQuery query = new FinderEJBQLQuery(methodName, parameterTypes, ejbQL);
                    ejb.addFinder(query);
                    queries.put(query, query);
                } else if (methodName.startsWith("ejbSelect")) {
                    boolean isLocal = true;
                    if (queryType.isSetResultTypeMapping()) {
                        String typeMapping = getString(queryType.getResultTypeMapping());
                        if (typeMapping.equals("Remote")) {
                            isLocal = false;
                        }
                    }
                    SelectEJBQLQuery query = new SelectEJBQLQuery(methodName, parameterTypes, ejbQL, isLocal); 
                    ejb.addSelect(query);
                    queries.put(query, query);
                } else {
                    throw new DeploymentException("Method " + methodName + " is neiher a finder nor a select.");
                }
            }
        }
        
        OpenejbQueryType[] openejbQueryTypes = openEjbEntity.getQueryArray();
        if (null != openejbQueryTypes) {
            for (int i = 0; i < openejbQueryTypes.length; i++) {
                OpenejbQueryType openejbQueryType = openejbQueryTypes[i];
                String methodName = openejbQueryType.getQueryMethod().getMethodName();
                Class[] parameterTypes = null;
                String[] javaTypeTypes = openejbQueryType.getQueryMethod().getMethodParams().getMethodParamArray();
                if (null != javaTypeTypes) {
                    parameterTypes = new Class[javaTypeTypes.length];
                    for (int j = 0; j < javaTypeTypes.length; j++) {
                        String paramType = javaTypeTypes[j];
                        try {
                            parameterTypes[j] = ClassLoading.loadClass(paramType, cl);
                        } catch (ClassNotFoundException e) {
                            throw new DeploymentException("Can not load parameter type " + paramType +
                                    " defined by method " + methodName);
                        }
                    }
                }

                boolean flushCacheBeforeQuery = !openejbQueryType.isSetNoCacheFlush();
                String ejbQL = null;
                if (openejbQueryType.isSetEjbQl()) {
                    ejbQL = openejbQueryType.getEjbQl();
                } else if (false == flushCacheBeforeQuery) {
                    throw new DeploymentException("No ejb-ql defined and flush-cache-before-query not set. method " + methodName);
                }
                
                if (methodName.startsWith("find")) {
                    FinderEJBQLQuery query = new FinderEJBQLQuery(methodName, parameterTypes, ejbQL);
                    if (null == ejbQL) {
                        query = (FinderEJBQLQuery) queries.get(query);
                        if (null == query) {
                            throw new DeploymentException("Method " + methodName + " does not define an ejb-ql.");
                        }
                    } else {
                        ejb.addFinder(query);
                    }
                    query.setFlushCacheBeforeQuery(flushCacheBeforeQuery);
                } else if (methodName.startsWith("ejbSelect")) {
                    boolean isLocal = true;
                    if (openejbQueryType.isSetResultTypeMapping()) {
                        String typeMapping = openejbQueryType.getResultTypeMapping();
                        if (typeMapping.equals("Remote")) {
                            isLocal = false;
                        }
                    }
                    SelectEJBQLQuery query = new SelectEJBQLQuery(methodName, parameterTypes, ejbQL, isLocal);
                    if (null == ejbQL) {
                        query = (SelectEJBQLQuery) queries.get(query);
                        if (null == query) {
                            throw new DeploymentException("Method " + methodName + " does not define an ejb-ql.");
                        }
                    } else {
                        ejb.addSelect(query);
                    }
                    query.setFlushCacheBeforeQuery(flushCacheBeforeQuery);
                } else {
                    throw new DeploymentException("Method " + methodName + " is neiher a finder nor a select.");
                }
            }
        }
    }
    
    private void processRelationships(EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, EJBSchema ejbSchema, SQLSchema sqlSchema) throws DeploymentException {
        if ( !ejbJar.isSetRelationships() ) {
            return;
        } else if ( !openejbEjbJar.isSetRelationships() ) {
            throw new DeploymentException("Relationships are not mapped by OpenEJB DD.");
        }
        
        Map openEjbRelations = new HashMap();
        OpenejbEjbRelationType[] openEJBRelations = openejbEjbJar.getRelationships().getEjbRelationArray();
        for (int i = 0; i < openEJBRelations.length; i++) {
            OpenejbEjbRelationType relation = openEJBRelations[i];
            OpenejbEjbRelationshipRoleType[] roles = relation.getEjbRelationshipRoleArray();
            for (int j = 0; j < roles.length; j++) {
                OpenejbEjbRelationshipRoleType role = roles[j];
                if ( !role.isSetCmrField() ) {
                    continue;
                }
                String ejbName = role.getRelationshipRoleSource().getEjbName();
                String cmrFieldName = role.getCmrField().getCmrFieldName();
                RoleInfo roleInfo = new RoleInfo(ejbName, cmrFieldName);
                openEjbRelations.put(roleInfo, relation);
            }
        }

        EjbRelationType[] relations = ejbJar.getRelationships().getEjbRelationArray();
        for (int i = 0; i < relations.length; i++) {
            EjbRelationshipRoleType[] roles = relations[i].getEjbRelationshipRoleArray();
            RoleInfo[] roleInfo = new RoleInfo[2];
            roleInfo[0] = extractRoleInfo(ejbSchema, sqlSchema, roles[0]);
            roleInfo[1] = extractRoleInfo(ejbSchema, sqlSchema, roles[1]);

            OpenejbEjbRelationType openEjbRelation = (OpenejbEjbRelationType) openEjbRelations.get(roleInfo[0]);
            if (null == openEjbRelation) {
                openEjbRelation = (OpenejbEjbRelationType) openEjbRelations.get(roleInfo[1]);
                if ( null == openEjbRelation ) {
                    throw new DeploymentException("No CMR mapping defined by OpenEJB DD for roles " + roleInfo[0] + " or " + roleInfo[1]);
                }
            }

            String mtmTableName = null;
            if ( !roleInfo[0].isOne && !roleInfo[1].isOne ) {
                if ( !openEjbRelation.isSetManyToManyTableName() ) {
                    throw new DeploymentException("MTM relation between " + roleInfo[0] + " and " + roleInfo[1] +
                            " is misconfigured: no many to many table defined by OpenEJB DD.");
                }
                mtmTableName = openEjbRelation.getManyToManyTableName();
            }

            OpenejbEjbRelationshipRoleType[] openEjbRoles = openEjbRelation.getEjbRelationshipRoleArray();
            for (int j = 0; j < openEjbRoles.length; j++) {
                extractJoinInfo(roleInfo, mtmTableName, ejbSchema, sqlSchema, openEjbRoles[j]);
            }
            
            buildSchemaForJoin(roleInfo, mtmTableName, ejbSchema, sqlSchema, i);
        }
    }

    private RoleInfo extractRoleInfo(EJBSchema ejbSchema, SQLSchema sqlSchema, EjbRelationshipRoleType role) {
        String entityName = role.getRelationshipRoleSource().getEjbName().getStringValue();
        String cmrFieldName = null;
        if ( role.isSetCmrField() ) {
            cmrFieldName = role.getCmrField().getCmrFieldName().getStringValue();
        }
        RoleInfo roleInfo = new RoleInfo(entityName, cmrFieldName);
        roleInfo.ejb = ejbSchema.getEJB(entityName);
        roleInfo.table = sqlSchema.getTable(entityName);
        if ("One".equals(role.getMultiplicity().getStringValue())) {
            roleInfo.isOne = true;
        }
        if ( role.isSetCascadeDelete() ) {
            roleInfo.isCascadeDelete = true;
        }
        return roleInfo;
    }

    private void extractJoinInfo(RoleInfo[] roleInfo, String mtmEntityName, EJBSchema ejbSchema, SQLSchema sqlSchema, OpenejbEjbRelationshipRoleType role) throws DeploymentException {
        String ejbName = role.getRelationshipRoleSource().getEjbName();
        String cmrFieldName = null;
        if ( role.isSetCmrField() ) {
            cmrFieldName = role.getCmrField().getCmrFieldName();
        }
        RoleInfo sourceRoleInfo = new RoleInfo(ejbName, cmrFieldName);
        RoleInfo[] mappedRoleInfo = new RoleInfo[2];
        if (roleInfo[0].equals(sourceRoleInfo)) {
            mappedRoleInfo = roleInfo;
            roleInfo[0].isOnPKSide |= true;
        } else {
            mappedRoleInfo[0] = roleInfo[1];
            mappedRoleInfo[1] = roleInfo[0];
            roleInfo[1].isOnPKSide |= true;
        }

        if ( role.isSetForeignKeyColumnOnSource() ) {
            RoleInfo tmp = mappedRoleInfo[0];
            mappedRoleInfo[0] = mappedRoleInfo[1];
            mappedRoleInfo[1] = tmp;
            roleInfo[0].isOnPKSide = !roleInfo[0].isOnPKSide;
            roleInfo[1].isOnPKSide = !roleInfo[1].isOnPKSide;
        }

        Map pkToFkMap = new HashMap();
        OpenejbEjbRelationshipRoleType.RoleMapping.CmrFieldMapping[] mappings = role.getRoleMapping().getCmrFieldMappingArray();
        for (int k = 0; k < mappings.length; k++) {
            OpenejbEjbRelationshipRoleType.RoleMapping.CmrFieldMapping mapping = mappings[k];
            pkToFkMap.put(mapping.getKeyColumn(), mapping.getForeignKeyColumn());
        }

        LinkedHashMap pkToFkMapEJB = new LinkedHashMap();
        LinkedHashMap pkToFkMapTable = new LinkedHashMap();
        Table pkTable = mappedRoleInfo[0].table;
        EJB pkEJB = mappedRoleInfo[0].ejb;
        for (Iterator attIter = pkTable.getPrimaryKeyFields().iterator(); attIter.hasNext();) {
            Column att = (Column) attIter.next();
            String pkColumn = att.getPhysicalName();
            String fkColumn = (String) pkToFkMap.get(pkColumn);
            if (null == fkColumn) {
                throw new DeploymentException("Role " + sourceRoleInfo + " is misconfigured: column [" + pkColumn + "] is not a primary key.");
            }
            pkToFkMapEJB.put(pkEJB.getAttribute(att.getName()), new FKField(fkColumn, att.getType()));
            FKColumn column = new FKColumn(fkColumn, att.getType());
            if (att.isSQLTypeSet()) {
                column.setSQLType(att.getSQLType());
            }
            if (att.isTypeConverterSet()) {
                column.setTypeConverter(att.getTypeConverter());
            }
            pkToFkMapTable.put(att, column);
        }

        EJB fkEJB = mappedRoleInfo[1].ejb;
        Table fkTable = mappedRoleInfo[1].table;
        if (null != mtmEntityName) {
            fkEJB = ejbSchema.getEJB(mtmEntityName);
            if (null == fkEJB) {
                fkEJB = new EJB(mtmEntityName, mtmEntityName);
                ejbSchema.addEJB(fkEJB);
            }
            fkTable = sqlSchema.getTable(mtmEntityName);
            if (null == fkTable) {
                fkTable = new Table(mtmEntityName);
                sqlSchema.addTable(fkTable);
            }
        }
        
        mappedRoleInfo[0].ejbJDef = new JoinDefinition(mappedRoleInfo[0].ejb, fkEJB, pkToFkMapEJB);
        mappedRoleInfo[0].tableJDef = new JoinDefinition(mappedRoleInfo[0].table, fkTable, pkToFkMapTable);
    }

    private void buildSchemaForJoin(RoleInfo[] roleInfo, String mtmEntityName, EJBSchema ejbSchema, SQLSchema sqlSchema, int id) {
        Relationship relationship;
        JoinTable joinTable;
        if (null == mtmEntityName) {
            if (null != roleInfo[0].ejbJDef) {
                relationship = new Relationship(roleInfo[0].ejbJDef);
                joinTable = new JoinTable(roleInfo[0].tableJDef);
            } else {
                relationship = new Relationship(roleInfo[1].ejbJDef);
                joinTable = new JoinTable(roleInfo[1].tableJDef);
            }
        } else {
            EJB mtmEJB = ejbSchema.getEJB(mtmEntityName);
            relationship = new Relationship(mtmEJB, roleInfo[0].ejbJDef, roleInfo[1].ejbJDef);
            Table mtmTable = sqlSchema.getTable(mtmEntityName);
            joinTable = new JoinTable(mtmTable, roleInfo[0].tableJDef, roleInfo[1].tableJDef);
        }

        boolean isVirtual = null == roleInfo[0].cmrFieldName;
        String endName0 = isVirtual ? "$VirtualEnd" + id : roleInfo[0].cmrFieldName;
        roleInfo[0].ejb.addCMRField(new CMRField(endName0, roleInfo[1].ejb, roleInfo[1].isOne, roleInfo[1].isCascadeDelete, relationship, isVirtual, roleInfo[0].isOnPKSide));
        roleInfo[0].table.addEndTable(new EndTable(endName0, roleInfo[1].table, roleInfo[1].isOne, roleInfo[1].isCascadeDelete, joinTable, isVirtual, roleInfo[0].isOnPKSide));
        
        isVirtual = null == roleInfo[1].cmrFieldName;
        String endName1 = isVirtual ? "$VirtualEnd" + id : roleInfo[1].cmrFieldName;
        roleInfo[1].ejb.addCMRField(new CMRField(endName1, roleInfo[0].ejb, roleInfo[0].isOne, roleInfo[0].isCascadeDelete, relationship, isVirtual, roleInfo[1].isOnPKSide));
        roleInfo[1].table.addEndTable(new EndTable(endName1, roleInfo[0].table, roleInfo[0].isOne, roleInfo[0].isCascadeDelete, joinTable, isVirtual, roleInfo[1].isOnPKSide));
        
        if (null != mtmEntityName) {
            EJB mtmEJB = ejbSchema.getEJB(mtmEntityName);
            Relationship mtmRelationship = new Relationship(relationship.getLeftJoinDefinition());
            mtmEJB.addCMRField(new CMRField(endName0, roleInfo[0].ejb, true, false, mtmRelationship, true, false));
            mtmRelationship.addAssociationEnd(roleInfo[0].ejb.getAssociationEnd(endName0));
            
            mtmRelationship = new Relationship(relationship.getRightJoinDefinition());
            mtmEJB.addCMRField(new CMRField(endName1, roleInfo[1].ejb, true, false, mtmRelationship, true, false));
            mtmRelationship.addAssociationEnd(roleInfo[1].ejb.getAssociationEnd(endName1));
            
            Table mtmTable = sqlSchema.getTable(mtmEntityName);
            JoinTable mtmJoinTable = new JoinTable(joinTable.getLeftJoinDefinition());
            mtmTable.addEndTable(new EndTable(endName0, roleInfo[0].table, true, false, mtmJoinTable, true, false));
            mtmJoinTable.addAssociationEnd(roleInfo[0].table.getAssociationEnd(endName0));
            
            mtmJoinTable = new JoinTable(joinTable.getRightJoinDefinition());
            mtmTable.addEndTable(new EndTable(endName1, roleInfo[1].table, true, false, mtmJoinTable, true, false));
            mtmJoinTable.addAssociationEnd(roleInfo[1].table.getAssociationEnd(endName1));
        }
    }

    private Class getCMPFieldType(boolean cmp2, String fieldName, Class beanClass) throws DeploymentException {
        if (cmp2) {
            try {
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = beanClass.getMethod(getterName, null);
                return getter.getReturnType();
            } catch (Exception e) {
                throw new DeploymentException("Getter for CMP field not found: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
            }
        } else {
            Field field;
            try {
                field = beanClass.getField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new DeploymentException("Class field for CMP field not found: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
            }

            if (!Modifier.isPublic(field.getModifiers())) {
                throw new DeploymentException("Class field for CMP field is not public: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
            }

            if (Modifier.isFinal(field.getModifiers())) {
                throw new DeploymentException("Class field for CMP field is final: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
            }

            if (Modifier.isTransient(field.getModifiers())) {
                throw new DeploymentException("Class field for CMP field is transient: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
            }

            if (Modifier.isStatic(field.getModifiers())) {
                throw new DeploymentException("Class field for CMP field is static: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
            }

            return field.getType();
        }
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


    public GBeanData createBean(EARContext earContext, EJBModule ejbModule, ObjectName containerObjectName, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, EJBSchema ejbSchema, SQLSchema sqlSchema, GlobalSchema globalSchema, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl, TransactionManagerDelegate tmDelegate) throws DeploymentException {
        String ejbName = getString(entityBean.getEjbName());
        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerObjectName.getCanonicalName());
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
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
        builder.setReentrant(entityBean.getReentrant().getBooleanValue());

        Permissions toBeChecked = new Permissions();
        ContainerSecurityBuilder containerSecurityBuilder = getModuleBuilder().getSecurityBuilder();
        containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
        containerSecurityBuilder.fillContainerBuilderSecurity(builder,
                toBeChecked,
                security,
                ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                getString(entityBean.getEjbName()),
                entityBean.getSecurityIdentity(),
                entityBean.getSecurityRoleRefArray());

        processEnvironmentRefs(builder, earContext, ejbModule, entityBean, openejbEntityBean, null, cl);

        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setGlobalSchema(globalSchema);
        builder.setTransactionManagerDelegate(tmDelegate);

        try {
            GBeanData gbean = builder.createConfiguration();
            gbean.setName(containerObjectName);
            gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName [" + ejbName + "]", e);
        }
    }
    
    private static class RoleInfo {
        private final String entityName;
        private final String cmrFieldName;
        private EJB ejb;
        private Table table;
        private boolean isOne;
        private boolean isCascadeDelete;
        private boolean isOnPKSide;
        private JoinDefinition ejbJDef;
        private JoinDefinition tableJDef;
        private RoleInfo(String entityName, String cmrFieldName) {
            this.entityName = entityName;
            this.cmrFieldName = cmrFieldName;
        }
        public int hashCode() {
            if ( null == cmrFieldName ) {
                return entityName.hashCode();
            } else {
                return entityName.hashCode() ^ cmrFieldName.hashCode();
            }
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof RoleInfo ) {
                return false;
            }
            RoleInfo other = (RoleInfo) obj;
            return entityName.equals(other.entityName) &&
                    null == cmrFieldName?null == other.cmrFieldName:cmrFieldName.equals(other.cmrFieldName);
        }
        public String toString() {
            return "EJB [" + entityName + "]; CMR field [" + cmrFieldName + "]";
        }
    }

    private static String getString(org.apache.geronimo.xbeans.j2ee.String value) {
        if (value == null) {
            return null;
        }
        return value.getStringValue();
    }

    private String getString(EjbNameType value) {
        if (value == null) {
            return null;
        }
        return value.getStringValue();
    }
}