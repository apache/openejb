package org.apache.maven.plugins;

import org.codehaus.mojo.exec.AbstractExecMojo;

/**
 * This abstract mojo is used by all mojos Deploy/Undeploy/Start/Stop
 * 
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractOpenEJBMojo extends AbstractExecMojo {
    
    // ----- PARAMETERS -----
    /**
     * Target OpenEJB command to execute.
     * 
     * @parameter expression="${openejb.command}"
     */
    protected String command;
    
    /**
     * OpenEJB Home
     * 
     * @parameter expression="${openejb.home}" default-value="${basedir}"
     */
    protected String openejbHome;

    /**
     * OpenEJB Base
     * 
     * @parameter expression="${openejb.base}" default-value="${basedir}"
     */
    protected String openejbBase;
    
}
