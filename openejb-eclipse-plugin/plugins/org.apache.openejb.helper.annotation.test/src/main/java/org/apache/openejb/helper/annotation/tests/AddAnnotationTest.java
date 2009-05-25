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

import java.util.HashMap;
import java.util.Map;

import javax.ejb.MessageDriven;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import junit.framework.TestCase;

import org.apache.openejb.devtools.core.JDTFacade;
import org.apache.openejb.helper.annotation.fixtures.ProjectFixture;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class AddAnnotationTest extends TestCase {

	protected void tearDown() throws Exception {
		fixture.delete();
		super.tearDown();
	}
	
	private class FakeProgressMonitor implements IProgressMonitor {

		boolean started = false;
		boolean finished = false;
		
		public void beginTask(String name, int totalWork) {
			started = true;
		}

		public void done() {
			finished = true;
		}

		public void internalWorked(double work) {
		}

		public boolean isCanceled() {
			return false;
		}

		public void setCanceled(boolean value) {
		}

		public void setTaskName(String name) {
		}

		public void subTask(String name) {
		}

		public void worked(int work) {
		}

		public boolean isFinished() {
			return finished;
		}
	}

	public static final String PROJECT_NAME = "TestProject"; //$NON-NLS-1$
	private ProjectFixture fixture;

	/**
	 * @param name
	 */
	public AddAnnotationTest(String name) {
		super(name);
	}

	public void testShouldAddAnAnnotationToAJavaSourceFile() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean1", fixture.getStreamContent(getClass().getResourceAsStream("Test1.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean1", Stateless.class, null); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);

		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult1.txt")), getClassContents("org.apache.openejb.test.TestBean1")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testShouldAddAnAnnotationToAJavaSourceFileAndNotAddAnImportIfTheImportAlreadyExists() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean2", fixture.getStreamContent(getClass().getResourceAsStream("Test2.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		
		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean2", Stateless.class, null); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult2.txt")), getClassContents("org.apache.openejb.test.TestBean2")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testShouldAddAnAnnotationToAJavaSourceFileAndNotAddAnImportIfThePackageIsAlreadyImported() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean3", fixture.getStreamContent(getClass().getResourceAsStream("Test3.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean3", Stateless.class, null); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult3.txt")), getClassContents("org.apache.openejb.test.TestBean3")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testShouldAddAnnotationWithProperties() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean4", fixture.getStreamContent(getClass().getResourceAsStream("Test4.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", "Test"); //$NON-NLS-1$ //$NON-NLS-2$

		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean4", Entity.class, properties); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult4.txt")), getClassContents("org.apache.openejb.test.TestBean4")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testShouldAddAnnotationWithEnumProperty() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean5", fixture.getStreamContent(getClass().getResourceAsStream("Test5.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("value", TransactionManagementType.BEAN); //$NON-NLS-1$

		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean5", TransactionManagement.class, properties); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult5.txt")), getClassContents("org.apache.openejb.test.TestBean5")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testShouldAddMethodAnnotationWithEnumProperty() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean6", fixture.getStreamContent(getClass().getResourceAsStream("Test6.txt"))); //$NON-NLS-1$ //$NON-NLS-2$

		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addMethodAnnotation("org.apache.openejb.test.TestBean6", "echoHelloWorld", new String[] {"java.lang.String"}, ManyToMany.class, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult6.txt")), getClassContents("org.apache.openejb.test.TestBean6")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testShouldAddAnnotationWithClassAttributeWithStringPassedIn() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean7", fixture.getStreamContent(getClass().getResourceAsStream("Test7.txt"))); //$NON-NLS-1$ //$NON-NLS-2$

		JDTFacade facade = new JDTFacade(fixture.getProject());
		Map<String, Object> properties = new HashMap<String,Object>();
		properties.put("value", new String[] { "org.apache.openejb.test.Test7" }); //$NON-NLS-1$ //$NON-NLS-2$
		facade.addClassAnnotation("org.apache.openejb.test.TestBean7", Remote.class, properties); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult7.txt")), getClassContents("org.apache.openejb.test.TestBean7")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testShouldAddAnnotationWithNestedAttributes() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean8", fixture.getStreamContent(getClass().getResourceAsStream("Test8.txt"))); //$NON-NLS-1$ //$NON-NLS-2$

		JDTFacade facade = new JDTFacade(fixture.getProject());
		Map<String, Object> properties = new HashMap<String, Object>();
		
		Map<String, Object> activationConfigProperty = new HashMap<String, Object>();
		activationConfigProperty.put("propertyName", "destinationType"); //$NON-NLS-1$ //$NON-NLS-2$
		activationConfigProperty.put("propertyValue", "javax.jms.Queue"); //$NON-NLS-1$ //$NON-NLS-2$
		
		properties.put("activationConfig", new Map[] { activationConfigProperty }); //$NON-NLS-1$
		
		facade.addClassAnnotation("org.apache.openejb.test.TestBean8", MessageDriven.class, properties); //$NON-NLS-1$
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedResult8.txt")), getClassContents("org.apache.openejb.test.TestBean8")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fixture = new ProjectFixture();
		fixture.reset();
	}

	private String getClassContents(String className) throws CoreException {
		return fixture.getClassContents(className);
	}

	private void addNewClassToProject(String className, String content) throws CoreException {
		fixture.addClassToProject(className, content);
		fixture.reset();
	}
}
