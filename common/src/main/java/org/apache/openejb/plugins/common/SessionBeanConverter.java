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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Map;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.MessageDriven;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransactionType;

public class SessionBeanConverter implements Converter {

	public static final String CLS_TRANSACTION_ATTRIBUTE = "javax.ejb.TransactionAttribute"; //$NON-NLS-1$
	public static final String CLS_APPLICATION_EXCEPTION = "javax.ejb.ApplicationException"; //$NON-NLS-1$
	public static final String CLS_STATEFUL = "javax.ejb.Stateful"; //$NON-NLS-1$
	public static final String CLS_STATELESS = "javax.ejb.Stateless"; //$NON-NLS-1$
	public static final String CLS_MESSAGE_DRIVEN = "javax.ejb.MessageDriven"; //$NON-NLS-1$
	public static final String STATELESS_CLASS = CLS_STATELESS;
	protected IJDTFacade annotationHelper;


	/**
	 * Constucts a new converter
	 * @param annotationHelper Annotation Facade to use for adding annotations
	 */
	public SessionBeanConverter(IJDTFacade annotationHelper) {
		this.annotationHelper = annotationHelper;
	}

	private void processApplicationExceptions(EjbJar ejbJar) {
		Collection<ApplicationException> exceptionList = ejbJar.getAssemblyDescriptor().getApplicationException();
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
		}

		processMethodPermissions(ejbJar);
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
			props.put("value", TransactionManagementType.BEAN); //$NON-NLS-1$

			annotationHelper.addClassAnnotation(bean.getEjbClass(), TransactionManagement.class, props);
		}

		
		//Map<String, List<MethodAttribute>> methodTransactions = descriptor.getMethodTransactionMap(bean.getEjbName());
		//if (methodTransactions.containsKey("*")) { //$NON-NLS-1$
		//	List<MethodAttribute> defaultTransactions = methodTransactions.get("*"); //$NON-NLS-1$
		//	MethodAttribute defaultTransaction = defaultTransactions.get(0);

		//	Map<String, Object> props = new HashMap<String, Object>();
		//	props.put("value", TransactionAttributeType.valueOf(defaultTransaction.getAttribute().name())); //$NON-NLS-1$
		//	annotationHelper.addClassAnnotation(bean.getEjbClass(), TransactionAttribute.class, props);
		//}

		///Iterator<String> iterator = methodTransactions.keySet().iterator();
		//while (iterator.hasNext()) {
		//	String methodName = (String) iterator.next();
		//	if ("*".equals(methodName)) { //$NON-NLS-1$
		//		continue;
		//	}

		//	List<MethodTransaction> transactions = methodTransactions.get(methodName);
		//	MethodTransaction methodTransaction = transactions.get(0);

		//	Map<String, Object> props = new HashMap<String, Object>();
		//	props.put("value", TransactionAttributeType.valueOf(methodTransaction.getAttribute().name())); //$NON-NLS-1$

		//	MethodParams methodParams = methodTransaction.getMethod().getMethodParams();
		//	String[] params = methodParams.getMethodParam().toArray(new String[0]);
		//	annotationHelper.addMethodAnnotation(bean.getEjbClass(), methodName, params, TransactionAttribute.class, props);
		//}
	}

	public void processMessageDrivenBean(MessageDrivenBean bean) {
		try {
			Map<String, Object> props = new HashMap<String, Object>();
			ActivationConfig activationConfig = bean.getActivationConfig();
			if (activationConfig != null) {
				List<Map<String, Object>> activationConfigPropertiesList = new ArrayList<Map<String, Object>>();

				List<ActivationConfigProperty> activationConfigProperties = activationConfig.getActivationConfigProperty();

				for (ActivationConfigProperty activationConfigProperty : activationConfigProperties) {
					HashMap<String, Object> configProps = new HashMap<String, Object>();
					configProps.put("propertyName", activationConfigProperty.getActivationConfigPropertyName()); //$NON-NLS-1$
					configProps.put("propertyValue", activationConfigProperty.getActivationConfigPropertyValue()); //$NON-NLS-1$

					activationConfigPropertiesList.add(configProps);
				}

				if (bean.getMessageDestinationLink() != null && bean.getMessageDestinationLink().length() > 0) {
					if (!hasConfigProperty(activationConfigPropertiesList, "destination")) { //$NON-NLS-1$
						HashMap<String, Object> configProps = new HashMap<String, Object>();
						configProps.put("propertyName", "destination"); //$NON-NLS-1$ //$NON-NLS-2$
						configProps.put("propertyValue", bean.getMessageDestinationLink()); //$NON-NLS-1$

						activationConfigPropertiesList.add(configProps);
					}
				}

				props.put("activationConfig", activationConfigPropertiesList.toArray(new HashMap[0])); //$NON-NLS-1$
			}
			props.put("name", bean.getEjbName()); //$NON-NLS-1$
			annotationHelper.addClassAnnotation(bean.getEjbClass(), MessageDriven.class, props);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.ejb.MessageDriven", bean.getEjbClass());
			annotationHelper.addWarning(warning);
		}
	}


	private boolean hasConfigProperty(List<Map<String, Object>> activationConfigPropertiesList, String propertyName) {
		for (Map<String,Object> configProperty : activationConfigPropertiesList) {
			if (configProperty.get("propertyName") != null && configProperty.get("propertyName").toString().equals(propertyName)) { //$NON-NLS-1$ //$NON-NLS-2$
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
			roleProps.put("value", roleList); //$NON-NLS-1$


			List<Method> methods = methodPermission.getMethod();
			Iterator<Method> methodIter = methods.iterator();

			while (methodIter.hasNext()) {
				Method method = (Method) methodIter.next();
				EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(method.getEjbName());
				try {
					MethodParams methodParams = method.getMethodParams();
					String[] params = methodParams.getMethodParam().toArray(new String[0]);
					if ((!"*".equals(method.getMethodName())) && descriptor.getExcludeList().getMethod().contains(method)) { //$NON-NLS-1$
						annotationHelper.addMethodAnnotation(enterpriseBean.getEjbClass(), method.getMethodName(), params, DenyAll.class, null);
						continue;
					}
					if (methodPermission.getUnchecked()) {
						if ("*".equals(method.getMethodName())) { //$NON-NLS-1$
							annotationHelper.addClassAnnotation(enterpriseBean.getEjbClass(), PermitAll.class, null);
						} else {
							annotationHelper.addMethodAnnotation(enterpriseBean.getEjbClass(), method.getMethodName(), params, PermitAll.class, null);
						}
					} else {
						if ("*".equals(method.getMethodName())) { //$NON-NLS-1$
							annotationHelper.addClassAnnotation(enterpriseBean.getEjbClass(), RolesAllowed.class, roleProps);
						} else {
							annotationHelper.addMethodAnnotation(enterpriseBean.getEjbClass(), method.getMethodName(), params, RolesAllowed.class, roleProps);
						}
					}
				} catch (Exception e) {
					String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.RolesAllowed", enterpriseBean.getEjbClass());
					annotationHelper.addWarning(warning);
				}
			}
		}
	}

	public void processBeanSecurityIdentity(EnterpriseBean bean) {
		if (bean.getSecurityIdentity() == null) {
			return;
		}

		Map<String, Object> runAsProps = new HashMap<String, Object>();
		runAsProps.put("value", bean.getSecurityIdentity().getRunAs()); //$NON-NLS-1$

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

		props.put("value", roleList.toArray(new String[0])); //$NON-NLS-1$
		annotationHelper.addClassAnnotation(bean.getEjbClass(), DeclareRoles.class, props);
	}

	public void processInterceptors(EjbJar ejbJar) {
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();

		for (InterceptorBinding interceptorBinding : interceptorBindings) {
			EnterpriseBean bean = ejbJar.getEnterpriseBean(interceptorBinding.getEjbName());

			try {
				List<String> interceptorClasses = interceptorBinding.getInterceptorClass();
				String[] classes = interceptorClasses.toArray(new String[0]);
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put("value", classes); //$NON-NLS-1$
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
			} catch (Exception e) {
				String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.interceptor.Interceptors", bean.getEjbClass());
				annotationHelper.addWarning(warning);
			}
		}
	}

	public void convert(AppModule module) {
		List<EjbModule> ejbModules = module.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			EjbJar ejbJar = ejbModule.getEjbJar();

			processEnterpriseBeans(ejbJar);
			processApplicationExceptions(ejbJar);
		}
	}
}
