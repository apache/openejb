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

import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import java.lang.reflect.Method;
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.ExcludeListType;
import org.apache.geronimo.xbeans.j2ee.MethodPermissionType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityIdentityType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;

import org.openejb.SecureBuilder;
import org.openejb.security.SecurityConfiguration;
import org.openejb.xbeans.ejbjar.OpenejbDefaultPrincipalType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbPrincipalType;
import org.openejb.xbeans.ejbjar.OpenejbRealmType;
import org.openejb.xbeans.ejbjar.OpenejbRoleMappingsType;
import org.openejb.xbeans.ejbjar.OpenejbRoleType;
import org.openejb.xbeans.ejbjar.OpenejbSecurityType;


class SecurityBuilder {

    protected final OpenEJBModuleBuilder moduleBuilder;

    public SecurityBuilder(final OpenEJBModuleBuilder moduleBuilder) {
        super();
        this.moduleBuilder = moduleBuilder;
    }

    public Security buildSecurityConfig(OpenejbOpenejbJarType openejbEjbJar) {
        Security security = null;

        OpenejbSecurityType securityType = openejbEjbJar.getSecurity();
        if (securityType != null) {
            security = new Security();

            security.setDoAsCurrentCaller(securityType.getDoasCurrentCaller());
            security.setUseContextHandler(securityType.getUseContextHandler());
            security.setDefaultRole(securityType.getDefaultRole());

            OpenejbDefaultPrincipalType defaultPrincipalType = securityType.getDefaultPrincipal();
            DefaultPrincipal defaultPrincipal = new DefaultPrincipal();

            defaultPrincipal.setRealmName(defaultPrincipalType.getRealmName());
            defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));

            security.setDefaultPrincipal(defaultPrincipal);

            OpenejbRoleMappingsType roleMappingsType = securityType.getRoleMappings();
            if (roleMappingsType != null) {
                for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                    OpenejbRoleType roleType = roleMappingsType.getRoleArray(i);
                    Role role = new Role();

                    role.setRoleName(roleType.getRoleName());

                    for (int j = 0; j < roleType.sizeOfRealmArray(); j++) {
                        OpenejbRealmType realmType = roleType.getRealmArray(j);
                        Realm realm = new Realm();

                        realm.setRealmName(realmType.getRealmName());

                        for (int k = 0; k < realmType.sizeOfPrincipalArray(); k++) {
                            realm.getPrincipals().add(buildPrincipal(realmType.getPrincipalArray(k)));
                        }

                        role.getRealms().add(realm);
                    }

                    security.getRoleMappings().add(role);
                }
            }
        }

        return security;
    }

    private Principal buildPrincipal(OpenejbPrincipalType principalType) {
        Principal principal = new Principal();

        principal.setClassName(principalType.getClass1());
        principal.setPrincipalName(principalType.getName());
        principal.setDesignatedRunAs(principalType.isSetDesignatedRunAs());

        return principal;
    }

    /**
     * Fill the container builder with the security information that it needs
     * to create the proper interceptors.  A <code>SecurityConfiguration</code>
     * is also filled with permissions that need to be used to fill the JACC
     * policy configuration.
     *
     * @param builder            the container builder that is to be filled
     * @param notAssigned        the set of all possible permissions.  These will be
     *                           culled so that all that are left are those that have
     *                           not been assigned roles.
     * @param security           the OpenEJB security information already parsed
     *                           from XML descriptor into a POJO
     * @param assemblyDescriptor the assembly descriptor
     * @param EJBName            the name of the EJB
     * @param securityIdentity   the EJB's security identity
     * @param roleReferences     the EJB's role references
     * @throws DeploymentException if any constraints are violated
     */
    protected void fillContainerBuilderSecurity(SecureBuilder builder,
                                                Permissions notAssigned,
                                                Security security,
                                                AssemblyDescriptorType assemblyDescriptor,
                                                String EJBName,
                                                SecurityIdentityType securityIdentity,
                                                SecurityRoleRefType[] roleReferences)
            throws DeploymentException {

        if (security == null) return;

        SecurityConfiguration securityConfiguration = new SecurityConfiguration();

        securityConfiguration.setPolicyContextId(builder.getContainerId());
        builder.setSecurityEnabled(true);
        builder.setSecurityConfiguration(securityConfiguration);
        builder.setDoAsCurrentCaller(security.isDoAsCurrentCaller());
        builder.setUseContextHandler(security.isUseContextHandler());

        /**
         * Add the default subject
         */
        builder.setDefaultSubject(createDefaultSubject(security));

        /**
         * JACC v1.0 section 3.1.5.1
         */
        MethodPermissionType[] methodPermissions = assemblyDescriptor.getMethodPermissionArray();
        if (methodPermissions != null) {
            for (int i = 0; i < methodPermissions.length; i++) {
                MethodPermissionType mpt = methodPermissions[i];
                MethodType[] methods = mpt.getMethodArray();
                RoleNameType[] roles = mpt.getRoleNameArray();
                boolean unchecked = (mpt.getUnchecked() != null);

                Map rolePermissions = securityConfiguration.getRolePolicies();

                for (int k = 0; k < methods.length; k++) {
                    MethodType method = methods[k];

                    if (!EJBName.equals(method.getEjbName().getStringValue())) continue;

                    String methodName = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodName());
                    String methodIntf = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodIntf());
                    String[] methodPara = (method.getMethodParams() != null ? ConfigurationUtil.toStringArray(method.getMethodParams().getMethodParamArray()) : null);

                    // map EJB semantics to JACC semantics for method names
                    if ("*".equals(methodName)) methodName = null;

                    EJBMethodPermission permission = new EJBMethodPermission(EJBName, methodName, methodIntf, methodPara);
                    notAssigned = cullPermissions(notAssigned, permission);
                    if (unchecked) {
                        securityConfiguration.getUncheckedPolicy().add(permission);
                    } else {
                        for (int j = 0; j < roles.length; j++) {
                            String rolename = roles[j].getStringValue();

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

                if (!EJBName.equals(method.getEjbName().getStringValue())) continue;

                String methodName = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodName());
                String methodIntf = OpenEJBModuleBuilder.getJ2eeStringValue(method.getMethodIntf());
                String[] methodPara = (method.getMethodParams() != null ? ConfigurationUtil.toStringArray(method.getMethodParams().getMethodParamArray()) : null);

                EJBMethodPermission permission = new EJBMethodPermission(EJBName, methodName, methodIntf, methodPara);

                securityConfiguration.getExcludedPolicy().add(permission);
                notAssigned = cullPermissions(notAssigned, permission);
            }
        }

        /**
         * JACC v1.0 section 3.1.5.3
         */
        if (roleReferences != null) {
            for (int i = 0; i < roleReferences.length; i++) {
                if (roleReferences[i].getRoleLink() == null) throw new DeploymentException("Missing role-link");

                String roleName = roleReferences[i].getRoleName().getStringValue();
                String roleLink = roleReferences[i].getRoleLink().getStringValue();

                Map roleRefPermissions = securityConfiguration.getRoleReferences();
                Set roleLinks = (Set) roleRefPermissions.get(roleLink);
                if (roleLinks == null) {
                    roleLinks = new HashSet();
                    roleRefPermissions.put(roleLink, roleLinks);

                }
                roleLinks.add(new EJBRoleRefPermission(EJBName, roleName));
            }
        }

        /**
         * Set the security interceptor's run-as subject, if one has been defined.
         */
        boolean found = (securityIdentity == null || securityIdentity.getRunAs() == null);
        String runAsName = (!found ? securityIdentity.getRunAs().getRoleName().getStringValue() : "");
        Iterator rollMappings = security.getRoleMappings().iterator();
        while (rollMappings.hasNext()) {
            Role role = (Role) rollMappings.next();

            String roleName = role.getRoleName();
            Subject roleDesignate = new Subject();
            Set principalSet = new HashSet();

            Iterator realms = role.getRealms().iterator();
            while (realms.hasNext()) {
                Realm realm = (Realm) realms.next();

                Iterator principals = realm.getPrincipals().iterator();
                while (principals.hasNext()) {
                    Principal principal = (Principal) principals.next();

                    RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal, realm.getRealmName());

                    if (realmPrincipal == null) throw new DeploymentException("Unable to create realm principal");

                    principalSet.add(realmPrincipal);
                    if (principal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(realmPrincipal);
                }
            }
            securityConfiguration.getRoleMapping().put(roleName, principalSet);

            if (!found && roleDesignate.getPrincipals().size() > 0 && runAsName.equals(roleName)) {
                builder.setRunAs(roleDesignate);
                found = true;
            }
        }
        if (!found) throw new DeploymentException("Role designate not found for role: " + runAsName);


        /**
         * EJB v2.1 section 21.3.2
         * <p/>
         * It is possible that some methods are not assigned to any security
         * roles nor contained in the <code>exclude-list</code> element. In
         * this case, it is the responsibility of the Deployer to assign method
         * permissions for all of the unspecified methods, either by assigning
         * them to security roles, or by marking them as <code>unchecked</code>.
         */
        Permissions permissions;
        if (security.getDefaultRole() == null || security.getDefaultRole().length() == 0) {
            permissions = securityConfiguration.getUncheckedPolicy();
        } else {
            Map rolePermissions = securityConfiguration.getRolePolicies();
            permissions = (Permissions) rolePermissions.get(security.getDefaultRole());
            if (permissions == null) {
                permissions = new Permissions();
                rolePermissions.put(security.getDefaultRole(), permissions);
            }
        }

        Enumeration enum = notAssigned.elements();
        while (enum.hasMoreElements()) {
            Permission p = (Permission) enum.nextElement();
            permissions.add(p);
        }
    }

    private Subject createDefaultSubject(Security security) {

        Subject defaultSubject = new Subject();

        DefaultPrincipal principal = security.getDefaultPrincipal();

        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal.getPrincipal(), principal.getRealmName());
        if (realmPrincipal == null) throw new GeronimoSecurityException("Unable to create realm principal");
        PrimaryRealmPrincipal primaryRealmPrincipal = ConfigurationUtil.generatePrimaryRealmPrincipal(principal.getPrincipal(), principal.getRealmName());
        if (primaryRealmPrincipal == null) throw new GeronimoSecurityException("Unable to create primary realm principal");

        defaultSubject.getPrincipals().add(realmPrincipal);
        defaultSubject.getPrincipals().add(primaryRealmPrincipal);

        return defaultSubject;
    }

    /**
     * Gernate all the possible permissions for a bean's interface.
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
    protected void addToPermissions(Permissions permissions,
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

        Enumeration enum = toBeChecked.elements();
        while (enum.hasMoreElements()) {
            Permission test = (Permission) enum.nextElement();
            if (!permission.implies(test)) {
                result.add(test);
            }
        }

        return result;
    }
}