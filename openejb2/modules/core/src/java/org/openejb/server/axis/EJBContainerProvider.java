/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.openejb.server.axis;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import javax.xml.rpc.holders.IntHolder;

import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.MessageContext;
import org.apache.axis.Handler;
import org.apache.axis.AxisFault;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.geronimo.webservices.MessageContextInvocationKey;
import org.apache.geronimo.core.service.InvocationResult;
import org.openejb.EJBContainer;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.EJBInterfaceType;

public class EJBContainerProvider extends RPCProvider {
    private final EJBContainer ejbContainer;

    public EJBContainerProvider(EJBContainer ejbContainer) {
        this.ejbContainer = ejbContainer;
    }

    public Object getServiceObject(MessageContext msgContext, Handler service, String clsName, IntHolder scopeHolder) throws Exception {
        return ejbContainer;
    }

    protected Object invokeMethod(MessageContext msgContext, Method method, Object obj, Object[] params) throws Exception {
        int index = ejbContainer.getMethodIndex(method);
        EJBInvocation invocation = new EJBInvocationImpl(EJBInterfaceType.WEB_SERVICE, null, index, params);
        javax.xml.rpc.handler.MessageContext messageContext = new SimpleMessageContext(new HashMap());
        invocation.put(MessageContextInvocationKey.INSTANCE, messageContext);
        try {
            InvocationResult invocationResult = ejbContainer.invoke(invocation);
            if (invocationResult.isException()) {
                throw (Throwable) invocationResult.getException();
            }
            return invocationResult.getResult();
        } catch (Throwable throwable) {
            if (throwable instanceof Exception){
                throw (Exception) throwable;
            } else {
                throw (Error) throwable;
            }
        }
    }

    private static class SimpleMessageContext implements javax.xml.rpc.handler.MessageContext {

        private final Map properties;

        public SimpleMessageContext(Map properties) {
            this.properties = new HashMap(properties);
        }

        public boolean containsProperty(String name) {
            return properties.containsKey(name);
        }

        public Object getProperty(String name) {
            return properties.get(name);
        }

        public Iterator getPropertyNames() {
            return properties.keySet().iterator();
        }

        public void removeProperty(String name) {
            properties.remove(name);
        }

        public void setProperty(String name, Object value) {
            properties.put(name, value);
        }
    }

}
