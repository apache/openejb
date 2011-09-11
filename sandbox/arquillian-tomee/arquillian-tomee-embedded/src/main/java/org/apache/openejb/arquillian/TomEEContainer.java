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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.catalina.startup.Bootstrap;
import org.apache.openejb.AppContext;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.tomcat.catalina.TomcatLoader;
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
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import javax.naming.NamingException;

public class TomEEContainer extends Container implements DeployableContainer<TomEEConfiguration> {

    public Class<TomEEConfiguration> getConfigurationClass() {
        return TomEEConfiguration.class;
    }

    public void setup(TomEEConfiguration configuration) {
        setup((Configuration)configuration);
    }

    public void start() throws LifecycleException {
        try {
            startInternal();

        } catch (Exception e) {
            e.printStackTrace();
            throw new LifecycleException("Something went wrong", e);
        }
    }

    public void stop() throws LifecycleException {
        try {
            stopInternal();
        } catch (Exception e) {
            throw new LifecycleException("Unable to stop server", e);
        }
    }

    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
    	try {

            final File tempDir = FileUtils.createTempDir();
            final String name = archive.getName();
            final File file = new File(tempDir, name);
        	archive.as(ZipExporter.class).exportTo(file, true);


            deploy(name, file);

            HTTPContext httpContext = new HTTPContext("0.0.0.0", configuration.getHttpPort());
            return new ProtocolMetaData().addContext(httpContext);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
    	try {
            final String name = archive.getName();
            undeploy(name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to undeploy", e);
        }
    }

    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
