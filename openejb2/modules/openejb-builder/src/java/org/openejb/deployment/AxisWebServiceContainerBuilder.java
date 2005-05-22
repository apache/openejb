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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarFile;
import javax.management.ObjectName;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.axis.builder.AxisServiceBuilder;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.openejb.server.axis.WSContainerGBean;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.ejbjar.OpenejbWebServiceSecurityType;

public class AxisWebServiceContainerBuilder {


    /*
     * The ultimate goal of this method is to create an XFireService GBean that wraps the EJBContainer with
     * the corresponding sessionObjectname and is capable of being indexed by its WSDL address location.
     */
    public void addGbean(EARContext earContext, EJBModule ejbModule, ClassLoader cl, ObjectName sessionObjectName, ObjectName listener, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, OpenejbWebServiceSecurityType webServiceSecurity) throws DeploymentException {

        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue());
        String serviceEndpointName = OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getServiceEndpoint());
        String ejbName = sessionBean.getEjbName().getStringValue().trim();

        if (!isStateless || serviceEndpointName == null) {
            return;
        }

        serviceEndpointName = serviceEndpointName.trim();
        String location = null;
        if (openejbSessionBean != null && openejbSessionBean.isSetWebServiceAddress()) {
            location = openejbSessionBean.getWebServiceAddress().trim();
        }

        GBeanData gBean = buildGBeanData(sessionObjectName, listener, ejbName, serviceEndpointName, location, ejbModule.getModuleFile(), cl, webServiceSecurity);

        earContext.addGBean(gBean);
    }

    GBeanData buildGBeanData(ObjectName sessionObjectName, ObjectName listener, String ejbName, String serviceEndpointName, String location, JarFile jarFile, ClassLoader cl, OpenejbWebServiceSecurityType webServiceSecurity) throws DeploymentException {
        ServiceInfo serviceInfo = AxisServiceBuilder.createServiceInfo(jarFile, ejbName, cl);
        JavaServiceDesc ejbServiceDesc = serviceInfo.getServiceDesc();

        // Strip the jar file path from the WSDL file since jar file location may change at runtime.
        String wsdlFile = ejbServiceDesc.getWSDLFile();
        wsdlFile = wsdlFile.substring(wsdlFile.indexOf("!")+2);

        URI wsdlURI = null;
        try {
            wsdlURI = new URI(wsdlFile);
        }
        catch (URISyntaxException e) {
            throw new DeploymentException("Invalid WSDL URI: "+ wsdlFile, e);
        }
        URI locationURI = null;
        try {
            locationURI = new URI(location != null? location: ejbServiceDesc.getEndpointURL());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid address location URI: "+ejbServiceDesc.getEndpointURL(), e);
        }
        String securityRealmName = null;
        String realmName = null;
        String transportGuarantee = null;
        String authMethod = null;
        if (webServiceSecurity != null) {
            securityRealmName = webServiceSecurity.getSecurityRealmName().trim();
            realmName = webServiceSecurity.getRealmName().trim();
            transportGuarantee = webServiceSecurity.getTransportGuarantee().toString();
            authMethod = webServiceSecurity.getAuthMethod().toString();
        }
        GBeanData gBean = WSContainerGBean.createGBean(ejbName,
                sessionObjectName,
                listener,
                locationURI,
                wsdlURI,
                serviceInfo,
                securityRealmName,
                realmName,
                transportGuarantee, authMethod);
        return gBean;
    }
}
