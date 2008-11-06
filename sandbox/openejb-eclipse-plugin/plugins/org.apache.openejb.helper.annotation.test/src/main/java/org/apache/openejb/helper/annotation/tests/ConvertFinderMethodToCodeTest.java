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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceUnit;

import org.apache.openejb.devtools.core.JDTFacade;
import org.apache.openejb.helper.annotation.fixtures.ProjectFixture;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import junit.framework.TestCase;

public class ConvertFinderMethodToCodeTest extends TestCase {
	private ProjectFixture fixture;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fixture = new ProjectFixture();
		fixture.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		fixture.delete();
		super.tearDown();
	}

	public void testShouldConvertEjbFinderMethodToCodeCase1() throws Exception {
		doTest("ProductManagerBean21-1.txt", "ProductManagerBean30-1.txt", "uk.me.jrg.ejb.ProductManagerBean1");
	}

	public void testShouldConvertEjbFinderMethodToCodeCase2() throws Exception {
		doTest("ProductManagerBean21-2.txt", "ProductManagerBean30-2.txt", "uk.me.jrg.ejb.ProductManagerBean2");
	}
	
	public void testShouldConvertEjbFinderMethodToCodeCase3() throws Exception {
		doTest("ProductManagerBean21-3.txt", "ProductManagerBean30-3.txt", "uk.me.jrg.ejb.ProductManagerBean3");
	}


	
	private void doTest(String startFilename, String expectedFilename, String bean) throws CoreException, IOException {
		fixture.addClassToProject("uk.me.jrg.jee.store.ejb.ProductBean", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21ProductBean.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		fixture.addClassToProject("uk.me.jrg.jee.store.ejb.ProductHome", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21ProductHome.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		fixture.addClassToProject(bean, fixture.getStreamContent(getClass().getResourceAsStream(startFilename))); //$NON-NLS-1$ //$NON-NLS-2$

		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.addField(bean, "entityManagerFactory", "javax.persistence.EntityManagerFactory");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("name", "TestPU");
		facade.addFieldAnnotation(bean, "entityManagerFactory", PersistenceUnit.class, properties);
		
		
		String code = "javax.persistence.EntityManager entityManager = entityManagerFactory.createEntityManager();\r\n" +
				"javax.persistence.Query query = entityManager.createQuery(\"SELECT p from Product p where p.name = ?1\");\r\n" +
				"query.getResultList();";
		
		facade.changeInvocationsTo("uk.me.jrg.jee.store.ejb.ProductHome", "findBy", new String[] { "java.lang.String"}, code); //$NON-NLS-1$
		
		Change change = facade.getChange();
		change.perform(new NullProgressMonitor());

		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream(expectedFilename)), fixture.getClassContents(bean)); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
