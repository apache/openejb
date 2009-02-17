package org.apache.openejb.eclipse;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;

public class OpenEjbServer {

    public void init(File baseDir) throws OpenEJBException {

	File homeDir = new File(System.getProperty("user.home"));
	homeDir = new File(homeDir, "openejb");
	if (!homeDir.exists()) {
	    homeDir.mkdir();
	}

	File metaInfDir = new File(baseDir, "META-INF");
	File configFile = new File(metaInfDir, "openejb.xml");

	// Setup environment
	Properties properties = new Properties();
	properties.put(Context.INITIAL_CONTEXT_FACTORY,
		"org.apache.openejb.client.LocalInitialContextFactory");

	properties.put("openejb.home", homeDir.getAbsolutePath());
	properties.put("openejb.base", baseDir.getAbsolutePath());
	properties.put("openejb.configuration", configFile.getAbsolutePath());
	properties.put("openejb.deployments.classpath", "false");

	OpenEJB.init(properties);
    }

    public void destroy() throws NoSuchApplicationException, UndeployException {
	Assembler assembler = SystemInstance.get()
		.getComponent(Assembler.class);
	for (AppInfo appInfo : assembler.getDeployedApplications()) {
	    assembler.destroyApplication(appInfo.jarPath);
	}
	OpenEJB.destroy();
    }
}
