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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.openejb.helper.annotation.JavaProjectAnnotationFacade;
import org.apache.openejb.helper.annotation.fixtures.ProjectFixture;
import org.apache.openejb.helper.annotation.fixtures.StreamFixture;
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

	public static final String PROJECT_NAME = "TestProject";
	private ProjectFixture fixture;

	/**
	 * @param name
	 */
	public AddAnnotationTest(String name) {
		super(name);
	}

	public void testShouldAddAnAnnotationToAJavaSourceFile() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean1", getStreamContents(getClass().getResourceAsStream("Test1.txt")));
		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean1", Stateless.class, null);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);

		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult1.txt")), getClassContents("org.apache.openejb.test.TestBean1"));
	}
	
	public void testShouldAddAnAnnotationToAJavaSourceFileAndNotAddAnImportIfTheImportAlreadyExists() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean2", getStreamContents(getClass().getResourceAsStream("Test2.txt")));
		
		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean2", Stateless.class, null);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult2.txt")), getClassContents("org.apache.openejb.test.TestBean2"));
	}

	public void testShouldAddAnAnnotationToAJavaSourceFileAndNotAddAnImportIfThePackageIsAlreadyImported() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean3", getStreamContents(getClass().getResourceAsStream("Test3.txt")));
		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean3", Stateless.class, null);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult3.txt")), getClassContents("org.apache.openejb.test.TestBean3"));
	}

	public void testShouldAddAnnotationWithProperties() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean4", getStreamContents(getClass().getResourceAsStream("Test4.txt")));
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", "Test");

		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean4", Entity.class, properties);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult4.txt")), getClassContents("org.apache.openejb.test.TestBean4"));
	}

	public void testShouldAddAnnotationWithEnumProperty() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean5", getStreamContents(getClass().getResourceAsStream("Test5.txt")));
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("value", TransactionManagementType.BEAN);

		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		facade.addClassAnnotation("org.apache.openejb.test.TestBean5", TransactionManagement.class, properties);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult5.txt")), getClassContents("org.apache.openejb.test.TestBean5"));
	}

	public void testShouldAddMethodAnnotationWithEnumProperty() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean6", getStreamContents(getClass().getResourceAsStream("Test6.txt")));

		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		facade.addMethodAnnotation("org.apache.openejb.test.TestBean6", "echoHelloWorld", new String[] {"java.lang.String"}, ManyToMany.class, null);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult6.txt")), getClassContents("org.apache.openejb.test.TestBean6"));
	}
	
	public void testShouldAddAnnotationWithClassAttributeWithStringPassedIn() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean7", getStreamContents(getClass().getResourceAsStream("Test7.txt")));

		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		Map<String, Object> properties = new HashMap<String,Object>();
		properties.put("value", new String[] { "org.apache.openejb.test.Test7" });
		facade.addClassAnnotation("org.apache.openejb.test.TestBean7", Remote.class, properties);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult7.txt")), getClassContents("org.apache.openejb.test.TestBean7"));
	}

	public void testShouldAddAnnotationWithNestedAttributes() throws Exception {
		addNewClassToProject("org.apache.openejb.test.TestBean8", getStreamContents(getClass().getResourceAsStream("Test8.txt")));

		JavaProjectAnnotationFacade facade = new JavaProjectAnnotationFacade(fixture.getProject());
		Map<String, Object> properties = new HashMap<String, Object>();
		
		Map<String, Object> activationConfigProperty = new HashMap<String, Object>();
		activationConfigProperty.put("propertyName", "destinationType");
		activationConfigProperty.put("propertyValue", "javax.jms.Queue");
		
		properties.put("activationConfig", new Map[] { activationConfigProperty });
		
		facade.addClassAnnotation("org.apache.openejb.test.TestBean8", MessageDriven.class, properties);
		
		Change change = facade.getChange();
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor();
		change.perform(progressMonitor);
		
		assertEquals(getStreamContents(getClass().getResourceAsStream("ExpectedResult8.txt")), getClassContents("org.apache.openejb.test.TestBean8"));
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

	private String getStreamContents(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		new StreamFixture().streamCopy(is, os);
		return new String(os.toByteArray());
	}
}
