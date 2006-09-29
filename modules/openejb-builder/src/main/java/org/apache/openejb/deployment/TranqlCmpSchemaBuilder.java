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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbNameType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationshipRoleType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.JavaTypeType;
import org.apache.geronimo.xbeans.j2ee.QueryType;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.entity.cmp.AutoIncrementTablePrimaryKeyGenerator;
import org.apache.openejb.entity.cmp.CmpFieldSchema;
import org.apache.openejb.entity.cmp.CustomPrimaryKeyGenerator;
import org.apache.openejb.entity.cmp.EntitySchema;
import org.apache.openejb.entity.cmp.ManyToManyRelationSchema;
import org.apache.openejb.entity.cmp.ModuleSchema;
import org.apache.openejb.entity.cmp.OneToManyRelationSchema;
import org.apache.openejb.entity.cmp.PrimaryKeyGenerator;
import org.apache.openejb.entity.cmp.QuerySpec;
import org.apache.openejb.entity.cmp.RelationSchema;
import org.apache.openejb.entity.cmp.RoleSchema;
import org.apache.openejb.entity.cmp.SequenceTableKeyGenerator;
import org.apache.openejb.entity.cmp.SqlPrimaryKeyGenerator;
import org.apache.openejb.entity.cmp.TranqlModuleCmpEngineGBean;
import org.apache.openejb.xbeans.ejbjar.OpenejbCmpFieldGroupMappingType;
import org.apache.openejb.xbeans.ejbjar.OpenejbCmrFieldGroupMappingType;
import org.apache.openejb.xbeans.ejbjar.OpenejbEjbRelationType;
import org.apache.openejb.xbeans.ejbjar.OpenejbEjbRelationshipRoleType;
import org.apache.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.apache.openejb.xbeans.ejbjar.OpenejbEntityGroupMappingType;
import org.apache.openejb.xbeans.ejbjar.OpenejbGroupType;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.apache.openejb.xbeans.ejbjar.OpenejbQueryType;
import org.apache.openejb.xbeans.pkgen.EjbAutoIncrementTableType;
import org.apache.openejb.xbeans.pkgen.EjbCustomGeneratorType;
import org.apache.openejb.xbeans.pkgen.EjbKeyGeneratorType;
import org.apache.openejb.xbeans.pkgen.EjbSequenceTableType;
import org.apache.openejb.xbeans.pkgen.EjbSqlGeneratorType;


/**
 * @version $Revision$ $Date$
 */
public class TranqlCmpSchemaBuilder implements CmpSchemaBuilder {
    public void initContext(EARContext earContext, EJBModule ejbModule, ClassLoader cl) throws DeploymentException {
        ModuleSchema moduleSchema = buildModuleSchema(earContext.getNaming(), ejbModule, cl);
        if (moduleSchema.getEntities().isEmpty()) {
            return;
        }

        AbstractName moduleCmpEngineName = earContext.getNaming().createChildName(ejbModule.getModuleName(), "moduleCmpEngine", "ModuleCmpEngine");
        GBeanData moduleCmpEngine = new GBeanData(moduleCmpEngineName, TranqlModuleCmpEngineGBean.GBEAN_INFO);

        // moduleSchema
        moduleCmpEngine.setAttribute("moduleSchema", moduleSchema);

        // transactionManager
        moduleCmpEngine.setReferencePattern("transactionManager", earContext.getTransactionManagerName());

        try {
            earContext.addGBean(moduleCmpEngine);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Unable to initialize TranQL module CMP engine", e);
        }
        ejbModule.setModuleCmpEngineName(moduleCmpEngineName);
    }

    public void addBeans(EARContext earContext, EJBModule ejbModule, ClassLoader cl) throws DeploymentException {
        AbstractName moduleCmpEngineName = ejbModule.getModuleCmpEngineName();
        if (null == moduleCmpEngineName) {
            return;
        }

        GBeanData moduleCmpEngine = null;
        try {
            moduleCmpEngine = earContext.getGBeanInstance(moduleCmpEngineName);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("No module CMP engine defined: moduleCmpEngineName=" + moduleCmpEngineName);
        }

        // add depenencies on custom pk generator gbeans
        ModuleSchema moduleSchema = (ModuleSchema) moduleCmpEngine.getAttribute("moduleSchema");
        Map entities = moduleSchema.getEntities();
        for (Iterator iterator = entities.values().iterator(); iterator.hasNext();) {
            EntitySchema entity = (EntitySchema) iterator.next();
            PrimaryKeyGenerator primaryKeyGenerator = entity.getPrimaryKeyGenerator();
            if (primaryKeyGenerator instanceof CustomPrimaryKeyGenerator) {
                CustomPrimaryKeyGenerator customPrimaryKeyGenerator = (CustomPrimaryKeyGenerator) primaryKeyGenerator;
                String generatorName = customPrimaryKeyGenerator.getGeneratorName();

                Map nameMap = new HashMap();
                nameMap.put("name", generatorName);
                nameMap.put("j2eeType", NameFactory.KEY_GENERATOR);
                AbstractNameQuery generatorQuery = new AbstractNameQuery(null, nameMap, org.tranql.pkgenerator.PrimaryKeyGenerator.class.getName());
                moduleCmpEngine.addDependency(generatorQuery);
            }
        }

        // connectionFactory
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) ejbModule.getVendorDD();
        GerResourceLocatorType connectionFactoryLocator = openejbEjbJar.getCmpConnectionFactory();
        if (connectionFactoryLocator == null) {
            throw new DeploymentException("A cmp-connection-factory element must be specified as CMP EntityBeans are defined.");
        }

        AbstractNameQuery connectionFactoryObjectName = null;
        try {
            connectionFactoryObjectName = OpenEjbModuleBuilder.getResourceContainerId(connectionFactoryLocator, earContext);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException(e);
        }
        //TODO this uses connection factory rather than datasource for the type.
        moduleCmpEngine.setReferencePattern("connectionFactory", connectionFactoryObjectName);
    }

    public ModuleSchema buildModuleSchema(Naming naming, EJBModule ejbModule, ClassLoader cl) throws DeploymentException {
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) ejbModule.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();

        ModuleSchema moduleSchema = new ModuleSchema(ejbModule.getName());
        if (openejbEjbJar.isSetEnforceForeignKeyConstraints()) {
            moduleSchema.setEnforceForeignKeyConstraints(openejbEjbJar.isSetEnforceForeignKeyConstraints());
        }
        if (openejbEjbJar.isSetEjbQlCompilerFactory()) {
            moduleSchema.setEjbQlCompilerFactory(openejbEjbJar.getEjbQlCompilerFactory().trim());
        }
        if (openejbEjbJar.isSetDbSyntaxFactory()) {
            moduleSchema.setDbSyntaxFactory(openejbEjbJar.getDbSyntaxFactory().trim());
        }

        try {
            processEnterpriseBeans(naming, moduleSchema, ejbModule, cl);
            processRelationships(moduleSchema, ejbJar, openejbEjbJar);
        } catch (Exception e) {
            throw new DeploymentException("Could not deploy module", e);
        }

        return moduleSchema;
    }

    private void processEnterpriseBeans(Naming naming, ModuleSchema moduleSchema, EJBModule ejbModule, ClassLoader cl) throws DeploymentException {
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) ejbModule.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();

        // index openejb descriptors
        Map openEjbEntities = new HashMap();
        OpenejbEntityBeanType[] openEJBEntities = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openEJBEntities.length; i++) {
            OpenejbEntityBeanType entity = openEJBEntities[i];
            openEjbEntities.put(entity.getEjbName(), entity);
        }


        EntityBeanType[] entityBeans = ejbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];
            if (!"Container".equals(getString(entityBean.getPersistenceType()))) {
                continue;
            }

            // ejbName
            String ejbName = getString(entityBean.getEjbName());

            // -- get the objenEjbMapping
            OpenejbEntityBeanType openEjbEntity = (OpenejbEntityBeanType) openEjbEntities.get(ejbName);
            if (null == openEjbEntity) {
                throw new DeploymentException("EJB [" + ejbName + "] is misconfigured: no CMP mapping defined by OpenEJB DD.");
            }

            // -- create entity schema
            EntitySchema entitySchema = moduleSchema.addEntity(ejbName);

            // containerId
            AbstractName containerId = naming.createChildName(ejbModule.getModuleName(), ejbName, NameFactory.ENTITY_BEAN);
            entitySchema.setContainerId(containerId.toString());

            // cmp2
            boolean cmp2 = isCMP2(entityBean);
            entitySchema.setCmp2(cmp2);

            // abstractSchemaName
            String abstractSchemaName;
            if (cmp2) {
                abstractSchemaName = getString(entityBean.getAbstractSchemaName());
            } else {
                abstractSchemaName = ejbName;
            }
            entitySchema.setAbstractSchemaName(abstractSchemaName);

            // tableName
            String tableName = openEjbEntity.getTableName().trim();
            entitySchema.setTableName(tableName);

            // remoteInterfaceName
            String remoteInterfaceName = getString(entityBean.getRemote());
            entitySchema.setRemoteInterfaceName(remoteInterfaceName);

            // homeInterfaceName
            String homeInterfaceName = getString(entityBean.getHome());
            entitySchema.setHomeInterfaceName(homeInterfaceName);

            // localInterfaceName
            String localInterfaceName = getString(entityBean.getLocal());
            entitySchema.setLocalInterfaceName(localInterfaceName);

            // localHomeInterfaceName
            String localHomeInterfaceName = getString(entityBean.getLocalHome());
            entitySchema.setLocalHomeInterfaceName(localHomeInterfaceName);

            // ejbClassName
            String ejbClassName = getString(entityBean.getEjbClass());
            entitySchema.setEjbClassName(ejbClassName);

            // static sql
            entitySchema.setStaticSql(openEjbEntity.isSetStaticSql());

            // -- load ejbClass
            Class ejbClass;
            try {
                ejbClass = cl.loadClass(ejbClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load cmp bean class: ejbName=" + ejbName + " ejbClass=" + getString(entityBean.getEjbClass()));
            }

            // -- index the CMP fields
            Map cmpFieldToMapping = new HashMap();
            OpenejbEntityBeanType.CmpFieldMapping mappings[] = openEjbEntity.getCmpFieldMappingArray();
            for (int j = 0; j < mappings.length; j++) {
                OpenejbEntityBeanType.CmpFieldMapping mapping = mappings[j];
                cmpFieldToMapping.put(mapping.getCmpFieldName().trim(), mapping);
            }

            // -- process primary key fields
            Set pkFieldNames = processPrimaryKeyFields(entitySchema, entityBean, openEjbEntity, cmpFieldToMapping, ejbClass, cl);

            // -- process cmpFields
            processCmpFields(entitySchema, entityBean, pkFieldNames, cmpFieldToMapping, ejbClass);

            // -- process primary key generator
            if (openEjbEntity.isSetKeyGenerator()) {
                PrimaryKeyGenerator primaryKeyGenerator = processPkGenerator(openEjbEntity.getKeyGenerator());
                entitySchema.setPrimaryKeyGenerator(primaryKeyGenerator);
            }

            // -- process cache
            processCache(entitySchema, openEjbEntity);

            // -- process prefetch groups
            processPrefetchGroups(entitySchema, openEjbEntity);

            // -- process the queries
            processQuery(entitySchema, entityBean, openEjbEntity);
        }
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

    private Set processPrimaryKeyFields(EntitySchema entitySchema, EntityBeanType entityBean, OpenejbEntityBeanType openEjbEntity, Map cmpFieldToMapping, Class ejbClass, ClassLoader cl) throws DeploymentException {
        // unknownPk
        boolean unknownPk = getString(entityBean.getPrimKeyClass()).equals("java.lang.Object");
        entitySchema.setUnknownPk(unknownPk);

        // pkClassName
        String pkClassName = getString(entityBean.getPrimKeyClass());

        // Handle "Unknown Primary Key Type" -- try to identify the PK class
        if (pkClassName.equals("java.lang.Object")) {
            // a primary key generator is required for an ejb with an "unkown" primary key type
            if (!openEjbEntity.isSetKeyGenerator()) {
                throw new DeploymentException("Automatic key generation is not defined: ejbName=" + entityBean.getEjbName());
            }
            if (!openEjbEntity.isSetPrimkeyField()) {
                throw new DeploymentException("EJB " + entityBean.getEjbName() + " has an \"unknown primary key type\" (java.lang.Object).  A primkey-field element must be present in openejb-jar.xml to indicate the actual primary key type.");
            }

            String pkFieldName = openEjbEntity.getPrimkeyField();
            OpenejbEntityBeanType.CmpFieldMapping pkField = (OpenejbEntityBeanType.CmpFieldMapping) cmpFieldToMapping.get(pkFieldName);
            if (pkField == null) {
                throw new DeploymentException("EJB " + entityBean.getEjbName() + " lists a primkey-field (" + pkFieldName + ") but there is no matching cmp-field-mapping");
            }
            if (pkField.isSetCmpFieldClass()) { // Check if the field class was provided in openejb-jar.xml
                pkClassName = pkField.getCmpFieldClass();
            } else { // Otherwise it has to be a getter on the EJB class itself
                pkClassName = getCMPFieldType(entitySchema.isCmp2(), pkFieldName, ejbClass).getName();
            }
            if (pkClassName == null) { // should never happen
                throw new DeploymentException("Cannot determine actual primary key field type for EJB " + entityBean.getEjbName());
            }
        }
        entitySchema.setPkClassName(pkClassName);

        // -- load the pkClass
        Class pkClass;
        try {
            pkClass = cl.loadClass(pkClassName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load cmp primary key class: ejbName=" + entitySchema.getEjbName() + " pkClass=" + getString(entityBean.getPrimKeyClass()));
        }

        // -- build a Set<String> containing all of the pkFieldNames
        Set pkFieldNames;
        if (unknownPk && openEjbEntity.isSetPrimkeyField()) {
            pkFieldNames = new HashSet(1);
            pkFieldNames.add(openEjbEntity.getPrimkeyField().trim());
        } else if (!entityBean.isSetPrimkeyField()) {
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
                throw new DeploymentException("Invalid primary key class: ejbName=" + entitySchema.getEjbName() + " pkClass=" + pkClass);
            }
        } else {
            // specific field is primary key
            pkFieldNames = new HashSet(1);
            pkFieldNames.add(getString(entityBean.getPrimkeyField()));
        }
        return pkFieldNames;
    }

    private void processCmpFields(EntitySchema entitySchema, EntityBeanType entityBean, Set pkFieldNames, Map cmpFieldToMapping, Class ejbClass) throws DeploymentException {
        // -- add cmp fields to ejb
        CmpFieldType[] cmpFieldTypes = entityBean.getCmpFieldArray();
        for (int cmpFieldIndex = 0; cmpFieldIndex < cmpFieldTypes.length; cmpFieldIndex++) {
            CmpFieldType cmpFieldType = cmpFieldTypes[cmpFieldIndex];

            // fieldName
            String fieldName = getString(cmpFieldType.getFieldName());

            // -- get OpenEJB mapping
            OpenejbEntityBeanType.CmpFieldMapping mapping = (OpenejbEntityBeanType.CmpFieldMapping) cmpFieldToMapping.remove(fieldName);
            if (null == mapping) {
                throw new DeploymentException("EJB [" + entitySchema.getEjbName() + "] is misconfigured: CMP field [" + fieldName + "] not mapped by OpenEJB DD.");
            }

            // -- create the field schema
            CmpFieldSchema cmpFieldSchema = entitySchema.addCmpField(fieldName);

            // columnName
            String columnName = mapping.getTableColumn().trim();
            cmpFieldSchema.setColumnName(columnName);

            // fieldType
            Class fieldType = getCMPFieldType(entitySchema.isCmp2(), fieldName, ejbClass);
            cmpFieldSchema.setFieldTypeName(fieldType.getName());

            // isPkField
            boolean isPKField = pkFieldNames.contains(fieldName);
            cmpFieldSchema.setPkField(isPKField);

            // sqlType
            if (mapping.isSetSqlType()) {
                String sqlType = mapping.getSqlType().trim();
                cmpFieldSchema.setSqlType(sqlType);
            }

            // typeConverterClassName
            if (mapping.isSetTypeConverter()) {
                String typeConverterClassName = mapping.getTypeConverter().trim();
                cmpFieldSchema.setTypeConverterClassName(typeConverterClassName);
            }

            if (isPKField) {
                pkFieldNames.remove(fieldName);
            }
        }

        // -- add virtual fields defined in openejb-jar.xml
        for (Iterator iter = cmpFieldToMapping.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            OpenejbEntityBeanType.CmpFieldMapping mapping = (OpenejbEntityBeanType.CmpFieldMapping) entry.getValue();

            // fieldName
            String fieldName = mapping.getCmpFieldName().trim();

            // -- verify type is set
            if (!mapping.isSetCmpFieldClass()) {
                throw new DeploymentException("Class must be defined for an automatic primary key field: ejbName=" + entitySchema.getEjbName() + " field=" + fieldName);
            }

            // -- create the field schema
            CmpFieldSchema cmpFieldSchema = entitySchema.addCmpField(fieldName);
            cmpFieldSchema.setVirtual(true);

            // columnName
            String columnName = mapping.getTableColumn().trim();
            cmpFieldSchema.setColumnName(columnName);

            // fieldClassName
            String fieldClass = mapping.getCmpFieldClass().trim();
            cmpFieldSchema.setFieldTypeName(fieldClass);

            // isPkField
            boolean isPKField = pkFieldNames.contains(fieldName);
            cmpFieldSchema.setPkField(isPKField);

            if (isPKField) {
                pkFieldNames.remove(fieldName);
            }
        }

        // -- verify we mapped all of the pk fields
        if (!pkFieldNames.isEmpty()) {
            StringBuffer fields = new StringBuffer();
            fields.append("EJB [").append(entitySchema.getEjbName()).append("] is misconfigured: could not find CMP fields for following pk fields:");
            for (Iterator iterator = pkFieldNames.iterator(); iterator.hasNext();) {
                fields.append(" [");
                fields.append(iterator.next());
                fields.append("]");
            }
            throw new DeploymentException(fields.toString());
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

    private PrimaryKeyGenerator processPkGenerator(EjbKeyGeneratorType config) throws DeploymentException {
        //todo: Handle a PK Class with multiple fields?
        if(config.isSetCustomGenerator()) {
            EjbCustomGeneratorType custom = config.getCustomGenerator();

            // generatorName
            String generatorName = custom.getGeneratorName();
            return new CustomPrimaryKeyGenerator(generatorName);
        } else if(config.isSetSqlGenerator()) {
            EjbSqlGeneratorType sqlGen = config.getSqlGenerator();

            // sql
            String sql = sqlGen.getSql();

            return new SqlPrimaryKeyGenerator(sql);
        } else if(config.isSetSequenceTable()) {
            EjbSequenceTableType seq = config.getSequenceTable();

            // tableName
            String tableName = seq.getTableName();

            // sequenceName
            String sequenceName = seq.getSequenceName();

            // batchSize
            int batchSize = seq.getBatchSize();

            return new SequenceTableKeyGenerator(tableName, sequenceName, batchSize);
        } else if(config.isSetAutoIncrementTable()) {
            EjbAutoIncrementTableType auto = config.getAutoIncrementTable();

            // sql
            String sql = auto.getSql();

            return new AutoIncrementTablePrimaryKeyGenerator(sql);
        }
        throw new DeploymentException("Unknown primay key generator " + config);
    }

    private void processPrefetchGroups(EntitySchema entity, OpenejbEntityBeanType openEJBEntity) throws DeploymentException {
        if (!openEJBEntity.isSetPrefetchGroup()) {
            return;
        }

        OpenejbGroupType[] groups = openEJBEntity.getPrefetchGroup().getGroupArray();
        for (int j = 0; j < groups.length; j++) {
            OpenejbGroupType group = groups[j];

            // groupName
            String groupName = group.getGroupName();

            // -- create the prefetchGroup
            org.apache.openejb.entity.cmp.PrefetchGroup prefetchGroup = entity.addPrefetchGroup(groupName);

            // cmpFields
            String[] cmpFields = group.getCmpFieldNameArray();
            for (int k = 0; k < cmpFields.length; k++) {
                String cmpField = cmpFields[k];
                prefetchGroup.addCmpField(cmpField);
            }

            // cmrFields
            OpenejbGroupType.CmrField[] cmrFields = group.getCmrFieldArray();
            for (int k = 0; k < cmrFields.length; k++) {
                String cmrFieldName = cmrFields[k].getCmrFieldName();
                String cmrGroupName;
                if (cmrFields[k].isSetGroupName()) {
                    cmrGroupName = cmrFields[k].getGroupName();
                } else {
                    cmrGroupName = groupName;
                }
                prefetchGroup.addCmrField(cmrFieldName, cmrGroupName);
            }
        }

        OpenejbEntityBeanType.PrefetchGroup prefetchGroup = openEJBEntity.getPrefetchGroup();

        if (prefetchGroup.isSetEntityGroupMapping()) {
            OpenejbEntityGroupMappingType mapping = prefetchGroup.getEntityGroupMapping();
            entity.setPrefetchGroupName(mapping.getGroupName());
        }

        OpenejbCmpFieldGroupMappingType[] cmpMappings = prefetchGroup.getCmpFieldGroupMappingArray();
        for (int j = 0; j < cmpMappings.length; j++) {
            OpenejbCmpFieldGroupMappingType mapping = cmpMappings[j];
            String cmpFieldName = mapping.getCmpFieldName();
            CmpFieldSchema cmpField = entity.getCmpField(cmpFieldName);
            if (null == cmpField) {
                throw new DeploymentException("EJB [" + entity.getEjbName() + "] does not define the CMP field [" + cmpFieldName + "].");
            }
            String groupName = mapping.getGroupName();
            cmpField.setPrefetchGroup(groupName);
        }

        OpenejbCmrFieldGroupMappingType[] cmrMappings = prefetchGroup.getCmrFieldGroupMappingArray();
        for (int j = 0; j < cmrMappings.length; j++) {
            OpenejbCmrFieldGroupMappingType mapping = cmrMappings[j];
            String cmrFieldName = mapping.getCmrFieldName();
            String groupName = mapping.getGroupName();
            entity.setCmrPrefetchGroup(cmrFieldName, groupName);
        }
    }

    private void processCache(EntitySchema entity, OpenejbEntityBeanType openEJBEntity) {
        if (!openEJBEntity.isSetCache()) {
            return;
        }

        OpenejbEntityBeanType.Cache cache = openEJBEntity.getCache();

        // cacheSize
        int cacheSize = cache.getSize();
        entity.setCacheSize(cacheSize);

        // isolationLevel
        OpenejbEntityBeanType.Cache.IsolationLevel.Enum isolationLevel = cache.getIsolationLevel();
        if (OpenejbEntityBeanType.Cache.IsolationLevel.READ_COMMITTED == isolationLevel) {
            entity.setIsolationLevel("READ_COMMITTED");
        } else if (OpenejbEntityBeanType.Cache.IsolationLevel.READ_UNCOMMITTED == isolationLevel) {
            entity.setIsolationLevel("READ_UNCOMMITTED");
        } else if (OpenejbEntityBeanType.Cache.IsolationLevel.REPEATABLE_READ == isolationLevel) {
            entity.setIsolationLevel("REPEATABLE_READ");
        } else {
            throw new AssertionError();
        }
    }

    private void processQuery(EntitySchema entitySchema, EntityBeanType entityBean, OpenejbEntityBeanType openEjbEntity) throws DeploymentException {
        // -- process queries defined in the ejb-jar.xml file
        QueryType[] queryTypes = entityBean.getQueryArray();
        if (null != queryTypes) {
            for (int i = 0; i < queryTypes.length; i++) {
                QueryType queryType = queryTypes[i];

                // methodName
                String methodName = getString(queryType.getQueryMethod().getMethodName());

                // -- build parameter type array
                String[] parameterTypeNames = null;
                JavaTypeType[] javaTypeTypes = queryType.getQueryMethod().getMethodParams().getMethodParamArray();
                if (javaTypeTypes != null) {
                    parameterTypeNames = new String[javaTypeTypes.length];
                    for (int j = 0; j < javaTypeTypes.length; j++) {
                        // parameterTypeName
                        String paramType = getString(javaTypeTypes[j]);
                        parameterTypeNames[j] = paramType;
                    }
                }

                // build the query spec
                MethodSignature methodSignature = new MethodSignature(methodName, parameterTypeNames);
                QuerySpec querySpec = entitySchema.addQuery(methodSignature);

                // local
                boolean local = true;
                if (queryType.isSetResultTypeMapping()) {
                    String resultTypeMapping = queryType.getResultTypeMapping().getStringValue();
                    local = !"Remote".equals(resultTypeMapping);
                }
                querySpec.setLocal(local);

                // ejbQL
                String ejbQL = queryType.getEjbQl().getStringValue();
                querySpec.setEjbQl(ejbQL);

            }
        }

        // -- process queries defined in the openejb-jar.xml file
        OpenejbQueryType[] openejbQueryTypes = openEjbEntity.getQueryArray();
        if (null != openejbQueryTypes) {
            for (int i = 0; i < openejbQueryTypes.length; i++) {
                OpenejbQueryType openejbQueryType = openejbQueryTypes[i];

                // methodName
                String methodName = openejbQueryType.getQueryMethod().getMethodName();

                // -- build parameter type array
                String[] parameterTypeNames = null;
                String[] javaTypeTypes = openejbQueryType.getQueryMethod().getMethodParams().getMethodParamArray();
                if (null != javaTypeTypes) {
                    for (int j = 0; j < javaTypeTypes.length; j++) {
                        // parameterType
                        String paramType = javaTypeTypes[j];
                        parameterTypeNames[i] = paramType;
                    }
                }

                // get the query spec or build a new one
                MethodSignature methodSignature = new MethodSignature(methodName, parameterTypeNames);
                QuerySpec querySpec = entitySchema.getQuery(methodSignature);
                if (querySpec == null) {
                    querySpec = entitySchema.addQuery(methodSignature);
                }

                // flushCacheBeforeQuery
                boolean flushCacheBeforeQuery = !openejbQueryType.isSetNoCacheFlush();
                querySpec.setFlushCacheBeforeQuery(flushCacheBeforeQuery);

                // ejbQl
                String ejbQL = null;
                if (openejbQueryType.isSetEjbQl()) {
                    ejbQL = openejbQueryType.getEjbQl();
                    querySpec.setEjbQl(ejbQL);
                } else if (!flushCacheBeforeQuery) {
                    throw new DeploymentException("No ejb-ql defined and flush-cache-before-query not set. method " + methodName);
                }

                // groupName
                String groupName = null;
                if (openejbQueryType.isSetGroupName()) {
                    groupName = openejbQueryType.getGroupName();
                    querySpec.setPrefetchGroup(groupName);
                }
            }
        }
    }

    private void processRelationships(ModuleSchema moduleSchema, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar) throws DeploymentException {
        if (!ejbJar.isSetRelationships()) {
            return;
        } else if (!openejbEjbJar.isSetRelationships()) {
            throw new DeploymentException("Relationships are not mapped by OpenEJB DD.");
        }

        // -- index the openejb relation mappings by roleInfo (unique key for a relation... defined below)
        Map openEjbRelations = new HashMap();
        OpenejbEjbRelationType[] openEJBRelations = openejbEjbJar.getRelationships().getEjbRelationArray();
        for (int i = 0; i < openEJBRelations.length; i++) {
            OpenejbEjbRelationType relation = openEJBRelations[i];
            String relationName = null;
            if (relation.isSetEjbRelationName()) {
                relationName = relation.getEjbRelationName();
            }
            OpenejbEjbRelationshipRoleType[] roles = relation.getEjbRelationshipRoleArray();
            for (int j = 0; j < roles.length; j++) {
                OpenejbEjbRelationshipRoleType role = roles[j];
                // Note: we're putting the whole relation into the map, not just the relationship-role that we found
                // Later, we'll dig out both roles for a many-to-many relationship, even if only one of them
                //   had a cmr-field and got past the isSetCmrField test above
                openEjbRelations.put(createRoleSchema(role, relationName), relation);
            }
        }

        EjbRelationType[] ejbRelations = ejbJar.getRelationships().getEjbRelationArray();
        for (int i = 0; i < ejbRelations.length; i++) {
            EjbRelationType ejbRelation = ejbRelations[i];
            RelationSchema relation = createRelation(ejbRelation, openEjbRelations);
            moduleSchema.addRelation(relation);
        }
    }

    private RelationSchema createRelation(EjbRelationType relation, Map openEjbRelations) throws DeploymentException {
        // relationName
        String relationName = null;
        if (relation.isSetEjbRelationName()) {
            relationName = relation.getEjbRelationName().getStringValue();
        }

        // -- build RoleInfo objects for each side of the relations
        EjbRelationshipRoleType[] relationshipRoles = relation.getEjbRelationshipRoleArray();
        RoleSchema[] roleSchemas = new RoleSchema[2];
        roleSchemas[0] = createRoleSchema(relationshipRoles[0], relationName);
        roleSchemas[1] = createRoleSchema(relationshipRoles[1], relationName);

        // -- locate the matching openejb relation configuration
        OpenejbEjbRelationType openEjbRelation = null;
        for (Iterator iter = openEjbRelations.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            RoleSchema currRoleInfo = (RoleSchema) entry.getKey();
            if (currRoleInfo.implies(roleSchemas[0]) || currRoleInfo.implies(roleSchemas[1])) {
                openEjbRelation = (OpenejbEjbRelationType) entry.getValue();
                break;
            }
        }
        if (null == openEjbRelation) {
            throw new DeploymentException("No CMR mapping defined by OpenEJB DD for roles " + roleSchemas[0] + " or " + roleSchemas[1]);
        }

        // one to one
        if (roleSchemas[0].isOne() || roleSchemas[1].isOne()) {
            return createOneToManyRelationSchema(roleSchemas, openEjbRelation, relationName);
        } else {
            return createManyToManyRelationSchema(roleSchemas, openEjbRelation, relationName);
        }
    }

    private OneToManyRelationSchema createOneToManyRelationSchema(RoleSchema[] roleSchemas, OpenejbEjbRelationType openEjbRelation, String relationName) throws DeploymentException {
        OpenejbEjbRelationshipRoleType[] openejbJarRoles = openEjbRelation.getEjbRelationshipRoleArray();
        if (openejbJarRoles.length != 1) {
            throw new DeploymentException("Relation between " + roleSchemas[0] + " and " + roleSchemas[1] + " is one-to-one or one-to-many but has cmr-field-mappings elements for both roles.");
        }

        // -- determine which role has the pk fields and which one has the fk fields
        RoleSchema pkRoleSchema;
        RoleSchema fkRoleSchema;

        // XOR: if the first role doesn't matche the first configured role or the fk columns are set on the other side (but not both) we need to swap the roles
        if (!roleSchemas[0].implies(createRoleSchema(openejbJarRoles[0], relationName)) ^ openejbJarRoles[0].isSetForeignKeyColumnOnSource()) {
            pkRoleSchema = roleSchemas[1];
            fkRoleSchema = roleSchemas[0];
        } else {
            pkRoleSchema = roleSchemas[0];
            fkRoleSchema = roleSchemas[1];
        }

        // map the pk columns
        Map pkMapping = createPkMapping(openejbJarRoles[0]);
        pkRoleSchema.setPkMapping(pkMapping);

        return new OneToManyRelationSchema(relationName, pkRoleSchema, fkRoleSchema);
    }

    private ManyToManyRelationSchema createManyToManyRelationSchema(RoleSchema[] roleSchemas, OpenejbEjbRelationType openEjbRelation, String relationName) throws DeploymentException {
        String manyToManyTableName = null;
        if (!openEjbRelation.isSetManyToManyTableName()) {
            throw new DeploymentException("Relation between " + roleSchemas[0] + " and " + roleSchemas[1] + " is many-to-many but does not declare the many-to-many-table-name.");
        }
        manyToManyTableName = openEjbRelation.getManyToManyTableName();

        OpenejbEjbRelationshipRoleType[] openejbJarRoles = openEjbRelation.getEjbRelationshipRoleArray();
        if (openejbJarRoles.length != 2) {
            throw new DeploymentException("Relation between " + roleSchemas[0] + " and " + roleSchemas[1] + " is many-to-many but does not have cmr-field-mappings elements for both roles.");
        }

        if (openejbJarRoles[0].isSetForeignKeyColumnOnSource() || openejbJarRoles[1].isSetForeignKeyColumnOnSource()) {
            throw new DeploymentException("Relation between " + roleSchemas[0] + " and " + roleSchemas[1] + " is many-to-many but uses the foreign-key-column-on-source element");
        }

        //
        RoleSchema leftRoleSchema;
        RoleSchema rightRoleSchema;

        // if the first role doesn't matche the first configured role or the fk columns are set on the other side (but not both) we need to swap the roles
        if (roleSchemas[0].implies(createRoleSchema(openejbJarRoles[0], relationName))) {
            leftRoleSchema = roleSchemas[0];
            rightRoleSchema = roleSchemas[1];
        } else {
            leftRoleSchema = roleSchemas[1];
            rightRoleSchema = roleSchemas[0];
        }

        Map leftPkMapping = createPkMapping(openejbJarRoles[0]);
        leftRoleSchema.setPkMapping(leftPkMapping);

        Map rightPkMapping = createPkMapping(openejbJarRoles[1]);
        rightRoleSchema.setPkMapping(rightPkMapping);


        return new ManyToManyRelationSchema(relationName, leftRoleSchema, rightRoleSchema, manyToManyTableName);
    }

    private RoleSchema createRoleSchema(EjbRelationshipRoleType role, String relationName) {
        // ejbName
        String ejbName = role.getRelationshipRoleSource().getEjbName().getStringValue();

        // relationshipRoleName
        String relationshipRoleName = role.isSetEjbRelationshipRoleName() ? role.getEjbRelationshipRoleName().getStringValue() : null;

        // cmrFieldName
        String cmrFieldName = role.isSetCmrField() ? role.getCmrField().getCmrFieldName().getStringValue() : null;

        RoleSchema roleSchema = new RoleSchema(relationName, relationshipRoleName, ejbName, cmrFieldName);

        // one
        if ("One".equals(role.getMultiplicity().getStringValue())) {
            roleSchema.setOne(true);
        }

        // cascadeDelete
        if (role.isSetCascadeDelete()) {
            roleSchema.setCascadeDelete(true);
        }
        return roleSchema;
    }

    private RoleSchema createRoleSchema(OpenejbEjbRelationshipRoleType openejbJarRole, String relationName) {
        // ejbName
        String ejbName = openejbJarRole.getRelationshipRoleSource().getEjbName();

        // relationshipRoleName
        String relationshipRoleName = openejbJarRole.isSetEjbRelationshipRoleName() ? openejbJarRole.getEjbRelationshipRoleName() : null;

        // cmrFieldName
        String cmrFieldName = openejbJarRole.isSetCmrField() ? openejbJarRole.getCmrField().getCmrFieldName() : null;

        RoleSchema roleSchema = new RoleSchema(relationName, relationshipRoleName, ejbName, cmrFieldName);

        return roleSchema;
    }

    private Map createPkMapping(OpenejbEjbRelationshipRoleType openejbJarRole) {
        Map pkMapping = new HashMap();
        OpenejbEjbRelationshipRoleType.RoleMapping.CmrFieldMapping[] mappings = openejbJarRole.getRoleMapping().getCmrFieldMappingArray();
        for (int i = 0; i < mappings.length; i++) {
            OpenejbEjbRelationshipRoleType.RoleMapping.CmrFieldMapping mapping = mappings[i];
            pkMapping.put(mapping.getKeyColumn(), mapping.getForeignKeyColumn());
        }
        return pkMapping;
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TranqlCmpSchemaBuilder.class, "CmpSchemaBuilder");
        infoBuilder.addInterface(CmpSchemaBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
