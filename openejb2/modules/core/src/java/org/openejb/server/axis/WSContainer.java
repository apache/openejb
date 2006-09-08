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
package org.openejb.server.axis;

import java.net.URI;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.geronimo.axis.server.AxisWebServiceContainer;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.webservices.SoapHandler;
import org.openejb.EJBContainer;

public class WSContainer implements GBeanLifecycle {

    private final SoapHandler soapHandler;
    private final URI location;

    protected WSContainer() {
        soapHandler = null;
        location = null;
    }

    public WSContainer(EJBContainer ejbContainer,
                       URI location,
                       URI wsdlURI,
                       SoapHandler soapHandler,
                       ServiceInfo serviceInfo,
                       String securityRealmName,
                       String realmName,
                       String transportGuarantee,
                       String authMethod,
                       String[] virtualHosts) throws Exception {

        this.soapHandler = soapHandler;
        this.location = location;
        //for use as a template
        if (ejbContainer == null) {
            return;
        }
        RPCProvider provider = new EJBContainerProvider(ejbContainer);
        SOAPService service = new SOAPService(null, provider, null);

        JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();
        service.setServiceDescription(serviceDesc);
        Class serviceEndpointInterface = ejbContainer.getProxyInfo().getServiceEndpointInterface();

        service.setOption("className", serviceEndpointInterface.getName());
        serviceDesc.setImplClass(serviceEndpointInterface);

        ClassLoader classLoader = ejbContainer.getClassLoader();
        AxisWebServiceContainer axisContainer = new AxisWebServiceContainer(location, wsdlURI, service, serviceInfo.getWsdlMap(), classLoader);
        if (soapHandler != null) {
            soapHandler.addWebService(location.getPath(), virtualHosts, axisContainer, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);
        }

    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        if (soapHandler != null) {
            soapHandler.removeWebService(location.getPath());
        }
    }

    public void doFail() {

    }
}
