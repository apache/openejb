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
package org.openejb.ui.jedi.openejb11.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.openejb.ui.jedi.openejb11.ejb.MetaDataContainer;
import org.opentools.deployer.plugins.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves data to an XML file using DOM.  The actual mechanism used to save
 * is parser-dependent.  The JEDI framework currently autodetects Xerces
 * and Sun's parser that ships with JAXP.  That bit is handled in the
 * OpenEjbMetaData class - this class just populates the DOM.
 *
 * @see org.openejb.ui.jedi.openejb11.server.OpenEjbMetaData
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
        Element root = doc.createElement("openejb");
        doc.appendChild(root);
        MetaDataContainer[] configs = data.getContainers();
        if(configs.length > 0) {
            Element base = null;
            for(int i=0; i<configs.length; i++) {
                if(configs[i].getName() != null && !configs[i].getName().equals("")) {
                    if(base == null) {
                        base = createChild(doc, root, "container-system");
                        base = createChild(doc, base, "containers");
                    }
                    Element config = createChild(doc, base, getContainerName(configs[i].getType()));
                    addContainer(config, configs[i]);
                }
            }
        }
        Element facilities = createChild(doc, root, "facilities");
        writeIntraVM(createChild(doc, facilities, "intra-vm-server"));
        if(data.getConnectionManagers().length > 0) {
            Element jcx = createChild(doc, facilities, "jcx-connectors");
            writeConnectionManagers(jcx);
            writeConnectors(jcx);
        }
        writeServices(createChild(doc, facilities, "services"));
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

    private void writeConnectionManagers(Element root) {
        CMMetaData[] cms = data.getConnectionManagers();
        for(int i=0; i<cms.length; i++) {
            if(cms[i].getName() != null && !cms[i].getName().equals("")) {
                writeConnectionManager(createChild(doc, root, "connection-manager"), cms[i]);
            }
        }
    }

    private void writeConnectionManager(Element root, CMMetaData cm) {
        createChildText(doc, root, "connection-manager-id", cm.getName());
        createChildText(doc, root, "class-name", cm.getClassName());
        writeProperties(root, cm.getMap());
    }

    private void writeConnectors(Element root) {
        ConnectorMetaData[] cons = data.getConnectors();
        for(int i=0; i<cons.length; i++) {
            if(cons[i].getName() != null && !cons[i].getName().equals("")) {
                writeConnector(createChild(doc, root, "connector"), cons[i]);
            }
        }
    }

    private void writeConnector(Element root, ConnectorMetaData con) {
        createChildText(doc, root, "connector-id", con.getName());
        createChildText(doc, root, "connection-manager-id", con.getConnectionManagerName());
        if(con.getMap().size() > 0) {
            Element config = createChild(doc, root, "connector-manager-configuration");
            writeProperties(config, con.getMap());
        }
    }

    private void addContainer(Element root, MetaDataContainer config) {
        createChildText(doc, root, "description", config.getDescription());
        createChildText(doc, root, "display-name", config.getDisplayName());
        createChildText(doc, root, "container-name", config.getName());
        writeProperties(root, config.getMap());
    }

    private void writeIntraVM(Element root) {
        ServerMetaData server = data.getServerData();
        Element proxy = createChild(doc, root, "proxy-factory");
        createChildText(doc, proxy, "class-name", server.getProxyFactoryClass());
        createChildText(doc, proxy, "codebase", server.getProxyFactoryCodebase());
        writeProperties(proxy, server.getProxyMap());
    }

    private void writeServices(Element root) {
        writeSecurityService(createChild(doc, root, "security-service"));
        writeTransactionService(createChild(doc, root, "transaction-service"));
    }

    private void writeSecurityService(Element root) {
        SecurityMetaData security = data.getSecurityData();
        createOptionalChildText(doc, root, "description", security.getDescription());
        createOptionalChildText(doc, root, "display-name", security.getDisplayName());
        createChildText(doc, root, "service-name", security.getName());
        createChildText(doc, root, "class-name", security.getClassName());
        writeProperties(root, security.getMap());
    }

    private void writeTransactionService(Element root) {
        TransactionMetaData trans = data.getTransactionData();
        createOptionalChildText(doc, root, "description", trans.getDescription());
        createOptionalChildText(doc, root, "display-name", trans.getDisplayName());
        createChildText(doc, root, "service-name", trans.getName());
        createChildText(doc, root, "class-name", trans.getClassName());
        writeProperties(root, trans.getMap());
    }

    private void writeProperties(Element root, Map properties) {
        if(properties == null || properties.size() == 0)
            return;
        Element props = createChild(doc, root, "properties");
        for(Iterator it = properties.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            Object value = properties.get(key);
            writeProperty(props, key, value);

        }
    }

    private void writeProperty(Element root, Object name, Object value) {
        if(name instanceof Collection) {
            for(Iterator it = ((Collection)name).iterator(); it.hasNext();) {
                writeProperty(root, it.next(), value);
            }
        } else {
            if(value instanceof Collection) {
                for(Iterator it = ((Collection)value).iterator(); it.hasNext();) {
                    writeProperty(root, name, it.next());
                }
            } else {
                Element prop = createChild(doc, root, "property");
                createChildText(doc, prop, "property-name", (String)name);
                createChildText(doc, prop, "property-value", (String)value);
            }
        }
    }
}
