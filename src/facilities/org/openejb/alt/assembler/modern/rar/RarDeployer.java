/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.alt.assembler.modern.rar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;

import javax.resource.spi.ManagedConnectionFactory;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.modern.AssemblerUtilities;
import org.openejb.alt.assembler.modern.DeployerService;
import org.openejb.alt.assembler.modern.GlobalContainerSystem;
import org.openejb.alt.assembler.modern.rar.jca10.ConnectorMetaData;

/**
 * Deploys J2EE Connector RARs into a running OpenEJB server.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class RarDeployer extends AssemblerUtilities implements DeployerService {
    private GlobalContainerSystem system;

    public RarDeployer(GlobalContainerSystem system) {
        this.system = system;
    }

    /**
     * Loads the metadata from the RAR (represented by a URL) and
     * deploys the connectors into OpenEJB.
     */
    public void deploy(String name, URL url) throws OpenEJBException {
        deploy(name, new URLClassLoader(new URL[]{url}));
    }
    /**
     * Loads the metadata from the RAR (represented by a ClassLoader) and
     * deploys the connectors into OpenEJB.
     */
    public void deploy(String name, ClassLoader loader) throws OpenEJBException {
    // 1: Make sure there's nothing already deployed with that name
        if(system.isRarDeployed(name)) {
            throw new OpenEJBException("There's already a RAR deployment with ID "+name);
        }
        system.deployRar(name, loader);

    // 2: Get the JCA 1.0 config file
        InputStream stream = loader.getResourceAsStream("META-INF/ra.xml");
        if(stream == null) {
            system.undeployRar(name);
            throw new OpenEJBException("Unable to deploy "+name+": no ra.xml found!");
        }

    // 3: Load the JCA 1.0 meta data
        ConnectorMetaData data = new ConnectorMetaData();
        try {
            data.loadXML(new InputStreamReader(stream));
        } catch(Exception e) {
            system.undeployRar(name);
            throw new OpenEJBException("Unable to load ra.xml", e);
        }
        try {
            stream.close();
        } catch(IOException e) {}

    // 4: Get the OpenEJB 1.0 config file
        stream = loader.getResourceAsStream("META-INF/openejb-ra.xml");
        if(stream == null) {
            system.undeployRar(name);
            throw new OpenEJBException("Unable to deploy "+name+": no openejb-ra.xml found!");
        }

    // 5: Load the OpenEJB 1.0 meta data
        JcaMetaData openData = new JcaMetaData(data);
        try {
            openData.loadXML(new InputStreamReader(stream));
        } catch(Exception e) {
            system.undeployRar(name);
            throw new OpenEJBException("Unable to load openejb-ra.xml", e);
        }
        try {
            stream.close();
        } catch(IOException e) {}

    // 6: Build and deploy the resource adapters
        build(openData, loader);
    }

    public void undeploy(String deploymentName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean isDeployed(String deploymentName) {
        return system.isJarDeployed(deploymentName);
    }

    /**
     * Validates that the provided J2EE module is acceptable for deployment.
     * If the Module ID has already been used, this may include checking
     * whether this is a legal replacement for the existing module.
     */
    public boolean validate(String moduleID, ClassLoader loader)
                   throws OpenEJBException {
        return true;
    }

    /**
     * Validates that the provided J2EE module is acceptable for deployment.
     * If the Module ID has already been used, this may include checking
     * whether this is a legal replacement for the existing module.
     */
    public boolean validate(String moduleID, URL url)
                   throws OpenEJBException {
        return validate(moduleID, new URLClassLoader(new URL[]{url}));
    }

    public String[] getModuleIDs() {
        return system.getDeployedJarNames();
    }

    public String[] getDeployedComponents(String deploymentName) {
        return system.getJarBeans(deploymentName);
    }

    /**
     * Given a ContainerSystem and a set of metadata, deploys the beans
     * into the server.
     */
    private void build(JcaMetaData data, ClassLoader loader) throws OpenEJBException {
        ManagedConnectionFactory[] factories = createRAFactories(data, loader);

        for(int i=0; i<factories.length; i++) {
            system.deployConnector(data.getDeployments()[i].getName(), factories[i]);
        }
    }
}
