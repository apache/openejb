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
package org.openejb.alt.assembler.modern.rar.jca10;

import org.openejb.alt.assembler.modern.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Loads metadata objects from a DOM tree.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class XMLReader extends XMLUtils {
    private Document doc;
    private ConnectorMetaData data;

    public XMLReader(Document doc, ConnectorMetaData data) {
        this.doc = doc;
        this.data = data;
    }

    public void load() {
        Element root = doc.getDocumentElement();
        data.setDescription(getChildText(root, "description"));
        data.setDisplayName(getChildText(root, "display-name"));
        data.setEISType(getChildText(root, "eis-type"));
        data.setIcon(getChildText(root, "icon"));
        data.setSpecVersion(getChildText(root, "spec-version"));
        data.setVendorName(getChildText(root, "vendor-name"));
        data.setVersion(getChildText(root, "version"));
        Element license = getChildByName(root, "license");
        if(license != null) {
            loadLicense(license);
        }

        Element ra = getChildByName(root, "resourceadapter");
        data.setConnectionClass(getChildText(ra, "connection-impl-class"));
        data.setConnectionFactoryClass(getChildText(ra, "connectionfactory-impl-class"));
        data.setConnectionFactoryInterface(getChildText(ra, "connectionfactory-interface"));
        data.setConnectionInterface(getChildText(ra, "connection-interface"));
        data.setManagedConnectionFactoryClass(getChildText(ra, "managedconnectionfactory-class"));
        data.setReauthentication(new Boolean(getChildText(ra, "reauthentication-support")).booleanValue());
        String trans = getChildText(ra, "transaction-support");
        if(trans.equalsIgnoreCase("no_transaction")) {
            data.setTransactionType(ConnectorMetaData.TRANSACTION_TYPE_NONE);
        } else if(trans.equalsIgnoreCase("local_transaction")) {
            data.setTransactionType(ConnectorMetaData.TRANSACTION_TYPE_LOCAL);
        } else if(trans.equalsIgnoreCase("xa_transaction")) {
            data.setTransactionType(ConnectorMetaData.TRANSACTION_TYPE_XA);
        } else {
            data.setTransactionType(ConnectorMetaData.TRANSACTION_TYPE_NONE);
        }

        Element[] auths = getChildrenByName(ra, "auth-mechanism");
        for(int i=0; i<auths.length; i++) {
            loadAuthMechanism(auths[i]);
        }
        Element[] props = getChildrenByName(ra, "config-property");
        for(int i=0; i<props.length; i++) {
            loadConfigProperty(props[i]);
        }
        Element[] perms = getChildrenByName(ra, "security-permission");
        for(int i=0; i<perms.length; i++) {
            loadSecurityPermission(perms[i]);
        }
    }

    private void loadAuthMechanism(Element root) {
        AuthMechanismMetaData auth = new AuthMechanismMetaData();
        auth.setDescription(getChildText(root, "description"));
        auth.setAuthMechType(getChildText(root, "auth-mech-type"));
        auth.setCredentialInterface(getChildText(root, "credential-interface"));
        data.addAuthMechanism(auth);
    }

    private void loadConfigProperty(Element root) {
        ConfigPropertyMetaData auth = new ConfigPropertyMetaData();
        auth.setDescription(getChildText(root, "description"));
        auth.setName(getChildText(root, "config-property-name"));
        auth.setType(getChildText(root, "config-property-type"));
        auth.setDefaultValue(getChildText(root, "config-property-value"));
        data.addConfigProperty(auth);
    }

    private void loadSecurityPermission(Element root) {
        SecurityPermissionMetaData auth = new SecurityPermissionMetaData();
        auth.setDescription(getChildText(root, "description"));
        auth.setSecurityPermission(getChildText(root, "security-permission-spec"));
        data.addSecurityPermission(auth);
    }

    private void loadLicense(Element root) {
        LicenseMetaData license = new LicenseMetaData();
        license.setDescription(getChildText(root, "description"));
        license.setRequired(new Boolean(getChildText(root, "license-required")).booleanValue());
        data.setLicense(license);
    }
}
