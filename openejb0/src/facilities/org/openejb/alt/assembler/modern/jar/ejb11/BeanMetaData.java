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
package org.openejb.alt.assembler.modern.jar.ejb11;

import java.util.ArrayList;

/**
 * Metadata for a standard EJB 1.1 bean.  This includes only the common
 * settings for both session and entity beans.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class BeanMetaData {
    private String description;
    private String displayName;
    private String smallIcon;
    private String largeIcon;
    private String ejbName;
    private String homeName;
    private String remoteName;
    private String implName;
    private ArrayList envVars = new ArrayList();
    private ArrayList ejbRefs = new ArrayList();
    private ArrayList roleRefs = new ArrayList();
    private ArrayList resourceRefs = new ArrayList();
    private EJB11MetaData parent;

    public BeanMetaData() {
    }

    public BeanMetaData(BeanMetaData source) {
        description = source.description;
        displayName = source.displayName;
        smallIcon = source.smallIcon;
        largeIcon = source.largeIcon;
        ejbName = source.ejbName;
        homeName = source.homeName;
        remoteName = source.remoteName;
        implName = source.implName;
        envVars.addAll(source.envVars);
        ejbRefs.addAll(source.ejbRefs);
        roleRefs.addAll(source.roleRefs);
        resourceRefs.addAll(source.resourceRefs);
    }

    void setParent(EJB11MetaData parent) {this.parent = parent;}
    public EJB11MetaData getParent() {return parent;}

    public void setDescription(String desc) {description = desc;}
    public String getDescription() {return description;}
    public void setDisplayName(String name) {displayName = name;}
    public String getDisplayName() {return displayName;}
    public void setSmallIcon(String icon) {smallIcon = icon;}
    public String getSmallIcon() {return smallIcon;}
    public void setLargeIcon(String icon) {largeIcon = icon;}
    public String getLargeIcon() {return largeIcon;}
    public void setEJBName(String name) {ejbName = name;}
    public String getEJBName() {return ejbName;}
    public void setHomeInterfaceName(String name) {homeName = name;}
    public String getHomeInterfaceName() {return homeName;}
    public void setRemoteInterfaceName(String name) {remoteName = name;}
    public String getRemoteInterfaceName() {return remoteName;}
    public void setBeanClassName(String name) {implName = name;}
    public String getBeanClassName() {return implName;}

    public void addEnvironmentVariable(EnvVariableMetaData var) {
        envVars.add(var);
    }
    public void removeEnvironmentVariable(EnvVariableMetaData var) {
        envVars.remove(var);
    }
    public EnvVariableMetaData[] getEnvironmentVariables() {
        return (EnvVariableMetaData[])envVars.toArray(new EnvVariableMetaData[envVars.size()]);
    }
    public void clearEnvironmentVariables() {
        envVars.clear();
    }

    public void addEjbRef(EjbRefMetaData ref) {
        ejbRefs.add(ref);
    }
    public void removeEjbRef(EjbRefMetaData ref) {
        ejbRefs.remove(ref);
    }
    public EjbRefMetaData[] getEjbRefs() {
        return (EjbRefMetaData[])ejbRefs.toArray(new EjbRefMetaData[ejbRefs.size()]);
    }
    public void clearEjbRefs() {
        ejbRefs.clear();
    }

    public void addSecurityRoleRef(RoleRefMetaData ref) {
        roleRefs.add(ref);
    }
    public void removeSecurityRoleRef(RoleRefMetaData ref) {
        roleRefs.remove(ref);
    }
    public RoleRefMetaData[] getSecurityRoleRefs() {
        return (RoleRefMetaData[])roleRefs.toArray(new RoleRefMetaData[roleRefs.size()]);
    }
    public void clearSecurityRoleRefs() {
        roleRefs.clear();
    }

    public void addResourceRef(ResourceRefMetaData ref) {
        resourceRefs.add(ref);
    }
    public void removeResourceRef(ResourceRefMetaData ref) {
        resourceRefs.remove(ref);
    }
    public ResourceRefMetaData[] getResourceRefs() {
        return (ResourceRefMetaData[])resourceRefs.toArray(new ResourceRefMetaData[resourceRefs.size()]);
    }
    public void clearResourceRefs() {
        resourceRefs.clear();
    }

    public String toString() {
        return displayName == null || displayName.length() == 0 ? ejbName : displayName;
    }
    public void dump() {
        System.out.println("EJB "+ejbName);
        System.out.println("     Desc: "+getDescription());
        System.out.println("  Display: "+getDisplayName());
        System.out.println("   SmallI: "+getSmallIcon());
        System.out.println("   LargeI: "+getLargeIcon());
        System.out.println("     Name: "+getEJBName());
        System.out.println("     Home: "+getHomeInterfaceName());
        System.out.println("   Remote: "+getRemoteInterfaceName());
        System.out.println("     Impl: "+getBeanClassName());
        System.out.print("     Envs:");
        for(int i=0; i<envVars.size(); System.out.print(" "+envVars.get(i++)));
        System.out.println();
        System.out.print("     EJBs:");
        for(int i=0; i<ejbRefs.size(); System.out.print(" "+ejbRefs.get(i++)));
        System.out.println();
        System.out.print("    Roles:");
        for(int i=0; i<roleRefs.size(); System.out.print(" "+roleRefs.get(i++)));
        System.out.println();
        System.out.print("     Ress:");
        for(int i=0; i<resourceRefs.size(); System.out.print(" "+resourceRefs.get(i++)));
        System.out.println();
    }
}
