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

import org.openejb.alt.assembler.modern.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Loads a set of metadata classes from a DOM tree.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class XMLReader extends XMLUtils {
    private Document doc;
    private EJB11MetaData data;

    public XMLReader(Document doc, EJB11MetaData data) {
        this.doc = doc;
        this.data = data;
    }

    public void load() {
        Element root = doc.getDocumentElement();
        data.setDescription(getChildText(root, "description"));
        data.setDisplayName(getChildText(root, "display-name"));
        data.setSmallIcon(getChildText(root, "small-icon"));
        data.setLargeIcon(getChildText(root, "large-icon"));
        data.setClientJar(getChildText(root, "ejb-client-jar"));
        loadBeans(getChildByName(root, "enterprise-beans"));
        Element assembly = getChildByName(root, "assembly-descriptor");
        if(assembly != null) {
            loadSecurityRoles(getChildrenByName(assembly, "security-role"));
            loadMethodPermissions(getChildrenByName(assembly, "method-permission"));
            loadContainerTransactions(getChildrenByName(assembly, "container-transaction"));
        }
    }

    private void loadSecurityRoles(Element[] roots) {
        for(int i=0; i<roots.length; i++)
            loadSecurityRole(roots[i]);
    }

    private void loadSecurityRole(Element root) {
        SecurityRoleMetaData role = new SecurityRoleMetaData();
        role.setDescription(getChildText(root, "description"));
        role.setRoleName(getChildText(root, "role-name"));
        data.addSecurityRole(role);
    }

    private void loadMethodPermissions(Element[] roots) {
        for(int i=0; i<roots.length; i++)
            loadMethodPermission(roots[i]);
    }

    private void loadMethodPermission(Element root) {
        MethodPermissionMetaData perm = new MethodPermissionMetaData();
        perm.setDescription(getChildText(root, "description"));
        Element[] roles = getChildrenByName(root, "role-name");
        for(int i=0; i<roles.length; i++)
            perm.addRole(getText(roles[i]));
        Element[] methods = getChildrenByName(root, "method");
        for(int i=0; i<methods.length; i++)
            perm.addMethod(getMethod(methods[i]));
        data.addMethodPermission(perm);
    }

    private void loadContainerTransactions(Element[] roots) {
        for(int i=0; i<roots.length; i++)
            loadContainerTransaction(roots[i]);
    }

    private void loadContainerTransaction(Element root) {
        ContainerTransactionMetaData trans = new ContainerTransactionMetaData();
        trans.setDescription(getChildText(root, "description"));
        trans.setTransactionAttribute(getChildText(root, "trans-attribute"));
        Element[] methods = getChildrenByName(root, "method");
        for(int i=0; i<methods.length; i++)
            trans.addMethod(getMethod(methods[i]));
        data.addContainerTransaction(trans);
    }

    private MethodMetaData getMethod(Element root) {
        MethodMetaData method = new MethodMetaData();
        method.setDescription(getChildText(root, "description"));
        method.setEJBName(getChildText(root, "ejb-name"));
        String intf = getChildText(root, "method-intf");
        method.setInterfaceRemote(intf == null || intf.length() == 0 ? null : new Boolean(intf.equals("Remote")));
        method.setMethodName(getChildText(root, "method-name"));
        Element params = getChildByName(root, "method-params");
        if(params != null) {
            Element[] list = getChildrenByName(params, "method-param");
            for(int i=0; i<list.length; i++)
                method.addArgument(getText(list[i]));
        }

        return method;
    }

    private void loadBeans(Element root) {
        NodeList nl = root.getChildNodes();
        int max = nl.getLength();
        for(int i=0; i<max; i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                if(n.getNodeName().equals("entity"))
                    loadEntityBean((Element)n);
                else if(n.getNodeName().equals("session"))
                    loadSessionBean((Element)n);
            }
        }
    }

    private void loadEntityBean(Element root) {
        EntityBeanMetaData bean = new EntityBeanMetaData();
        loadEnterpriseBean(root, bean);
        bean.setCMP(getChildText(root, "persistence-type").equalsIgnoreCase("Container"));
        bean.setPrimaryKeyClassName(getChildText(root, "prim-key-class"));
        bean.setPrimaryKeyFieldName(getChildText(root, "primkey-field"));
        bean.setReentrant(getChildText(root, "reentrant").equalsIgnoreCase("True"));
        Element[] list = getChildrenByName(root, "cmp-field");
        for(int i=0; i<list.length; i++)
            bean.addCMPField(loadCMPField(list[i]));

        data.addEJB(bean);
    }

    private void loadSessionBean(Element root) {
        SessionBeanMetaData bean = new SessionBeanMetaData();
        loadEnterpriseBean(root, bean);
        bean.setContainerTransaction(getChildText(root, "transaction-type").equalsIgnoreCase("Container"));
        bean.setStatefulSession(getChildText(root, "session-type").equalsIgnoreCase("Stateful"));

        data.addEJB(bean);
    }

    private void loadEnterpriseBean(Element root, BeanMetaData bean) {
        bean.setDescription(getChildText(root, "description"));
        bean.setDisplayName(getChildText(root, "display-name"));
        bean.setSmallIcon(getChildText(root, "small-icon"));
        bean.setLargeIcon(getChildText(root, "large-icon"));
        bean.setEJBName(getChildText(root, "ejb-name"));
        bean.setHomeInterfaceName(getChildText(root, "home"));
        bean.setRemoteInterfaceName(getChildText(root, "remote"));
        bean.setBeanClassName(getChildText(root, "ejb-class"));
        Element[] list = getChildrenByName(root, "env-entry");
        for(int i=0; i<list.length; i++)
            bean.addEnvironmentVariable(loadEnvVariable(list[i]));
        list = getChildrenByName(root, "ejb-ref");
        for(int i=0; i<list.length; i++)
            bean.addEjbRef(loadEjbRef(list[i]));
        list = getChildrenByName(root, "security-role-ref");
        for(int i=0; i<list.length; i++)
            bean.addSecurityRoleRef(loadRoleRef(list[i]));
        list = getChildrenByName(root, "resource-ref");
        for(int i=0; i<list.length; i++)
            bean.addResourceRef(loadResourceRef(list[i]));
    }

    private EnvVariableMetaData loadEnvVariable(Element root) {
        EnvVariableMetaData var = new EnvVariableMetaData();
        var.setDescription(getChildText(root, "description"));
        var.setName(getChildText(root, "env-entry-name"));
        var.setType(getChildText(root, "env-entry-type"));
        var.setValue(getChildText(root, "env-entry-value"));
        return var;
    }

    private CMPFieldMetaData loadCMPField(Element root) {
        CMPFieldMetaData field = new CMPFieldMetaData();
        field.setDescription(getChildText(root, "description"));
        field.setName(getChildText(root, "field-name"));
        return field;
    }

    private EjbRefMetaData loadEjbRef(Element root) {
        EjbRefMetaData ref = new EjbRefMetaData();
        ref.setDescription(getChildText(root, "description"));
        ref.setRefName(getChildText(root, "ejb-ref-name"));
        ref.setRefTypeSession(getChildText(root, "ejb-ref-type").equalsIgnoreCase("Session"));
        ref.setHomeInterfaceName(getChildText(root, "home"));
        ref.setRemoteInterfaceName(getChildText(root, "remote"));
        ref.setEjbLink(getChildText(root, "ejb-link"));
        return ref;
    }

    private RoleRefMetaData loadRoleRef(Element root) {
        RoleRefMetaData ref = new RoleRefMetaData();
        ref.setDescription(getChildText(root, "description"));
        ref.setRoleName(getChildText(root, "role-name"));
        ref.setRoleLink(getChildText(root, "role-link"));
        return ref;
    }

    private ResourceRefMetaData loadResourceRef(Element root) {
        ResourceRefMetaData ref = new ResourceRefMetaData();
        ref.setDescription(getChildText(root, "description"));
        ref.setRefName(getChildText(root, "res-ref-name"));
        ref.setResourceType(getChildText(root, "res-type"));
        ref.setAuthContainer(getChildText(root, "res-auth").equalsIgnoreCase("Container"));
        return ref;
    }
}
