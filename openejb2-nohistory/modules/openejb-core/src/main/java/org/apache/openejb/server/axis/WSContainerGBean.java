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
 * $Id: WSContainerGBean.java 445872 2006-02-01 11:50:15Z dain $
 */
package org.apache.openejb.server.axis;

import java.net.URI;

import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.openejb.RpcEjbDeployment;

public class WSContainerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(WSContainerGBean.class, WSContainer.class, NameFactory.WEB_SERVICE_LINK);

//        infoFactory.addOperation("invoke", new Class[]{WebServiceContainer.Request.class, WebServiceContainer.Response.class});

        infoFactory.addReference("EJBContainer", RpcEjbDeployment.class);
        infoFactory.addAttribute("location", URI.class, true);
        infoFactory.addAttribute("wsdlURI", URI.class, true);
        infoFactory.addAttribute("securityRealmName", String.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("transportGuarantee", String.class, true);
        infoFactory.addAttribute("authMethod", String.class, true);
        infoFactory.addAttribute("serviceInfo", ServiceInfo.class, true);
        infoFactory.addAttribute("virtualHosts", String[].class, true);
        infoFactory.addReference("WebServiceContainer", SoapHandler.class);

        infoFactory.setConstructor(new String[]{
            "EJBContainer",
            "location",
            "wsdlURI",
            "WebServiceContainer",
            "serviceInfo",
            "securityRealmName",
            "realmName",
            "transportGuarantee",
            "authMethod",
            "virtualHosts"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    //TODO why is a test method in runtime code?
//    public static ObjectName addGBean(Kernel kernel, String name, ObjectName ejbContainer, ObjectName listener, URI location, URI wsdlURI, ServiceInfo serviceInfo) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//        GBeanData gbean = createGBean(name, ejbContainer, listener, location, wsdlURI, serviceInfo, null, null, null, null);
//        kernel.loadGBean(gbean, WSContainer.class.getClassLoader());
//        kernel.startGBean(gbean.getName());
//        return gbean.getName();
//    }
//
//    private static GBeanData createGBean(String name, ObjectName ejbContainer, ObjectName listener, URI location, URI wsdlURI, ServiceInfo serviceInfo, String securityRealmName, String realmName, String transportGuarantee, String authMethod) {
//        assert ejbContainer != null : "EJBContainer objectname is null";
//
//        ObjectName gbeanName = JMXUtil.getObjectName("openejb:type=WSContainer,name=" + name);
//
//        GBeanData gbean = new GBeanData(gbeanName, WSContainerGBean.GBEAN_INFO);
//        gbean.setReferencePattern("EJBContainer", ejbContainer);
//        gbean.setAttribute("location", location);
//        gbean.setAttribute("wsdlURI", wsdlURI);
//        gbean.setAttribute("serviceInfo", serviceInfo);
//        gbean.setAttribute("securityRealmName", securityRealmName);
//        gbean.setAttribute("realmName", realmName);
//        gbean.setAttribute("transportGuarantee", transportGuarantee);
//        gbean.setAttribute("authMethod", authMethod);
//
//        gbean.setReferencePattern("WebServiceContainer", listener);
//
//        return gbean;
//    }
}
