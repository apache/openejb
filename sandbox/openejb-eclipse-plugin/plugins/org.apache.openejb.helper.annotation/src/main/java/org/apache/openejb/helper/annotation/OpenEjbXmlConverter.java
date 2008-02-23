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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.MessageDriven;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.CmpJpaConversion;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.InitEjbDeployments;
import org.apache.openejb.config.OpenEjb2Conversion;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.MethodTransaction;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.CascadeType;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.FetchType;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.QueryHint;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.eclipse.core.resources.IProject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Scans an ejb-jar.xml file using a JAXB parser, and adds annotations
 * to source based on the XML.
 * 
 * Depends on an implementation of IJavaProjectAnnotationFacade
 *
 */
public class OpenEjbXmlConverter {

	public static final String CLS_TRANSACTION_ATTRIBUTE = "javax.ejb.TransactionAttribute";
	public static final String CLS_APPLICATION_EXCEPTION = "javax.ejb.ApplicationException";
	public static final String CLS_STATEFUL = "javax.ejb.Stateful";
	public static final String CLS_STATELESS = "javax.ejb.Stateless";
	public static final String CLS_MESSAGE_DRIVEN = "javax.ejb.MessageDriven";
	public static final String STATELESS_CLASS = CLS_STATELESS;
	protected IJavaProjectAnnotationFacade annotationHelper;
	

	/**
	 * Constucts a new converter
	 * @param annotationHelper Annotation Facade to use for adding annotations 
	 */
	public OpenEjbXmlConverter(IJavaProjectAnnotationFacade annotationHelper) {
		this.annotationHelper = annotationHelper;
	}

	/**
	 * Constructs a new converter - uses the default implementation of
	 * IJavaProjectAnnotationFacade - JavaProjectAnnotationFacade
	 * @param project An eclipse Java project
	 */
	public OpenEjbXmlConverter(IProject project) {
		this(new JavaProjectAnnotationFacade(project));
	}
	
	/**
	 * Parses the XML
	 * @param source An input source to the content of ejb-jar.xml
	 * @return Whether or not the parsing was successful
	 * @throws ConversionException 
	 */
	public boolean convert(InputSource source) throws ConversionException {
		return convert(source, null);
	}

	/**
	 * Parses the XML
	 * @param ejbJarSrc An input source to the content of ejb-jar.xml
	 * @param openEjbJarSrc An input source to the content of openejb-jar.xml (optional)
	 * @return Whether or not the parsing was successful
	 * @throws ConversionException 
	 */
	@SuppressWarnings("unchecked")
	public boolean convert(InputSource ejbJarSrc, InputSource openEjbJarSrc) throws ConversionException {
		try {	
			EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
	        EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());

			processEnterpriseBeans(ejbJar);
			processApplicationExceptions(ejbJar);
			
			if (openEjbJarSrc != null) {
		        InitEjbDeployments initEjbDeployments = new InitEjbDeployments();
		        initEjbDeployments.deploy(ejbModule, new HashMap<String,String>());
		        AppModule appModule = new AppModule(getClass().getClassLoader(), "TestModule");
		        appModule.getEjbModules().add(ejbModule);

		        JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, openEjbJarSrc.getByteStream());
		        OpenejbJarType openejbJarType = (OpenejbJarType) element.getValue();
		        ejbModule.getAltDDs().put("openejb-jar.xml", openejbJarType);

		        CmpJpaConversion cmpJpaConversion = new CmpJpaConversion();
		        cmpJpaConversion.deploy(appModule);

		        OpenEjb2Conversion openEjb2Conversion = new OpenEjb2Conversion();
		        openEjb2Conversion.deploy(appModule);

				processEntityBeans(appModule);
			}
			
			return true;
		} catch (JAXBException e) {
			throw new ConversionException("Unable to unmarshal XML", e);
		} catch (ParserConfigurationException e) {
			throw new ConversionException("Unable to unmarshal XML (parser configuration error)", e);
		} catch (SAXException e) {
			throw new ConversionException("Unable to unmarshal XML (SAX error - XML badly formed?)", e);
		} catch (OpenEJBException e) {
			throw new ConversionException("Unable to convert openejb-jar.xml to orm.xml");
		}
	}

	private void processEntityBeans(AppModule appModule) {
		EntityMappings entityMappings = appModule.getCmpMappings();
		List<EntityBean> entityBeans = getEntityBeans(appModule);
		
		for (EntityBean entityBean : entityBeans) {
			annotationHelper.addClassAnnotation(entityBean.getEjbClass(), Entity.class, null);
			org.apache.openejb.jee.jpa.Entity entity = getEntity(entityMappings, entityBean.getEjbName());
			
			if (entity != null) {
				addTableAnnotation(entityBean, entity);
			
				Attributes attributes = entity.getAttributes();
				addBasicAnnotations(entityBean, attributes.getBasic());
				addNamedQueriesAnnotations(entityBean, entity);
				addRelationshipAnnotations(entityBean, entity);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addRelationshipAnnotations(EntityBean entityBean,
			org.apache.openejb.jee.jpa.Entity entity) {

		List<OneToMany> relationships = entity.getAttributes().getOneToMany();
		for (OneToMany relationship : relationships) {
			
			
			String targetEntity = relationship.getTargetEntity();
			FetchType fetch = relationship.getFetch();
			CascadeType cascade = relationship.getCascade();
			String mappedBy = relationship.getMappedBy();
			String name = relationship.getName();
			
			String nameGetter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			
			Map oneToManyProperties = new HashMap();
			if (fetch != null) {
				oneToManyProperties.put("fetch", fetch.value());
			}
			
			if (mappedBy != null) {
				oneToManyProperties.put("mappedBy", mappedBy);
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
				
				oneToManyProperties.put("cascade", cascadeList.toArray(new Object[0]));
			}
			
			if (targetEntity != null && targetEntity.length() > 0) {
				oneToManyProperties.put("targetEntity", targetEntity);
			}
		
			annotationHelper.addMethodAnnotation(entityBean.getEjbClass(), nameGetter, new String[] {} , javax.persistence.OneToMany.class, oneToManyProperties);
		}
	}

	@SuppressWarnings("unchecked")
	private void addNamedQueriesAnnotations(EntityBean entityBean,
			org.apache.openejb.jee.jpa.Entity entity) {

		
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
		
		annotationHelper.addClassAnnotation(entityBean.getEjbClass(), NamedQueries.class, namedQueriesProperties);
	}

	private void addBasicAnnotations(EntityBean entityBean,	List<Basic> basicAttributes) {
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
		if (optional != null) basicProps.put("optional", optional.booleanValue());
		if (fetchType != null) basicProps.put("fetch", fetchType.value());
		
//		annotationHelper.addFieldAnnotation(entityBean.getEjbClass(), fieldName, javax.persistence.Basic.class, basicProps);

		String methodName = convertFieldNameToGetterName(fieldName);
		annotationHelper.addMethodAnnotation(entityBean.getEjbClass(), methodName, new String[0], javax.persistence.Basic.class, basicProps);
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
		if (column.getName() != null) columnProps.put("name", column.getName());
		if (column.isUnique() != null) columnProps.put("unique", column.isUnique().booleanValue());
		if (column.isNullable() != null) columnProps.put("nullable", column.isNullable().booleanValue());
		if (column.isInsertable() != null) columnProps.put("insertable", column.isInsertable().booleanValue());
		if (column.isUpdatable() != null) columnProps.put("updatable", column.isUpdatable().booleanValue());
		if (column.getColumnDefinition() != null) columnProps.put("columnDefinition", column.getColumnDefinition());
		if (column.getTable() != null) columnProps.put("table", column.getTable());
		if (column.getLength() != null) columnProps.put("length", column.getLength().intValue());
		if (column.getPrecision() != null) columnProps.put("precision", column.getPrecision().intValue());
		if (column.getScale() != null) columnProps.put("scale", column.getScale().intValue());
		
//		annotationHelper.addFieldAnnotation(entityBean.getEjbClass(), fieldName, javax.persistence.Column.class, columnProps);

		String methodName = convertFieldNameToGetterName(fieldName);
		annotationHelper.addMethodAnnotation(entityBean.getEjbClass(), methodName, new String[0], javax.persistence.Column.class, columnProps);

	}

	private void addTableAnnotation(EntityBean entityBean,
			org.apache.openejb.jee.jpa.Entity entity) {
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
		
		annotationHelper.addClassAnnotation(entityBean.getEjbClass(), Table.class, tableProperties);
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

	private void processApplicationExceptions(EjbJar ejbJar) {
		List<ApplicationException> exceptionList = ejbJar.getAssemblyDescriptor().getApplicationException();
		Iterator<ApplicationException> iterator = exceptionList.iterator();
		
		while (iterator.hasNext()) {
			ApplicationException element = (ApplicationException) iterator.next();
			String exceptionClass = element.getExceptionClass();
			
			annotationHelper.addClassAnnotation(exceptionClass, javax.ejb.ApplicationException.class, null);
		}
	}

	private void processEnterpriseBeans(EjbJar ejbJar) {
		EnterpriseBean[] enterpriseBeans = ejbJar.getEnterpriseBeans();
		Iterator<EnterpriseBean> iterator = Arrays.asList(enterpriseBeans).iterator();
		while (iterator.hasNext()) {
			EnterpriseBean bean = (EnterpriseBean) iterator.next();
			if (bean instanceof SessionBean) {
				SessionBean sessionBean = (SessionBean) bean;
				processSessionBean(sessionBean);
			} else if (bean instanceof MessageDrivenBean) {
				MessageDrivenBean messageDriven = (MessageDrivenBean) bean;
				processMessageDrivenBean(messageDriven);
			}
			
			processTransactionManagement(bean, ejbJar.getAssemblyDescriptor());
			processBeanSecurityIdentity(bean);
			processDeclaredRoles(bean);
			processMethodPermissions(ejbJar);
		}
		
		
		
	}

	/**
	 * Generates transaction management annotations for an Enterprise Bean
	 * @param bean The enterprise bean to generate annotations for
	 * @param descriptor The assembly descriptor
	 */
	public void processTransactionManagement(EnterpriseBean bean, AssemblyDescriptor descriptor) {
		TransactionType transactionType = bean.getTransactionType();
		
		if (transactionType != null && (! TransactionType.CONTAINER.equals(transactionType))) {
			Map<String,Object> props = new HashMap<String, Object>();
			props.put("value", TransactionManagementType.BEAN);
			
			annotationHelper.addClassAnnotation(bean.getEjbClass(), TransactionManagement.class, props);
		}
		
		Map<String, List<MethodTransaction>> methodTransactions = descriptor.getMethodTransactions(bean.getEjbName());
		if (methodTransactions.containsKey("*")) {
			List<MethodTransaction> defaultTransactions = methodTransactions.get("*");
			MethodTransaction defaultTransaction = defaultTransactions.get(0);
			
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("value", TransactionAttributeType.valueOf(defaultTransaction.getAttribute().name()));
			annotationHelper.addClassAnnotation(bean.getEjbClass(), TransactionAttribute.class, props);
		}
		
		Iterator<String> iterator = methodTransactions.keySet().iterator();
		while (iterator.hasNext()) {
			String methodName = (String) iterator.next();
			if ("*".equals(methodName)) {
				continue;
			}
			
			List<MethodTransaction> transactions = methodTransactions.get(methodName);
			MethodTransaction methodTransaction = transactions.get(0);
			
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("value", TransactionAttributeType.valueOf(methodTransaction.getAttribute().name()));
			
			MethodParams methodParams = methodTransaction.getMethod().getMethodParams();
			String[] params = methodParams.getMethodParam().toArray(new String[0]);
			annotationHelper.addMethodAnnotation(bean.getEjbClass(), methodName, params, TransactionAttribute.class, props);
		}
	}

	public void processMessageDrivenBean(MessageDrivenBean bean) {
		Map<String, Object> props = new HashMap<String, Object>();
		
		ActivationConfig activationConfig = bean.getActivationConfig();
		if (activationConfig != null) {
			List<Map<String, Object>> activationConfigPropertiesList = new ArrayList<Map<String,Object>>();
			
			List<ActivationConfigProperty> activationConfigProperties = activationConfig.getActivationConfigProperty();

			for (ActivationConfigProperty activationConfigProperty : activationConfigProperties) {
				HashMap<String, Object> configProps = new HashMap<String, Object>();
				configProps.put("propertyName", activationConfigProperty.getActivationConfigPropertyName());
				configProps.put("propertyValue", activationConfigProperty.getActivationConfigPropertyValue());
				
				activationConfigPropertiesList.add(configProps);
			}
			
			if (bean.getMessageDestinationLink() != null && bean.getMessageDestinationLink().length() > 0) {
				if (! hasConfigProperty(activationConfigPropertiesList, "destination")) {
					HashMap<String, Object> configProps = new HashMap<String, Object>();
					configProps.put("propertyName", "destination");
					configProps.put("propertyValue", bean.getMessageDestinationLink());
					
					activationConfigPropertiesList.add(configProps);
				}
			}

			props.put("activationConfig", activationConfigPropertiesList.toArray(new HashMap[0]));
		}
		
		props.put("name", bean.getEjbName());
		annotationHelper.addClassAnnotation(bean.getEjbClass(), MessageDriven.class, props);
	}


	private boolean hasConfigProperty(List<Map<String, Object>> activationConfigPropertiesList, String propertyName) {
		for (Map<String,Object> configProperty : activationConfigPropertiesList) {
			if (configProperty.get("propertyName") != null && configProperty.get("propertyName").toString().equals(propertyName)) {
				return true;
			}
		}
		
		return false;
	}

	public void processSessionBean(SessionBean sessionBean) {
		String ejbClass = sessionBean.getEjbClass();
		if (sessionBean instanceof StatelessBean || sessionBean.getSessionType() == SessionType.STATELESS) {
			annotationHelper.addClassAnnotation(ejbClass, Stateless.class, null);
		} else if (sessionBean instanceof StatefulBean || sessionBean.getSessionType() == SessionType.STATEFUL) {
			annotationHelper.addClassAnnotation(ejbClass, Stateful.class, null);
		} 
		
		if (sessionBean instanceof RemoteBean) {
			if (sessionBean.getRemote() != null && sessionBean.getRemote().length() > 0) {
				annotationHelper.addClassAnnotation(sessionBean.getRemote(), Remote.class, null);
			}
			
			if (sessionBean.getHome() != null && sessionBean.getHome().length() > 0) {
				Map<String, Object> props = new HashMap<String, Object>();
				props.put("value", sessionBean.getHome());
				annotationHelper.addClassAnnotation(ejbClass, RemoteHome.class, props);
			}
		}
	}

	public void processMethodPermissions(EjbJar ejbJar) {
		AssemblyDescriptor descriptor = ejbJar.getAssemblyDescriptor();		
		
		List<MethodPermission> methodPermissions = descriptor.getMethodPermission();
		Iterator<MethodPermission> iterator = methodPermissions.iterator();
		
		while (iterator.hasNext()) {
			MethodPermission methodPermission = (MethodPermission) iterator.next();
			List<String> roles = methodPermission.getRoleName();
			
			if (roles == null || roles.size() == 0) {
				continue;
			}
			
			String[] roleList = roles.toArray(new String[0]);
			Map<String, Object> roleProps = new HashMap<String, Object>();
			roleProps.put("value", roleList);

			
			List<Method> methods = methodPermission.getMethod();
			Iterator<Method> methodIter = methods.iterator();
			
			while (methodIter.hasNext()) {
				Method method = (Method) methodIter.next();
				EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(method.getEjbName());

				MethodParams methodParams = method.getMethodParams();
				String[] params = methodParams.getMethodParam().toArray(new String[0]);
				
				if ((! "*".equals(method.getMethodName())) &&  descriptor.getExcludeList().getMethod().contains(method)) {
					annotationHelper.addMethodAnnotation(enterpriseBean.getEjbClass(), method.getMethodName(), params, DenyAll.class, null);
					continue;
				}
				
				if (methodPermission.getUnchecked()) {
					if ("*".equals(method.getMethodName())) {
						annotationHelper.addClassAnnotation(enterpriseBean.getEjbClass(), PermitAll.class, null);
					} else {
						annotationHelper.addMethodAnnotation(enterpriseBean.getEjbClass(), method.getMethodName(), params, PermitAll.class, null);
					}
				} else {
					if ("*".equals(method.getMethodName())) {
						annotationHelper.addClassAnnotation(enterpriseBean.getEjbClass(), RolesAllowed.class, roleProps);
					} else {
						annotationHelper.addMethodAnnotation(enterpriseBean.getEjbClass(), method.getMethodName(), params, RolesAllowed.class, roleProps);
					}
				}
			}
		}
	}

	public void processBeanSecurityIdentity(EnterpriseBean bean) {
		if (bean.getSecurityIdentity() == null) {
			return;
		}
		
		Map<String, Object> runAsProps = new HashMap<String, Object>();
		runAsProps.put("value", bean.getSecurityIdentity().getRunAs());
		
		annotationHelper.addClassAnnotation(bean.getEjbClass(), RunAs.class, runAsProps);
	}

	public void processDeclaredRoles(EnterpriseBean bean) {
		if (! (bean instanceof RemoteBean)) {
			return;
		}
		
		RemoteBean remoteBean = (RemoteBean) bean;
		List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
		
		if (securityRoleRefs == null || securityRoleRefs.size() == 0) {
			return;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		List<String> roleList = new ArrayList<String>();
		
		for (SecurityRoleRef securityRoleRef : securityRoleRefs) {
			roleList.add(securityRoleRef.getRoleName());
		}
		
		props.put("value", roleList.toArray(new String[0]));
		annotationHelper.addClassAnnotation(bean.getEjbClass(), DeclareRoles.class, props);
	}

	public void processInterceptors(EjbJar ejbJar) {
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		for (InterceptorBinding interceptorBinding : interceptorBindings) {
			EnterpriseBean bean = ejbJar.getEnterpriseBean(interceptorBinding.getEjbName());
			
			List<String> interceptorClasses = interceptorBinding.getInterceptorClass();
						
			String[] classes = interceptorClasses.toArray(new String[0]);
			
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("value", classes);
			
			if (interceptorBinding.getMethod() == null) {
				if (interceptorBinding.getExcludeDefaultInterceptors()) {
					annotationHelper.addClassAnnotation(bean.getEjbClass(), ExcludeDefaultInterceptors.class, properties);
				}

				if (interceptorBinding.getExcludeClassInterceptors()) {
					annotationHelper.addClassAnnotation(bean.getEjbClass(), ExcludeClassInterceptors.class, properties);
				}
				
				annotationHelper.addClassAnnotation(bean.getEjbClass(), Interceptors.class, properties);
			} else {
				NamedMethod method = interceptorBinding.getMethod();
				String[] signature = method.getMethodParams().getMethodParam().toArray(new String[0]);
				
				if (interceptorBinding.getExcludeDefaultInterceptors()) {
					annotationHelper.addMethodAnnotation(bean.getEjbClass(), method.getMethodName(), signature, ExcludeDefaultInterceptors.class, properties);
				}

				if (interceptorBinding.getExcludeClassInterceptors()) {
					annotationHelper.addMethodAnnotation(bean.getEjbClass(), method.getMethodName(), signature, ExcludeClassInterceptors.class, properties);
				}

				annotationHelper.addMethodAnnotation(bean.getEjbClass(), method.getMethodName(), signature, Interceptors.class, properties);
			}
		}
	}
}
