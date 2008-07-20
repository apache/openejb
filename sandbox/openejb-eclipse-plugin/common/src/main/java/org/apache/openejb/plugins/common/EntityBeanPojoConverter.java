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
package org.apache.openejb.plugins.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Relationships;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Id;

public class EntityBeanPojoConverter implements Converter {

	private IJDTFacade facade;

	public EntityBeanPojoConverter(IJDTFacade facade) {
		super();
		this.facade = facade;
	}

	public void convert(AppModule module) {
		EntityMappings cmpMappings = module.getCmpMappings();

		List<EjbModule> ejbModules = module.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			EjbJar ejbJar = ejbModule.getEjbJar();
			EnterpriseBean[] enterpriseBeans = ejbJar.getEnterpriseBeans();
			Relationships relationships = ejbJar.getRelationships();

			for (EnterpriseBean enterpriseBean : enterpriseBeans) {
				if (!(enterpriseBean instanceof EntityBean)) {
					continue;
				}

				EntityBean entityBean = (EntityBean) enterpriseBean;
				Entity entity = getMapping(cmpMappings, entityBean.getEjbName());

				if (entity != null) {
					convertBeanToPojo(entityBean, entity);
				}
			}
			
			List<EjbRelation> ejbRelations = relationships.getEjbRelation();
			for (Iterator<EjbRelation> relationIterator = ejbRelations.iterator(); relationIterator.hasNext();) {
				EjbRelation ejbRelation = relationIterator.next();
				List<EjbRelationshipRole> ejbRelationshipRoles = ejbRelation.getEjbRelationshipRole();
				for (Iterator<EjbRelationshipRole> roleIteratore = ejbRelationshipRoles.iterator(); roleIteratore.hasNext();) {
					EjbRelationshipRole ejbRelationshipRole = roleIteratore.next();
					
					String ejbName = ejbRelationshipRole.getRelationshipRoleSource().getEjbName();
					EntityBean bean = getEntityBean(enterpriseBeans, ejbName);
					if (bean == null) {
						continue;
					}
					
					if (ejbRelationshipRole.getCmrField() != null)
						convertGetterAndSetterToNonAbstract(bean.getEjbClass(), ejbRelationshipRole.getCmrField().getCmrFieldName());
				}
			}
		}
	}

	private EntityBean getEntityBean(EnterpriseBean[] enterpriseBeans, String ejbName) {
		for (EnterpriseBean enterpriseBean : enterpriseBeans) {
			if (enterpriseBean instanceof EntityBean && ejbName.equals(enterpriseBean.getEjbName())) {
				return (EntityBean) enterpriseBean;
			}
		}
		
		return null;
	}

	private Entity getMapping(EntityMappings entityMappings, String ejbName) {
			Collection<Entity> entities = entityMappings.getEntity();

			for (Entity entity : entities) {
				if (entity.getEjbName().equals(ejbName)) {
					return entity;
				}
			}

			return null;
	}

	private void convertBeanToPojo(EntityBean entityBean, Entity entity) {
		facade.removeAbstractModifierFromClass(entityBean.getEjbClass());
        facade.removeInterface(entityBean.getEjbClass(), "javax.ejb.EntityBean");

        List<Basic> basicList = entity.getAttributes().getBasic();
		for (Basic basic : basicList) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), basic.getName());
		}

		List<Id> ids = entity.getAttributes().getId();
		for (Id id : ids) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), id.getName());
		}
	}

	private void convertGetterAndSetterToNonAbstract(String cls, String fieldName) {
		try {
			String[] emptySignature = new String[0];
			String getterMethodName = convertFieldNameToGetterName(fieldName);
			String setterMethodName = convertFieldNameToSetterName(fieldName);
			String fieldType = facade.getMethodReturnType(cls,
					getterMethodName, emptySignature);
			facade.addField(cls, fieldName, fieldType);
			String getterMethodBody = "return " + fieldName + ";"; //$NON-NLS-1$ //$NON-NLS-2$
			String setterMethodBody = "this." + fieldName + " = ${0};"; //$NON-NLS-1$ //$NON-NLS-2$
			facade.removeAbstractModifierFromMethod(cls, getterMethodName,
					emptySignature, getterMethodBody);
			facade.removeAbstractModifierFromMethod(cls, setterMethodName,
					new String[] { fieldType }, setterMethodBody);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.7"), cls, fieldName);
			facade.addWarning(warning);
		}
	}

	private String convertFieldNameToGetterName(String fieldName) {
		String methodName = "get" + capitaliseFirstLetter(fieldName); //$NON-NLS-1$
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
		String restOfWord = ""; //$NON-NLS-1$

		if (fieldName.length() > 1) {
			restOfWord = fieldName.substring(1);
		}

		return firstLetter + restOfWord;
	}

	private String convertFieldNameToSetterName(String fieldName) {
		String methodName = "set" + capitaliseFirstLetter(fieldName); //$NON-NLS-1$
		return methodName;
	}
}
