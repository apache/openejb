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

package org.apache.openejb.helper.annotation.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.openejb.helper.annotation.JavaProjectAnnotationFacade;
import org.apache.openejb.helper.annotation.OpenEjbXmlConverter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.xml.sax.InputSource;

public class EJBMigrationRefactoring extends Refactoring {

	protected String ejbJarXmlFile;
	protected String openEjbJarXmlFile;
	protected IProject project;
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {

		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {

		if (ejbJarXmlFile == null || ejbJarXmlFile.length() == 0) {
			return RefactoringStatus.createErrorStatus("No ejb-jar.xml specified");
		}

		IFile file = project.getFile(ejbJarXmlFile);
		if (! (file.exists())) {
			return RefactoringStatus.createErrorStatus("Specified ejb-jar.xml does not exist");
		}
		
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
		IFile file = project.getFile(ejbJarXmlFile);
		if (! (file.exists())) {
			return null;
		}

		JavaProjectAnnotationFacade annotationFacade = new JavaProjectAnnotationFacade(project);
		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(annotationFacade);
		converter.convert(new InputSource(file.getContents()));
		
		return annotationFacade.getChange();
	}

	@Override
	public String getName() {
		return "EJB 3.0 Annotation Migration Refactoring Wizard";
	}

	public String getEjbJarXmlFile() {
		return ejbJarXmlFile;
	}

	public void setEjbJarXmlFile(String ejbJarXmlFile) {
		this.ejbJarXmlFile = ejbJarXmlFile;
	}

	public String getOpenEjbJarXmlFile() {
		return openEjbJarXmlFile;
	}

	public void setOpenEjbJarXmlFile(String openEjbJarXmlFile) {
		this.openEjbJarXmlFile = openEjbJarXmlFile;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	
}
