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
package org.openejb.alt.assembler.classic.xml;

import java.io.IOException;
import java.util.Properties;

import org.apache.xerces.parsers.DOMParser;
import org.openejb.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.OpenEjbConfiguration;
import org.openejb.alt.assembler.classic.OpenEjbConfigurationFactory;
import org.openejb.util.OpenEJBErrorHandler;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;


/**
 * Factory for creating an instance of the OpenEjbConfiguration using DOM and and XML configuration file
 * 
 * <p>
 * DomOpenEjbConfigurationFactory is the default OpenEjbConfigurationFactory, which
 * creates an OpenEjbConfiguration object based on XML config files located on the
 * local system.</p>
 * 
 * <p>
 * The OpenEjbConfiguration object structure provides the inforamtion about the
 * configuration of OpenEJB and the container system and is used by the
 * org.openejb.alt.assembler.classic.Assembler to build a running unstance of OpenEJB.</p>
 * 
 * <p>
 * Other OpenEjbConfigurationFactory implementations can be created that might populate
 * this object using a different approach.  Other usefull implementations might be:<br>
 * <UL>
 * <LI>Populating the OpenEjbConfiguration from values in a RDBMS.
 * <LI>Populating the OpenEjbConfiguration from values in a Properties file.
 * <LI>Retrieving the OpenEjbConfiguration from a ODBMS.
 * <LI>Creating the OpenEjbConfiguration using a JavaBeans enabled editing tool or wizard.
 * </UL>
 * 
 * <p>
 * If you are interested in creating alternate an OpenEjbConfigurationFactory to do
 * any of the above techniques or a new approach, email the
 * <a href="mailto:openejb-dev@exolab.org">OpenEJB Developer list</a> with a description
 * of the new OpenEjbConfigurationFactory implementation.
 * </p>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.spi.Assembler
 * @see org.openejb.alt.assembler.classic.Assembler
 * @see org.openejb.alt.assembler.classic.OpenEjbConfiguration
 * @see org.openejb.alt.assembler.classic.OpenEjbConfigurationFactory
 */
public class DomOpenEjbConfigurationFactory implements OpenEjbConfigurationFactory{
    
    private SafeToolkit toolkit = SafeToolkit.getToolkit("DomConfig");
    private XmlOpenEJBConfiguration config;
    private String configXml;

    /**
     * Initializes the {@link org.openejb.alt.assembler.classic.OpenEjbConfiguration} with the 
     * XML config file specified by the value of {@link EnvProps.CONFIGURATION} in 
     * the environment variables used to construct this container system.
     * @exeption OpenEJBException 
     * @param props  A Properties object containing the EnvProps.CONFIGURATION entry
     * @exception OpenEJBException
     *                   if there was a problem parsing the XML file, the XML file is invalid or the XML file could not be found.
     * @see org.openejb.alt.assembler.classic.OpenEjbConfiguration
     * @see org.openejb.EnvProps.CONFIGURATION
     */
    public void init(Properties props) throws OpenEJBException {
        SafeProperties safeProps = toolkit.getSafeProperties(props);
        configXml = safeProps.getProperty(EnvProps.CONFIGURATION);
        java.io.File tmp = new java.io.File(configXml);
    }
    
    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        try {
            if (config == null) {
                config = new XmlOpenEJBConfiguration();
                DOMParser parser = new DOMParser();
                
                parser.setErrorHandler(new XMLErrorHandler());
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
                parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
                parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
                parser.parse(configXml);
    
                Document document = parser.getDocument();
                config.initializeFromDOM(document);
            }

            return config;
        } catch (IOException e) { 
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        } catch (SAXNotSupportedException e) {
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        } catch (SAXNotRecognizedException e) {
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        } catch (SAXException e) {
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        }
    }

}

class XMLErrorHandler implements ErrorHandler{

    public void warning (SAXParseException exception) throws SAXException{
        handleError("warning",exception);
    }

    public void error (SAXParseException exception) throws SAXException{
        handleError("error",exception);
    }

    public void fatalError (SAXParseException exception) throws SAXException{
        handleError("fatal error",exception);
    }

    private void handleError(String errorType, SAXParseException e) throws SAXException{
        OpenEJBErrorHandler.configurationParsingError(errorType, e.getLocalizedMessage(), e.getLineNumber()+"", e.getColumnNumber()+"");
    }
}


