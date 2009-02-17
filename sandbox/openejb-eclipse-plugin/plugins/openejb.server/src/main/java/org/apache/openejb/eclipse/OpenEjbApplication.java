package org.apache.openejb.eclipse;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.loader.SystemInstance;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.osgi.framework.Bundle;

public class OpenEjbApplication {

    private AppInfo appInfo;
    private URL rootUrl;

    public OpenEjbApplication(Bundle bundle) throws IOException {
	this.rootUrl = bundle.getEntry("/");
	this.rootUrl = FileLocator.toFileURL(this.rootUrl);
	this.appInfo = null;
    }

    public OpenEjbApplication(URL rootUrl) {
	this.appInfo = null;
	this.rootUrl = rootUrl;
    }

    public AppInfo getAppInfo() {
	return appInfo;
    }

    public URL getRootUrl() {
	return rootUrl;
    }

    public void deploy(Properties properties) throws OpenEJBException {

	ConfigurationFactory factory = new ConfigurationFactory();

	String url = rootUrl.getPath();
	Assembler assembler = SystemInstance.get()
		.getComponent(Assembler.class);
	Openejb openejb = storeProperties(properties, factory);

	for (Resource resource : openejb.getResource()) {
	    ResourceInfo serviceInfo = factory.configureService(resource,
		    ResourceInfo.class);
	    assembler.createResource(serviceInfo);
	}

	Deployer deployer = new DeployerEjb();
	this.appInfo = deployer.deploy(url, properties);
    }

    public void undeploy() throws NoSuchApplicationException, UndeployException {
	Assembler assembler = SystemInstance.get()
		.getComponent(Assembler.class);
	assembler.destroyApplication(this.appInfo.jarPath);
    }

    private Openejb storeProperties(Properties properties,
	    ConfigurationFactory factory) {

	IStringVariableManager manager = VariablesPlugin.getDefault()
		.getStringVariableManager();
	Openejb openejb = new Openejb();
	for (Object k : properties.keySet()) {
	    Object v = properties.get(k);
	    if (v instanceof String) {
		String key = (String) k;
		String value = (String) v;
		try {
		    value = manager.performStringSubstitution(value, false);
		} catch (CoreException e) {
		    e.printStackTrace();
		}
		SystemInstance.get().setProperty(key, value);
		if (value.startsWith("new://")) {
		    try {
			URI uri = new URI(value);
			openejb.add(factory.toConfigDeclaration(key, uri));
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	return openejb;
    }
}
