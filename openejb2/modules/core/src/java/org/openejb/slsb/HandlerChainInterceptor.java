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
import java.util.List;
import java.util.Map;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.soap.SOAPFaultException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.webservices.MessageContextInvocationKey;

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

        HandlerChain handlerChain = new HandlerChainImpl(handlerInfoList, roles);

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

        private int lastInvoked = -1;

        public HandlerChainImpl(List handlerInfos, String[] roles) {
            this.roles = roles;
            for (int i = 0; i < handlerInfos.size(); i++) {
                HandlerInfo handlerInfo = (HandlerInfo) handlerInfos.get(i);
                try {
                    Handler handler1 = (Handler) handlerInfo.getHandlerClass().newInstance();
                    handler1.init(handlerInfo);
                    add(handler1);
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
            for (int i = (lastInvoked != -1) ? lastInvoked : size() - 1; i >= 0; i--) {
                getHandler(i).destroy();
            }
            lastInvoked = -1;
            clear();
        }

        public boolean handleFault(MessageContext context) {
            for (int i = (lastInvoked != -1) ? lastInvoked : size() - 1; i >= 0; i--) {
                if (getHandler(i).handleFault(context) == false) {
                    return false;
                }
            }
            return true;
        }

        public boolean handleRequest(MessageContext context) {
            for (int i = 0; i < size(); i++) {
                Handler currentHandler = getHandler(i);
                try {
                    if (currentHandler.handleRequest(context) == false) {
                        lastInvoked = i;
                        return false;
                    }
                } catch (SOAPFaultException e) {
                    lastInvoked = i;
                    throw e;
                }
            }
            return true;
        }

        public boolean handleResponse(MessageContext context) {
            for (int i = (lastInvoked != -1) ? lastInvoked : size() - 1; i >= 0; i--) {
                if (getHandler(i).handleResponse(context) == false) {
                    return false;
                }
            }
            return true;
        }

        private Handler getHandler(int index) {
            return (Handler) get(index);
        }

    }
}
