package org.openejb.alt.assembler.dynamic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import org.openejb.alt.config.ConfigurationFactory;
import org.openejb.alt.config.ConfigUtils;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.alt.assembler.classic.*;
import org.openejb.OpenEJBException;
import org.openejb.util.FileUtils;

/**
 * A configuration factory that doesn't try to load any EJBs.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class DynamicConfigurationFactory extends ConfigurationFactory {
    private File[] jarFiles;

    /**
     * Same as superclass, except that it doesn't try to process EJB JARs, and
     * it doesn't initialize any EJB-related container system settings.
     */
    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        // Load configuration
        // Validate Configuration
        openejb = ConfigUtils.readConfig(configLocation);

        // Resolve File Locations
        // Resolve Classes
        resolveDependencies(openejb);

        // Load deployments
        jarFiles = identifyDeployments(openejb);

        // Build the base OpenEjbConfiguration
        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();

        initJndiProviders(openejb, sys.facilities);
        initTransactionService(openejb, sys.facilities);
        initConnectors(openejb, sys.facilities);
        initConnectionManagers(openejb, sys.facilities);
        initProxyFactory(openejb, sys.facilities);

        // Fills the four container info arrays
        // in this class
        initContainerInfos(openejb);

        sys.containerSystem.containers = cntrs;
        sys.containerSystem.entityContainers = entyCntrs;
        sys.containerSystem.statefulContainers = stflCntrs;
        sys.containerSystem.statelessContainers = stlsCntrs;

        // Add the defaults
        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
        defaultRole.description = "The role applied to recurity references that are not linked.";
        defaultRole.roleName = DEFAULT_SECURITY_ROLE;
        sRoleInfos.add(defaultRole);

        initSecurityService(openejb, sys.facilities);

        return sys;
    }

    public File[] getDeployments() {
        return jarFiles;
    }

    public Map getContainers() {
        return containerTable;
    }

    private File[] identifyDeployments(Openejb openejb) throws OpenEJBException {
        String[] jarsToLoad = getJarLocations(openejb.getDeployments());
        File[] results = new File[jarsToLoad.length];
        for(int i = 0; i < jarsToLoad.length; i++) {
            try {
                results[i] = FileUtils.getBase().getFile(jarsToLoad[i], true);
            } catch(FileNotFoundException e) {
                throw new OpenEJBException("Cannot locate JAR to deploy '"+jarsToLoad[i]+"'");
            }
        }
        return results;
    }
}
