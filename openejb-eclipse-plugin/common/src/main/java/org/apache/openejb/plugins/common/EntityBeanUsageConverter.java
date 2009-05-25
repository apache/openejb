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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Query;

public class EntityBeanUsageConverter implements Converter {

	private IJDTFacade facade;
	
	public EntityBeanUsageConverter(IJDTFacade facade) {
		super();
		this.facade = facade;
	}

	public void convert(AppModule module) {
		List<EjbModule> ejbModules = module.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			convert(ejbModule);
		}
	}

	private void convert(EjbModule ejbModule) {
		EjbJar ejbJar = ejbModule.getEjbJar();
		EnterpriseBean[] enterpriseBeans = ejbJar.getEnterpriseBeans();
		
		for (EnterpriseBean enterpriseBean : enterpriseBeans) {
			if (dependsOnEntityBean(ejbJar, enterpriseBean)) {
				addEntityManagerFactoryField(enterpriseBean);
			}
			
			if (! (enterpriseBean instanceof EntityBean)) {
				continue;
			}
			
			EntityBean entityBean = (EntityBean) enterpriseBean;
			String beanClass = entityBean.getEjbClass();
			String home = entityBean.getHome();
			
			
			if (home == null || home.length() == 0) {
				home = entityBean.getLocalHome();
			}
			
			List<String[]> createSignatures = facade.getSignatures(home, "create");
			for (String[] createSignature : createSignatures) {
				facade.convertMethodToConstructor(beanClass, "ejbCreate", createSignature);
				facade.changeInvocationsToConstructor(home, "create", createSignature, beanClass);
			}
			
			List<Query> queries = entityBean.getQuery();
			for (Query query : queries) {
				String ejbQl = query.getEjbQl();
				String methodName = query.getQueryMethod().getMethodName();
				List<String> paramList = query.getQueryMethod().getMethodParams().getMethodParam();
				String[] signature = paramList.toArray(new String[paramList.size()]);
				
				String returnType = facade.getMethodReturnType(home, methodName, signature);
				StringBuffer code = new StringBuffer();
				code.append("javax.persistence.EntityManager entityManager = entityManagerFactory.createEntityManager();\r\n");
				code.append("javax.persistence.Query query = entityManager.createQuery(\"");
				code.append(ejbQl);
				code.append("\");\r\n");
				
				// add a cast for the return
//				code.append("(");
//				code.append(returnType);
//				code.append(") (");
				
				if (facade.isTypeCollection(returnType)) {
					code.append("query.getResultList()");
				} else {
					code.append("query.getSingleResult()");
				}
				
				code.append(";\r\n");
				facade.changeInvocationsTo(home, methodName, signature, code.toString());
			}
		}
	}
	
	private boolean dependsOnEntityBean(EjbJar ejbJar, EnterpriseBean enterpriseBean) {
		Collection<EjbRef> ejbRefs = enterpriseBean.getEjbRef();
		for (EjbReference ejbRef : ejbRefs) {
			String ejbLink = ejbRef.getEjbLink();
			
			EnterpriseBean refBean = ejbJar.getEnterpriseBean(ejbLink);
			if (refBean != null && refBean instanceof EntityBean) {
				return true;
			}
		}

		Collection<EjbLocalRef> ejbLocalRefs = enterpriseBean.getEjbLocalRef();
		for (EjbReference ejbRef : ejbLocalRefs) {
			String ejbLink = ejbRef.getEjbLink();
			
			EnterpriseBean refBean = ejbJar.getEnterpriseBean(ejbLink);
			if (refBean != null && refBean instanceof EntityBean) {
				return true;
			}
		}
		
		return false;
	}
	
	private void addEntityManagerFactoryField(EnterpriseBean enterpriseBean) {
		facade.addField(enterpriseBean.getEjbClass(), "emf", EntityManagerFactory.class.getName());
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("name", "OpenEJBPU");
		facade.addFieldAnnotation(enterpriseBean.getEjbClass(), "emf", PersistenceUnit.class, properties);
	}
}
