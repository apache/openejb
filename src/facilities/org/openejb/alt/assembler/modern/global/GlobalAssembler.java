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
package org.openejb.alt.assembler.modern.global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.transaction.TransactionManager;

import org.openejb.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.modern.AssemblerUtilities;
import org.openejb.alt.assembler.modern.DeployerService;
import org.openejb.alt.assembler.modern.GlobalContainerSystem;
import org.openejb.spi.Assembler;
import org.openejb.spi.SecurityService;
import org.openejb.util.SafeProperties;

/**
 * Assembler implementation that handles global OpenEJB configuration.  It
 * loads the global OpenEJB configuration metadata and creates and
 * initializes the OpenEJB data structures accordingly.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */

// FIXME: use a real logger instead of System.out for this class?
public class GlobalAssembler extends AssemblerUtilities implements Assembler {
    private OpenEjbMetaData data;
    private GlobalContainerSystem system;
    private SecurityService security;
    private TransactionManager transaction;
    private String jarDir;
    private String rarDir;

    public GlobalAssembler() {
    }

    /**
     * Loads the global OpenEJB metadata.
     */
    public void init(Properties props) throws OpenEJBException {
        SafeProperties safeProps = toolkit.getSafeProperties(props);
        String configXml = safeProps.getProperty(EnvProps.CONFIGURATION);
        File configFile = new File(configXml);
        try {
            Reader reader = new BufferedReader(new FileReader(configFile));
            data = new OpenEjbMetaData();
            String[] msgs = data.loadXML(reader);
            for(int i=0; i<msgs.length; i++) {
                System.out.println(msgs[i]);
            }
        } catch(Exception e) {
            throw new OpenEJBException(e);
        }
        rarDir = safeProps.getProperty(EnvProps.DEPLOYED_RAR_DIRECTORY, null);
        if(rarDir == null || rarDir.equals("")) {
            rarDir = null;
            System.out.println("WARNING: Not loading any RARs at startup.");
        }
        jarDir = safeProps.getProperty(EnvProps.DEPLOYED_JAR_DIRECTORY, null);
        if(jarDir == null || jarDir.equals("")) {
            jarDir = null;
            System.out.println("WARNING: Not loading any EJB JARs at startup.");
        }
    }

    /**
     * Applies the metadata loaded by init().
     */
    public void build() throws OpenEJBException {
        buildContainerSystem();
        if(rarDir != null) {
            deployRARs();
        }
        if(jarDir != null) {
            deployJARs();
        }
    }

    /**
     * Identifies the RARs in the RAR deployment directory, and loads them.
     */
    private void deployRARs() throws OpenEJBException {
        DeployerService rarDeployer = system.getRarDeployer();
        File dir = new File(rarDir);
        if(!dir.exists()) {
            throw new OpenEJBException("RAR deploy directory '"+rarDir+"' not found!");
        }
        if(!dir.isDirectory()) {
            throw new OpenEJBException("RAR deploy directory '"+rarDir+"' not a directory!");
        }

        File[] rars = dir.listFiles(new FilenameFilter() {
            public boolean accept(File parent, String name) {
                return name.toLowerCase().endsWith(".rar");
            }
        });
        if(rars.length > 0) {
            for(int i=0; i<rars.length; i++) {
                // FIXME: pay attention to the manifest classpaths in the RARs
                try {
                    System.out.println("Attempting to deploy "+rars[i].getName()+"...");
                    rarDeployer.deploy(rars[i].getName(), getRarClassLoader(rars[i]));
                } catch(MalformedURLException e) {
                    System.out.println("Unable to deploy RAR '"+rars[i]+"': "+e);
                } catch(IOException e) {
                    System.out.println("Unable to read RAR '"+rars[i]+"': "+e);
                } catch(OpenEJBException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Identifies the EJB JARs in the JAR deployment directory, and loads them.
     */
    private void deployJARs() throws OpenEJBException {
        DeployerService jarDeployer = system.getJarDeployer();
        File dir = new File(jarDir);
        if(!dir.exists()) {
            throw new OpenEJBException("JAR deploy directory '"+jarDir+"' not found!");
        }
        if(!dir.isDirectory()) {
            throw new OpenEJBException("JAR deploy directory '"+jarDir+"' not a directory!");
        }

        File[] jars = dir.listFiles(new FilenameFilter() {
            public boolean accept(File parent, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });
        if(jars.length > 0) {
            for(int i=0; i<jars.length; i++) {
                // FIXME: pay attention to the manifest classpaths in the EJB JARs
                // FIXME: don't use the URLClassLoader as it locks & caches files
                //        which disallows redeploy
                try {
                    System.out.println("Attempting to deploy "+jars[i].getName()+"...");
                    ClassLoader cl = new URLClassLoader(new URL[]{jars[i].toURL()});
                    jarDeployer.deploy(jars[i].getName(), cl);
                } catch(MalformedURLException e) {
                    System.out.println("Unable to deploy JAR '"+jars[i]+"': "+e);
                } catch(OpenEJBException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public org.openejb.spi.ContainerSystem getContainerSystem() {
        return system;
    }
    public TransactionManager getTransactionManager() {
        return transaction;
    }
    public SecurityService getSecurityService() {
        return security;
    }

    /**
     * Actually does the work of initializing OpenEJB.
     */
    private void buildContainerSystem() throws OpenEJBException {
    // 0: Create the container system
        system = new GlobalContainerSystem();
    // 1: Initialize the proxy factory, which will be used by everything else
        createProxyFactory(data.getServerData());
    // 2: Create the containers
        createContainers(system, data.getContainers());
    // 3: Create the security service
        security = createSecurityService(data.getSecurityData());
    // 4: Create the transaction service
        transaction = createTransactionService(data.getTransactionData());
    // 5: Create the ConnectionManagers
        system.setConnectionManagers(createConnectionManagers(data.getConnectionManagers()));
    // 6: List the connectors we want to deploy
        ConnectorMetaData[] cons = data.getConnectors();
        for(int i=0; i<cons.length; i++) {
            system.addDesiredConnector(cons[i].getName(),
                                       cons[i].getConnectionManagerName(),
                                       cons[i].asProperties(cons[i].getMap()));
        }
    }
}
