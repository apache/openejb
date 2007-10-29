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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;

import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MethodTransaction;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransactionType;
import org.eclipse.core.resources.IProject;
import org.xml.sax.InputSource;

/**
 * Scans an openejb-jar.xml file using a SAX parser, and adds annotations
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
	 */
	public boolean convert(InputSource source) {
		try {
			EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, source.getByteStream());
			
			processEnterpriseBeans(ejbJar);
			processApplicationExceptions(ejbJar);
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void processApplicationExceptions(EjbJar ejbJar) {
		List<ApplicationException> exceptionList = ejbJar.getAssemblyDescriptor().getApplicationException();
		Iterator<ApplicationException> iterator = exceptionList.iterator();
		
		while (iterator.hasNext()) {
			ApplicationException element = (ApplicationException) iterator.next();
			String exceptionClass = element.getExceptionClass();
			
			annotationHelper.addClassAnnotation(exceptionClass, CLS_APPLICATION_EXCEPTION);
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
			} else if (bean instanceof EntityBean) {
				EntityBean entityBean = (EntityBean) bean;
				processEntityBean(entityBean);
			} else if (bean instanceof MessageDrivenBean) {
				MessageDrivenBean messageDriven = (MessageDrivenBean) bean;
				processMessageDrivenBean(messageDriven);
			}
			
			processTransactionManagement(bean, ejbJar.getAssemblyDescriptor());
		}
		
	}

	private void processTransactionManagement(EnterpriseBean bean, AssemblyDescriptor descriptor) {
		TransactionType transactionType = bean.getTransactionType();
		
		if (transactionType != null && (! TransactionType.CONTAINER.equals(transactionType))) {
			Map<String,Object> props = new HashMap<String, Object>();
			props.put("value", TransactionManagementType.BEAN);
			
			annotationHelper.addClassAnnotation(bean.getEjbClass(), "javax.ejb.TransactionManagement", props);
		}
		
		Map<String, List<MethodTransaction>> methodTransactions = descriptor.getMethodTransactions(bean.getEjbName());
		if (methodTransactions.containsKey("*")) {
			List<MethodTransaction> defaultTransactions = methodTransactions.get("*");
			MethodTransaction defaultTransaction = defaultTransactions.get(0);
			
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("value", TransactionAttributeType.valueOf(defaultTransaction.getAttribute().name()));
			annotationHelper.addClassAnnotation(bean.getEjbClass(), CLS_TRANSACTION_ATTRIBUTE, props);
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
			annotationHelper.addMethodAnnotation(bean.getEjbClass(), methodName, CLS_TRANSACTION_ATTRIBUTE, props);
		}
	}

	private void processMessageDrivenBean(MessageDrivenBean entityBean) {
		annotationHelper.addClassAnnotation(entityBean.getEjbClass(), CLS_MESSAGE_DRIVEN);
	}

	private void processEntityBean(EntityBean entityBean) {
	}

	private void processSessionBean(SessionBean sessionBean) {
		String ejbClass = sessionBean.getEjbClass();
		if (sessionBean instanceof StatelessBean || sessionBean.getSessionType() == SessionType.STATELESS) {
			annotationHelper.addClassAnnotation(ejbClass, CLS_STATELESS);
		} else if (sessionBean instanceof StatefulBean || sessionBean.getSessionType() == SessionType.STATELESS) {
			annotationHelper.addClassAnnotation(ejbClass, CLS_STATEFUL);
		} 
	}
}
