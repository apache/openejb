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
package org.openejb.ui.jedi.openejb11.ejb;

import org.opentools.deployer.plugins.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Loads metadata from an XML file using DOM.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class XMLReader extends XMLUtils {
    private Document doc;
    private OpenEjbMetaData data;

    public XMLReader(OpenEjbMetaData data, Document doc) {
        this.data = data;
        this.doc = doc;
    }

    public void load() {
        Element root = doc.getDocumentElement();
        Element[] containers = getChildrenByName(root, "containers");
        if(containers.length > 0) {
            Element config[] = getChildrenByName(containers[0], "stateful-session-container");
            for(int i=0; i<config.length; i++)
                loadContainer(config[i], MetaDataContainer.STATEFUL_CONTAINER_TYPE);
            config = getChildrenByName(containers[0], "stateless-session-container");
            for(int i=0; i<config.length; i++)
                loadContainer(config[i], MetaDataContainer.STATELESS_CONTAINER_TYPE);
            config = getChildrenByName(containers[0], "entity-container");
            for(int i=0; i<config.length; i++)
                loadContainer(config[i], MetaDataContainer.ENTITY_CONTAINER_TYPE);
        }
        loadBeans(getChildByName(root, "enterprise-beans"));
        Element assembly = getChildByName(root, "assembly-descriptor");
        if(assembly != null) {
            Element roles[] = getChildrenByName(assembly, "security-role");
            for(int i=0; i<roles.length; i++) {
                loadSecurityRole(roles[i]);
            }
        }
    }

    private void loadBeans(Element root) {
        NodeList nl = root.getChildNodes();
        int max = nl.getLength();
        for(int i=0; i<max; i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                if(n.getNodeName().equals("entity"))
                    loadBean((Element)n, true);
                else if(n.getNodeName().equals("session"))
                    loadBean((Element)n, false);
            }
        }
    }

    private void loadBean(Element root, boolean entity) {
        MetaDataBean bean = new MetaDataBean();
        bean.setEntity(entity);
        bean.setEJBName(getChildText(root, "ejb-name"));
        bean.setContainerName(getChildText(root, "container-name"));
        Element[] list = getChildrenByName(root, "ejb-ref");
        for(int i=0; i<list.length; i++)
            bean.addEjbRef(loadEjbRef(list[i]));
        list = getChildrenByName(root, "resource-ref");
        for(int i=0; i<list.length; i++)
            bean.addResourceRef(loadResourceRef(list[i]));
        data.addEJB(bean);
    }

    private MetaDataEjbRef loadEjbRef(Element root) {
        MetaDataEjbRef ref = new MetaDataEjbRef();
        ref.setEJBRefName(getChildText(root, "ejb-ref-name"));
        Element child = getChildByName(root, "ejb-ref-location");
        String test = getChildText(child, "ejb-deployment-id");
        if(!test.equals("")) {
            ref.setOpenEjbName(test);
        } else {
            Element e = getChildByName(child, "connection");
            e = getChildByName(e, "properties");
            Element[] list = getChildrenByName(e, "property");
            for(int i=0; i<list.length; i++) {
                String name = getChildText(list[i], "property-name");
                String value = getChildText(list[i], "property-value");
                ref.addMapping(name, value);
            }
        }
        return ref;
    }

    private MetaDataResourceRef loadResourceRef(Element root) {
        MetaDataResourceRef ref = new MetaDataResourceRef();
        ref.setRefName(getChildText(root, "res-ref-name"));
        ref.setConnectorName(getChildText(root, "resource-deployment-id"));
        return ref;
    }

    private void loadContainer(Element root, byte type) {
        MetaDataContainer config = new MetaDataContainer();
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

    private void loadSecurityRole(Element root) {
        MetaDataSecurityRole role = new MetaDataSecurityRole();
        role.setEjbRoleName(getChildText(root, "role-name"));
        Element[] physicals = getChildrenByName(root, "physical-role-name");
        for(int i=0; i<physicals.length; i++) {
            role.addPhysicalRoleName(getText(physicals[i]));
        }
        data.addSecurityRole(role);
    }
}
