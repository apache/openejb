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
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.slsb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.io.ObjectStreamClass;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.webservices.MessageContextInvocationKey;
import org.openejb.server.axis.EJBContainerProvider;

/**
 * @version $Revision$ $Date$
 */
public class HandlerChainInterceptor implements Interceptor {
    private final Interceptor next;
    private final List handlerInfoList;
    private final String[] roles;

    public HandlerChainInterceptor(Interceptor next, List handlerInfoList, String[] roles) {
        this.next = next;
        this.handlerInfoList = Collections.unmodifiableList(handlerInfoList);
        this.roles = roles;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        MessageContext messageContext = (MessageContext) invocation.get(MessageContextInvocationKey.INSTANCE);

        if (messageContext == null) {
            return next.invoke(invocation);
        }

        HandlerChain handlerChain = new org.apache.axis.handlers.HandlerChainImpl(handlerInfoList);
        // new HandlerChainImpl(handlerInfoList, roles);

        InvocationResult invocationResult;
        try {
            try {
                if (handlerChain.handleRequest(messageContext)) {
                    invocationResult = next.invoke(invocation);

                } else {
                    /* The Handler implementation class has the responsibility of setting
                     * the response SOAP message in the handleRequest method and perform
                     * additional processing in the handleResponse method.
                     */
                    invocationResult = new SimpleInvocationResult(true, null);
                }
            } catch (SOAPFaultException e) {
                handlerChain.handleFault(messageContext);
                return new SimpleInvocationResult(false, e);
            }

            handlerChain.handleResponse(messageContext);
        } finally {
            handlerChain.destroy();
        }

        return invocationResult;
    }

    static class HandlerChainImpl extends ArrayList implements javax.xml.rpc.handler.HandlerChain {
        private String[] roles;
        private Stack invokedHandlers = new Stack();

        public HandlerChainImpl(List handlerInfos, String[] roles) {
            this.roles = roles;
            for (int i = 0; i < handlerInfos.size(); i++) {
                HandlerInfo handlerInfo = (HandlerInfo) handlerInfos.get(i);
                try {
                    Handler handler = (Handler) handlerInfo.getHandlerClass().newInstance();
                    handler.init(handlerInfo);
                    add(handler);
                } catch (Exception e) {
                    throw new JAXRPCException("Unable to initialize handler class: " + handlerInfo.getHandlerClass().getName(), e);
                }
            }
        }

        public String[] getRoles() {
            return roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles != null ? roles : new String[]{};
        }

        public void init(Map map) {
        }

        public void destroy() {
            for (Iterator iterator = invokedHandlers.iterator(); iterator.hasNext();) {
                Handler handler = (Handler) iterator.next();
                handler.destroy();
            }
            invokedHandlers.clear();
            clear();
        }

        public boolean handleRequest(MessageContext context) {
            MessageSnapshot snapshot = new MessageSnapshot(context);
            try {
                for (int i = 0; i < size(); i++) {
                    Handler currentHandler = (Handler) get(i);
                    invokedHandlers.push(currentHandler);
                    try {
                        if (currentHandler.handleRequest(context) == false) {
                            return false;
                        }
                    } catch (SOAPFaultException e) {
                        throw e;
                    }
                }
                return true;
            } finally {
                if (!snapshot.equals(context)) {
                    throw new IllegalStateException("The soap message operation or arguments were illegally modified by the HandlerChain");
                }
            }
        }

        public boolean handleResponse(MessageContext context) {
            MessageSnapshot snapshot = new MessageSnapshot(context);
            try {
                for (Iterator iterator = invokedHandlers.iterator(); iterator.hasNext();) {
                    Handler handler = (Handler) iterator.next();
                    if (handler.handleResponse(context) == false) {
                        return false;
                    }
                }
                return true;
            } finally {
                if (!snapshot.equals(context)) {
                    throw new IllegalStateException("The soap message operation or arguments were illegally modified by the HandlerChain");
                }
            }
        }

        public boolean handleFault(MessageContext context) {
            MessageSnapshot snapshot = new MessageSnapshot(context);
            try {
                for (Iterator iterator = invokedHandlers.iterator(); iterator.hasNext();) {
                    Handler handler = (Handler) iterator.next();
                    if (handler.handleFault(context) == false) {
                        return false;
                    }
                }
                return true;
            } finally {
                if (!snapshot.equals(context)) {
                    throw new IllegalStateException("The soap message operation or arguments were illegally modified by the HandlerChain");
                }
            }
        }
    }

    /**
     * Handlers cannot:
     * <p/>
     * - re-target a request to a different component.
     * - change the operation
     * - change the message part types
     * - change the number of message parts.
     */
    static class MessageSnapshot {
        private final String operationName;
        private final List parameterNames;

        public MessageSnapshot(MessageContext soapMessage) {

            SOAPMessage message = ((SOAPMessageContext) soapMessage).getMessage();
            if (message == null || message.getSOAPPart() == null) {
                operationName = null;
                parameterNames = null;
            } else {
                SOAPBody body = getBody(message);

                SOAPElement operation = ((SOAPElement) body.getChildElements().next());
                this.operationName = operation.getElementName().toString();

                this.parameterNames = new ArrayList();
                for (Iterator i = operation.getChildElements(); i.hasNext();) {
                    SOAPElement parameter = (SOAPElement) i.next();
                    String element = parameter.getElementName().toString();
                    parameterNames.add(element);
                }
            }
        }

        private SOAPBody getBody(SOAPMessage message) {
            try {
                return message.getSOAPPart().getEnvelope().getBody();
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean equals(Object obj) {
            return (obj instanceof SOAPMessageContext) ? equals((SOAPMessageContext) obj) : false;
        }

        private boolean equals(SOAPMessageContext soapMessage) {
            SOAPMessage message = soapMessage.getMessage();

            if (operationName == null) {
                return message == null || message.getSOAPPart() == null;
            }

            SOAPBody body = getBody(message);

            // Handlers can't change the operation
            SOAPElement operation = ((SOAPElement) body.getChildElements().next());
            if (!this.operationName.equals(operation.getElementName().toString())) {
                return false;
            }

            Iterator parameters = operation.getChildElements();
            for (Iterator i = parameterNames.iterator(); i.hasNext();) {
                // Handlers can't remove parameters
                if (!parameters.hasNext()) {
                    return false;
                }

                String original = (String) i.next();
                SOAPElement parameter = (SOAPElement) parameters.next();
                // Handlers can't change parameter types
                if (parameter == null || !original.equals(parameter.getElementName().toString())) {
                    return false;
                }
            }

            // Handlers can't add parameters
            return !parameters.hasNext();
        }
    }
}