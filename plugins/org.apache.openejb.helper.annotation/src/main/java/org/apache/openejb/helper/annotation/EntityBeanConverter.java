/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.helper.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedQueries;
import javax.persistence.Table;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.CascadeType;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.FetchType;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.GenerationType;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.IdClass;
import org.apache.openejb.jee.jpa.ManyToOne;
import org.apache.openejb.jee.jpa.NamedNativeQuery;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.QueryHint;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.SequenceGenerator;
import org.apache.openejb.jee.jpa.TableGenerator;
import org.apache.openejb.jee.jpa.UniqueConstraint;

public class EntityBeanConverter implements Converter {

	private IJDTFacade facade;

	public EntityBeanConverter(IJDTFacade annotationHelper) {
		super();
		this.facade = annotationHelper;
	}

	@SuppressWarnings("unchecked")
	private void addOneToManyRelationshipAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {

		List<OneToMany> relationships = entity.getAttributes().getOneToMany();
		for (OneToMany relationship : relationships) {

			Map oneToManyProperties = getPropertiesForRelationship(relationship);

			String name = relationship.getName();
			String nameGetter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);

			facade.addMethodAnnotation(entityBean.getEjbClass(), nameGetter, new String[] {}, javax.persistence.OneToMany.class, oneToManyProperties);
		}
	}

	@SuppressWarnings("unchecked")
	private void addNamedQueriesAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {

		List<NamedQuery> namedQueries = entity.getNamedQuery();
		List namedQueriesValues = new ArrayList();

		for (NamedQuery namedQuery : namedQueries) {
			String name = namedQuery.getName();
			String query = namedQuery.getQuery();

			List<QueryHint> hints = namedQuery.getHint();

			List hintProperties = new ArrayList();

			for (QueryHint hint : hints) {
				String hintName = hint.getName();
				String hintValue = hint.getValue();

				Map hintProperty = new HashMap();
				hintProperty.put("name", hintName);
				hintProperty.put("value", hintValue);
				hintProperties.add(hintProperty);
			}

			Map namedQueryProperties = new HashMap();
			namedQueryProperties.put("name", name);
			namedQueryProperties.put("query", query);
			namedQueryProperties.put("hints", hintProperties.toArray(new Object[0]));

			namedQueriesValues.add(namedQueryProperties);
		}

		Map namedQueriesProperties = new HashMap();
		namedQueriesProperties.put("value", namedQueriesValues.toArray(new Object[0]));

		facade.addClassAnnotation(entityBean.getEjbClass(), NamedQueries.class, namedQueriesProperties);
	}

	private void addBasicAnnotations(EntityBean entityBean, List<Basic> basicAttributes) {
		for (Basic basic : basicAttributes) {
			String fieldName = basic.getName();
			Column column = basic.getColumn();

			addBasicAnnotation(entityBean, fieldName, basic, column);
		}
	}

	private void addBasicAnnotation(EntityBean entityBean, String fieldName, Basic basic, Column column) {
		addColumnAnnotation(entityBean, fieldName, column);
		Boolean optional = basic.isOptional();
		FetchType fetchType = basic.getFetch();

		Map<String, Object> basicProps = new HashMap<String, Object>();
		if (optional != null)
			basicProps.put("optional", optional.booleanValue());
		if (fetchType != null)
			basicProps.put("fetch", fetchType.value());

		// annotationHelper.addFieldAnnotation(entityBean.getEjbClass(),
		// fieldName, javax.persistence.Basic.class, basicProps);

		String methodName = convertFieldNameToGetterName(fieldName);
		facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, new String[0], javax.persistence.Basic.class, basicProps);
	}

	private String convertFieldNameToGetterName(String fieldName) {
		String methodName = "get" + capitaliseFirstLetter(fieldName);
		return methodName;
	}

	private String capitaliseFirstLetter(String fieldName) {
		if (fieldName == null) {
			return null;
		}

		if (fieldName.length() == 0) {
			return fieldName;
		}

		String firstLetter = fieldName.substring(0, 1).toUpperCase();
		String restOfWord = "";

		if (fieldName.length() > 1) {
			restOfWord = fieldName.substring(1);
		}

		return firstLetter + restOfWord;
	}

	private void addColumnAnnotation(EntityBean entityBean, String fieldName, Column column) {
		Map<String, Object> columnProps = new HashMap<String, Object>();
		if (column.getName() != null)
			columnProps.put("name", column.getName());
		if (column.isUnique() != null)
			columnProps.put("unique", column.isUnique().booleanValue());
		if (column.isNullable() != null)
			columnProps.put("nullable", column.isNullable().booleanValue());
		if (column.isInsertable() != null)
			columnProps.put("insertable", column.isInsertable().booleanValue());
		if (column.isUpdatable() != null)
			columnProps.put("updatable", column.isUpdatable().booleanValue());
		if (column.getColumnDefinition() != null)
			columnProps.put("columnDefinition", column.getColumnDefinition());
		if (column.getTable() != null)
			columnProps.put("table", column.getTable());
		if (column.getLength() != null)
			columnProps.put("length", column.getLength().intValue());
		if (column.getPrecision() != null)
			columnProps.put("precision", column.getPrecision().intValue());
		if (column.getScale() != null)
			columnProps.put("scale", column.getScale().intValue());

		// annotationHelper.addFieldAnnotation(entityBean.getEjbClass(),
		// fieldName, javax.persistence.Column.class, columnProps);

		String methodName = convertFieldNameToGetterName(fieldName);
		facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, new String[0], javax.persistence.Column.class, columnProps);

	}

	private void addTableAnnotation(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		String tableName = entity.getTable().getName();
		String schemaName = entity.getTable().getSchema();
		String catalogName = entity.getTable().getCatalog();

		Map<String, Object> tableProperties = new HashMap<String, Object>();
		if (tableName != null && tableName.length() > 0) {
			tableProperties.put("name", tableName);
		}

		if (schemaName != null && schemaName.length() > 0) {
			tableProperties.put("schema", schemaName);
		}

		if (catalogName != null && catalogName.length() > 0) {
			tableProperties.put("catalog", catalogName);
		}

		facade.addClassAnnotation(entityBean.getEjbClass(), Table.class, tableProperties);
	}

	private org.apache.openejb.jee.jpa.Entity getEntity(EntityMappings entityMappings, String ejbName) {
		List<org.apache.openejb.jee.jpa.Entity> entities = entityMappings.getEntity();

		for (org.apache.openejb.jee.jpa.Entity entity : entities) {
			if (entity.getName().equals(ejbName)) {
				return entity;
			}
		}

		return null;
	}

	private List<EntityBean> getEntityBeans(AppModule appModule) {
		List<EntityBean> result = new ArrayList<EntityBean>();

		List<EjbModule> ejbModules = appModule.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			EnterpriseBean[] enterpriseBeans = ejbModule.getEjbJar().getEnterpriseBeans();

			for (EnterpriseBean enterpriseBean : enterpriseBeans) {
				if (enterpriseBean instanceof EntityBean) {
					result.add((EntityBean) enterpriseBean);
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public void convert(AppModule appModule) {
		EntityMappings entityMappings = appModule.getCmpMappings();
		List<EntityBean> entityBeans = getEntityBeans(appModule);

		for (EntityBean entityBean : entityBeans) {

			checkEjbClassName(entityBean);

			facade.addClassAnnotation(entityBean.getEjbClass(), Entity.class, null);
			org.apache.openejb.jee.jpa.Entity entity = getEntity(entityMappings, entityBean.getEjbName());

			if (entity != null) {
				addTableAnnotation(entityBean, entity);

				Attributes attributes = entity.getAttributes();
				
				addIdAnnotations(entityBean, attributes.getId());
				addIdClassAnnotation(entityBean, entity);
				addBasicAnnotations(entityBean, attributes.getBasic());
				addNamedQueriesAnnotations(entityBean, entity);
				addNamedNativeQueriesAnnotations(entityBean, entity);
				addOneToManyRelationshipAnnotations(entityBean, entity);
				addManyToOneRelationshipAnnotations(entityBean, entity);
				addOneToOneRelationshipAnnotations(entityBean, entity);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addOneToOneRelationshipAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		List<OneToOne> relationships = entity.getAttributes().getOneToOne();
		for (OneToOne relationship : relationships) {

			String name = relationship.getName();
			String nameGetter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			
			Map<String, Object> oneToOneProperties = getPropertiesForRelationship(relationship);

			facade.addMethodAnnotation(entityBean.getEjbClass(), nameGetter, new String[] {}, javax.persistence.OneToOne.class, oneToOneProperties);
		}
	}

	@SuppressWarnings("unchecked")
	private void addManyToOneRelationshipAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		List<ManyToOne> relationships = entity.getAttributes().getManyToOne();
		for (ManyToOne relationship : relationships) {

			String name = relationship.getName();
			String nameGetter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);

			Map manyToOneProperties = getPropertiesForRelationship(relationship);
			
			Boolean optional = relationship.isOptional();
			if (optional != null) {
				manyToOneProperties.put("optional", optional.booleanValue());
			}
			
			
			facade.addMethodAnnotation(entityBean.getEjbClass(), nameGetter, new String[] {}, javax.persistence.OneToOne.class, manyToOneProperties);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> getPropertiesForRelationship(RelationField relationship) {
		String targetEntity = relationship.getTargetEntity();
		FetchType fetch = relationship.getFetch();
		CascadeType cascade = relationship.getCascade();
		
		
		Map<String, Object> manyToOneProperties = new HashMap<String, Object>();
		if (fetch != null) {
			manyToOneProperties.put("fetch", fetch.value());
		}

		if (cascade != null) {
			List cascadeList = new ArrayList();
			if (cascade.isCascadeAll()) {
				cascadeList.add(javax.persistence.CascadeType.ALL);
			}

			if (cascade.isCascadeMerge()) {
				cascadeList.add(javax.persistence.CascadeType.MERGE);
			}

			if (cascade.isCascadePersist()) {
				cascadeList.add(javax.persistence.CascadeType.PERSIST);
			}

			if (cascade.isCascadeRefresh()) {
				cascadeList.add(javax.persistence.CascadeType.REFRESH);
			}

			if (cascade.isCascadeRemove()) {
				cascadeList.add(javax.persistence.CascadeType.REMOVE);
			}

			manyToOneProperties.put("cascade", cascadeList.toArray(new Object[0]));
		}

		if (targetEntity != null && targetEntity.length() > 0) {
			manyToOneProperties.put("targetEntity", targetEntity);
		}
		return manyToOneProperties;
	}

	@SuppressWarnings("unchecked")
	private void addIdClassAnnotation(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		IdClass idClass = entity.getIdClass();
		
		if (idClass == null) {
			return;
		}
		
		String cls = idClass.getClazz();
		
		Map props = new HashMap();
		props.put("value", cls);
		
		facade.addClassAnnotation(entityBean.getEjbClass(), javax.persistence.IdClass.class, props);
	}

	@SuppressWarnings("unchecked")
	private void addNamedNativeQueriesAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		List<NamedNativeQuery> namedQueries = entity.getNamedNativeQuery();
		List namedQueriesValues = new ArrayList();

		for (NamedNativeQuery namedQuery : namedQueries) {
			String name = namedQuery.getName();
			String query = namedQuery.getQuery();

			List<QueryHint> hints = namedQuery.getHint();

			List hintProperties = new ArrayList();

			for (QueryHint hint : hints) {
				String hintName = hint.getName();
				String hintValue = hint.getValue();

				Map hintProperty = new HashMap();
				hintProperty.put("name", hintName);
				hintProperty.put("value", hintValue);
				hintProperties.add(hintProperty);
			}

			Map namedQueryProperties = new HashMap();
			namedQueryProperties.put("name", name);
			namedQueryProperties.put("query", query);
			namedQueryProperties.put("hints", hintProperties.toArray(new Object[0]));

			namedQueriesValues.add(namedQueryProperties);
		}

		if (namedQueriesValues.size() > 0) {
			Map namedQueriesProperties = new HashMap();
			namedQueriesProperties.put("value", namedQueriesValues.toArray(new Object[0]));
	
			facade.addClassAnnotation(entityBean.getEjbClass(), NamedNativeQueries.class, namedQueriesProperties);
		}
	}

	private void addIdAnnotations(EntityBean entityBean, List<Id> ids) {
		for (Id id : ids) {
			addIdAnnotation(entityBean, id);
		
			String fieldName = id.getName();
			Column column = id.getColumn();
			
			addColumnAnnotation(entityBean, fieldName, column);
		}
	}

	@SuppressWarnings("unchecked")
	private void addIdAnnotation(EntityBean entityBean, Id id) {
		String fieldName = id.getName();
		
		String methodName = convertFieldNameToGetterName(fieldName);
		String[] emptySignature = new String[0];
		facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature , javax.persistence.Id.class, null);
		
		
		GeneratedValue generatedValue = id.getGeneratedValue();
		if (generatedValue != null) {
			Map<String, Object> generatedValueProps = new HashMap<String, Object>();
			generatedValueProps.put("generator", generatedValue.getGenerator());
			
			GenerationType strategy = generatedValue.getStrategy();
			switch (strategy) {
				case AUTO:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.AUTO);
					break;
				case IDENTITY:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.IDENTITY);
					break;
				case SEQUENCE:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.SEQUENCE);
					break;
				case TABLE:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.TABLE);
					break;
			}
			
			facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.GeneratedValue.class, generatedValueProps);
		}
		
		SequenceGenerator sequenceGenerator = id.getSequenceGenerator();
		if (sequenceGenerator != null) {
			Map<String, Object> sequenceGeneratorProps = new HashMap<String, Object>();
			sequenceGeneratorProps.put("name", sequenceGenerator.getName());
			sequenceGeneratorProps.put("sequenceName", sequenceGenerator.getSequenceName());
			sequenceGeneratorProps.put("initialValue", sequenceGenerator.getInitialValue().intValue());
			sequenceGeneratorProps.put("allocationSize", sequenceGenerator.getAllocationSize().intValue());
			
			facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.SequenceGenerator.class, sequenceGeneratorProps);
		}
		
		TableGenerator tableGenerator = id.getTableGenerator();
		if (tableGenerator != null) {
			Map<String, Object> tableGeneratorProps = new HashMap<String, Object>();
			tableGeneratorProps.put("name", tableGenerator.getName());
			tableGeneratorProps.put("table", tableGenerator.getTable());
			tableGeneratorProps.put("catalog", tableGenerator.getCatalog());
			tableGeneratorProps.put("schema", tableGenerator.getSchema());
			tableGeneratorProps.put("pkColumnName", tableGenerator.getPkColumnName());
			tableGeneratorProps.put("valueColumnName", tableGenerator.getValueColumnName());
			tableGeneratorProps.put("pkColumnValue", tableGenerator.getPkColumnValue());
			tableGeneratorProps.put("initialValue", tableGenerator.getInitialValue().intValue());
			tableGeneratorProps.put("allocationSize", tableGenerator.getAllocationSize().intValue());

			List uniqueConstraintPropsList = new ArrayList();
			
			List<UniqueConstraint> uniqueConstraints = tableGenerator.getUniqueConstraint();
			for (UniqueConstraint uniqueConstriant : uniqueConstraints) {
				Map<String, Object> uniqueConstraintProps = new HashMap<String, Object>();
				List<String> columns = uniqueConstriant.getColumnName();
				uniqueConstraintProps.put("columnNames", columns.toArray(new String[0]));
				uniqueConstraintPropsList.add(uniqueConstraintProps);
			}
			
			tableGeneratorProps.put("uniqueConstraints", uniqueConstraintPropsList.toArray(new Object[0]));
			facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.TableGenerator.class, tableGeneratorProps);
		}
	}

	private void checkEjbClassName(EntityBean entityBean) {
		String ejbClass = entityBean.getEjbClass();
		String ejbSuperClass = facade.getSuperClass(ejbClass);
		if (ejbSuperClass != null && ejbSuperClass.length() > 0) {
			if (facade.classImplements(ejbSuperClass, "javax.ejb.EntityBean")) {
				entityBean.setEjbClass(ejbSuperClass);
			}
		}
	}
}
