package org.apache.openejb.builder;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.DependsOn;

import org.apache.openejb.devtools.core.JDTFacade;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {

	
	
	public MarkerResolutionGenerator() {
		super();
	}

	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution markerResolution = new IMarkerResolution() {

			public String getLabel() {
				return "Set dependencies";
			}

			public void run(IMarker marker) {
				try {
					String bean = (String) marker.getAttribute(ISingletonDependencyMarker.BEAN);
					String[] dependencies = (String[]) marker.getAttribute(ISingletonDependencyMarker.DEPENDENCIES);
					
					IProject project = marker.getResource().getProject();

					JDTFacade facade = new JDTFacade(project);
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put("value", dependencies);
					
					facade.removeClassAnnotation(bean, DependsOn.class);
					
					if (dependencies != null && dependencies.length > 0) {
						facade.addClassAnnotation(bean, DependsOn.class, properties);
					}
					
					facade.getChange().perform(new NullProgressMonitor());
					
				} catch (JavaModelException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
		};
		
		return new IMarkerResolution[] { markerResolution };
	}

}
