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
package org.apache.openejb.arquillian.remote;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

import org.apache.openejb.arquillian.common.MavenCache;
import org.apache.openejb.arquillian.common.SimpleMavenBuilderImpl;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.config.RemoteServer;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.resolver.impl.maven.filter.StrictFilter;
import org.sonatype.aether.artifact.Artifact;

public class RemoteTomEEContainer extends TomEEContainer {
	private RemoteServer container;
	private boolean needsStart = false;

    public void start() throws LifecycleException {
    	// see if TomEE is already running by checking the http port
    	try {
			connect(configuration.getHttpPort());
		} catch (Exception e) {
			needsStart = true;
		}
    	
    	if (! needsStart) {
    		return;
    	}
    	
    	File workingDirectory = new File(configuration.getDir());
    	workingDirectory.mkdirs();

    	File openejbHome = null;
    	
    	if (configuration.getTomcatVersion() == null || configuration.getTomcatVersion().length() == 0) {
        	downloadTomEE(workingDirectory);
        	openejbHome = findOpenEJBHome(workingDirectory);
    	} else {
    		downloadTomcat(workingDirectory, configuration.getTomcatVersion());
    		openejbHome = findOpenEJBHome(workingDirectory);
            File webappsOpenEJB = new File(openejbHome, "webapps/openejb");
            webappsOpenEJB.mkdirs();
            downloadOpenEJBWebapp(webappsOpenEJB);
    	}

    	// TODO: then we need to use the Installer to fix up the ports we want to use.
    	
    	if (openejbHome == null || (! openejbHome.exists())) {
    		throw new LifecycleException("Error finding OPENEJB_HOME");
    	}
    	
    	System.setProperty("tomee.http.port", String.valueOf(configuration.getHttpPort()));
    	System.setProperty("tomee.shutdown.port", String.valueOf(configuration.getStopPort()));
    	System.setProperty("java.naming.provider.url","http://localhost:" + configuration.getHttpPort() + "/openejb/ejb");
    	System.setProperty("connect.tries","90");
    	System.setProperty("server.http.port", String.valueOf(configuration.getHttpPort()));
    	System.setProperty("server.shutdown.port", String.valueOf(configuration.getStopPort()));
    	System.setProperty("java.opts", "-Xmx512m -Xms256m -XX:PermSize=64m -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=64m");
    	System.setProperty("openejb.home", openejbHome.getAbsolutePath());

    	container = new RemoteServer();
		container.start();
    }

	private File findOpenEJBHome(File directory) {
		File conf = new File(directory, "conf");
		File webapps = new File(directory, "webapps");
		
		if (conf.exists() && conf.isDirectory() && webapps.exists() && webapps.isDirectory()) {
			return directory;
		}
		
		for (File file : directory.listFiles()) {
			if (".".equals(file.getName()) || "..".equals(file.getName())) continue;
			
			File found = findOpenEJBHome(file);
			if (found != null) {
				return found;
			}
		}
		
		return null;
	}

	protected void downloadTomEE(File catalinaDirectory) throws LifecycleException {
		String artifactName;
		if (configuration.isPlusContainer()) {
			artifactName = "org.apache.openejb:apache-tomee:zip:plus:" + configuration.getOpenejbVersion();
		} else {
			artifactName = "org.apache.openejb:apache-tomee:zip:webprofile:" + configuration.getOpenejbVersion();
		}
            
        File zipFile = downloadFile(artifactName, null);
        ZipExtractor.unzip(zipFile, catalinaDirectory);
	}

    protected File downloadFile(String artifactName, String altUrl) {
        Artifact artifact = new MavenCache().getArtifact(artifactName, altUrl);
        return artifact.getFile();
    }

	protected void downloadOpenEJBWebapp(File targetDirectory) throws LifecycleException {
		String artifactName;
		if (configuration.isPlusContainer()) {
			artifactName = "org.apache.openejb:openejb-tomcat-plus-webapp:war:" + configuration.getOpenejbVersion();
		} else {
			artifactName = "org.apache.openejb:openejb-tomcat-webapp:war:" + configuration.getOpenejbVersion();
		}

        File zipFile = downloadFile(artifactName, null);
        ZipExtractor.unzip(zipFile, targetDirectory);
    }
	
	protected void downloadTomcat(File catalinaDirectory, String tomcatVersion) throws LifecycleException {
		String source = null;

		if (tomcatVersion.startsWith("7.")) {
			source = "http://archive.apache.org/dist/tomcat/tomcat-7/v"	+ tomcatVersion + "/bin/apache-tomcat-" + tomcatVersion	+ ".zip";
		}

		if (tomcatVersion.startsWith("6.")) {
			source = "http://archive.apache.org/dist/tomcat/tomcat-6/v"	+ tomcatVersion + "/bin/apache-tomcat-" + tomcatVersion	+ ".zip";
		}

		if (tomcatVersion.startsWith("5.5")) {
			source = "http://archive.apache.org/dist/tomcat/tomcat-5/v"	+ tomcatVersion + "/bin/apache-tomcat-" + tomcatVersion	+ ".zip";
		}

		if (source == null) {
			throw new LifecycleException("Unable to find URL for Tomcat " + tomcatVersion);
		}

        File zipFile = downloadFile("org.apache.openejb:tomcat:zip:" + tomcatVersion, source);
        ZipExtractor.unzip(zipFile, catalinaDirectory);
	}

    public void stop() throws LifecycleException {
    	// only stop the container if we started it
    	if (needsStart) {
    		container.stop();
    	}
    }
    
    public void connect(int port) throws Exception {
    	Socket socket = new Socket("localhost", port);
        OutputStream out = socket.getOutputStream();
        out.close();
    }



}
