package org.openejb.spi;

/**
 * Indicates a problem deploying an application.
 *
 * @version $Revision$
 */
public class DeploymentException extends Exception {
    public DeploymentException() {
    }

    public DeploymentException(String s) {
        super(s);
    }
}
