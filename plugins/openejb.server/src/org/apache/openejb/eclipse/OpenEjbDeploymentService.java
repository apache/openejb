package org.apache.openejb.eclipse;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class OpenEjbDeploymentService {

    private ServiceTracker deploymentTracker;
    private BundleContext bundleContext;
    private OpenEjbServer ejbServer;

    public OpenEjbDeploymentService(BundleContext bundleContext) {
	this.bundleContext = bundleContext;
	this.deploymentTracker = null;
	this.ejbServer = null;
    }

    public void open() {

	try {
	    if (ejbServer == null) {
		Bundle bundle = bundleContext.getBundle();
		URL baseUrl = bundle.getEntry("/");
		baseUrl = FileLocator.toFileURL(baseUrl);
		File baseDir = new File(baseUrl.getPath());

		ejbServer = new OpenEjbServer();
		ejbServer.init(baseDir);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}

	deploymentTracker = new ServiceTracker(bundleContext,
		OpenEjbApplication.class.getName(), null);
	deploymentTracker.open();

	ServiceListener sl = new ServiceListener() {

	    public void serviceChanged(ServiceEvent ev) {
		ServiceReference sr = ev.getServiceReference();
		switch (ev.getType()) {

		case ServiceEvent.REGISTERED:
		    deployApplication(sr);
		    break;

		case ServiceEvent.UNREGISTERING:
		    undeployApplication(sr);
		    break;
		}
	    }
	};

	// Generate a ServiceEvent for any existing OpenEJbApplication services.

	String filter = "(objectclass=" + OpenEjbApplication.class.getName()
		+ ")";
	try {
	    bundleContext.addServiceListener(sl, filter);
	    ServiceReference[] srl = bundleContext.getServiceReferences(null,
		    filter);
	    if (srl != null) {
		for (ServiceReference sr : srl) {
		    sl.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
			    sr));
		}
	    }
	} catch (InvalidSyntaxException e) {
	    e.printStackTrace();
	}
    }

    public void close() throws Exception {
	deploymentTracker.close();
	ejbServer.destroy();
    }

    private void deployApplication(ServiceReference sr) {
	OpenEjbApplication application = (OpenEjbApplication) bundleContext
		.getService(sr);
	Properties properties = new Properties();
	for (String key : sr.getPropertyKeys()) {
	    Object value = sr.getProperty(key);
	    if (value instanceof String) {
		properties.put(key, value);
	    }
	}
	try {
	    application.deploy(properties);
	} catch (OpenEJBException e) {
	    e.printStackTrace();
	}
    }

    private void undeployApplication(ServiceReference sr) {
	try {
	    OpenEjbApplication application = (OpenEjbApplication) bundleContext
		    .getService(sr);
	    application.undeploy();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
