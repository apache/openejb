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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.opentools.deployer.plugins.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves data to an XML file using DOM.  The actual mechanism used to save
 * is parser-dependent.  The JEDI framework currently autodetects Xerces
 * and Sun's parser that ships with JAXP.  That bit is handled in the
 * OpenEjbMetaData class - this class just populates the DOM.
 *
 * @see org.openejb.ui.jedi.openejb11.ejb.OpenEjbMetaData
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class XMLWriter extends XMLUtils {
    private Document doc;
    private OpenEjbMetaData data;
    private LinkedList warnings;

    public XMLWriter(Document doc, OpenEjbMetaData data) {
        this.doc = doc;
        this.data = data;
        warnings = new LinkedList();
    }

    public String[] getWarnings() {
        return (String[])warnings.toArray(new String[warnings.size()]);
    }

    public void save() {
        Element root = doc.createElement("openejb-jar");
        doc.appendChild(root);
        MetaDataContainer[] configs = data.getContainers();
        if(configs.length > 0) {
            Element base = null;
            for(int i=0; i<configs.length; i++) {
                if(configs[i].getName() != null && !configs[i].getName().equals("")) {
                    if(base == null) {
                        base = createChild(doc, root, "containers");
                    }
                    Element config = createChild(doc, base, getContainerName(configs[i].getType()));
                    addContainer(config, configs[i]);
                }
            }
        }
        MetaDataBean[] beans = data.getEJBs();
        if(beans.length > 0) {
            Element base = null;
            for(int i=0; i<beans.length; i++) {
                String name = beans[i].getEJBName();
                if(name != null && !name.equals("")) {
                    if(base == null) {
                        base = createChild(doc, root, "enterprise-beans");
                    }
                    Element bean = createChild(doc, base, beans[i].isEntity() ? "entity" : "session");
                    addBean(bean, beans[i]);
                }
            }
        }
        MetaDataSecurityRole[] roles = data.getSecurityRoles();
        if(roles.length > 0) {
            Element base = null;
            for(int i=0; i<roles.length; i++) {
                String name = roles[i].getEjbRoleName();
                if(name != null && !name.equals("") && roles[i].getPhysicalRoleNames().size() > 0) {
                    if(base == null) {
                        base = createChild(doc, root, "assembly-descriptor");
                    }
                    Element role = createChild(doc, base, "security-role");
                    addSecurityRole(role, roles[i]);
                }
            }
        }
    }

    private static String getContainerName(byte type) {
        switch(type) {
            case MetaDataContainer.ENTITY_CONTAINER_TYPE:
                return "entity-container";
            case MetaDataContainer.STATEFUL_CONTAINER_TYPE:
                return "stateful-session-container";
            case MetaDataContainer.STATELESS_CONTAINER_TYPE:
                return "stateless-session-container";
            default:
                throw new IllegalArgumentException();
        }
    }

    private void addBean(Element root, MetaDataBean bean) {
        createChildText(doc, root, "ejb-name", bean.getEJBName());
        createChildText(doc, root, "container-name", bean.getContainerName());
        MetaDataEjbRef ejbs[] = bean.getEjbRefs();
        for(int i=0; i<ejbs.length; i++) {
            if(ejbs[i].getEJBRefName() != null && !ejbs[i].getEJBRefName().equals("")) {
                addEjbRef(createChild(doc, root, "ejb-ref"), ejbs[i]);
            }
        }
        MetaDataResourceRef res[] = bean.getResourceRefs();
        for(int i=0; i<res.length; i++) {
            if(res[i].getRefName() != null && !res[i].getRefName().equals("")) {
                addResourceRef(createChild(doc, root, "resource-ref"), res[i]);
            }
        }
    }

    private void addEjbRef(Element root, MetaDataEjbRef ref) {
        createChildText(doc, root, "ejb-ref-name", ref.getEJBRefName());
        Element e = createChild(doc, root, "ejb-ref-location");
        String local = ref.getOpenEjbName() ;
        if(local != null && !local.equals("")) {
            createChildText(doc, e, "ejb-deployment-id", local);
        } else {
            e = createChild(doc, e, "connection");
            writeProperties(e, ref.getMap());
        }
    }

    private void addResourceRef(Element root, MetaDataResourceRef ref) {
        createChildText(doc, root, "res-ref-name", ref.getRefName());
        createChildText(doc, root, "resource-deployment-id", ref.getConnectorName());
    }

    private void addContainer(Element root, MetaDataContainer config) {
        createChildText(doc, root, "description", config.getDescription());
        createChildText(doc, root, "display-name", config.getDisplayName());
        createChildText(doc, root, "container-name", config.getName());
        writeProperties(root, config.getMap());
    }

    private void addSecurityRole(Element root, MetaDataSecurityRole role) {
        createChildText(doc, root, "role-name", role.getEjbRoleName());
        for(Iterator it = role.getPhysicalRoleNames().iterator(); it.hasNext();) {
            createChildText(doc, root, "physical-role-name", (String)it.next());
        }
    }

    private void writeProperties(Element root, Map properties) {
        if(properties == null || properties.size() == 0)
            return;
        Element props = createChild(doc, root, "properties");
        for(Iterator it = properties.keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            String value = (String)properties.get(key);
            Element prop = createChild(doc, props, "property");
            createChildText(doc, prop, "property-name", key);
            createChildText(doc, prop, "property-value", value);
        }
    }
}
