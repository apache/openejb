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
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

import org.apache.openejb.arquillian.common.SimpleMavenBuilderImpl;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.config.RemoteServer;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.resolver.impl.maven.filter.StrictFilter;

public class RemoteTomEEContainer extends TomEEContainer {
	private static final String OPENEJB_VERSION = "4.0.0-beta-1-SNAPSHOT";

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
    	
    	File catalinaDirectory = new File(configuration.getDir());
    	catalinaDirectory.mkdirs();

    	String artifactName;
		if (configuration.isPlusContainer()) {
			artifactName = "org.apache.openejb:apache-tomee:zip:plus:" + OPENEJB_VERSION;
		} else {
			artifactName = "org.apache.openejb:apache-tomee:zip:webprofile:" + OPENEJB_VERSION;
		}
            
    	Collection<GenericArchive> archives = new SimpleMavenBuilderImpl()
            .artifact(artifactName)
            .resolveAs(GenericArchive.class, new StrictFilter());

    	GenericArchive archive = archives.iterator().next();
    	archive.as(ExplodedExporter.class).exportExploded(catalinaDirectory);
    	
    	File parent = new File(catalinaDirectory, archive.getName());
    	if ((!parent.exists()) || (!parent.isDirectory())) {
    		throw new LifecycleException("Unable to unpack TomEE zip file");
    	}
    	
    	File openejbHome = null;
    	
    	for (File directory : parent.listFiles()) {
    		if (".".equals(directory.getName()) || "..".equals(directory.getName())) continue;
    		
    		if (directory.isDirectory()) {
    			openejbHome = directory;
    			break;
    		}
    	}
    	
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
