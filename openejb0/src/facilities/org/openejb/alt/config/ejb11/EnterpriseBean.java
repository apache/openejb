package org.openejb.alt.config.ejb11;

/**
 * Common tags between session beans and entity beans.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface EnterpriseBean {
    public String getId();
    public String getDisplayName();
    public String getDescription();
    public String getSmallIcon();
    public String getLargeIcon();
    public String getEjbName();
    public String getHome();
    public String getRemote();
    public String getEjbClass();
    public EnvEntry[] getEnvEntry();
    public EjbRef[] getEjbRef();
    public SecurityRoleRef[] getSecurityRoleRef();
    public ResourceRef[] getResourceRef();
}
