package org.apache.openejb.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class OpenEjbPlugin extends Plugin {

    private OpenEjbDeploymentService service = null;
    private IStringVariableManager manager = null;

    private static OpenEjbPlugin plugin;
    
    public OpenEjbPlugin () {
	super();
	manager = VariablesPlugin.getDefault().getStringVariableManager();
	addPropertyVariables(System.getProperties());
	plugin = this;
    }
    
    public static OpenEjbPlugin getDefault() {
	return plugin;
    }
    
    public void start(BundleContext aContext) throws Exception {
	super.start(aContext);
	service = new OpenEjbDeploymentService(aContext);
	service.open();
    }

    public void stop(BundleContext aContext) throws Exception {
	service.close();
	super.stop(aContext);
    }

    public Properties loadProperties(Bundle bundle, Path path) {
	Properties properties = new Properties();
	URL[] urls = FileLocator.findEntries(bundle, path);
	for (URL url : urls) {
	    Properties p = new Properties();
	    try {
		InputStream is = url.openStream();
		p.load(is);
		is.close();
	    } catch (IOException ioe) {
		// Ignore any properties partially loaded from a bad file.
		p.clear();
	    }
	    addPropertyVariables(p);
	    properties.putAll(p);
	}
	return properties;
    }
    
    private void addPropertyVariables(Properties properties) {
	for (Object k : properties.keySet()) {
	    String key = (String) k;
	    String value = properties.getProperty(key);
	    IValueVariable variable = manager.getValueVariable(key);
	    if (variable == null) {
		variable = manager.newValueVariable(key, "", false, value);
		try {
		    manager.addVariables(new IValueVariable[] {variable});
		} catch (CoreException e) {
		    e.printStackTrace();
		}
	    } else {
		variable.setValue(value);
	    }
	}
    }

}
