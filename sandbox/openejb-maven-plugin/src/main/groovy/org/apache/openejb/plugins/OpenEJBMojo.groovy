package org.apache.openejb.plugins

import org.apache.maven.plugin.AbstractMojo

/**
 * Validate EJBs using OpenEJB validate features
 *
 * @goal validate
 */
public class ValidateMojo extends AbstractMojo
{
    void execute() {
        log.info('Go OpenEJB go!')
    }
}
