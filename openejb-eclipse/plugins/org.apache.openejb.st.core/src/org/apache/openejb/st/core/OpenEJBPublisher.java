package org.apache.openejb.st.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jst.server.generic.core.internal.GenericPublisher;
import org.eclipse.wst.server.core.IModuleArtifact;

public class OpenEJBPublisher extends GenericPublisher {
	
	/**
	 * Deploys the EJB project
	 */
	public IStatus[] publish(IModuleArtifact[] resource, IProgressMonitor monitor) {
		return null;
	}
	
	/**
	 * Undeploys the EJB project
	 */
	public IStatus[] unpublish(IProgressMonitor monitor) {
		return null;
	}
}