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

import junit.framework.TestCase;

import org.apache.openejb.devtools.core.JDTFacade;
import org.apache.openejb.helper.annotation.fixtures.ProjectFixture;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class ConvertEjbCreateMethodToConstructorTest extends TestCase {

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

	public void testShouldConvertEjbCreateMethodToAConstructor() throws Exception {
		fixture.addClassToProject("org.superbiz.ProductBean", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21ProductBean.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		JDTFacade facade = new JDTFacade(fixture.getProject());
		facade.convertMethodToConstructor("org.superbiz.ProductBean", "ejbCreate", new String[] { "java.lang.Integer", "java.lang.String", "java.lang.String", "java.lang.String"}); //$NON-NLS-1$
		
		Change change = facade.getChange();
		change.perform(new NullProgressMonitor());

		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("Expected30ProductBean.txt")), fixture.getClassContents("org.superbiz.ProductBean")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testShouldChangeUsesOfEjbCreateToUseConstructor() throws Exception {
		fixture.addClassToProject("org.superbiz.ProductBean", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21ProductBean.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		fixture.addClassToProject("org.superbiz.ProductHome", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21ProductHome.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		fixture.addClassToProject("org.superbiz.SessionBean", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21SessionBean.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		fixture.addClassToProject("org.superbiz.Product", fixture.getStreamContent(getClass().getResourceAsStream("Ejb21Product.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
		
		JDTFacade facade = new JDTFacade(fixture.getProject());
		String[] signature = new String[] { "java.lang.Integer", "java.lang.String", "java.lang.String", "java.lang.String" };
		facade.changeInvocationsToConstructor("org.superbiz.ProductHome", "create", signature, "org.superbiz.ProductBean");

		Change change = facade.getChange();
		change.perform(new NullProgressMonitor());

		assertEquals(fixture.getStreamContent(getClass().getResourceAsStream("ExpectedSessionBean.txt")), fixture.getClassContents("org.superbiz.SessionBean")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
