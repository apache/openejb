package org.openejb.spi;

import java.net.URL;

/**
 * Knows how to deploy, undeploy, and redeploy applications.  The basic
 * features cover accepting an EJB JAR URL or a ClassLoader and
 * deploying that as a new application, as well as undeploying and
 * redeploying named applications.  Specific implementations may offer
 * additional deployment options.
 *
 * @version $Revision$
 */
public interface Deployer extends Service {
    /**
     * Makes a new EJB application available in OpenEJB.  The advantage to
     * providing a ClassLoader is that the actual EJB JAR storage mechanism
     * can be anything, any dependent classes can be made available, and the
     * server can arrange the ClassLoader hierarchy to suit.  When this
     * method returns, the application is available for clients to use.
     *
     * @param name    The name of the application, which must be unique.
     * @param ejbJars A ClassLoader from which the content of one or more
     *                EJB JARs is available.
     *
     * @throws DeploymentException Occurs when the application could not be deployed,
     *                             because an application with the same name is already
     *                             deployed, or due to a validation, configuration, or
     *                             deployment error.
     */
    public void deployApplication(String name, ClassLoader ejbJars) throws DeploymentException;

    /**
     * Makes a new EJB application available in OpenEJB.  If the specified JARs
     * are not file URLs, they will typically be downloaded and cached locally.
     * All EJB JARs passed together in one call to this method will be treated as
     * part of the same application.  The implementation will respect Manifest
     * Class-Path entries for the specified JARs if it is possible to construct
     * URLs to the referenced JARs by simply replacing the part of the URL after
     * the last slash with the referenced JAR names.  When this method returns,
     * the application is available for clients to use.
     *
     * @param name    The name of the application, which must be unique.
     * @param ejbJars URLs to one or more EJB JARs for this application.
     *
     * @throws DeploymentException Occurs when the application could not be deployed,
     *                             because an application with the same name is already
     *                             deployed, or due to a validation, configuration, or
     *                             deployment error.
     */
    public void deployApplication(String name, URL[] ejbJars) throws DeploymentException;

    /**
     * Gets the names of all applications which are curently deployed.
     */
    public String[] getDeployedApplications();

    /**
     * Removes an application from OpenEJB.  When this method returns, no
     * further client requests for the application will be handled.
     *
     * @param name The name of the application, as specified to the deploy
     *             method when it was originally deployed.
     *
     * @throws DeploymentException Occurs when no application with the
     *                             specified name is currently deployed.
     */
    public void undeployApplication(String name) throws DeploymentException;

    /**
     * Atomically redeploys an application.  No client requests for this
     * application should be rejected while the application is redeployed,
     * though they may be resolved against either the new application or
     * the old application until this method returns.  If this application
     * cannot be redeployed, the old application (if any) will continue
     * to be deployed.
     *
     * @param name    The name of the application, as specified to the deploy
     *                method when it was originally deployed.
     * @param ejbJars A ClassLoader from which the content of one or more
     *                EJB JARs is available.
     *
     * @throws DeploymentException Occurs when the application could not be redeployed,
     *                             because no application with the same name is
     *                             currently deployed, or due to a validation,
     *                             configuration, or deployment error.
     */
    public void redeployApplication(String name, ClassLoader ejbJars) throws DeploymentException;

    /**
     * Atomically redeploys an application.  No client requests for this
     * application should be rejected while the application is redeployed,
     * though they may be resolved against either the new application or
     * the old application until this method returns.  If this application
     * cannot be redeployed, the old application (if any) will continue
     * to be deployed.
     *
     * @param name    The name of the application, as specified to the deploy
     *                method when it was originally deployed.
     * @param ejbJars URLs to one or more EJB JARs for this application.
     *
     * @throws DeploymentException Occurs when the application could not be redeployed,
     *                             because no application with the same name is
     *                             currently deployed, or due to a validation,
     *                             configuration, or deployment error.
     */
    public void redeployApplication(String name, URL[] ejbJars) throws DeploymentException;

    /**
     * Called once the OpenEJB system is initialized, to indicate that
     * any pending deployments should be processed and any automatic
     * deployment processes can be initiated.  For some implementations,
     * this may do nothing.  OpenEJB will not finish starting until this
     * method returns, so it should not block or otherwise take too long.
     * If there are errors during initial deployments, they should be
     * logged instead of thrown as exceptions here.
     */
    public void startDeploying();
}
