/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.deployment;

import java.lang.reflect.Method;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.ExcludeListType;
import org.apache.geronimo.xbeans.j2ee.JavaTypeType;
import org.apache.geronimo.xbeans.j2ee.MethodPermissionType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityIdentityType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;


class ContainerSecurityBuilder {

    public void setDetails(SecurityIdentityType securityIdentity, SecurityConfiguration securityConfiguration, String policyContextID, SecureBuilder builder) throws DeploymentException {
        builder.setSecurityEnabled(true);
        builder.setDoAsCurrentCaller(securityConfiguration.isDoAsCurrentCaller());
        builder.setUseContextHandler(securityConfiguration.isUseContextHandler());
        boolean needsRunAs = (securityIdentity != null && securityIdentity.isSetRunAs());
        if (needsRunAs) {
            String runAsName = securityIdentity.getRunAs().getRoleName().getStringValue().trim();
            Subject roleDesignate = (Subject) securityConfiguration.getRoleDesignates().get(runAsName);
            if (roleDesignate == null) {
                throw new DeploymentException("No role designate found for run-as name: " + runAsName);
            }
            builder.setRunAs(roleDesignate);
        }
        /**
         * Add the default subject
         */
        builder.setDefaultPrincipal(securityConfiguration.getDefaultPrincipal());
        builder.setPolicyContextID(policyContextID);
    }

    /**
     * Fill the container moduleBuilder with the security information that it needs
     * to create the proper interceptors.  A <code>SecurityConfiguration</code>
     * is also filled with permissions that need to be used to fill the JACC
     * policy configuration.
     *
     * @param notAssigned          the set of all possible permissions.  These will be
     *                             culled so that all that are left are those that have
     *                             not been assigned roles.
     * @param assemblyDescriptor   the assembly descriptor
     * @param EJBName              the name of the EJB
     * @param roleReferences       the EJB's role references
     * @param componentPermissions
     * @throws DeploymentException if any constraints are violated
     */
    public void addComponentPermissions(String defaultRole,
                                        Permissions notAssigned,
                                        AssemblyDescriptorType assemblyDescriptor,
                                        String EJBName,
                                        SecurityRoleRefType[] roleReferences,
                                        ComponentPermissions componentPermissions)
            throws DeploymentException {

        PermissionCollection uncheckedPermissions = componentPermissions.getUncheckedPermissions();
        PermissionCollection excludedPermissions = componentPermissions.getExcludedPermissions();
        Map rolePermissions = componentPermissions.getRolePermissions();

        //this can occur in an ear when one ejb module has security and one doesn't.  In this case we still need
        //to make the non-secure one completely unchecked.
        if (assemblyDescriptor != null) {
            /**
             * JACC v1.0 section 3.1.5.1
             */
            MethodPermissionType[] methodPermissions = assemblyDescriptor.getMethodPermissionArray();
            if (methodPermissions != null) {
                for (int i = 0; i < methodPermissions.length; i++) {
                    MethodPermissionType mpt = methodPermissions[i];
                    MethodType[] methods = mpt.getMethodArray();
                    RoleNameType[] roles = mpt.getRoleNameArray();
                    boolean unchecked = mpt.isSetUnchecked();


                    for (int k = 0; k < methods.length; k++) {
                        MethodType method = methods[k];

                        if (!EJBName.equals(method.getEjbName().getStringValue().trim())) continue;

                        String methodName = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodName());
                        String methodIntf = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodIntf());
                        String[] methodPara = (method.isSetMethodParams() ? toStringArray(method.getMethodParams().getMethodParamArray()) : null);

                        // map EJB semantics to JACC semantics for method names
                        if ("*".equals(methodName)) methodName = null;

                        EJBMethodPermission permission = new EJBMethodPermission(EJBName, methodName, methodIntf, methodPara);
                        notAssigned = cullPermissions(notAssigned, permission);
                        if (unchecked) {
                            uncheckedPermissions.add(permission);
                        } else {
                            for (int j = 0; j < roles.length; j++) {
                                String rolename = roles[j].getStringValue().trim();

                                Permissions permissions = (Permissions) rolePermissions.get(rolename);
                                if (permissions == null) {
                                    permissions = new Permissions();
                                    rolePermissions.put(rolename, permissions);
                                }
                                permissions.add(permission);
                            }
                        }
                    }

                }
            }

            /**
             * JACC v1.0 section 3.1.5.2
             */
            ExcludeListType excludeList = assemblyDescriptor.getExcludeList();
            if (excludeList != null) {
                MethodType[] methods = excludeList.getMethodArray();
                for (int i = 0; i < methods.length; i++) {
                    MethodType method = methods[i];

                    if (!EJBName.equals(method.getEjbName().getStringValue().trim())) continue;

                    String methodName = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodName());
                    String methodIntf = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodIntf());
                    String[] methodPara = (method.isSetMethodParams() ? toStringArray(method.getMethodParams().getMethodParamArray()) : null);

                    EJBMethodPermission permission = new EJBMethodPermission(EJBName, methodName, methodIntf, methodPara);

                    excludedPermissions.add(permission);
                    notAssigned = cullPermissions(notAssigned, permission);
                }
            }

            /**
             * JACC v1.0 section 3.1.5.3
             */
            if (roleReferences != null) {
                for (int i = 0; i < roleReferences.length; i++) {
                    if (!roleReferences[i].isSetRoleLink()) throw new DeploymentException("Missing role-link");

                    String roleName = roleReferences[i].getRoleName().getStringValue().trim();
                    String roleLink = roleReferences[i].getRoleLink().getStringValue().trim();

                    PermissionCollection roleLinks = (PermissionCollection) rolePermissions.get(roleLink);
                    if (roleLinks == null) {
                        roleLinks = new Permissions();
                        rolePermissions.put(roleLink, roleLinks);

                    }
                    roleLinks.add(new EJBRoleRefPermission(EJBName, roleName));
                }
            }
        }
        /**
         * EJB v2.1 section 21.3.2
         * <p/>
         * It is possible that some methods are not assigned to any security
         * roles nor contained in the <code>exclude-list</code> element. In
         * this case, it is the responsibility of the Deployer to assign method
         * permissions for all of the unspecified methods, either by assigning
         * them to security roles, or by marking them as <code>unchecked</code>.
         */
        PermissionCollection permissions;
        if (defaultRole == null) {
            permissions = uncheckedPermissions;
        } else {
            permissions = (PermissionCollection) rolePermissions.get(defaultRole);
            if (permissions == null) {
                permissions = new Permissions();
                rolePermissions.put(defaultRole, permissions);
            }
        }

        Enumeration e = notAssigned.elements();
        while (e.hasMoreElements()) {
            Permission p = (Permission) e.nextElement();
            permissions.add(p);
        }

    }

    /**
     * Generate all the possible permissions for a bean's interface.
     * <p/>
     * Method permissions are defined in the deployment descriptor as a binary
     * relation from the set of security roles to the set of methods of the
     * home, component, and/or web service endpoint interfaces of session and
     * entity beans, including all their superinterfaces (including the methods
     * of the <code>EJBHome</code> and <code>EJBObject</code> interfaces and/or
     * <code>EJBLocalHome</code> and <code>EJBLocalObject</code> interfaces).
     *
     * @param permissions     the permission set to be extended
     * @param EJBName         the name of the EJB
     * @param methodInterface the EJB method interface
     * @param interfaceClass  the class name of the interface to be used to
     *                        generate the permissions
     * @param cl              the class loader to be used in obtaining the interface class
     * @throws DeploymentException
     */
    public void addToPermissions(Permissions permissions,
                                 String EJBName, String methodInterface, String interfaceClass,
                                 ClassLoader cl)
            throws DeploymentException {

        if (interfaceClass == null) return;

        try {
            Class clazz = Class.forName(interfaceClass, false, cl);
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                permissions.add(new EJBMethodPermission(EJBName, methodInterface, methods[i]));
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(e);
        }

    }

    /**
     * Removes permissions from <code>toBeChecked</code> that are implied by
     * <code>permission</code>.
     *
     * @param toBeChecked the permissions that are to be checked and possibly
     *                    culled
     * @param permission  the permission that is to be used for culling
     * @return the culled set of permissions that are not implied by
     *         <code>permission</code>
     */
    private Permissions cullPermissions(Permissions toBeChecked, Permission permission) {
        Permissions result = new Permissions();

        Enumeration e = toBeChecked.elements();
        while (e.hasMoreElements()) {
            Permission test = (Permission) e.nextElement();
            if (!permission.implies(test)) {
                result.add(test);
            }
        }

        return result;
    }

    private static String[] toStringArray(JavaTypeType[] methodParamArray) {
        String[] result = new String[methodParamArray.length];
        for (int i = 0; i < methodParamArray.length; i++) {
            result[i] = methodParamArray[i].getStringValue();
        }
        return result;
    }

}