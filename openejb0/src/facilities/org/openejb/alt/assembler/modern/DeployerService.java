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
package org.openejb.alt.assembler.modern;

import java.net.URL;

import org.openejb.OpenEJBException;

/**
 * A Deployer is an <b>optional feature</b> that an Assembler can provide
 * (via the assembled ContainerSystem) if it supports hot deployment
 * for J2EE modules like EJB JARs or J2EE Connector RARs.  If it uses
 * Deployers, it can provide a different one for each module type and
 * the client code should respect that.  You can deploy or undeploy
 * a J2EE module through this interface.  For example, you might set
 * up a process to watch a directory, and any time a new JAR file is
 * written to the directory, try to deploy it.  The code might look
 * something like this:
 * <pre>
 * private void foundNewJAR(File jar) {
 *     Deployer deployer = OpenEJB.getJarDeployer();
 *     if(deployer.isDeployed(jar.getName())) {
 *         deployer.undeploy(jar.getName());
 *     }
 *     deployer.deploy(jar.getName(), createClassLoader(jar));
 * }
 * </pre>
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public interface DeployerService {
    /**
     * Adds a J2EE module to the container.  By the time this method returns,
     * the container should accept requests for the new module.
     */
    public void deploy(String moduleID, ClassLoader loader) throws OpenEJBException;

    /**
     * Adds a J2EE module to the container.  By the time this method returns,
     * the container should accept requests for the new module.
     */
    public void deploy(String moduleID, URL url) throws OpenEJBException;

    /**
     * Remove a J2EE module from the container.  By the time this method
     * returns, the container should have stopped accepting requests for
     * the new module.  Requests currently in progress at the time of the
     * undeployment will be completed, though they may get errors if they
     * try to look up access additional components within the module after
     * this has been called.
     */
    public void undeploy(String moduleID) throws OpenEJBException;

    /**
     * Checks whether a J2EE module with the given name has been deployed
     * in the container.  If so, you must undeploy it before deploying
     * another one with the same name.
     */
    public boolean isDeployed(String moduleID);

    /**
     * Validates that the provided J2EE module is acceptable for deployment.
     * If the Module ID has already been used, this may include checking
     * whether this is a legal replacement for the existing module.
     */
    public boolean validate(String moduleID, ClassLoader loader)
                   throws OpenEJBException;

    /**
     * Validates that the provided J2EE module is acceptable for deployment.
     * If the Module ID has already been used, this may include checking
     * whether this is a legal replacement for the existing module.
     */
    public boolean validate(String moduleID, URL url)
                   throws OpenEJBException;

    /**
     * Gets a list of the J2EE modules that have been deployed.
     */
    public String[] getModuleIDs();

    /**
     * Gets a list of subcomponents that were deployed with a specific
     * J2EE module deployment.  This might be EJBs for an EJB JAR,
     * resource adapter deployments for an RAR, etc.
     */
    public String[] getDeployedComponents(String moduleID);
}