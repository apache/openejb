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
package org.openejb.alt.assembler.modern.jar;

import org.openejb.alt.assembler.modern.ContainerMetaData;
import org.openejb.alt.assembler.modern.LoadException;
import org.openejb.alt.assembler.modern.jar.ejb11.BeanMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.EJB11MetaData;
import org.openejb.alt.assembler.modern.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates metadata objects from a DOM tree.  You first load the standard
 * EJB 1.1 metadata, and then give that to this class before it loads the
 * OpenEJB extensions.  In that manner, it can load a set of OpenEJB
 * metadata classes that includes the infomation declared in both places.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class XMLReader extends XMLUtils {
    private Document doc;
    private OpenEjbMetaData data;
    private EJB11MetaData source;

    public XMLReader(OpenEjbMetaData data, EJB11MetaData source, Document doc) {
        this.data = data;
        this.doc = doc;
        this.source = source;
    }

    public void load() throws LoadException {
        Element root = doc.getDocumentElement();
        Element[] containers = getChildrenByName(root, "containers");
        if(containers.length > 0) {
            Element config[] = getChildrenByName(containers[0], "stateful-session-container");
            for(int i=0; i<config.length; i++)
                loadContainer(config[i], ContainerMetaData.STATEFUL_CONTAINER_TYPE);
            config = getChildrenByName(containers[0], "stateless-session-container");
            for(int i=0; i<config.length; i++)
                loadContainer(config[i], ContainerMetaData.STATELESS_CONTAINER_TYPE);
            config = getChildrenByName(containers[0], "entity-container");
            for(int i=0; i<config.length; i++)
                loadContainer(config[i], ContainerMetaData.ENTITY_CONTAINER_TYPE);
        }
        Element beans = getChildByName(root, "enterprise-beans");
        loadBeans(beans);
        Element assembly = getChildByName(root, "assembly-descriptor");
        if(assembly != null) {
            Element[] roles = getChildrenByName(assembly, "security-role");
            for(int i=0; i<roles.length; i++) {
                loadSecurityRole(roles[i]);
            }
        }
        checkExtraSecurityRoles();
    }

    private void loadBeans(Element root) throws LoadException {
        NodeList nl = root.getChildNodes();
        int max = nl.getLength();
        for(int i=0; i<max; i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                if(n.getNodeName().equals("entity")) {
                    loadBean((Element)n, true);
                } else if(n.getNodeName().equals("session")) {
                    loadBean((Element)n, false);
                }
            }
        }
        BeanMetaData extras[] = source.getEJBs();
        for(int i=0; i<extras.length; i++) {
            throw new LoadException("Bean '"+extras[i].getEJBName()+"' wasn't configured for OpenEJB.");
        }
    }

    private void loadBean(Element root, boolean entity) throws LoadException {
        String name = getChildText(root, "ejb-name");
        BeanMetaData bean;
        BeanMetaData sourceBean = getBean(source.getEJBs(), name);
        if(sourceBean == null) {
            throw new LoadException("Unmatched bean in openejb-jar.xml: '"+name+"'");
        } else {
            source.removeEJB(sourceBean);
        }
        String container = getChildText(root, "container-name");
        if(entity) {
            bean = new EntityBeanMetaData(sourceBean);
            ((EntityBeanMetaData)bean).setContainerName(container);
        } else {
            bean = new SessionBeanMetaData(sourceBean);
            ((SessionBeanMetaData)bean).setContainerName(container);
        }

        Element[] list = getChildrenByName(root, "ejb-ref");
        for(int i=0; i<list.length; i++)
            bean.addEjbRef(loadEjbRef(sourceBean, list[i]));
        org.openejb.alt.assembler.modern.jar.ejb11.EjbRefMetaData extras[] = sourceBean.getEjbRefs();
        for(int i=0; i<extras.length; i++) {
            bean.addEjbRef(new EjbRefMetaData(extras[i]));
System.out.println("EJB ref '"+extras[i].getRefName()+"' not matched in openejb-jar.xml");
        }


        list = getChildrenByName(root, "resource-ref");
        for(int i=0; i<list.length; i++)
            bean.addResourceRef(loadResourceRef(sourceBean, list[i]));
        org.openejb.alt.assembler.modern.jar.ejb11.ResourceRefMetaData extraRes[] = sourceBean.getResourceRefs();
        for(int i=0; i<extraRes.length; i++) {
            bean.addResourceRef(new ResourceRefMetaData(extraRes[i]));
System.out.println("Resource ref '"+extraRes[i].getRefName()+"' not matched in openejb-jar.xml");
        }

        data.addEJB(bean);
    }

    private static BeanMetaData getBean(BeanMetaData[] list, String name) {
        for(int i=0; i<list.length; i++)
            if(list[i].getEJBName().equals(name))
                return list[i];
        return null;
    }
    private static org.openejb.alt.assembler.modern.jar.ejb11.EjbRefMetaData getEjbRef(org.openejb.alt.assembler.modern.jar.ejb11.EjbRefMetaData[] list, String name) {
        for(int i=0; i<list.length; i++)
            if(list[i].getRefName().equals(name))
                return list[i];
        return null;
    }
    private static org.openejb.alt.assembler.modern.jar.ejb11.ResourceRefMetaData getResourceRef(org.openejb.alt.assembler.modern.jar.ejb11.ResourceRefMetaData[] list, String name) {
        for(int i=0; i<list.length; i++)
            if(list[i].getRefName().equals(name))
                return list[i];
        return null;
    }
    private static org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData getSecurityRole(org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData[] list, String name) {
        for(int i=0; i<list.length; i++)
            if(list[i].getRoleName().equals(name))
                return list[i];
        return null;
    }

    private EjbRefMetaData loadEjbRef(BeanMetaData sourceBean, Element root) throws LoadException {
        String name = getChildText(root, "ejb-ref-name");
        org.openejb.alt.assembler.modern.jar.ejb11.EjbRefMetaData sourceRef = getEjbRef(sourceBean.getEjbRefs(), name);
        if(sourceRef == null) {
            throw new LoadException("Unmatched EJB-REF for bean "+sourceBean.getEJBName()+" in openejb-jar.xml: '"+name+"'");
        } else {
            sourceBean.removeEjbRef(sourceRef);
        }
        EjbRefMetaData ref = new EjbRefMetaData(sourceRef);
        Element e = getChildByName(root, "properties");
        Element[] list = getChildrenByName(e, "property");
        for(int i=0; i<list.length; i++) {
            String pname = getChildText(list[i], "property-name");
            String pvalue = getChildText(list[i], "property-value");
            ref.addMapping(pname, pvalue);
        }
        return ref;
    }

    private ResourceRefMetaData loadResourceRef(BeanMetaData sourceBean, Element root) throws LoadException {
        String name = getChildText(root, "res-ref-name");
        org.openejb.alt.assembler.modern.jar.ejb11.ResourceRefMetaData sourceRef = getResourceRef(sourceBean.getResourceRefs(), name);
        if(sourceRef == null) {
            throw new LoadException("Unmatched RESOURCE-REF for bean "+sourceBean.getEJBName()+" in openejb-jar.xml: '"+name+"'");
        } else {
            sourceBean.removeResourceRef(sourceRef);
        }
        ResourceRefMetaData ref = new ResourceRefMetaData(sourceRef);
        ref.setConnectorName(getChildText(root, "resource-deployment-id"));
        return ref;
    }

    private void loadSecurityRole(Element root) throws LoadException {
        String name = getChildText(root, "role-name");
        org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData sourceRole = getSecurityRole(source.getSecurityRoles(), name);
        if(sourceRole == null) {
            throw new LoadException("Unmatched SECURITY ROLE in openejb-jar.xml: '"+name+"'");
        } else {
            source.removeSecurityRole(sourceRole);
        }
        SecurityRoleMetaData role = new SecurityRoleMetaData(sourceRole);
        Element physicals[] = getChildrenByName(root, "physical-role-name");
        for(int i=0; i<physicals.length; i++) {
            role.addPhysicalRoleName(getText(physicals[i]));
        }
        data.addSecurityRole(role);
    }

    private void checkExtraSecurityRoles() {
        org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData[] roles = source.getSecurityRoles();
        for(int i=0; i<roles.length; i++) {
            SecurityRoleMetaData role = new SecurityRoleMetaData(roles[i]);
            role.addPhysicalRoleName(role.getRoleName());
            data.addSecurityRole(role);
        }
    }

    private void loadContainer(Element root, byte type) {
        ContainerMetaData config = new ContainerMetaData();
        config.setType(type);
        config.setName(getChildText(root, "container-name"));
        config.setDescription(getChildText(root, "description"));
        config.setDisplayName(getChildText(root, "display-name"));
        Element props = getChildByName(root, "properties");
        if(props != null) {
            Element[] list = getChildrenByName(props, "property");
            for(int i=0; i<list.length; i++) {
                String name = getChildText(list[i], "property-name");
                String value = getChildText(list[i], "property-value");
                config.addMapping(name, value);
            }
        }
        data.addContainer(config);
    }
}
