package org.openejb;

import java.util.Set;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public interface ResourceEnvironmentBuilder {
    Set getUnshareableResources();

    void setUnshareableResources(Set unshareableResources);

    Set getApplicationManagedSecurityResources();

    void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources);
}
