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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openejb.alt.assembler.modern.ContainerMetaData;
import org.openejb.alt.assembler.modern.LoadException;
import org.openejb.alt.assembler.modern.jar.ejb11.EJB11MetaData;
import org.openejb.alt.assembler.modern.xml.DTDResolver;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The main metadata structure for EJB JAR metadata.  Since all the OpenEJB
 * classes extend the normal EJB classes, both types of information
 * (EJB standard and OpenEJB extensions) are available through this class.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class OpenEjbMetaData extends EJB11MetaData {
    private List containers;

    public OpenEjbMetaData() {
        containers = new LinkedList();
    }
    public OpenEjbMetaData(EJB11MetaData source) {
        super(source);
        clearEJBs();
        clearSecurityRoles();
        containers = new LinkedList();
    }

    public void addContainer(ContainerMetaData cont) {
        containers.add(cont);
    }

    public void removeContainer(ContainerMetaData cont) {
        containers.remove(cont);
    }

    public ContainerMetaData[] getContainers() {
        return (ContainerMetaData[])containers.toArray(new ContainerMetaData[containers.size()]);
    }

    public String[] loadXML(EJB11MetaData source, Reader in) throws LoadException, IOException {
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
                        warnings.add("XML WARNING: openejb-jar.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                        if(exception.getMessage().equals("Valid documents must have a <!DOCTYPE declaration.")) {
                            warnings.add("  Cannot validate deployment descriptor without DOCTYPE");
                            warnings.add("  (you may want to save it from here to add the DOCTYPE).");
                            enabled = false;
                        }
                    }
                }
                public void error (SAXParseException exception) throws SAXException {
                    if(enabled)
                        warnings.add("XML ERROR: openejb-jar.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                }
                public void fatalError (SAXParseException exception) throws SAXException {
                }
            });
            Document doc = parser.parse(new InputSource(new BufferedReader(in)));
            XMLReader reader = new XMLReader(this, source, doc);
            reader.load();
        } catch(SAXParseException e) {
            System.out.println("XML Exception on line: "+e.getLineNumber()+", col "+e.getColumnNumber());
            e.printStackTrace();
            throw new IOException("XML Error: "+e);
        } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("XML Error: "+e);
        }
        return (String[])warnings.toArray(new String[warnings.size()]);
    }
}
