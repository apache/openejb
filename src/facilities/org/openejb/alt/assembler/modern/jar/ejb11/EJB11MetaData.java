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

import java.io.BufferedReader;
import java.io.IOException;
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
 * The main metadata class for EJB JARs.  It includes some top-level
 * settings that are generally ignored at runtime, and references to
 * the rest of the metadata structures.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class EJB11MetaData extends MetaData {
    private String description;
    private String displayName;
    private String smallIcon;
    private String largeIcon;
    private String clientJar;
    private Vector beans = new Vector();
    private Vector transactions = new Vector();
    private Vector roles = new Vector();
    private Vector permissions = new Vector();

    public EJB11MetaData() {
    }
    public EJB11MetaData(EJB11MetaData source) {
        description = source.description;
        displayName = source.displayName;
        smallIcon = source.smallIcon;
        largeIcon = source.largeIcon;
        clientJar = source.clientJar;
        beans.addAll(source.beans);
        transactions.addAll(source.transactions);
        roles.addAll(source.roles);
        permissions.addAll(source.permissions);
    }

    public void setDescription(String desc) {description = desc;}
    public String getDescription() {return description;}
    public void setDisplayName(String name) {displayName = name;}
    public String getDisplayName() {return displayName;}
    public void setSmallIcon(String icon) {smallIcon = icon;}
    public String getSmallIcon() {return smallIcon;}
    public void setLargeIcon(String icon) {largeIcon = icon;}
    public String getLargeIcon() {return largeIcon;}
    public void setClientJar(String jar) {clientJar = jar;}
    public String getClientJar() {return clientJar;}

    public void addEJB(BeanMetaData bean) {
        if(bean.getParent() != null)
            throw new IllegalArgumentException("Bean '"+bean.getEJBName()+"' already has a parent!");
        beans.addElement(bean);
        bean.setParent(this);
    }
    public void removeEJB(BeanMetaData bean) {
        beans.remove(bean);
        bean.setParent(null);
    }
    public BeanMetaData[] getEJBs() {
        return (BeanMetaData[])beans.toArray(new BeanMetaData[beans.size()]);
    }
    public void clearEJBs() {
        beans.clear();
    }

    public void addSecurityRole(SecurityRoleMetaData role) {
        roles.addElement(role);
    }
    public void removeSecurityRole(SecurityRoleMetaData role) {
        roles.remove(role);
    }
    public SecurityRoleMetaData[] getSecurityRoles() {
        return (SecurityRoleMetaData[])roles.toArray(new SecurityRoleMetaData[roles.size()]);
    }
    public void clearSecurityRoles() {
        roles.clear();
    }

    public void addMethodPermission(MethodPermissionMetaData perm) {
        permissions.addElement(perm);
    }
    public void removeMethodPermission(MethodPermissionMetaData perm) {
        permissions.remove(perm);
    }
    public MethodPermissionMetaData[] getMethodPermissions() {
        return (MethodPermissionMetaData[])permissions.toArray(new MethodPermissionMetaData[permissions.size()]);
    }
    public void clearMethodPermissions() {
        permissions.clear();
    }

    public void addContainerTransaction(ContainerTransactionMetaData trans) {
        transactions.addElement(trans);
    }
    public void removeContainerTransaction(ContainerTransactionMetaData trans) {
        transactions.remove(trans);
    }
    public ContainerTransactionMetaData[] getContainerTransactions() {
        return (ContainerTransactionMetaData[])transactions.toArray(new ContainerTransactionMetaData[transactions.size()]);
    }
    public void clearContainerTransactions() {
        transactions.clear();
    }

    public String[] loadXML(Reader in) throws LoadException, IOException {
        beans.clear();
        transactions.clear();
        roles.clear();
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
                        warnings.add("XML WARNING: ejb-jar.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                        if(exception.getMessage().equals("Valid documents must have a <!DOCTYPE declaration.")) {
                            warnings.add("  Cannot validate deployment descriptor without DOCTYPE");
                            warnings.add("  (you may want to save it from here to add the DOCTYPE).");
                            enabled = false;
                        }
                    }
                }
                public void error (SAXParseException exception) throws SAXException {
                    if(enabled)
                        warnings.add("XML ERROR: ejb-jar.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                }
                public void fatalError (SAXParseException exception) throws SAXException {
                }
            });
            Document doc = parser.parse(new InputSource(new BufferedReader(in)));
            new XMLReader(doc, this).load();
        } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("XML Error: "+e);
        }
        return (String[])warnings.toArray(new String[warnings.size()]);
    }
}
