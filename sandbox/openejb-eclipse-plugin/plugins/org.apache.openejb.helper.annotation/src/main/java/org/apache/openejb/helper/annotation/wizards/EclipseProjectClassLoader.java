package org.apache.openejb.helper.annotation.wizards;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class EclipseProjectClassLoader extends URLClassLoader {

	public EclipseProjectClassLoader(ClassLoader parent, IProject project) {
		super(new URL[0], parent);
		addURLs(project);
	}

	private void addURLs(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		
		try {
			IPath path = javaProject.getOutputLocation();
			addURL(convertPathToFile(path).toURL());
			
			IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(true);
			for (IClasspathEntry cpEntry : resolvedClasspath) {
				try {
					URL cpURL = null;
					if (IClasspathEntry.CPE_SOURCE == cpEntry.getEntryKind()){
						if (cpEntry.getOutputLocation() != null) {
							IPath outputLocation = cpEntry.getOutputLocation();
							cpURL = convertPathToFile(outputLocation).toURL();
						}
						
					} else if (IClasspathEntry.CPE_PROJECT == cpEntry.getEntryKind()) {
						IProject foundProject = findProject(cpEntry.getPath());
						
						if (foundProject != null) {
							addURLs(foundProject);
						}
					} else {
						cpURL = convertPathToFile(cpEntry.getPath()).toURL();
					}
					
					if (cpURL != null) {
						addURL(cpURL);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private IProject findProject(IPath path) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			if (project.getFullPath().equals(path)) {
				return project;
			}
		}
		
		return null;
	}

	private File convertPathToFile(IPath path) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		
		String osfile = file != null && file.getRawLocation() != null ? file.getRawLocation().toOSString() : path.toOSString();			

		File result = new File(osfile);
		return result;
	}

}
