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
package org.openejb.alt.assembler.modern.services;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.modern.AssemblerUtilities;
import org.openejb.alt.assembler.modern.DeployerService;

/**
 * The name says it all.  This service watches a directory.  Any time a new
 * JAR or RAR is copied to the directory, it tries to deploy it.  If a JAR
 * or RAR is updated, it tries to redeploy it.  If a JAR or RAR is removed,
 * it tries to undeploy it.  It does this by polling, so you have to wait for
 * it to notice, but the default interval is only 5 seconds.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class DirectoryWatchingHotDeployer implements Runnable {
    private final static int DEFAULT_SLEEP_TIME = 5000;
    private DeployerService jarDeployer;
    private DeployerService rarDeployer;
    private File directory;
    private Map deployments;
    private Thread runner;
    private int sleepTime = DEFAULT_SLEEP_TIME;

    public DirectoryWatchingHotDeployer() {
        deployments = new HashMap();
    }

    /**
     * Sets the directory to watch.  This takes effect the next time the
     * service polls the directory.
     * @throws java.lang.IllegalArgumentException
     *         Occurs when this argument is not a valid directory
     */
    public void setDirectory(File directory) {
        if(!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            throw new IllegalArgumentException("Bad directory "+directory);
        }
        this.directory = directory;
    }

    /**
     * Gets the directory that this service is watching.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Sets the interval (in ms) for polling the directory to check for new
     * files.
     */
    public void setSleepTime(int milliseconds) {
        sleepTime = milliseconds;
    }

    /**
     * Gets the interval (in ms) for polling the directory to check for new
     * files.
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * Starts watching the specified directory.
     * @throws java.lang.IllegalStateException
     *         Occurs when the service is already watching the directory, or
     *         when no directory has been specified.
     */
    public void start() {
        if(runner != null) {
            throw new IllegalStateException("Deployer thread is already running!");
        }
        if(directory == null) {
            throw new IllegalStateException("No directory set.");
        }
        runner = new Thread(this, "DirectoryDeployer");
        runner.setDaemon(true);
        runner.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        runner.start();
    }

    /**
     * Stops watching the specified directory.
     */
    public void stop() {
        directory = null;
        runner.interrupt();
    }

    /**
     * Implementation of Runnable that pauses and then looks for file activity.
     */
    public void run() {
        while(directory != null) {
            examineFiles();
            try {
                Thread.currentThread().sleep(sleepTime);
            } catch(InterruptedException e) {}
        }
        runner = null;
    }

    private void examineFiles() {
        // List the interesting files
        // Do all RARs first, since any given JAR may depend on any given RAR
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".rar");
            }
        });
        examineFiles(files, ".rar");
        // Do all JARs second, since any given JAR may depend on any given RAR
        files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jar");
            }
        });
        examineFiles(files, ".jar");
    }

    private void examineFiles(File[] files, String extension) {
        // Check for new or updated files
        for(int i=0; i<files.length; i++) {
            Long last = (Long)deployments.get(files[i].getName());
            if(last == null) {
                try {
                    deploy(files[i]);
                } catch(OpenEJBException e) {
                    System.err.println("Unable to deploy file: "+files[i]+": "+e.getMessage());
                }
            } else if(last.longValue() < files[i].lastModified()) {
                try {
                    redeploy(files[i]);
                } catch(OpenEJBException e) {
                    System.err.println("Unable to deploy file: "+files[i]+": "+e.getMessage());
                }
            }
        }

        // Check for removed files
        Iterator it = deployments.keySet().iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            if(name.toLowerCase().endsWith(extension)) {
                File test = new File(directory, name);
                if(!test.exists()) {
                    try {
                        undeploy(test);
                    } catch(OpenEJBException e) {
                        System.err.println("Unable to deploy file: "+test+": "+e.getMessage());
                    }
                    it.remove();
                }
            }
        }
    }

    private boolean deploy(File file) throws OpenEJBException {
        if(jarDeployer == null) {
            loadDeployers();
        }
        // Mark the file that we tried to deploy it.  Even if the deployment
        // fails, we shouldn't try again until the file has been changed.
        deployments.put(file.getName(), new Long(file.lastModified()));

        // Try to deploy the file
        try {
            if(file.getName().toLowerCase().endsWith(".jar")) {
                jarDeployer.deploy(file.getName(), new URLClassLoader(new URL[]{file.toURL()}, Thread.currentThread().getContextClassLoader()));
            } else if(file.getName().toLowerCase().endsWith(".rar")) {
                rarDeployer.deploy(file.getName(), AssemblerUtilities.getRarClassLoader(file));
            }
        } catch(IOException e) {
            throw new OpenEJBException("Unable to generate ClassLoader to deploy "+file);
        }

        return true;
    }

    private boolean undeploy(File file) throws OpenEJBException {
        if(jarDeployer == null) {
            loadDeployers();
        }
        System.out.println("Undeploying file "+file.getName());

        return true;
    }

    private boolean redeploy(File file) throws OpenEJBException {
        if(undeploy(file)) {
            return deploy(file);
        }
        return false;
    }

    private void loadDeployers() {
        try {
            Context ctx = OpenEJB.getJNDIContext();
            jarDeployer = (DeployerService)ctx.lookup("openejb/assembler/modern/JarDeployer");
            rarDeployer = (DeployerService)ctx.lookup("openejb/assembler/modern/RarDeployer");
        } catch(NamingException e) {
            e.printStackTrace();
        }
    }
}