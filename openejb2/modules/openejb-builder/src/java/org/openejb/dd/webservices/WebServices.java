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

import org.openejb.dd.webservices.WebServiceDescription;

/**
 * A dtd version of the J2EE webservices.xml file would look like this:
 * <p/>
 * webservices (webservice-description+)
 * webservice-description (webservice-description-name, wsdl-file, jaxrpc-mapping-file, port-component+)
 * port-component (port-component-name, wsdl-port, service-endpoint-interface, service-impl-bean, handler*)
 * service-impl-bean (ejb-link|servlet-link)
 * handler (handler-name, handler-class, init-param*, soap-header*, soap-role*)
 */
public class WebServices {
    /**
     * List of WebServiceDescription objects
     *
     * @see org.openejb.dd.webservices.WebServiceDescription
     */
    private ArrayList webServiceDescriptionList = new ArrayList();
    /**
     * Map of WebServiceDescription objects indexed by webServiceDescriptionName
     *
     * @see org.openejb.dd.webservices.WebServiceDescription#getWebServiceDescriptionName
     */
    private HashMap webServiceDescriptionMap = new HashMap();

    public void addWebServiceDescription(WebServiceDescription webServiceDescription) throws IndexOutOfBoundsException {
        webServiceDescriptionList.add(webServiceDescription);
        webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
    }

    public void addWebServiceDescription(int index, WebServiceDescription webServiceDescription) throws IndexOutOfBoundsException {
        webServiceDescriptionList.add(index, webServiceDescription);
        webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
    }

    public boolean removeWebServiceDescription(WebServiceDescription webServiceDescription) {
        webServiceDescriptionMap.remove(webServiceDescription.getWebServiceDescriptionName());
        return webServiceDescriptionList.remove(webServiceDescription);
    }

    public WebServiceDescription getWebServiceDescription(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > webServiceDescriptionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (WebServiceDescription) webServiceDescriptionList.get(index);
    }

    public WebServiceDescription[] getWebServiceDescription() {
        int size = webServiceDescriptionList.size();
        WebServiceDescription[] mArray = new WebServiceDescription[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (WebServiceDescription) webServiceDescriptionList.get(index);
        }
        return mArray;
    }

    public WebServiceDescription getWebServiceDescription(String webServiceDescriptionName) {
        return (WebServiceDescription) webServiceDescriptionMap.get(webServiceDescriptionName);
    }

    public void setWebServiceDescription(int index, WebServiceDescription webServiceDescription) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > webServiceDescriptionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        WebServiceDescription removed = (WebServiceDescription) webServiceDescriptionList.set(index, webServiceDescription);
        webServiceDescriptionMap.remove(removed.getWebServiceDescriptionName());
        webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
    }

    public void setWebServiceDescription(WebServiceDescription[] webServiceDescriptionArray) {
        clearWebServiceDescription();
        for (int i = 0; i < webServiceDescriptionArray.length; i++) {
            WebServiceDescription webServiceDescription = webServiceDescriptionArray[i];
            webServiceDescriptionList.add(webServiceDescription);
            webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
        }
    }

    public void clearWebServiceDescription() {
        webServiceDescriptionList.clear();
        webServiceDescriptionMap.clear();
    }
}
