package org.openejb.alt.assembler.dynamic;

import java.io.File;
import org.openejb.alt.config.ejb11.EjbJar;
import org.openejb.alt.config.ejb11.OpenejbJar;

/**
 * The information needed for the dynamic deployer to deploy an application.
 * Usually this is created and manipulated under the covers, but a server
 * could manually create it if it wants total control over assembling the
 * information OpenEJB requires to deploy the application (if, for example,
 * it didn't want to use the OpenEJB deployment descriptors).
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class DynamicDeploymentData {
    /**
     * The name of the application.  When deployed, this application will
     * replace any other application with the same name.
     */
    public String application;

    /**
     * The ClassLoader used to access the classes and resources for the
     * application.
     */
    public ClassLoader loader;

    /**
     * Each application may include one or more EJB JARs.  This is a set
     * of the standard EJB 1.1 deployment information for all the EJB JARs
     * in the application.  The length of this array must match the length
     * of the openejbDD array, and the entries must be in the same order.
     */
    public EjbJar[] standardDD;

    /**
     * Each application may include one or more EJB JARs.  This is a set
     * of the OpenEJB EJB 1.1 deployment information for all the EJB JARs
     * in the application.  The length of this array must match the length
     * of the standardDD array, and the entries must be in the same order.
     */
    public OpenejbJar[] openejbDD;

    /**
     * If the interfaces and client classes were loaded into a separate JAR and
     * added to the server CLASSPATH, this is the file where those are stored.
     * This is only used by the default deployer implementation -- a server
     * would need to implement those features on its own if it wants them.
     */
    File interfaceFile;
}
