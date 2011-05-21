/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.catalina.startup.Bootstrap;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class TomEEContainer implements DeployableContainer<TomEEConfiguration> {

	private Bootstrap bootstrap;
	private TomEEConfiguration configuration;
	private File catalinaDirectory;
	private boolean usingOpenEJB;

	public Class<TomEEConfiguration> getConfigurationClass() {
		return TomEEConfiguration.class;
	}

	public void setup(TomEEConfiguration configuration) {
		this.configuration = configuration;
	}

	public void start() throws LifecycleException {
		try {
			System.setProperty("openejb.deployments.classpath.exclude", ".*");
			System.setProperty("openejb.deployments.classpath.include", "");
			
			catalinaDirectory = new File(configuration.getDir());
			if (catalinaDirectory.exists()) {
				catalinaDirectory.delete();
			}
			
			catalinaDirectory.mkdirs();
			catalinaDirectory.deleteOnExit();

			createTomcatDirectories(catalinaDirectory);

			// copy configs
			copyConfigs(catalinaDirectory);
			
			// deploy status helper app
			copyArchive(getStatusApp());
			
			// call Bootstrap();
			System.out.println("Starting TomEE from: " + catalinaDirectory.getAbsolutePath());
			
			String catalinaBase = catalinaDirectory.getAbsolutePath();

			System.setProperty("catalina.home", catalinaBase);
			System.setProperty("catalina.base", catalinaBase);
			
			bootstrap = new Bootstrap();
			bootstrap.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw new LifecycleException("Summat went wrong", e);
		}		
	}

	private Archive<?> getStatusApp() {
		return ShrinkWrap.create(JavaArchive.class, "status.jar")
		.addClass(AppStatus.class)
		.addClass(AppStatusRemote.class)
		.addClass(AppLookupException.class)
		.addAsResource(new StringAsset("<ejb-jar/>"), "META-INF/ejb-jar.xml");
	}

	public void stop() throws LifecycleException {
		try {
 			bootstrap.stopServer();
			deleteTree(catalinaDirectory);
		} catch (Exception e) {
			throw new LifecycleException("Unable to stop server", e);
		}
	}

	public ProtocolDescription getDefaultProtocol() {
		return new ProtocolDescription("Servlet 3.0");
	}

	public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
		try {
			copyArchive(archive);
			
			boolean deployed = false;
			int attempts = 0;
			while (attempts < configuration.getTimeout() && deployed == false) {
				// need to poll for the app being deployed
				attempts++;
				Thread.sleep(1000);
				
				deployed = checkDeploymentStatus(archive);
			}
			
			HTTPContext httpContext = new HTTPContext("0.0.0.0", configuration.getHttpPort());
			return new ProtocolMetaData().addContext(httpContext);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeploymentException("Unable to deploy", e);
		}
	}

	private void copyArchive(Archive<?> archive) throws IOException, FileNotFoundException {
		InputStream is = archive.as(ZipExporter.class).exportAsInputStream();
		copyStream(is, new FileOutputStream(new File(catalinaDirectory, "webapps/" + archive.getName())));
	}

	private boolean checkDeploymentStatus(Archive<?> archive) throws Exception {
		if (usingOpenEJB) {
			try {
				String dir = getDir(catalinaDirectory.getAbsolutePath() + "/webapps/" + archive.getName());
				
				Properties properties = new Properties();
				properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
				properties.setProperty(Context.PROVIDER_URL, "http://localhost:" + configuration.getHttpPort() + "/openejb/ejb");
				
				InitialContext context = new InitialContext(properties);
				AppStatusRemote appStatus = (AppStatusRemote) context.lookup("AppStatusRemote");
				String[] deployedApps = appStatus.getDeployedApps();
				for (String deployedApp : deployedApps) {
					if (deployedApp.equals(dir)) {
						return true;
					}
				}
			} catch (Exception e) {
			}
			
		    return false;
		} else {
			String context = getDir(archive.getName());
			
			MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			Set<ObjectInstance> mbeans = platformMBeanServer.queryMBeans(new ObjectName("Catalina:type=Manager,context=/" + context + ",*"), null);
			for (ObjectInstance objectInstance : mbeans) {
				String status = (String) platformMBeanServer.getAttribute(objectInstance.getObjectName(), "stateName");
				if ("STARTED".equals(status)) {
					return true;
				}
			}
			return false;
		}
	}

	public void undeploy(Archive<?> archive) throws DeploymentException {
		// remove the archive file
		new File(catalinaDirectory,"webapps/" + archive.getName()).delete();
		
		// remove the directory
		deleteTree(new File(catalinaDirectory, "/webapps/" + getDir(archive.getName())));
	}

	private String getDir(String filename) {
		int lastDot = filename.lastIndexOf(".");
		if (lastDot == -1) {
			return filename;
		}
		
		return filename.substring(0, lastDot);
	}

	private void deleteTree(File file) {
		if (file == null) return;
		if (! file.exists()) return;
		
		if (file.isFile()) {
			file.delete();
			return;
		}
		
		if (file.isDirectory()) {
			if (".".equals(file.getName())) return;
			if ("..".equals(file.getName())) return;
			
			File[] children = file.listFiles();
			
			for (File child : children) {
				deleteTree(child);
			}
			
			file.delete();
		}
	}

	public void deploy(Descriptor descriptor) throws DeploymentException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void undeploy(Descriptor descriptor) throws DeploymentException {
		throw new UnsupportedOperationException("Not implemented");
	}

	private void copyConfigs(File directory) throws Exception {
		File confDir = new File(directory, "conf");
		copyFileTo(confDir, "catalina.policy");
		copyTemplateTo(confDir, "catalina.properties");
		copyFileTo(confDir, "context.xml");
		copyFileTo(confDir, "logging.properties");
		copyFileTo(confDir, "openejb.xml");
		copyFileTo(confDir, "server.xml");
		copyFileTo(confDir, "tomcat-users.xml");
		copyFileTo(confDir, "web.xml");
		
		String openejbPath = configuration.getOpenejbPath();
		
		
		if (openejbPath != null && openejbPath.length() > 0) {
			usingOpenEJB = true;
			try {
				if (openejbPath.startsWith("classpath:/")) {
					URL resource = TomEEContainerTest.class.getResource(openejbPath.substring(10));
					copyStream(resource.openStream(), new FileOutputStream(new File(directory, "webapps/openejb.war")));
				} else {
					FileInputStream is = new FileInputStream(new File(openejbPath));
					copyStream(is, new FileOutputStream(new File(directory, "webapps/openejb.war")));
				}
			} catch (Exception e) {
				usingOpenEJB = false;
			}
		}
	}

	private void copyTemplateTo(File targetDir, String filename) throws Exception {
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new Log4JLogChute());
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
        Velocity.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        Template template = Velocity.getTemplate("/org/apache/openejb/tomee/configs/" + filename);
        VelocityContext context = new VelocityContext();
        context.put("tomcatHttpPort", Integer.toString(configuration.getHttpPort()));
        context.put("tomcatShutdownPort", Integer.toString(configuration.getStopPort()));
        context.put("tomcatShutdownCommand", configuration.getStopCommand());
        Writer writer = new FileWriter(new File(targetDir, filename));
        template.merge(context, writer);
        writer.flush();
        writer.close();		
	}

	private void copyFileTo(File targetDir, String filename) throws IOException {
		InputStream is = getClass().getResourceAsStream("/org/apache/openejb/tomee/configs/" + filename);
		FileOutputStream os = new FileOutputStream(new File(targetDir, filename));
		
		copyStream(is, os);
	}

	private void copyStream(InputStream is, FileOutputStream os)
			throws IOException {
		byte[] buffer = new byte[8192];
		int bytesRead = -1;
		
		while ((bytesRead = is.read(buffer)) > -1) {
			os.write(buffer, 0, bytesRead);
		}
		
		is.close();
		os.close();
	}

	private void createTomcatDirectories(File directory) {
		createDirectory(directory, "apps");
		createDirectory(directory, "conf");
		createDirectory(directory, "lib");
		createDirectory(directory, "logs");
		createDirectory(directory, "webapps");
		createDirectory(directory, "temp");
		createDirectory(directory, "work");
	}

	private void createDirectory(File parent, String directory) {
		new File(parent, directory).mkdirs();
	}
}
