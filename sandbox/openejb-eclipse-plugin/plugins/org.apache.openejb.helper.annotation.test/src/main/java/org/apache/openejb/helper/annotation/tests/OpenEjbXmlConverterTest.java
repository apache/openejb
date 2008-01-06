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
 
package org.apache.openejb.helper.annotation.tests;

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

import junit.framework.TestCase;

import org.apache.openejb.helper.annotation.IJavaProjectAnnotationFacade;
import org.apache.openejb.helper.annotation.OpenEjbXmlConverter;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SecurityIdentity;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.TransactionType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.xml.sax.InputSource;


/**
 * Test case to ensure that the xml converter calls the correct methods
 * on the annotation facade
 */
public class OpenEjbXmlConverterTest extends TestCase {
	
	protected Mockery context = new Mockery();
	
	public void testShouldAddTwoStatelessAnnotationsToSampleBeans() throws Exception {
		// setup
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.Test1Bean", Stateless.class, null);
			one(facade).addClassAnnotation("test.Test2Bean", Stateless.class, null);
			one(facade).addClassAnnotation("test.Test1", Remote.class, null);
			one(facade).addClassAnnotation("test.Test1Bean", RemoteHome.class, createNameValuePair("value", "test.Test1Home"));
			one(facade).addClassAnnotation("test.Test2", Remote.class, null);
			one(facade).addClassAnnotation("test.Test2Bean", RemoteHome.class, createNameValuePair("value", "test.Test2Home"));
		}});

		// execute
		converter.convert(new InputSource(getClass().getResourceAsStream("sample-openejb-jar-two-statelessessionbeans.xml")));

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddTransactionManagementAttributes() throws Exception {
		// setup
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", TransactionManagement.class, createNameValuePair("value", TransactionManagementType.BEAN));
			one(facade).addClassAnnotation("test.TestBean", TransactionAttribute.class, createNameValuePair("value", TransactionAttributeType.MANDATORY));
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[] { "java.lang.String" }, TransactionAttribute.class, createNameValuePair("value", TransactionAttributeType.MANDATORY));
		}});


		EnterpriseBean enterpriseBean = new StatefulBean();
		enterpriseBean.setEjbName("TestBean");
		enterpriseBean.setEjbClass("test.TestBean");
		enterpriseBean.setTransactionType(TransactionType.BEAN);
		
		AssemblyDescriptor descriptor = new AssemblyDescriptor();
		
		addMethodTransactionToDescriptor(descriptor, "TestBean", "*", new String[0]);
		addMethodTransactionToDescriptor(descriptor, "TestBean", "test", new String[] { "java.lang.String" });
		
		// execute
		converter.processTransactionManagement(enterpriseBean, descriptor);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateMethodPermissionAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], RolesAllowed.class, createNameValuePair("value", new String[] { "Admin" }));
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "test", new String[] { "Admin" }, false, false);
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}

	private void addMethodToEjbJarDescriptor(EjbJar ejbJar, String beanName,
			String methodName, String[] rolesAllowed, boolean unchecked, boolean deny) {
		AssemblyDescriptor descriptor = ejbJar.getAssemblyDescriptor();
		List<MethodPermission> methodPermissions = descriptor.getMethodPermission();
		MethodPermission methodPermission = new MethodPermission();
		List<Method> methods = methodPermission.getMethod();
		methodPermission.getRoleName().addAll(Arrays.asList(rolesAllowed));
		methodPermission.setUnchecked(unchecked);
		methodPermissions.add(methodPermission);
		Method method = new Method();
		method.setEjbName(beanName);
		method.setMethodName(methodName);
		method.setMethodParams(new MethodParams());
		methods.add(method);
		
		if (deny) {
			descriptor.getExcludeList().addMethod(method);
		}
	}

	private EnterpriseBean addStatefulBeanToEjbJar(EjbJar ejbJar, String beanName,
			String beanClass) {
		EnterpriseBean enterpriseBean = new StatefulBean();
		enterpriseBean.setEjbName(beanName);
		enterpriseBean.setEjbClass(beanClass);
		ejbJar.addEnterpriseBean(enterpriseBean);
		
		return enterpriseBean;
	}
	
	public void testShouldAddRolesAllowedAnnotationToClass() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", RolesAllowed.class, createNameValuePair("value", new String[] { "Admin" }));
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "*", new String[] { "Admin" }, false, false);
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddPermitAllToMethod() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], PermitAll.class, null);
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "test", new String[] { "Admin" }, true, false);
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddPermitAllToClass() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", PermitAll.class, null);
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "*", new String[] { "Admin" }, true, false);
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddDenyAllToMethod() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], DenyAll.class, null);
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "test", new String[] { "Admin" }, true, true);
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateRunAsAnnotation() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", RunAs.class, createNameValuePair("value", "Administrator"));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		SecurityIdentity securityIdentity = new SecurityIdentity();
		securityIdentity.setRunAs("Administrator");
		bean.setSecurityIdentity(securityIdentity);
		
		// execute
		converter.processBeanSecurityIdentity(bean);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateDeclaredRolesAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", DeclareRoles.class, createNameValuePair("value", new String[] { "Admin" }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");

		RemoteBean remoteBean = (RemoteBean) bean;
		remoteBean.getSecurityRoleRef().add(new SecurityRoleRef("Admin"));
		
		// execute
		converter.processDeclaredRoles(bean);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateInterceptorAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Interceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		Interceptor interceptor = new Interceptor(OpenEjbXmlConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		interceptorBindings.add(binding);
		
		// execute
		converter.processInterceptors(ejbJar);

		// verify
		context.assertIsSatisfied();
	}

	public void testShouldGenerateMethodInterceptorAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], Interceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		Interceptor interceptor = new Interceptor(OpenEjbXmlConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		NamedMethod method = new NamedMethod();
		method.setMethodName("test");
		method.setMethodParams(new MethodParams());
		binding.setMethod(method);
		interceptorBindings.add(binding);
		
		// execute
		converter.processInterceptors(ejbJar);

		// verify
		context.assertIsSatisfied();
	}

	public void testShouldGenerateExcludeDefaultInterceptorAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Interceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
			one(facade).addClassAnnotation("test.TestBean", ExcludeDefaultInterceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		Interceptor interceptor = new Interceptor(OpenEjbXmlConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		binding.setExcludeDefaultInterceptors(true);
		interceptorBindings.add(binding);
		
		// execute
		converter.processInterceptors(ejbJar);

		// verify
		context.assertIsSatisfied();
	}

	public void testShouldGenerateDefaultExcludeMethodInterceptorAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], ExcludeDefaultInterceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], Interceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		Interceptor interceptor = new Interceptor(OpenEjbXmlConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		binding.setExcludeDefaultInterceptors(true);
		NamedMethod method = new NamedMethod();
		method.setMethodName("test");
		method.setMethodParams(new MethodParams());
		binding.setMethod(method);
		interceptorBindings.add(binding);
		
		// execute
		converter.processInterceptors(ejbJar);

		// verify
		context.assertIsSatisfied();
	}

	public void testShouldGenerateExcludeClassInterceptorAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Interceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
			one(facade).addClassAnnotation("test.TestBean", ExcludeClassInterceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		Interceptor interceptor = new Interceptor(OpenEjbXmlConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		binding.setExcludeClassInterceptors(true);
		interceptorBindings.add(binding);
		
		// execute
		converter.processInterceptors(ejbJar);

		// verify
		context.assertIsSatisfied();
	}

	public void testShouldGenerateClassExcludeMethodInterceptorAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], ExcludeClassInterceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], Interceptors.class, createNameValuePair("value", new String[] { OpenEjbXmlConverterTest.class.getCanonicalName() }));
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		
		Interceptor interceptor = new Interceptor(OpenEjbXmlConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		binding.setExcludeClassInterceptors(true);
		NamedMethod method = new NamedMethod();
		method.setMethodName("test");
		method.setMethodParams(new MethodParams());
		binding.setMethod(method);
		interceptorBindings.add(binding);
		
		// execute
		converter.processInterceptors(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateRemoteAnnotations() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Stateful.class, null);
			one(facade).addClassAnnotation("test.TestBean", RemoteHome.class, createNameValuePair("value", "test.TestHome"));
			one(facade).addClassAnnotation("test.Test", Remote.class, null);
		}});

		EjbJar ejbJar = new EjbJar();
		StatefulBean bean = (StatefulBean) addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean");
		bean.setHome("test.TestHome");
		bean.setRemote("test.Test");
		
		// execute
		converter.processSessionBean((SessionBean) bean);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldProcessMessageDrivenBean() throws Exception {
		// setup
		
		final IJavaProjectAnnotationFacade facade = context.mock(IJavaProjectAnnotationFacade.class);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.MessageDrivenBean1", MessageDriven.class, createActivationConfigProperty("destinationType", "javax.jms.Queue"));
		}

		private Map<String, Object> createActivationConfigProperty(
				String propertyName, String propertyValue) {

			Map<String, Object> activationConfigProperty = createNameValuePair("propertyName", propertyName);
			activationConfigProperty.put("propertyValue", propertyValue);
			
			Map<String, Object> props = createNameValuePair("activationConfig", new Object[] { activationConfigProperty });
			
			return props;
		}});

		EjbJar ejbJar = new EjbJar();
		MessageDrivenBean bean = new MessageDrivenBean();
		bean.setEjbName("Test");
		bean.setEjbClass("test.MessageDrivenBean1");
		
		bean.setActivationConfig(new ActivationConfig());
		bean.getActivationConfig().addProperty("destinationType", "javax.jms.Queue");
		bean.setMessageDestinationType("javax.jms.Queue");
		bean.setMessageDestinationLink("TestQueue");
				
		ejbJar.addEnterpriseBean(bean);
		
		
		// execute
		converter.processMessageDrivenBean(bean);

		// verify
		context.assertIsSatisfied();
	}
	
	private void addMethodTransactionToDescriptor(AssemblyDescriptor descriptor, String ejbName, String methodName, String[] params) {
		List<ContainerTransaction> containerTransactions = descriptor.getContainerTransaction();
		ContainerTransaction containerTransaction = new ContainerTransaction();

		containerTransaction.getMethod().add(createMethod(ejbName, methodName, params));
		containerTransaction.setTransAttribute(TransAttribute.MANDATORY);
		containerTransactions.add(containerTransaction);
	}

	private Method createMethod(String ejbName, String methodName, String[] params) {
		Method method = new Method(ejbName, methodName);
		MethodParams methodParams = new MethodParams();

		for (int i = 0; i < params.length; i++) {
			methodParams.getMethodParam().add(params[i]);
		}
		
		method.setMethodParams(methodParams);
		return method;
	}
	
	private Map<String, Object> createNameValuePair(String key,
			Object value) {
		
		Map<String, Object> result = new HashMap<String, Object>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean equals(Object otherObj) {
				
				if (!( otherObj instanceof Map)) {
					return false;
				}
				
				if (otherObj == null) {
					return false;
				}
				
				Map<?,?> otherMap = (Map<?,?>) otherObj;
				
				Iterator<String> iterator = this.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					Object value = this.get(key);
					
					if (value.getClass().isArray()) {
						if (! Arrays.deepEquals((Object[])value, (Object[])otherMap.get(key))) {
							return false;
						}
					} else {
						if (! value.equals(otherMap.get(key))) {
							return false;
						}
					}
				}
				
				return true;
			}
			
		};
		
		result.put(key, value);
		return result;
	}
}
