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
package org.openejb.alt.assembler.modern.global;

import org.openejb.alt.assembler.modern.ContainerMetaData;
import org.openejb.alt.assembler.modern.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Constructs metadata classes from a DOM tree.
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
        Element system = getChildByName(root, "container-system");
        if(system != null) {
            Element[] containers = getChildrenByName(system, "containers");
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
        }
        Element facilities = getChildByName(root, "facilities");
        if(facilities != null) {
            loadIntraVM(getChildByName(facilities, "intra-vm-server"));
            Element jcx = getChildByName(facilities, "jcx-connectors");
            if(jcx != null) {
                loadConnectionManagers(getChildrenByName(jcx, "connection-manager"));
                loadConnectors(getChildrenByName(jcx, "connector"));
            }
            loadServices(getChildByName(facilities, "services"));
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

    private void loadIntraVM(Element root) {
        ServerMetaData server = new ServerMetaData();
        Element factory = getChildByName(root, "proxy-factory");
        server.setProxyFactoryClass(getChildText(factory, "class-name"));
        server.setProxyFactoryCodebase(getChildText(factory, "codebase"));
        Element props = getChildByName(factory, "properties");
        if(props != null) {
            Element[] list = getChildrenByName(props, "property");
            for(int i=0; i<list.length; i++) {
                String name = getChildText(list[i], "property-name");
                String value = getChildText(list[i], "property-value");
                server.addProxyMapping(name, value);
            }
        }
        data.setServerData(server);
    }

    private void loadConnectionManagers(Element[] root) {
        for(int i=0; i<root.length; i++)
            loadConnectionManager(root[i]);
    }

    private void loadConnectionManager(Element root) {
        CMMetaData cm = new CMMetaData();
        cm.setClassName(getChildText(root, "class-name"));
        cm.setName(getChildText(root, "connection-manager-id"));
        Element props = getChildByName(root, "properties");
        if(props != null) {
            Element[] list = getChildrenByName(props, "property");
            for(int i=0; i<list.length; i++) {
                String name = getChildText(list[i], "property-name");
                String value = getChildText(list[i], "property-value");
                cm.addMapping(name, value);
            }
        }
        data.addConnectionManager(cm);
    }

    private void loadConnectors(Element[] root) {
        for(int i=0; i<root.length; i++)
            loadConnector(root[i]);
    }

    private void loadConnector(Element root) {
        ConnectorMetaData con = new ConnectorMetaData();
        con.setName(getChildText(root, "connector-id"));
        con.setConnectionManagerName(getChildText(root, "connection-manager-id"));
        Element props = getChildByName(root, "connector-manager-configuration");
        if(props != null) {
            props = getChildByName(props, "properties");
            Element[] list = getChildrenByName(props, "property");
            for(int i=0; i<list.length; i++) {
                String name = getChildText(list[i], "property-name");
                String value = getChildText(list[i], "property-value");
                con.addMapping(name, value);
            }
        }
        data.addConnector(con);
    }

    private void loadServices(Element root) {
        loadSecurityService(getChildByName(root, "security-service"));
        loadTransactionService(getChildByName(root, "transaction-service"));
    }

    private void loadSecurityService(Element root) {
        SecurityMetaData security = new SecurityMetaData();
        loadBaseService(root, security);
        data.setSecurityData(security);
    }

    private void loadTransactionService(Element root) {
        TransactionMetaData trans = new TransactionMetaData();
        loadBaseService(root, trans);
        data.setTransactionData(trans);
    }

    private void loadBaseService(Element root, ServiceMetaData data) {
        data.setDescription(getChildText(root, "description"));
        data.setDisplayName(getChildText(root, "display-name"));
        data.setName(getChildText(root, "service-name"));
        data.setClassName(getChildText(root, "class-name"));
        Element props = getChildByName(root, "properties");
        if(props != null) {
            Element[] list = getChildrenByName(props, "property");
            for(int i=0; i<list.length; i++) {
                String name = getChildText(list[i], "property-name");
                String value = getChildText(list[i], "property-value");
                data.addMapping(name, value);
            }
        }
    }
}
