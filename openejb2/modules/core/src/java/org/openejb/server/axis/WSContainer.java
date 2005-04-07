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
import org.apache.axis.handlers.HandlerInfoChainFactory;
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

    public WSContainer(EJBContainer ejbContainer, URI location, URI wsdlURI, SoapHandler soapHandler, ServiceInfo serviceInfo) throws Exception {
        try {

            this.soapHandler = soapHandler;
            this.location = location;

            RPCProvider provider = new EJBContainerProvider(ejbContainer);
            SOAPService service = new SOAPService(null, provider, null);

            JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();
            service.setServiceDescription(serviceDesc);
            Class serviceEndpointInterface = ejbContainer.getProxyInfo().getServiceEndpointInterface();

            service.setOption("className", serviceEndpointInterface.getName());
            serviceDesc.setImplClass(serviceEndpointInterface);

            HandlerInfoChainFactory handlerInfoChainFactory = new HandlerInfoChainFactory(serviceInfo.getHanlderInfos());
            service.setOption(org.apache.axis.Constants.ATTR_HANDLERINFOCHAIN, handlerInfoChainFactory);

            ClassLoader classLoader = ejbContainer.getClassLoader();
            AxisWebServiceContainer axisContainer = new AxisWebServiceContainer(location, wsdlURI.toString(), service, classLoader);
            if (soapHandler != null) {
                soapHandler.addWebService(location.getPath(), axisContainer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


//    public WSContainer(final EJBContainer ejbContainer, Definition definition, URI location, URL wsdlURL, String namespace, String encoding, String style, WebServiceContainer webServiceContainer) throws Exception {
//        this.webServiceContainer = webServiceContainer;
//        this.ejbContainer = ejbContainer;
//        this.location = location;
//        this.wsdlURL = wsdlURL;
//
//        JavaServiceDesc serviceDesc = createServiceDesc();
//
//        RPCProvider provider = new EJBContainerProvider(ejbContainer);
//        service = new SOAPService(null, provider, null);
//        service.setServiceDescription(serviceDesc);
//        service.setOption("className", "org.openejb.test.simple.slsb.SimpleStatelessSessionEJB");
//
//        if (webServiceContainer != null) {
//            webServiceContainer.addWebService(location.getPath(), this);
//        }
//    }
//
//    private JavaServiceDesc createServiceDesc() {
//        JavaServiceDesc serviceDesc = new JavaServiceDesc();
//        serviceDesc.setName("SimpleService");
//        serviceDesc.setStyle(Style.RPC);
//        serviceDesc.setUse(Use.ENCODED);
//
//        ParameterDesc parameterDesc = new ParameterDesc();
//        parameterDesc.setName("String_1");
//        parameterDesc.setTypeQName(new QName(XSD_NS, "string"));
//
//        OperationDesc operation = new OperationDesc("echo", new ParameterDesc[]{parameterDesc}, new QName("result"));
//        operation.setReturnType(new QName(XSD_NS, "string"));
//        serviceDesc.addOperationDesc(operation);
//
//        TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl();
//        typeMappingRegistry.doRegisterFromVersion("1.3");
//        org.apache.axis.encoding.TypeMapping typeMapping = typeMappingRegistry.getOrMakeTypeMapping(Use.ENCODED_STR);
//
//        serviceDesc.setTypeMappingRegistry(typeMappingRegistry);
//        serviceDesc.setTypeMapping(typeMapping);
//
//        SerializerFactory ser = BaseSerializerFactory.createFactory(SimpleSerializerFactory.class, String.class, new QName(XSD_NS, "string"));
//        DeserializerFactory deser = BaseDeserializerFactory.createFactory(SimpleDeserializerFactory.class, String.class, new QName(XSD_NS, "string"));
//        typeMapping.register(String.class, new QName(XSD_NS, "string"), ser, deser);
//        return serviceDesc;
//    }


    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        soapHandler.removeWebService(location.getPath());
    }

    public void doFail() {

    }
}
