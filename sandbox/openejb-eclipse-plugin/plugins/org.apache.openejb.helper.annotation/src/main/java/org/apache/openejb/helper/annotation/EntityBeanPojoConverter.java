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

import java.util.List;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.ManyToMany;
import org.apache.openejb.jee.jpa.ManyToOne;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.OneToOne;

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
		}
	}

	private Entity getMapping(EntityMappings entityMappings, String ejbName) {
			List<org.apache.openejb.jee.jpa.Entity> entities = entityMappings.getEntity();

			for (org.apache.openejb.jee.jpa.Entity entity : entities) {
				if (entity.getName().equals(ejbName)) {
					return entity;
				}
			}

			return null;
	}

	private void convertBeanToPojo(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		facade.removeAbstractModifierFromClass(entityBean.getEjbClass());

		List<Basic> basicList = entity.getAttributes().getBasic();
		for (Basic basic : basicList) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), basic.getName());
		}
		
		List<Id> ids = entity.getAttributes().getId();
		for (Id id : ids) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), id.getName());
		}
		
		List<ManyToMany> manyToMany = entity.getAttributes().getManyToMany();
		for (ManyToMany relationship : manyToMany) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), relationship.getName());
		}

		List<OneToMany> oneToMany = entity.getAttributes().getOneToMany();
		for (OneToMany relationship : oneToMany) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), relationship.getName());
		}
		
		List<ManyToOne> manyToOne = entity.getAttributes().getManyToOne();
		for (ManyToOne relationship : manyToOne) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), relationship.getName());
		}
		
		List<OneToOne> oneToOne = entity.getAttributes().getOneToOne();
		for (OneToOne relationship : oneToOne) {
			convertGetterAndSetterToNonAbstract(entityBean.getEjbClass(), relationship.getName());
		}
	}
	
	private void convertGetterAndSetterToNonAbstract(String cls, String fieldName) {
		String[] emptySignature = new String[0];
		String getterMethodName = convertFieldNameToGetterName(fieldName);
		String setterMethodName = convertFieldNameToSetterName(fieldName);

		String fieldType = facade.getMethodReturnType(cls, getterMethodName, emptySignature);
		facade.addField(cls, fieldName, fieldType);
		
		String getterMethodBody = "return " + fieldName + ";";
		String setterMethodBody = "this." + fieldName + " = ${0};";
		
		facade.removeAbstractModifierFromMethod(cls, getterMethodName, emptySignature, getterMethodBody);
		facade.removeAbstractModifierFromMethod(cls, setterMethodName, new String[] { fieldType }, setterMethodBody);
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

	private String convertFieldNameToSetterName(String fieldName) {
		String methodName = "set" + capitaliseFirstLetter(fieldName);
		return methodName;
	}
}
