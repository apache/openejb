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
package org.openejb.dd.webservices;

import java.util.ArrayList;
import java.util.HashMap;

import org.openejb.dd.webservices.PortComponent;

public class WebServiceDescription {
    private String webServiceDescriptionName;
    private String wsdlFile;
    private String jaxrpcMappingFile;

    /**
     * List of PortComponent objects
     *
     * @see org.openejb.dd.webservices.PortComponent
     */
    private ArrayList portComponentList = new ArrayList();
    /**
     * Map of PortComponent objects indexed by portComponentName
     *
     * @see org.openejb.dd.webservices.PortComponent#getPortComponentName
     */
    private HashMap portComponentMap = new HashMap();

    public String getWebServiceDescriptionName() {
        return webServiceDescriptionName;
    }

    public void setWebServiceDescriptionName(String webServiceDescriptionName) {
        this.webServiceDescriptionName = webServiceDescriptionName;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wsdlFile) {
        this.wsdlFile = wsdlFile;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(String jaxrpcMappingFile) {
        this.jaxrpcMappingFile = jaxrpcMappingFile;
    }

    public void addPortComponent(PortComponent portComponent) throws IndexOutOfBoundsException {
        portComponentList.add(portComponent);
        portComponentMap.put(portComponent.getPortComponentName(), portComponent);
    }

    public void addPortComponent(int index, PortComponent portComponent) throws IndexOutOfBoundsException {
        portComponentList.add(index, portComponent);
        portComponentMap.put(portComponent.getPortComponentName(), portComponent);
    }

    public boolean removePortComponent(PortComponent portComponent) {
        portComponentMap.remove(portComponent.getPortComponentName());
        return portComponentList.remove(portComponent);
    }

    public PortComponent getPortComponent(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > portComponentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (PortComponent) portComponentList.get(index);
    }

    public PortComponent[] getPortComponent() {
        int size = portComponentList.size();
        PortComponent[] mArray = new PortComponent[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (PortComponent) portComponentList.get(index);
        }
        return mArray;
    }

    public PortComponent getPortComponent(String portComponentName) {
        return (PortComponent) portComponentMap.get(portComponentName);
    }

    public void setPortComponent(int index, PortComponent portComponent) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > portComponentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        PortComponent removed = (PortComponent) portComponentList.set(index, portComponent);
        portComponentMap.remove(removed.getPortComponentName());
        portComponentMap.put(portComponent.getPortComponentName(), portComponent);
    }

    public void setPortComponent(PortComponent[] portComponentArray) {
        portComponentList.clear();
        for (int i = 0; i < portComponentArray.length; i++) {
            PortComponent portComponent = portComponentArray[i];
            portComponentList.add(portComponent);
            portComponentMap.put(portComponent.getPortComponentName(), portComponent);
        }
    }

    public void clearPortComponent() {
        portComponentList.clear();
        portComponentMap.clear();
    }

}
