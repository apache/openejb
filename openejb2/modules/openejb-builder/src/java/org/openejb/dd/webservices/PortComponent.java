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

public class PortComponent {
    private String portComponentName;
    private String wsdlPort;
    private String serviceEndpointInterface;
    private ServiceImplBean serviceImplBean;

    /**
     * List of Handler objects
     *
     * @see org.openejb.dd.webservices.Handler
     */
    private ArrayList handlerList = new ArrayList();
    /**
     * Map of Handler objects indexed by handlerName
     *
     * @see org.openejb.dd.webservices.Handler#getHandlerName
     */
    private HashMap handlerMap = new HashMap();

    public void addHandler(Handler handler) throws IndexOutOfBoundsException {
        handlerList.add(handler);
        handlerMap.put(handler.getHandlerName(), handler);
    }

    public void addHandler(int index, Handler handler) throws IndexOutOfBoundsException {
        handlerList.add(index, handler);
        handlerMap.put(handler.getHandlerName(), handler);
    }

    public boolean removeHandler(Handler handler) {
        handlerMap.remove(handler.getHandlerName());
        return handlerList.remove(handler);
    }

    public Handler getHandler(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > handlerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (Handler) handlerList.get(index);
    }

    public Handler[] getHandler() {
        int size = handlerList.size();
        Handler[] mArray = new Handler[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Handler) handlerList.get(index);
        }
        return mArray;
    }

    public Handler getHandler(String handlerName) {
        return (Handler) handlerMap.get(handlerName);
    }

    public void setHandler(int index, Handler handler) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > handlerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        Handler removed = (Handler) handlerList.set(index, handler);
        handlerMap.remove(removed.getHandlerName());
        handlerMap.put(handler.getHandlerName(), handler);
    }

    public void setHandler(Handler[] handlerArray) {
        handlerList.clear();
        for (int i = 0; i < handlerArray.length; i++) {
            Handler handler = handlerArray[i];
            handlerList.add(handler);
            handlerMap.put(handler.getHandlerName(), handler);
        }
    }

    public void clearHandler() {
        handlerList.clear();
        handlerMap.clear();
    }

    public String getPortComponentName() {
        return portComponentName;
    }

    public void setPortComponentName(String portComponentName) {
        this.portComponentName = portComponentName;
    }

    public String getWsdlPort() {
        return wsdlPort;
    }

    public void setWsdlPort(String wsdlPort) {
        this.wsdlPort = wsdlPort;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public ServiceImplBean getServiceImplBean() {
        return serviceImplBean;
    }

    public void setServiceImplBean(ServiceImplBean serviceImplBean) {
        this.serviceImplBean = serviceImplBean;
    }
}
