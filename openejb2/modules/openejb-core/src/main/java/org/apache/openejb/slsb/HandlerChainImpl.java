/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.slsb;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of HandlerChain
 */
public class HandlerChainImpl extends ArrayList implements HandlerChain {

    private List handlerInfos = new ArrayList();

    private String[] roles;

    private int falseIndex = -1;

    public HandlerChainImpl() {
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public void init(Map map) {
        // DO SOMETHING WITH THIS
    }

    public HandlerChainImpl(List handlerInfos) {
        this.handlerInfos = handlerInfos;
        for (int i = 0; i < handlerInfos.size(); i++) {
            add(newHandler(getHandlerInfo(i)));
        }
    }

    public void addNewHandler(String className, Map config) {
        try {
            Class handlerClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            HandlerInfo handlerInfo = new HandlerInfo(handlerClass,
                    config,
                    null);
            handlerInfos.add(handlerInfo);
            add(newHandler(handlerInfo));
        } catch (Exception ex) {
            throw new JAXRPCException("Unable to create handler of type " + className, ex);
        }
    }

    public boolean handleFault(MessageContext _context) {
        SOAPMessageContext context = (SOAPMessageContext) _context;

        for (int i = size() - 1; i >= 0; i--) {
            if (!getHandlerInstance(i).handleFault(context)) {
                return false;
            }
        }
        return true;
    }

    public boolean handleRequest(MessageContext _context) {
        SOAPMessageContext context = (SOAPMessageContext) _context;

        falseIndex = -1;
        for (int i = 0; i < size(); i++) {
            Handler currentHandler = getHandlerInstance(i);
            try {
                if (!currentHandler.handleRequest(context)) {
                    falseIndex = i;
                    return false;
                }
            } catch (SOAPFaultException sfe) {
                throw sfe;
            }
        }
        return true;
    }

    public boolean handleResponse(MessageContext context) {
        int endIdx = size() - 1;
        if (falseIndex != -1) {
            endIdx = falseIndex;
        }
        for (int i = endIdx; i >= 0; i--) {
            if (!getHandlerInstance(i).handleResponse(context)) {
                return false;
            }
        }
        return true;
    }

    public void destroy() {
        for (int i = 0; i < size(); i++) {
            getHandlerInstance(i).destroy();
        }
        clear();
    }

    private Handler getHandlerInstance(int index) {
        return (Handler) get(index);
    }

    private HandlerInfo getHandlerInfo(int index) {
        return (HandlerInfo) handlerInfos.get(index);
    }

    private Handler newHandler(HandlerInfo handlerInfo) {
        try {
            Handler handler = (Handler) handlerInfo.getHandlerClass().newInstance();
            handler.init(handlerInfo);
            return handler;
        } catch (Exception ex) {
            throw new JAXRPCException("Unable to create handler of type " + handlerInfo.getHandlerClass().toString(), ex);
        }
    }
}
