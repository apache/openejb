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
package org.openejb.alt.assembler.modern.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;

import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.modern.AssemblerUtilities;
import org.openejb.alt.assembler.modern.DeployerService;
import org.openejb.alt.assembler.modern.GlobalContainerSystem;
import org.openejb.alt.assembler.modern.jar.ejb11.BeanMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.EJB11MetaData;
import org.openejb.core.DeploymentInfo;

/**
 * Deploys EJB JARs into a running OpenEJB server.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class JarDeployer extends AssemblerUtilities implements DeployerService {
    private GlobalContainerSystem system;

    public JarDeployer(GlobalContainerSystem system) {
        this.system = system;
    }

    /**
     * Loads the metadata from the EJB JAR (represented by a URL) and
     * deploys the beans into OpenEJB.
     */
    public void deploy(String name, URL url) throws OpenEJBException {
        deploy(name, new URLClassLoader(new URL[]{url}));
    }

    /**
     * Loads the metadata from the EJB JAR (represented by a ClassLoader) and
     * deploys the beans into OpenEJB.
     */
    public void deploy(String name, ClassLoader loader) throws OpenEJBException {
    // 1: Make sure there's nothing already deployed with that name
        if(system.isJarDeployed(name)) {
            throw new OpenEJBException("There's already a deployment with ID "+name);
        }
        system.deployJar(name, loader);

    // 2: Get the EJB 1.1 config file
        InputStream stream = loader.getResourceAsStream("META-INF/ejb-jar.xml");
        if(stream == null) {
            system.undeployJar(name);
            throw new OpenEJBException("Unable to deploy "+name+": no ejb-jar.xml found!");
        }

    // 3: Load the EJB 1.1 meta data
        EJB11MetaData data = new EJB11MetaData();
        try {
            data.loadXML(new InputStreamReader(stream));
        } catch(Exception e) {
            system.undeployJar(name);
            throw new OpenEJBException("Unable to load ejb-jar.xml", e);
        }
        try {
            stream.close();
        } catch(IOException e) {}

    // 4: Get the OpenEJB 1.0 config file
        stream = loader.getResourceAsStream("META-INF/openejb-jar.xml");
        if(stream == null) {
            system.undeployJar(name);
            throw new OpenEJBException("Unable to deploy "+name+": no openejb-jar.xml found!");
        }

    // 5: Load the OpenEJB 1.0 meta data
        OpenEjbMetaData openData = new OpenEjbMetaData(data);
        try {
            openData.loadXML(data, new InputStreamReader(stream));
        } catch(Exception e) {
            system.undeployJar(name);
            throw new OpenEJBException("Unable to load openejb-jar.xml", e);
        }
        try {
            stream.close();
        } catch(IOException e) {}

    // 6: Build and deploy the EJBs
        build(name, openData, loader);
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
    private void build(String jarName, OpenEjbMetaData data, ClassLoader loader) throws OpenEJBException {
    // 1: Create containers defined in this JAR
        Container containers[] = createContainers(data.getContainers());
    // 2: Deploy beans
        BeanMetaData[] beans = data.getEJBs();
        DeploymentInfo[] deployments = createDeployments(system, containers, beans, data.getSecurityRoles(), loader);
    // 3: Apply security roles & method permissions
        createMethodPermissions(data.getMethodPermissions(), deployments, data.getSecurityRoles(), loader);
    // 4: Apply container transactions
        createContainerTransactions(data.getContainerTransactions(), deployments, loader);
    // 5: Update the container system's list of beans
        String[] beanNames = new String[deployments.length];
        for(int i=0; i<deployments.length; i++) {
            beanNames[i] = (String)deployments[i].getDeploymentID();
        }
        system.setJarBeans(jarName, beanNames);
    }
}
