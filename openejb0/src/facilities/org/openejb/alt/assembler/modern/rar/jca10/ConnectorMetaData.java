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

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openejb.alt.assembler.modern.LoadException;
import org.openejb.alt.assembler.modern.MetaData;
import org.openejb.alt.assembler.modern.xml.DTDResolver;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Metadata for a resource adapter.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class ConnectorMetaData extends MetaData {
    public final static byte TRANSACTION_TYPE_NONE = -3;
    public final static byte TRANSACTION_TYPE_LOCAL = -5;
    public final static byte TRANSACTION_TYPE_XA = -7;
    private String mcfClass;
    private String cfInterface;
    private String cfClass;
    private String conInterface;
    private String conClass;
    private String description;
    private String displayName;
    private String icon;
    private String vendor;
    private String specVersion;
    private String eisType;
    private String version;
    private byte transactionType;
    private boolean reauthentication;
    private Vector properties = new Vector();
    private Vector authMechanisms = new Vector();
    private Vector permissions = new Vector();
    private LicenseMetaData license;

    public ConnectorMetaData() {
    }
    public ConnectorMetaData(ConnectorMetaData source) {
        mcfClass = source.mcfClass;
        cfInterface = source.cfInterface;
        cfClass = source.cfClass;
        conInterface = source.conInterface;
        conClass = source.conClass;
        description = source.description;
        displayName = source.displayName;
        icon = source.icon;
        vendor = source.vendor;
        specVersion = source.specVersion;
        eisType = source.eisType;
        version = source.version;
        transactionType = source.transactionType;
        reauthentication = source.reauthentication;
        properties.addAll(source.properties);
        authMechanisms.addAll(source.authMechanisms);
        permissions.addAll(source.permissions);
        license = source.license;
    }

    public void setDescription(String desc) {description = desc;}
    public String getDescription() {return description;}
    public void setDisplayName(String name) {displayName = name;}
    public String getDisplayName() {return displayName;}
    public void setIcon(String icon) {this.icon = icon;}
    public String getIcon() {return icon;}
    public void setVendorName(String name) {vendor = name;}
    public String getVendorName() {return vendor;}
    public void setSpecVersion(String version) {specVersion = version;}
    public String getSpecVersion() {return specVersion;}
    public void setVersion(String version) {this.version = version;}
    public String getVersion() {return version;}
    public void setEISType(String type) {eisType = type;}
    public String getEISType() {return eisType;}
    public void setManagedConnectionFactoryClass(String name) {mcfClass = name;}
    public String getManagedConnectionFactoryClass() {return mcfClass;}
    public void setConnectionFactoryInterface(String name) {cfInterface = name;}
    public String getConnectionFactoryInterface() {return cfInterface;}
    public void setConnectionFactoryClass(String name) {cfClass = name;}
    public String getConnectionFactoryClass() {return cfClass;}
    public void setConnectionInterface(String name) {conInterface = name;}
    public String getConnectionInterface() {return conInterface;}
    public void setConnectionClass(String name) {conClass = name;}
    public String getConnectionClass() {return conClass;}
    public void setTransactionType(byte type) {transactionType = type;}
    public byte getTransactionType() {return transactionType;}
    public void setReauthentication(boolean reauth) {reauthentication = reauth;}
    public boolean isReauthentication() {return reauthentication;}
    public void setLicense(LicenseMetaData license) {this.license = license;}
    public LicenseMetaData getLicense() {return license;}

    public void addAuthMechanism(AuthMechanismMetaData auth) {
        authMechanisms.addElement(auth);
    }
    public void removeAuthMechanism(AuthMechanismMetaData auth) {
        authMechanisms.remove(auth);
    }
    public AuthMechanismMetaData[] getAuthMechanisms() {
        return (AuthMechanismMetaData[])authMechanisms.toArray(new AuthMechanismMetaData[authMechanisms.size()]);
    }

    public void addConfigProperty(ConfigPropertyMetaData property) {
        properties.addElement(property);
    }
    public void removeConfigProperty(ConfigPropertyMetaData property) {
        properties.remove(property);
    }
    public ConfigPropertyMetaData[] getConfigProperties() {
        return (ConfigPropertyMetaData[])properties.toArray(new ConfigPropertyMetaData[properties.size()]);
    }

    public void addSecurityPermission(SecurityPermissionMetaData permissionSpec) {
        permissions.addElement(permissionSpec);
    }
    public void removeSecurityPermission(SecurityPermissionMetaData permissionSpec) {
        permissions.remove(permissionSpec);
    }
    public SecurityPermissionMetaData[] getSecurityPermissions() {
        return (SecurityPermissionMetaData[])permissions.toArray(new SecurityPermissionMetaData[permissions.size()]);
    }

    public String[] loadXML(Reader in) throws LoadException {
        properties.clear();
        authMechanisms.clear();
        permissions.clear();
        final List warnings = new ArrayList();
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(true);
            DocumentBuilder parser = fac.newDocumentBuilder();
            parser.setEntityResolver(new DTDResolver());
            parser.setErrorHandler(new ErrorHandler() {
                boolean enabled = true;
                public void warning (SAXParseException exception) throws SAXException {
                    if(enabled) {
                        warnings.add("XML WARNING: ra.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                        if(exception.getMessage().equals("Valid documents must have a <!DOCTYPE declaration.")) {
                            warnings.add("  Cannot validate deployment descriptor without DOCTYPE");
                            warnings.add("  (you may want to save it from here to add the DOCTYPE).");
                            enabled = false;
                        }
                    }
                }
                public void error (SAXParseException exception) throws SAXException {
                    if(enabled)
                        warnings.add("XML ERROR: ra.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                }
                public void fatalError (SAXParseException exception) throws SAXException {
                }
            });
            Document doc = parser.parse(new InputSource(new BufferedReader(in)));
            new XMLReader(doc, this).load();
        } catch(SAXParseException e) {
            e.printStackTrace();
            System.out.println("Line: "+e.getLineNumber());
            System.out.println("Col: "+e.getColumnNumber());
        } catch(Exception e) {
            e.printStackTrace();
            throw new LoadException("XML Error: "+e);
        }
        return (String[])warnings.toArray(new String[warnings.size()]);
    }
}
