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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.deployment;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.geronimo.webservices.*;
import org.apache.geronimo.validator.ValidationContext;
import org.apache.geronimo.validator.ValidationError;
import org.apache.geronimo.validator.ValidationFailure;
import org.openejb.server.xfire.WSContainerGBean;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;

public class XFireWebServiceContainerBuilder {


    private static final Log log = LogFactory.getLog(XFireWebServiceContainerBuilder.class);

    /*
     * The ultimate goal of this method is to create an XFireService GBean that wraps the EJBContainer with
     * the corresponding sessionObjectname and is capable of being indexed by its WSDL address location.
     */
    public void addGbean(EARContext earContext, EJBModule ejbModule, ClassLoader cl, ObjectName sessionObjectName, ObjectName listener, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, Security security) throws DeploymentException {

        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue());
        String serviceEndpointName = OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getServiceEndpoint());
        String ejbName = sessionBean.getEjbName().getStringValue().trim();

        if (!isStateless || serviceEndpointName == null) {
            return;
        }

        serviceEndpointName = serviceEndpointName.trim();

        GBeanData gBean = buildGBeanData(sessionObjectName, listener, ejbName, serviceEndpointName, ejbModule.getModuleFile());

        earContext.addGBean(gBean);
    }

    public GBeanData buildGBeanData(ObjectName sessionObjectName, ObjectName listener, String ejbName, String serviceEndpointName, JarFile jarFile) throws DeploymentException {
        WebServices webservice;
        try {
            URL webservicesURL = DeploymentUtil.createJarURL(jarFile, "META-INF/webservices.xml");
            webservice = WebServicesFactory.getInstance().readXML(webservicesURL);
        } catch (MalformedURLException e1) {
            throw new DeploymentException("Invalid URL to webservices.xml", e1);
        }

        WebServiceDescription webServiceDescription = null;
        PortComponent portComponent = null;

        WebServiceDescription[] webServiceDescriptions = webservice.getWebServiceDescription();

        // Grab the WebServiceDescription and PortComponent for this EJB
        search: for (int i = 0; i < webServiceDescriptions.length; i++) {
            webServiceDescription = webServiceDescriptions[i];
            PortComponent[] portComponents = webServiceDescription.getPortComponent();
            for (int j = 0; j < portComponents.length; j++) {
                portComponent = portComponents[j];
                ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                if (ejbName.equals(serviceImplBean.getEjbLink())) {
                    break search;
                }
            }
        }

        // TODO: Should be flushed out in a pure validation phase
        // TODO: Use i18n messages
        if (webServiceDescription == null || portComponent == null) {
            throw new DeploymentException("There is no matching port-component for ejb: ejb-name=" + ejbName);
        }

        // TODO: Should be flushed out in a pure validation phase
        // TODO: Use i18n messages
        if (!serviceEndpointName.equals(portComponent.getServiceEndpointInterface().trim())) {
            throw new DeploymentException("The service-endpoint interface of the port-component does " +
                    "not match that of the ejb: ejb-name=" + ejbName + ", \nexpected = " + serviceEndpointName + ", \nfound    = " + portComponent.getServiceEndpointInterface());
        }

        String wsdlFile = webServiceDescription.getWsdlFile();

        URL wsdlURL;
        Definition definition;
        try {
            wsdlURL = DeploymentUtil.createJarURL(jarFile, wsdlFile);
            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            definition = wsdlReader.readWSDL(wsdlURL.toExternalForm());
        } catch (MalformedURLException e) {
            throw new DeploymentException("Webservices.xml file references a wsdl file that does not exist: " + wsdlFile, e);
        } catch (WSDLException e) {
            throw new DeploymentException("Could not parse the WSDL file: " + wsdlFile, e);
        }

        if (!isTargetNamespaceImported(definition)){
            log.warn("Target namespace declared, but not included in document namespace declarations: "+wsdlURL);
        }

        LightWeightMappingValidator validator = new LightWeightMappingValidator(definition);
        ValidationContext result = validator.validate();
        if (result.hasErrors()){
            log.info("Unable to deploy web service");
            ValidationError[] errors = result.getErrors();
            for (int i = 0; i < errors.length; i++) {
                log.error(errors[i]);
            }
            throw new DeploymentException("Unable to deploy web service.  See log for details");
        }
        if (result.hasFailures()){
            log.info("The web service could not be deployed as it doesn't meed the following requirements for light-weight mapping");
            ValidationFailure[] failures = result.getFailures();
            for (int i = 0; i < failures.length; i++) {
                log.info(failures[i]);
            }
            throw new DeploymentException("Invalid light-weight mapping.  See log for details");
        }


        String[] strings = portComponent.getWsdlPort().split(":");
        String portName = strings[strings.length - 1];

        // Find the port definition with the name
        Port port = null;
        Map services = definition.getServices();
        for (Iterator iterator = services.values().iterator(); iterator.hasNext();) {
            Service service = (Service) iterator.next();
            port = service.getPort(portName);
            if (port != null) {
                break;
            }
        }

        URI location = null;
        List extensibilityElements = port.getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            Object element = extensibilityElements.get(i);
            if (element instanceof SOAPAddress) {
                SOAPAddress address = (SOAPAddress) element;
                try {
                    location = new URI(address.getLocationURI());
                    break;
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Invalid location URI for port: port-name=" + port.getName(), e);
                }
            }
        }

        String style = null;
        extensibilityElements = port.getBinding().getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            Object element = extensibilityElements.get(i);
            if (element instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding) element;
                style = soapBinding.getStyle();
            }
        }

        if (style == null || !style.matches("rpc|document")){
            throw new DeploymentException("Cannot determine the messaging style of the binding: "+port.getBinding().getQName());
        }

        String encoding = null;
        List bindingOperations = port.getBinding().getBindingOperations();
        for (int i = 0; i < bindingOperations.size(); i++) {
            BindingOperation bindingOperation = (BindingOperation) bindingOperations.get(i);
            extensibilityElements = bindingOperation.getBindingInput().getExtensibilityElements();
            for (int j = 0; j < extensibilityElements.size(); j++) {
                Object element = extensibilityElements.get(j);
                if (element instanceof SOAPBody){
                    SOAPBody body = (SOAPBody) element;
                    encoding = body.getUse();
                    break;
                }
            }
        }
        if (encoding == null || !encoding.matches("literal|encoded")){
            throw new DeploymentException("Cannot determine the encoding of the binding: "+port.getBinding().getQName());
        }

        GBeanData gBean = WSContainerGBean.createGBean(ejbName, sessionObjectName, listener, definition, location, wsdlURL, definition.getTargetNamespace(), encoding, style);
        return gBean;
    }

    private boolean isTargetNamespaceImported(Definition definition) {
        String targetNamespace = definition.getTargetNamespace();
        Collection namespaces = definition.getNamespaces().values();
        for (Iterator iterator = namespaces.iterator(); iterator.hasNext();) {
            String namespace = (String) iterator.next();
            if (targetNamespace.equals(namespace)){
                return true;
            }
        }
        return false;
    }
}
