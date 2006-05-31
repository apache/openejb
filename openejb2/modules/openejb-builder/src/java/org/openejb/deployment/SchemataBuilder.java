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
 * $Id$
 */
package org.openejb.deployment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbNameType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationshipRoleType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.JavaTypeType;
import org.apache.geronimo.xbeans.j2ee.QueryType;
import org.openejb.xbeans.ejbjar.OpenejbCmpFieldGroupMappingType;
import org.openejb.xbeans.ejbjar.OpenejbCmrFieldGroupMappingType;
import org.openejb.xbeans.ejbjar.OpenejbEjbRelationType;
import org.openejb.xbeans.ejbjar.OpenejbEjbRelationshipRoleType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbEntityGroupMappingType;
import org.openejb.xbeans.ejbjar.OpenejbGroupType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbQueryType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.Cache;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.CmpFieldMapping;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.PrefetchGroup;
import org.openejb.xbeans.ejbjar.OpenejbGroupType.CmrField;
import org.openejb.xbeans.pkgen.EjbKeyGeneratorType;
import org.tranql.builder.DynamicCommandBuilder;
import org.tranql.builder.GlobalSchemaBuilder;
import org.tranql.builder.StaticCommandBuilder;
import org.tranql.cache.CacheFlushStrategyFactory;
import org.tranql.cache.CacheTable;
import org.tranql.cache.EnforceRelationshipsFlushStrategyFactory;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.SimpleFlushStrategyFactory;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBProxyFactory;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.FKField;
import org.tranql.ejb.FinderEJBQLQuery;
import org.tranql.ejb.Relationship;
import org.tranql.ejb.SelectEJBQLQuery;
import org.tranql.ejbqlcompiler.DerbyDBSyntaxtFactory;
import org.tranql.ejbqlcompiler.DerbyEJBQLCompilerFactory;
import org.tranql.intertxcache.CacheFactory;
import org.tranql.intertxcache.ReadCommittedCacheFactory;
import org.tranql.intertxcache.ReadUncommittedCacheFactory;
import org.tranql.intertxcache.RepeatableReadCacheFactory;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.ql.QueryException;
import org.tranql.schema.Association.JoinDefinition;
import org.tranql.sql.BaseSQLSchema;
import org.tranql.sql.Column;
import org.tranql.sql.DBSyntaxFactory;
import org.tranql.sql.EJBQLCompilerFactory;
import org.tranql.sql.EndTable;
import org.tranql.sql.FKColumn;
import org.tranql.sql.JoinTable;
import org.tranql.sql.SQLSchema;
import org.tranql.sql.Table;
import org.tranql.sql.TypeConverter;
import org.tranql.sql.UpdateCommandBuilder;
import org.tranql.sql.jdbc.SQLTypeLoader;
import org.tranql.sql.prefetch.PrefetchGroupDictionary;


/**
 *
 * @version $Revision$ $Date$
 */
public abstract class SchemataBuilder {
    private EJBSchema ejbSchema;
    private SQLSchema sqlSchema;
    private GlobalSchema globalSchema;
    
    public Schemata buildSchemata(String moduleName, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, DataSource datasource, ClassLoader cl) throws DeploymentException {
        CacheFlushStrategyFactory flushStrategyFactory;
        if (openejbEjbJar.isSetEnforceForeignKeyConstraints()) {
            flushStrategyFactory = new EnforceRelationshipsFlushStrategyFactory();
        } else {
            flushStrategyFactory = new SimpleFlushStrategyFactory();
        }

        EJBQLCompilerFactory compilerFactory = new DerbyEJBQLCompilerFactory();
        try {
            if (openejbEjbJar.isSetEjbQlCompilerFactory()) {
                String className = openejbEjbJar.getEjbQlCompilerFactory().toString();
                //TODO we need resolve class loading issues.  Currently we're going to default to the current classloader for Alternate Syntax and EJBQLFactories
                //  Class clazz = cl.loadClass(className);
                Class clazz = Class.forName(className);
                Constructor constructor = clazz.getConstructor(null);
                Object factory = constructor.newInstance(null);
                if (false == factory instanceof EJBQLCompilerFactory) {
                    throw new DeploymentException("EJBQLCompilerFactory expected. was=" + factory);
                }
                compilerFactory = (EJBQLCompilerFactory) factory;
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize ejb-ql-compiler-factory=" + openejbEjbJar.getEjbQlCompilerFactory(), e);
        }

        DBSyntaxFactory syntaxFactory = new DerbyDBSyntaxtFactory();
        try {
            if (openejbEjbJar.isSetDbSyntaxFactory()) {
                String className = openejbEjbJar.getDbSyntaxFactory().toString();
                //TODO we need resolve class loading issues.  Currently we're going to default to the current classloader for Alternate Syntax and EJBQLFactories
                //  Class clazz = cl.loadClass(className);
                Class clazz = Class.forName(className);
                Constructor constructor = clazz.getConstructor(null);
                Object factory = constructor.newInstance(null);
                if (false == factory instanceof DBSyntaxFactory) {
                    throw new DeploymentException("DBSyntaxFactory expected. was=" + factory);
                }
                syntaxFactory = (DBSyntaxFactory) factory;
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize ejb-ql-compiler-factory=" + openejbEjbJar.getEjbQlCompilerFactory(), e);
        }

        ejbSchema = new EJBSchema(moduleName);
        sqlSchema = new BaseSQLSchema(moduleName, datasource, syntaxFactory, compilerFactory);
        globalSchema = new GlobalSchema(moduleName, flushStrategyFactory);
        
        try {
            processEnterpriseBeans(ejbJar, openejbEjbJar, cl);
            processRelationships(ejbJar, openejbEjbJar);
            processGroups(openejbEjbJar);
            GlobalSchemaBuilder loader = new GlobalSchemaBuilder(globalSchema, ejbSchema, sqlSchema);
            loader.build();
            processEnterpriseBeanCaches(openejbEjbJar);
        } catch (Exception e) {
            throw new DeploymentException("Could not deploy module", e);
        }
        
        return new Schemata(ejbSchema, sqlSchema, globalSchema);
    }

    private void processEnterpriseBeanCaches(OpenejbOpenejbJarType openejbEjbJar) {
        OpenejbEntityBeanType[] openEJBEntities = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openEJBEntities.length; i++) {
            String ejbName = openEJBEntities[i].getEjbName();
            if (false == openEJBEntities[i].isSetCache()) {
                continue;
            }
            
            CacheFactory factory;
            Cache cache = openEJBEntities[i].getCache();
            int size = cache.getSize();
            if (Cache.IsolationLevel.READ_COMMITTED == cache.getIsolationLevel()) {
                factory = new ReadCommittedCacheFactory(size);
            } else if (Cache.IsolationLevel.READ_UNCOMMITTED == cache.getIsolationLevel()) {
                factory = new ReadUncommittedCacheFactory(size);
            } else if (Cache.IsolationLevel.REPEATABLE_READ == cache.getIsolationLevel()) {
                factory = new RepeatableReadCacheFactory(size);
            } else {
                throw new AssertionError();
            }
            
            CacheTable cacheTable = globalSchema.getCacheTable(ejbName);
            cacheTable.setCacheFactory(factory);
        }
    }

    private void processGroups(OpenejbOpenejbJarType openejbEjbJar)
        throws DeploymentException {
        PrefetchGroupDictionary groupDictionary = sqlSchema.getGroupDictionary();
        OpenejbEntityBeanType[] openEJBEntities = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openEJBEntities.length; i++) {
            String ejbName = openEJBEntities[i].getEjbName();
            if (false == openEJBEntities[i].isSetPrefetchGroup()) {
                continue;
            }
            OpenejbGroupType[] groups = openEJBEntities[i].getPrefetchGroup().getGroupArray();
            for (int j = 0; j < groups.length; j++) {
                OpenejbGroupType group = groups[j];
                String groupName = group.getGroupName();
                String[] cmpFields = group.getCmpFieldNameArray();
                CmrField[] cmrFields = group.getCmrFieldArray();
                PrefetchGroupDictionary.AssociationEndDesc[] endTableDescs = new PrefetchGroupDictionary.AssociationEndDesc[cmrFields.length];
                for (int k = 0; k < cmrFields.length; k++) {
                    String cmrFieldName = cmrFields[k].getCmrFieldName();
                    String cmrGroupName;
                    if (cmrFields[k].isSetGroupName()) {
                        cmrGroupName = cmrFields[k].getGroupName(); 
                    } else {
                        cmrGroupName = groupName;
                    }
                    endTableDescs[k] = new PrefetchGroupDictionary.AssociationEndDesc(cmrFieldName, cmrGroupName);
                }
                groupDictionary.addPrefetchGroup(groupName, ejbName, cmpFields, endTableDescs);
            }
            
            EJB ejb = ejbSchema.getEJB(ejbName);
            PrefetchGroup prefetchGroup = openEJBEntities[i].getPrefetchGroup();
            
            if (prefetchGroup.isSetEntityGroupMapping()) {
                OpenejbEntityGroupMappingType mapping = prefetchGroup.getEntityGroupMapping();
                ejb.setPrefetchGroup(mapping.getGroupName());
            }
            
            OpenejbCmpFieldGroupMappingType[] cmpMappings = prefetchGroup.getCmpFieldGroupMappingArray();
            for (int j = 0; j < cmpMappings.length; j++) {
                OpenejbCmpFieldGroupMappingType mapping = cmpMappings[j];
                CMPField cmpField = (CMPField) ejb.getAttribute(mapping.getCmpFieldName());
                if (null == cmpField) {
                    throw new DeploymentException("EJB [" + ejbName + "] does not define the CMP field [" + 
                            mapping.getCmpFieldName() + "].");
                }
                cmpField.setPrefetchGroup(mapping.getGroupName());
            }
            
            OpenejbCmrFieldGroupMappingType[] cmrMappings = prefetchGroup.getCmrFieldGroupMappingArray();
            for (int j = 0; j < cmrMappings.length; j++) {
                OpenejbCmrFieldGroupMappingType mapping = cmrMappings[j];
                CMRField cmrField = (CMRField) ejb.getAssociationEnd(mapping.getCmrFieldName());
                if (null == cmrField) {
                    throw new DeploymentException("EJB [" + ejbName + "] does not define the CMR field [" + 
                            mapping.getCmrFieldName() + "].");
                }
                cmrField.setPrefetchGroup(mapping.getGroupName());
            }
        }
    }
    
    private void processEnterpriseBeans(EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl) throws DeploymentException {
        Map openEjbEntities = new HashMap();
        OpenejbEntityBeanType[] openEJBEntities = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openEJBEntities.length; i++) {
            OpenejbEntityBeanType entity = openEJBEntities[i];
            openEjbEntities.put(entity.getEjbName(), entity);
        }

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

            EJBProxyFactory proxyFactory = buildEJBProxyFactory(entityBean,
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

            // Index the CMP fields
            Map cmpFieldToMapping = new HashMap();
            CmpFieldMapping mappings[] = openEjbEntity.getCmpFieldMappingArray();
            for (int j = 0; j < mappings.length; j++) {
                CmpFieldMapping mapping = mappings[j];
                cmpFieldToMapping.put(mapping.getCmpFieldName().trim(), mapping);
            }

            // Handle "Unknown Primary Key Type" -- try to identify the PK class
            boolean unknownPK = false;
            Class pkClass;
            try {
                String pkClassName = getString(entityBean.getPrimKeyClass());
                if ( pkClassName.equals("java.lang.Object") ) {
                    unknownPK = true;
                    // If the key type is not known, we assume the provided code does not set the PK, and
                    //   therefore we need a key generator to be set.
                    if ( false == openEjbEntity.isSetKeyGenerator() ) {
                        throw new DeploymentException("Automatic key generation is not defined: ejbName=" + ejbName);
                    }
                    if(false == openEjbEntity.isSetPrimkeyField()) {
                        throw new DeploymentException("EJB "+ejbName+" has an \"unknown primary key type\" (java.lang.Object).  A primkey-field element must be present in openejb-jar.xml to indicate the actual primary key type.");
                    }
                    String pkFieldName = openEjbEntity.getPrimkeyField();
                    CmpFieldMapping pkField = (OpenejbEntityBeanType.CmpFieldMapping) cmpFieldToMapping.get(pkFieldName);
                    if(pkField == null) {
                        throw new DeploymentException("EJB "+ejbName+" lists a primkey-field ("+pkFieldName+") but there is no matching cmp-field-mapping");
                    }
                    if(pkField.isSetCmpFieldClass()) { // Check if the field class was provided in openejb-jar.xml
                        pkClassName = pkField.getCmpFieldClass();
                    } else { // Otherwise it has to be a getter on the EJB class itself
                        pkClassName = getCMPFieldType(cmp2, pkFieldName, ejbClass).getName();
                    }
                    if(pkClassName == null) { // should never happen
                        throw new DeploymentException("Cannot determine actual primary key field type for EJB "+ejbName);
                    }
                }
                pkClass = cl.loadClass(pkClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load cmp primary key class: ejbName=" + ejbName + " pkClass=" + getString(entityBean.getPrimKeyClass()));
            }

            EJB ejb;
            PrimaryKeyGenerator keyGenerator = null;
            if(openEjbEntity.isSetKeyGenerator()) {
                try {
                    keyGenerator = buildPKGenerator(openEjbEntity.getKeyGenerator(), pkClass);
                } catch (QueryException e) {
                    throw new DeploymentException("Unable to load PK Generator for EJB "+ejbName, e);
                }
            }
            ejb = new EJB(ejbName, abstractSchemaName, pkClass, proxyFactory, keyGenerator, unknownPK);

            Table table = new Table(ejbName, openEjbEntity.getTableName());

            UpdateCommandBuilder commandBuilder;
            if (openEjbEntity.isSetStaticSql()) {
                commandBuilder = new StaticCommandBuilder(ejbName, ejbSchema, sqlSchema, globalSchema);
            } else {
                commandBuilder = new DynamicCommandBuilder(ejbName, ejbSchema, sqlSchema, globalSchema);
            }
            table.setCommandBuilder(commandBuilder);
            
            Set pkFieldNames;
            if ( unknownPK && openEjbEntity.isSetPrimkeyField() ) {
                pkFieldNames = new HashSet(1);
                pkFieldNames.add(openEjbEntity.getPrimkeyField().trim());
            } else if ( false == entityBean.isSetPrimkeyField() ) {
                // no field name specified, must be a compound pk so get the field names from the public fields
                Field[] fields = pkClass.getFields();
                pkFieldNames = new HashSet(fields.length);
                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    pkFieldNames.add(field.getName());
                }
                if (0 == pkFieldNames.size()) {
                    throw new DeploymentException("Invalid primary key class: ejbName=" + ejbName + " pkClass=" + pkClass);
                }
            } else {
                // specific field is primary key
                pkFieldNames = new HashSet(1);
                pkFieldNames.add(getString(entityBean.getPrimkeyField()));
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
                CMPField cmpField = new CMPField(fieldName, fieldName, fieldType, isPKField); 
                ejb.addCMPField(cmpField);
                Column column = new Column(fieldName, mapping.getTableColumn().trim(), fieldType, isPKField);
                if (mapping.isSetSqlType()) {
                    column.setSQLType(SQLTypeLoader.getSQLType(mapping.getSqlType().trim()));
                }
                if (mapping.isSetTypeConverter()) {
                    TypeConverter typeConverter;
                    try {
                        Class typeConverterClass = cl.loadClass(mapping.getTypeConverter().trim());
                        typeConverter = (TypeConverter) typeConverterClass.newInstance();
                    } catch (Exception e) {
                        throw new DeploymentException("Cannot create type converter " + mapping.getTypeConverter(), e);
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
                String fieldName = mapping.getCmpFieldName().trim();
                if ( false == mapping.isSetCmpFieldClass() ) {
                    throw new DeploymentException("Class must be defined for an automatic primary key field: ejbName=" + ejbName + " field=" + fieldName);
                }
                String fieldClass = mapping.getCmpFieldClass().trim();
                Class fieldType;
                try {
                    fieldType = cl.loadClass(fieldClass);
                } catch (ClassNotFoundException e1) {
                    throw new DeploymentException("Could not load automatic primary field: ejbName=" + ejbName + " field=" + fieldName);
                }
                boolean isPKField = pkFieldNames.contains(fieldName);
                CMPField cmpField = new CMPField(fieldName, fieldName, fieldType, isPKField);
                ejb.addVirtualCMPField(cmpField);
                table.addColumn(new Column(fieldName, mapping.getTableColumn().trim(), fieldType, isPKField));
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

                String groupName = null;
                if (openejbQueryType.isSetGroupName()) {
                    groupName = openejbQueryType.getGroupName();
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
                    query.setPrefetchGroup(groupName);
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
                    query.setPrefetchGroup(groupName);
                } else {
                    throw new DeploymentException("Method " + methodName + " is neiher a finder nor a select.");
                }
            }
        }
    }

    private void processRelationships(EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar) throws DeploymentException {
        if ( !ejbJar.isSetRelationships() ) {
            return;
        } else if ( !openejbEjbJar.isSetRelationships() ) {
            throw new DeploymentException("Relationships are not mapped by OpenEJB DD.");
        }

        Map openEjbRelations = new HashMap();
        OpenejbEjbRelationType[] openEJBRelations = openejbEjbJar.getRelationships().getEjbRelationArray();
        for (int i = 0; i < openEJBRelations.length; i++) {
            OpenejbEjbRelationType relation = openEJBRelations[i];
            String relationName = relation.isSetEjbRelationName() ? relation.getEjbRelationName() : null;
            OpenejbEjbRelationshipRoleType[] roles = relation.getEjbRelationshipRoleArray();
            for (int j = 0; j < roles.length; j++) {
                OpenejbEjbRelationshipRoleType role = roles[j];
                String roleName = role.isSetEjbRelationshipRoleName() ? role.getEjbRelationshipRoleName() : null;
                String ejbName = role.getRelationshipRoleSource().getEjbName();
                String cmrFieldName = role.isSetCmrField() ? role.getCmrField().getCmrFieldName() : null;
                RoleInfo roleInfo = new RoleInfo(relationName, roleName, ejbName, cmrFieldName);
                // Note: we're putting the whole relation into the map, not just the relationship-role that we found
                // Later, we'll dig out both roles for a many-to-many relationship, even if only one of them
                //   had a cmr-field and got past the isSetCmrField test above
                openEjbRelations.put(roleInfo, relation);
            }
        }

        EjbRelationType[] relations = ejbJar.getRelationships().getEjbRelationArray();
        for (int i = 0; i < relations.length; i++) {
            EjbRelationshipRoleType[] roles = relations[i].getEjbRelationshipRoleArray();
            String relationName = relations[i].isSetEjbRelationName() ? relations[i].getEjbRelationName().getStringValue() : null; 
            RoleInfo[] roleInfo = new RoleInfo[2];
            roleInfo[0] = extractRoleInfo(roles[0], relationName);
            roleInfo[1] = extractRoleInfo(roles[1], relationName);

            OpenejbEjbRelationType openEjbRelation = null; 
            for (Iterator iter = openEjbRelations.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                RoleInfo currRoleInfo = (RoleInfo) entry.getKey();
                if (currRoleInfo.implies(roleInfo[0]) || currRoleInfo.implies(roleInfo[1])) {
                    openEjbRelation = (OpenejbEjbRelationType) entry.getValue();
                    break;
                }
            }
            
            if (null == openEjbRelation) {
                throw new DeploymentException("No CMR mapping defined by OpenEJB DD for roles " + roleInfo[0] + " or " + roleInfo[1]);
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
                extractJoinInfo(roleInfo, mtmTableName, openEjbRoles[j], relationName);
            }

            buildSchemaForJoin(roleInfo, mtmTableName, i);
        }
    }

    private RoleInfo extractRoleInfo(EjbRelationshipRoleType role, String relationName) {
        String entityName = role.getRelationshipRoleSource().getEjbName().getStringValue();
        String roleName = role.isSetEjbRelationshipRoleName() ? role.getEjbRelationshipRoleName().getStringValue() : null;
        String cmrFieldName = role.isSetCmrField() ? role.getCmrField().getCmrFieldName().getStringValue() : null;
        RoleInfo roleInfo = new RoleInfo(relationName, roleName, entityName, cmrFieldName);
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

    private void extractJoinInfo(RoleInfo[] roleInfo, String mtmEntityName, OpenejbEjbRelationshipRoleType role, String relationName) throws DeploymentException {
        String ejbName = role.getRelationshipRoleSource().getEjbName();
        String roleName = role.isSetEjbRelationshipRoleName() ? role.getEjbRelationshipRoleName() : null;
        String cmrFieldName = role.isSetCmrField() ? role.getCmrField().getCmrFieldName() : null;
        RoleInfo sourceRoleInfo = new RoleInfo(relationName, roleName, ejbName, cmrFieldName);
        RoleInfo[] mappedRoleInfo = new RoleInfo[2];
        if (roleInfo[0].implies(sourceRoleInfo)) {
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
        Table fkTable = mappedRoleInfo[1].table;
        EJB pkEJB = mappedRoleInfo[0].ejb;
        for (Iterator attIter = pkTable.getPrimaryKeyFields().iterator(); attIter.hasNext();) {
            Column att = (Column) attIter.next();
            String pkColumn = att.getPhysicalName();
            String fkColumn = (String) pkToFkMap.get(pkColumn);
            if (null == fkColumn) {
                throw new DeploymentException("Role " + sourceRoleInfo + " is misconfigured: primary key column [" +
                        pkColumn + "] is not mapped to a foreign key.");
            }
            String fkColumnName = fkColumn;
            for (Iterator iter = fkTable.getAttributes().iterator(); iter.hasNext();) {
                Column column = (Column) iter.next();
                if (column.getPhysicalName().equals(fkColumn)) {
                    fkColumnName = column.getName();
                    break;
                }
            }
            pkToFkMapEJB.put(pkEJB.getAttribute(att.getName()), new FKField(fkColumnName, fkColumn, att.getType()));
            FKColumn column = new FKColumn(fkColumnName, fkColumn, att.getType());
            if (att.isSQLTypeSet()) {
                column.setSQLType(att.getSQLType());
            }
            if (att.isTypeConverterSet()) {
                column.setTypeConverter(att.getTypeConverter());
            }
            pkToFkMapTable.put(att, column);
        }

        EJB fkEJB = mappedRoleInfo[1].ejb;
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

    private void buildSchemaForJoin(RoleInfo[] roleInfo, String mtmEntityName, int id) {
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
        CMRField cmrField = new CMRField(endName0, roleInfo[1].ejb, roleInfo[1].isOne, roleInfo[1].isCascadeDelete, relationship, isVirtual, roleInfo[0].isOnPKSide);
        roleInfo[0].ejb.addCMRField(cmrField);
        roleInfo[0].table.addEndTable(new EndTable(endName0, roleInfo[1].table, roleInfo[1].isOne, roleInfo[1].isCascadeDelete, joinTable, isVirtual, roleInfo[0].isOnPKSide));

        isVirtual = null == roleInfo[1].cmrFieldName;
        String endName1 = isVirtual ? "$VirtualEnd" + id : roleInfo[1].cmrFieldName;
        cmrField = new CMRField(endName1, roleInfo[0].ejb, roleInfo[0].isOne, roleInfo[0].isCascadeDelete, relationship, isVirtual, roleInfo[1].isOnPKSide);
        roleInfo[1].ejb.addCMRField(cmrField);
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
        }
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

    private static boolean isCMP2(EntityBeanType entityBean) throws DeploymentException {
        if (!entityBean.isSetCmpVersion()) {
            return true;
        }
        String version = getString(entityBean.getCmpVersion());
        if ("1.x".equals(version)) {
            return false;
        } else if ("2.x".equals(version)) {
            return true;
        } else {
            throw new DeploymentException("cmp-version must be either 1.x or 2.x, but was " + version);
        }
    }

    private static class RoleInfo {
        private final String relationName;
        private final String roleName;
        private final String entityName;
        final String cmrFieldName;
        EJB ejb;
        Table table;
        boolean isOne;
        boolean isCascadeDelete;
        boolean isOnPKSide;
        JoinDefinition ejbJDef;
        JoinDefinition tableJDef;
        private RoleInfo(String relationName, String roleName, String entityName, String cmrFieldName) {
            this.relationName = relationName;
            this.roleName = roleName;
            this.entityName = entityName;
            this.cmrFieldName = cmrFieldName;
        }
        public boolean implies(RoleInfo other) {
            if (false == entityName.equals(other.entityName)) {
                return false;
            }
            if (null != relationName && relationName.equals(other.relationName)) {
                if (null != roleName && null != other.roleName) {
                    if (false == roleName.equals(other.roleName)) {
                        return false;
                    }
                    if (null != cmrFieldName && null != other.cmrFieldName && false == cmrFieldName.equals(other.cmrFieldName)) {
                        throw new IllegalArgumentException("ejb-relation-name [" + relationName +
                                "]/ejb-relationship-role-name [" + roleName +"] is invalid: CMR field [" +
                                other.cmrFieldName + "] is expected for this role. Found [" + cmrFieldName + "].");
                    }
                    return true;
                }
            }
            return null != cmrFieldName && null != other.cmrFieldName && cmrFieldName.equals(other.cmrFieldName);
        }
        public int hashCode() {
            int code = entityName.hashCode();
            if (null != relationName) {
                code ^= relationName.hashCode();
            }
            if (null != roleName) {
                code ^= roleName.hashCode();
            }
            if (null != cmrFieldName) {
                code ^= cmrFieldName.hashCode();
            }
            return code;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof RoleInfo ) {
                return false;
            }
            RoleInfo other = (RoleInfo) obj;
            if (null != relationName && false == relationName.equals(other.relationName)) {
                return false;
            }
            if (null != roleName && false == roleName.equals(other.roleName)) {
                return false;
            }
            if (null != cmrFieldName && false == cmrFieldName.equals(other.cmrFieldName)) {
                return false;
            }
            return entityName.equals(other.entityName);
        }
        public String toString() {
            return "Relation Name [" + relationName + "]; Role Name [" + roleName + 
                "]; EJB [" + entityName + "]; CMR field [" + cmrFieldName + "]";
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
    
    protected abstract EJBProxyFactory buildEJBProxyFactory(EntityBeanType entityBean, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) throws DeploymentException;
    
    protected abstract PrimaryKeyGenerator buildPKGenerator(EjbKeyGeneratorType config, Class pkClass) throws DeploymentException, QueryException;
}