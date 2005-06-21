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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.utils.JavaUtils;
import org.apache.geronimo.core.service.InvocationKey;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.webservices.MessageContextInvocationKey;
import org.openejb.EJBContainer;
import org.openejb.EJBInstanceContext;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.xml.sax.SAXException;

public class EJBContainerProvider extends RPCProvider {
    private final EJBContainer ejbContainer;

    public EJBContainerProvider(EJBContainer ejbContainer) {
        this.ejbContainer = ejbContainer;
    }

    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv, Object obj) throws Exception {

        RPCElement body = getBody(reqEnv, msgContext);
        OperationDesc operation = getOperationDesc(msgContext, body);

        int index = ejbContainer.getMethodIndex(operation.getMethod());
        AxisRpcInvocation invocation = new AxisRpcInvocation(operation, msgContext, index);
        invocation.put(MessageContextInvocationKey.INSTANCE, msgContext);
        SOAPMessage message = ((SOAPMessageContext) msgContext).getMessage();

        Object objRes = null;
        try {
            message.getSOAPPart().getEnvelope();
            msgContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.FALSE);

            InvocationResult invocationResult = ejbContainer.invoke(invocation);
            if (invocationResult.isException()) {
                throw (Throwable) invocationResult.getException();
            }
            objRes = invocationResult.getResult();
        } catch (Throwable throwable) {
            throw new AxisFault("Web Service EJB Invocation failed: method " + operation.getMethod(), throwable);
        }

        SOAPService service = msgContext.getService();
        ServiceDesc serviceDescription = service.getServiceDescription();
        ArrayList inoutParams = invocation.getInOutParams();
        RPCElement resBody = createResponseBody(body, msgContext, operation, serviceDescription, objRes, resEnv, inoutParams);
        resEnv.addBodyElement(resBody);
    }

    public Object getServiceObject(MessageContext msgContext, Handler service, String clsName, IntHolder scopeHolder) throws Exception {
        return ejbContainer;
    }

    /**
     * This class is intentionally not static  or top level class
     * as it leverages logic in RPCProvider
     *
     * @see org.apache.axis.providers.java.RPCProvider
     */
    private class AxisRpcInvocation implements EJBInvocation {
        private int index;

        // Valid in server-side interceptor stack once an instance has been identified
        private transient EJBInstanceContext instanceContext;

        // Valid in server-side interceptor stack once a TransactionContext has been created
        private transient TransactionContext transactionContext;

        private InvocationResult result;
        private Map attributes = new HashMap();
        private OperationDesc operation;
        private MessageContext messageContext;

        public AxisRpcInvocation(OperationDesc operation, MessageContext msgContext, int index) throws Exception {
            this.messageContext = msgContext;
            this.index = index;
            this.operation = operation;
        }

        public int getMethodIndex() {
            return index;
        }

        public EJBInterfaceType getType() {
            return EJBInterfaceType.WEB_SERVICE;
        }

        public Object[] getArguments() {
            try {
                return demarshallArguments();
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Cannot demarshal the soap parts into arguments").initCause(e);
            }
        }

        private Object[] demarshallArguments() throws Exception {
            SOAPMessage message = ((SOAPMessageContext) messageContext).getMessage();
            messageContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.TRUE);
            if (message != null) {
                message.saveChanges();
            }
            try {
                Message reqMsg = messageContext.getRequestMessage();
                SOAPEnvelope requestEnvelope = reqMsg.getSOAPEnvelope();
                RPCElement body = getBody(requestEnvelope, messageContext);
                body.setNeedDeser(true);
                Vector args = null;
                try {
                    args = body.getParams();
                } catch (SAXException e) {
                    if (e.getException() != null) throw e.getException();
                    throw e;
                }

                Object[] argValues = new Object[operation.getNumParams()];

                for (int i = 0; i < args.size(); i++) {
                    RPCParam rpcParam = (RPCParam) args.get(i);
                    Object value = rpcParam.getObjectValue();

                    ParameterDesc paramDesc = rpcParam.getParamDesc();

                    if (paramDesc != null && paramDesc.getJavaType() != null) {
                        value = JavaUtils.convert(value, paramDesc.getJavaType());
                        rpcParam.setObjectValue(value);
                    }
                    int order = (paramDesc == null || paramDesc.getOrder() == -1) ? i : paramDesc.getOrder();
                    argValues[order] = value;
                }
                return argValues;
            } finally {
                messageContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.FALSE);
            }
        }

        public Object getId() {
            return null;
        }

        public EJBInstanceContext getEJBInstanceContext() {
            return instanceContext;
        }

        public void setEJBInstanceContext(EJBInstanceContext instanceContext) {
            this.instanceContext = instanceContext;
        }

        public TransactionContext getTransactionContext() {
            return transactionContext;
        }

        public void setTransactionContext(TransactionContext transactionContext) {
            this.transactionContext = transactionContext;
        }

        public InvocationResult createResult(Object object) {
            messageContext.setPastPivot(true);
            try {
                Message requestMessage = messageContext.getRequestMessage();
                SOAPEnvelope requestEnvelope = requestMessage.getSOAPEnvelope();
                RPCElement requestBody = getBody(requestEnvelope, messageContext);

                Message responseMessage = messageContext.getResponseMessage();
                SOAPEnvelope responseEnvelope = responseMessage.getSOAPEnvelope();
                ServiceDesc serviceDescription = messageContext.getService().getServiceDescription();
                RPCElement responseBody = createResponseBody(requestBody, messageContext, operation, serviceDescription, object, responseEnvelope, getInOutParams());

                responseEnvelope.addBodyElement(responseBody);
            } catch (Exception e) {
                throw new RuntimeException("Failed while creating response message body", e);
            }

            return new SimpleInvocationResult(true, object);
        }

        public InvocationResult createExceptionResult(Exception exception) {
            messageContext.setPastPivot(true);

            SOAPFault fault = new SOAPFault(new AxisFault("Server", "Server Error", null, null));
            SOAPEnvelope envelope = new SOAPEnvelope();
            envelope.addBodyElement(fault);
            Message message = new Message(envelope);
            message.setMessageType(Message.RESPONSE);
            messageContext.setResponseMessage(message);

            return new SimpleInvocationResult(false, exception);
        }

        public Object get(InvocationKey key) {
            return attributes.get(key);
        }

        public void put(InvocationKey key, Object value) {
            attributes.put(key, value);
        }

        public ArrayList getInOutParams() {
            return new ArrayList(); //TODO collect out an inout params in demarshalArguments
        }
    }
}
