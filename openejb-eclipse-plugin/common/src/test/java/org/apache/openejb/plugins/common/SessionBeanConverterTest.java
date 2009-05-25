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
import org.apache.openejb.plugins.common.OpenEjbXmlConverter;
import org.apache.openejb.plugins.common.SessionBeanConverter;
import org.apache.openejb.plugins.common.Converter;
import org.apache.openejb.plugins.common.IJDTFacade;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.xml.sax.InputSource;


/**
 * Test case to ensure that the xml converter calls the correct methods
 * on the annotation facade
 */
public class SessionBeanConverterTest extends TestCase {
	
	protected Mockery context = new Mockery();
	
	public void testShouldAddTwoStatelessAnnotationsToSampleBeans() throws Exception {
		// setup
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		Converter[] converters = {
			new SessionBeanConverter(facade)
		};

		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(converters);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.Test1Bean", Stateless.class, null); //$NON-NLS-1$
			one(facade).addClassAnnotation("test.Test2Bean", Stateless.class, null); //$NON-NLS-1$
		}});

		// execute
		converter.convert(new InputSource(getClass().getResourceAsStream("sample-openejb-jar-two-statelessessionbeans.xml"))); //$NON-NLS-1$

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddTransactionManagementAttributes() throws Exception {
		// setup
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", TransactionManagement.class, createNameValuePair("value", TransactionManagementType.BEAN)); //$NON-NLS-1$ //$NON-NLS-2$
//			one(facade).addClassAnnotation("test.TestBean", TransactionAttribute.class, createNameValuePair("value", TransactionAttributeType.MANDATORY)); //$NON-NLS-1$ //$NON-NLS-2$
//			one(facade).addMethodAnnotation("test.TestBean", "test", new String[] { "java.lang.String" }, TransactionAttribute.class, createNameValuePair("value", TransactionAttributeType.MANDATORY)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}});


		EnterpriseBean enterpriseBean = new StatefulBean();
		enterpriseBean.setEjbName("TestBean"); //$NON-NLS-1$
		enterpriseBean.setEjbClass("test.TestBean"); //$NON-NLS-1$
		enterpriseBean.setTransactionType(TransactionType.BEAN);
		
		AssemblyDescriptor descriptor = new AssemblyDescriptor();
		
		addMethodTransactionToDescriptor(descriptor, "TestBean", "*", new String[0]); //$NON-NLS-1$ //$NON-NLS-2$
		addMethodTransactionToDescriptor(descriptor, "TestBean", "test", new String[] { "java.lang.String" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// execute
		converter.processTransactionManagement(enterpriseBean, descriptor);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateMethodPermissionAnnotations() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], RolesAllowed.class, createNameValuePair("value", new String[] { "Admin" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "test", new String[] { "Admin" }, false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", RolesAllowed.class, createNameValuePair("value", new String[] { "Admin" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "*", new String[] { "Admin" }, false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddPermitAllToMethod() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], PermitAll.class, null); //$NON-NLS-1$ //$NON-NLS-2$
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "test", new String[] { "Admin" }, true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddPermitAllToClass() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", PermitAll.class, null); //$NON-NLS-1$
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "*", new String[] { "Admin" }, true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldAddDenyAllToMethod() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], DenyAll.class, null); //$NON-NLS-1$ //$NON-NLS-2$
		}});

		EjbJar ejbJar = new EjbJar();
		addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		addMethodToEjbJarDescriptor(ejbJar, "TestBean", "test", new String[] { "Admin" }, true, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// execute
		converter.processMethodPermissions(ejbJar);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateRunAsAnnotation() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", RunAs.class, createNameValuePair("value", "Administrator")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		SecurityIdentity securityIdentity = new SecurityIdentity();
		securityIdentity.setRunAs("Administrator"); //$NON-NLS-1$
		bean.setSecurityIdentity(securityIdentity);
		
		// execute
		converter.processBeanSecurityIdentity(bean);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateDeclaredRolesAnnotations() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", DeclareRoles.class, createNameValuePair("value", new String[] { "Admin" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$

		RemoteBean remoteBean = (RemoteBean) bean;
		remoteBean.getSecurityRoleRef().add(new SecurityRoleRef("Admin")); //$NON-NLS-1$
		
		// execute
		converter.processDeclaredRoles(bean);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldGenerateInterceptorAnnotations() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Interceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Interceptor interceptor = new Interceptor(SessionBeanConverterTest.class);
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], Interceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Interceptor interceptor = new Interceptor(SessionBeanConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		NamedMethod method = new NamedMethod();
		method.setMethodName("test"); //$NON-NLS-1$
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Interceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$
			one(facade).addClassAnnotation("test.TestBean", ExcludeDefaultInterceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Interceptor interceptor = new Interceptor(SessionBeanConverterTest.class);
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], ExcludeDefaultInterceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], Interceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Interceptor interceptor = new Interceptor(SessionBeanConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		binding.setExcludeDefaultInterceptors(true);
		NamedMethod method = new NamedMethod();
		method.setMethodName("test"); //$NON-NLS-1$
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Interceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$
			one(facade).addClassAnnotation("test.TestBean", ExcludeClassInterceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Interceptor interceptor = new Interceptor(SessionBeanConverterTest.class);
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], ExcludeClassInterceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			one(facade).addMethodAnnotation("test.TestBean", "test", new String[0], Interceptors.class, createNameValuePair("value", new String[] { SessionBeanConverterTest.class.getCanonicalName() })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		EnterpriseBean bean = addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Interceptor interceptor = new Interceptor(SessionBeanConverterTest.class);
		ejbJar.addInterceptor(interceptor);
		List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
		
		InterceptorBinding binding = new InterceptorBinding(bean, interceptor);
		binding.setExcludeClassInterceptors(true);
		NamedMethod method = new NamedMethod();
		method.setMethodName("test"); //$NON-NLS-1$
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
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.TestBean", Stateful.class, null); //$NON-NLS-1$
		}});

		EjbJar ejbJar = new EjbJar();
		StatefulBean bean = (StatefulBean) addStatefulBeanToEjbJar(ejbJar, "TestBean", "test.TestBean"); //$NON-NLS-1$ //$NON-NLS-2$
		bean.setHome("test.TestHome"); //$NON-NLS-1$
		bean.setRemote("test.Test"); //$NON-NLS-1$
		
		// execute
		converter.processSessionBean((SessionBean) bean);

		// verify
		context.assertIsSatisfied();
	}
	
	public void testShouldProcessMessageDrivenBean() throws Exception {
		// setup
		
		final IJDTFacade facade = context.mock(IJDTFacade.class);
		SessionBeanConverter converter = new SessionBeanConverter(facade);

		final Map<String,Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put("destination", "TestQueue"); //$NON-NLS-1$ //$NON-NLS-2$
		expectedMap.put("destinationType", "javax.jms.Queue"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// expectations
		context.checking(new Expectations(){{
			one(facade).addClassAnnotation("test.MessageDrivenBean1", MessageDriven.class, createNameValuePair("name", "Test")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}});

		EjbJar ejbJar = new EjbJar();
		MessageDrivenBean bean = new MessageDrivenBean();
		bean.setEjbName("Test"); //$NON-NLS-1$
		bean.setEjbClass("test.MessageDrivenBean1"); //$NON-NLS-1$
		
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
