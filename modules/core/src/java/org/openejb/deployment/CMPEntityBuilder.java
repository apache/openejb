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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbNameType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationType;
import org.apache.geronimo.xbeans.j2ee.EjbRelationshipRoleType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.openejb.dispatch.MethodSignature;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.xbeans.ejbjar.OpenejbEjbRelationType;
import org.openejb.xbeans.ejbjar.OpenejbEjbRelationshipRoleType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.AutomaticKeyGeneration;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType.CmpFieldMapping;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbQueryType;
import org.openejb.transaction.TransactionPolicySource;
import org.tranql.cache.CacheSlot;
import org.tranql.cache.CacheTable;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBQueryBuilder;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.FKField;
import org.tranql.ejb.Relationship;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.identity.IdentityDefinerBuilder;
import org.tranql.pkgenerator.PrimaryKeyGeneratorDelegate;
import org.tranql.ql.QueryException;
import org.tranql.query.CommandTransform;
import org.tranql.query.SchemaMapper;
import org.tranql.query.UpdateCommand;
import org.tranql.schema.Association;
import org.tranql.schema.Association.JoinDefinition;
import org.tranql.schema.AssociationEnd;
import org.tranql.schema.Attribute;
import org.tranql.schema.Entity;
import org.tranql.schema.Schema;
import org.tranql.sql.Column;
import org.tranql.sql.EndTable;
import org.tranql.sql.FKColumn;
import org.tranql.sql.JoinTable;
import org.tranql.sql.SQLSchema;
import org.tranql.sql.Table;
import org.tranql.sql.sql92.SQL92Schema;


class CMPEntityBuilder extends EntityBuilder {
    public CMPEntityBuilder(OpenEJBModuleBuilder builder) {
        super(builder);
    }

    protected void buildBeans(EARContext earContext, J2eeContext moduleJ2eeContext, ClassLoader cl, EJBModule ejbModule, String connectionFactoryName, EJBSchema ejbSchema, SQL92Schema sqlSchema, GlobalSchema globalSchema, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, Security security, EnterpriseBeansType enterpriseBeans, TransactionManagerDelegate tmDelegate) throws DeploymentException {
        // CMP Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            if (!"Container".equals(getString(entityBean.getPersistenceType()))) {
                continue;
            }

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(getString(entityBean.getEjbName()));
            ObjectName entityObjectName = super.createEJBObjectName(moduleJ2eeContext, entityBean);

            GBeanMBean entityGBean = createBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, ejbSchema, sqlSchema, globalSchema, connectionFactoryName, transactionPolicyHelper, security, cl, tmDelegate);

            earContext.addGBean(entityObjectName, entityGBean);
        }
    }
    

    public void buildCMPSchema(EARContext earContext, J2eeContext moduleJ2eeContext, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQL92Schema sqlSchema, GlobalSchema globalSchema) throws DeploymentException {
        try {
            Collection entities = processEnterpriseBeans(earContext, moduleJ2eeContext, ejbJar, openejbEjbJar, cl, ejbSchema, sqlSchema);
            processRelationships(ejbJar, openejbEjbJar, ejbSchema, sqlSchema);
            populateGlobalSchema(entities, globalSchema, ejbSchema, sqlSchema);
        } catch (Exception e) {
            throw new DeploymentException("Module [" + moduleJ2eeContext.getJ2eeModuleName() + "]", e);
        }
    }

    private Collection processEnterpriseBeans(EARContext earContext, J2eeContext moduleJ2eeContext, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQL92Schema sqlSchema) throws DeploymentException {
        Collection entities = new ArrayList();

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
                    ObjectName wrapperGeneratorObjectName;
                    GBeanMBean keyGenerator = new GBeanMBean(PrimaryKeyGeneratorWrapper.GBEAN_INFO, cl);
                    try {
                        ObjectName generatorObjectName = new ObjectName(generatorName);
                        wrapperGeneratorObjectName = new ObjectName(generatorName + ",isWrapper=true");
                        keyGenerator.setReferencePatterns("PrimaryKeyGenerator", Collections.singleton(generatorObjectName));
                        keyGenerator.setAttribute("primaryKeyGeneratorDelegate", keyGeneratorDelegate);
                    } catch (Exception e) {
                        throw new DeploymentException("Unable to initialize PrimaryKeyGeneratorWrapper GBean", e);
                    }
                    earContext.addGBean(wrapperGeneratorObjectName, keyGenerator);
                    
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
                table.addColumn(new Column(fieldName, mapping.getTableColumn(), fieldType, isPKField));
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
                
            ejbSchema.addEJB(ejb);
            sqlSchema.addTable(table);
            entities.add(ejb);
        }
        
        return entities;
    }

    private void processRelationships(EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, EJBSchema ejbSchema, SQL92Schema sqlSchema) throws DeploymentException {
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

            OpenejbEjbRelationshipRoleType[] openEjbRoles = openEjbRelation.getEjbRelationshipRoleArray();
            for (int j = 0; j < openEjbRoles.length; j++) {
                extractJoinInfo(roleInfo, openEjbRoles[j]);
            }
            
            String mtmTableName = null;
            if ( !roleInfo[0].isOne && !roleInfo[1].isOne ) {
                if ( !openEjbRelation.isSetManyToManyTableName() ) {
                    throw new DeploymentException("MTM relation between " + roleInfo[0] + " and " + roleInfo[1] +
                            " is misconfigured: no many to many table defined by OpenEJB DD.");
                }
                mtmTableName = openEjbRelation.getManyToManyTableName();
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
        return roleInfo;
    }

    private void extractJoinInfo(RoleInfo[] roleInfo, OpenejbEjbRelationshipRoleType role) throws DeploymentException {
        String ejbName = role.getRelationshipRoleSource().getEjbName();
        String cmrFieldName = null;
        if ( role.isSetCmrField() ) {
            cmrFieldName = role.getCmrField().getCmrFieldName();
        }
        RoleInfo sourceRoleInfo = new RoleInfo(ejbName, cmrFieldName);
        RoleInfo[] mappedRoleInfo = new RoleInfo[2];
        if (roleInfo[0].equals(sourceRoleInfo)) {
            mappedRoleInfo = roleInfo;
        } else {
            mappedRoleInfo[0] = roleInfo[1];
            mappedRoleInfo[1] = roleInfo[0];
        }

        if ( role.isSetForeignKeyColumnOnSource() ) {
            RoleInfo tmp = mappedRoleInfo[0];
            mappedRoleInfo[0] = mappedRoleInfo[1];
            mappedRoleInfo[1] = tmp;
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
            Attribute att = (Attribute) attIter.next();
            String pkColumn = att.getPhysicalName();
            String fkColumn = (String) pkToFkMap.get(pkColumn);
            if (null == fkColumn) {
                throw new DeploymentException("Role " + sourceRoleInfo + " is misconfigured: column [" + pkColumn + "] is not a primary key.");
            }
            pkToFkMapEJB.put(pkEJB.getAttribute(att.getName()), new FKField(fkColumn, att.getType()));
            pkToFkMapTable.put(att, new FKColumn(fkColumn, att.getType()));
        }

        mappedRoleInfo[0].ejbJDef = new JoinDefinition(mappedRoleInfo[0].ejb, mappedRoleInfo[1].ejb, pkToFkMapEJB);
        mappedRoleInfo[0].tableJDef = new JoinDefinition(mappedRoleInfo[0].table, mappedRoleInfo[1].table, pkToFkMapTable);
    }

    private void buildSchemaForJoin(RoleInfo[] roleInfo, String mtmEntityName, EJBSchema ejbSchema, SQL92Schema sqlSchema, int id) {
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
            EJB mtmEJB = new EJB(mtmEntityName, mtmEntityName, null, null);
            relationship = new Relationship(mtmEJB, roleInfo[0].ejbJDef, roleInfo[1].ejbJDef);
            Table mtmTable = new Table(mtmEntityName);
            joinTable = new JoinTable(mtmTable, roleInfo[0].tableJDef, roleInfo[1].tableJDef);
            ejbSchema.addEJB(mtmEJB);
            sqlSchema.addTable(mtmTable);
        }

        boolean isVirtual = null == roleInfo[0].cmrFieldName;
        String endName = isVirtual ? "$VirtualEnd" + id : roleInfo[0].cmrFieldName;
        roleInfo[0].ejb.addCMRField(new CMRField(endName, roleInfo[1].ejb, roleInfo[1].isOne, relationship, isVirtual));
        roleInfo[0].table.addEndTable(new EndTable(endName, roleInfo[1].table, roleInfo[1].isOne, joinTable, isVirtual));

        isVirtual = null == roleInfo[1].cmrFieldName;
        endName = isVirtual ? "$VirtualEnd" + id : roleInfo[1].cmrFieldName;
        roleInfo[1].ejb.addCMRField(new CMRField(endName, roleInfo[0].ejb, roleInfo[0].isOne, relationship, isVirtual));
        roleInfo[1].table.addEndTable(new EndTable(endName, roleInfo[0].table, roleInfo[0].isOne, joinTable, isVirtual));
    }

    private void populateGlobalSchema(Collection entities, GlobalSchema globalSchema, EJBSchema ejbSchema, SQL92Schema sqlSchema) throws QueryException {
        EJBQueryBuilder queryBuilder = new EJBQueryBuilder(ejbSchema, new IdentityDefinerBuilder(globalSchema));
        CommandTransform mapper = new SchemaMapper(sqlSchema);

        Set mtmEntities = new HashSet();

        for (Iterator iter = entities.iterator(); iter.hasNext();) {
            EJB ejb = (EJB) iter.next();

            String name = ejb.getName();

            List attributes = ejb.getAttributes();
            List associationEnds = ejb.getAssociationEnds();
            CacheSlot[] slots = new CacheSlot[attributes.size() + associationEnds.size()];
            for (int i = 0; i < attributes.size(); i++) {
                Attribute attr = (Attribute) attributes.get(i);
                slots[i] = new CacheSlot(attr.getName(), attr.getType(), getDefault(attr.getType()));
            }

            for (int i = 0; i < associationEnds.size(); i++) {
                AssociationEnd end = (AssociationEnd) associationEnds.get(i);
                Association association = end.getAssociation();
                if (association.isManyToMany()) {
                    mtmEntities.add(association);
                }
                EJB targetEJB = (EJB) end.getEntity();
                Class type = targetEJB.getProxyFactory().getLocalInterfaceClass();
                Object defaultValue = null;
                if (end.isMulti()) {
                    defaultValue = new HashSet();
                }
                slots[i + attributes.size()] = new CacheSlot(end.getName(), type, defaultValue);
            }

            UpdateCommand createCommand = mapper.transform(queryBuilder.buildCreate(name));
            UpdateCommand storeCommand = mapper.transform(queryBuilder.buildStore(name));
            UpdateCommand removeCommand = mapper.transform(queryBuilder.buildRemove(name));

            globalSchema.addCacheTable(new CacheTable(name, slots, createCommand, storeCommand, removeCommand));
        }

        for (Iterator iter = mtmEntities.iterator(); iter.hasNext();) {
            Association association = (Association) iter.next();
            Entity mtmEntity = association.getManyToManyEntity();
            AssociationEnd ends[] = association.getAssociationEnds();
            CacheSlot[] mtmSlots = new CacheSlot[]{
                new CacheSlot(ends[0].getName(), null, null),
                new CacheSlot(ends[1].getName(), null, null)};
            UpdateCommand mtmCreate = mapper.transform(queryBuilder.buildMTMCreate(association));
            UpdateCommand mtmRemove = mapper.transform(queryBuilder.buildMTMRemove(association));
            globalSchema.addCacheTable(new CacheTable(mtmEntity.getName(), mtmSlots, mtmCreate, null, mtmRemove));
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


    public GBeanMBean createBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, EJBSchema ejbSchema, Schema sqlSchema, GlobalSchema globalSchema, String connectionFactoryName, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl, TransactionManagerDelegate tmDelegate) throws DeploymentException {
        String ejbName = getString(entityBean.getEjbName());
        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
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

        Permissions toBeChecked = new Permissions();
        SecurityBuilder securityBuilder = getModuleBuilder().getSecurityBuilder();
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
        securityBuilder.fillContainerBuilderSecurity(builder,
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
//        builder.setConnectionFactoryName(connectionFactoryName);
        builder.setTransactionManagerDelegate(tmDelegate);

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
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName [" + ejbName + "]", e);
        }
    }
    
    private static final Map DEFAULTS = new HashMap();

    static {
        DEFAULTS.put(Boolean.TYPE, Boolean.FALSE);
        DEFAULTS.put(Byte.TYPE, new Byte((byte) 0));
        DEFAULTS.put(Short.TYPE, new Short((short) 0));
        DEFAULTS.put(Integer.TYPE, new Integer(0));
        DEFAULTS.put(Long.TYPE, new Long(0L));
        DEFAULTS.put(Float.TYPE, new Float(0.0f));
        DEFAULTS.put(Double.TYPE, new Double(0.0d));
        DEFAULTS.put(Character.TYPE, new Character(Character.MIN_VALUE));
    }

    private static Object getDefault(Class type) {
        // assumes get returns null and that is valid ...
        return DEFAULTS.get(type);
    }
    
    private static class RoleInfo {
        private final String entityName;
        private final String cmrFieldName;
        private EJB ejb;
        private Table table;
        private boolean isOne;
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