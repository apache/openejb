/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import javax.management.MalformedObjectNameException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.openejb.DeploymentIndex;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.proxy.ProxyInfo;

/**
 */
class JndiRequestHandler implements ResponseCodes, RequestMethods {

    private final DeploymentIndex deploymentIndex;
    private static final Log log = LogFactory.getLog(JndiRequestHandler.class);

    JndiRequestHandler(DeploymentIndex deploymentIndex) {
        this.deploymentIndex = deploymentIndex;
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        JNDIRequest req = new JNDIRequest();
        JNDIResponse res = new JNDIResponse();

        // We are assuming that the request method is JNDI_LOOKUP
        // TODO: Implement the JNDI_LIST and JNDI_LIST_BINDINGS methods


        try {
            req.readExternal(in);
        } catch (Throwable e) {
            replyWithFatalError(out, e, "Failed to read request");
            return;
        }

        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        try {
            if (req.getClientModuleID() != null) {
                contextClassLoader = thread.getContextClassLoader();
                try {
                    URI uri = new URI(req.getClientModuleID());
                    AbstractName abstractName = new AbstractName(uri);
                    ClassLoader classLoader = KernelRegistry.getSingleKernel().getClassLoaderFor(abstractName);
                    thread.setContextClassLoader(classLoader);
                } catch (Throwable e) {
                    replyWithFatalError(out, e, "Failed to set the correct classloader");
                    return;
                }
            }

            try {
                switch (req.getRequestMethod()) {
                    case JNDI_LOOKUP:
                        doLookup(req, res);
                        break;
                    case JNDI_LIST:
                        doList(req, res);
                        break;
                    case JNDI_LIST_BINDINGS:
                        doListBindings(req, res);
                        break;
                    default: throw new UnsupportedOperationException("Request method not supported: "+req.getRequestMethod());
                }
            } catch (Exception e) {
                log.error("JNDI request error", e);
                res.setResponseCode(JNDI_ERROR);
                res.setResult(e);
            } finally {
                try {
                    res.writeExternal(out);
                } catch (Throwable t) {
                    log.error("Failed to write to JNDIResponse", t);
                }
                if (req.getClientModuleID() != null) {
                    thread.setContextClassLoader(contextClassLoader);
                }
            }
        } finally {
            thread.setContextClassLoader(contextClassLoader);
        }
    }

    private void doListBindings(JNDIRequest req, JNDIResponse res) {
        //TODO Implement listbindings
        throw new UnsupportedOperationException("List bindings operation not implemented");
    }

    private void doList(JNDIRequest req, JNDIResponse res) {
        //TODO Implement list
        throw new UnsupportedOperationException("List operation not implemented");
    }

    private void doLookup(JNDIRequest req, JNDIResponse res) throws Exception {
        String jndiName = req.getRequestString();
        if (jndiName.startsWith("/")) {
            jndiName = jndiName.substring(1);
        }

        if (req.getClientModuleID() != null) {
            try {
                URI uri = new URI(req.getClientModuleID());
                AbstractName abstractName = new AbstractName(uri);
                Object context = KernelRegistry.getSingleKernel().getAttribute(abstractName, "componentContext");

                res.setResponseCode(JNDI_CONTEXT_TREE);
                res.setResult(context);

            } catch (MalformedObjectNameException e) {
                throw (Exception)new NamingException("Invalid client module id in request: "+req.getClientModuleID()).initCause(e);
            } catch (Exception e) {
                throw (Exception)new NamingException("Unable to retrieve context for module: "+req.getClientModuleID()).initCause(e);
            }
        } else {
            int index = deploymentIndex.getDeploymentIndexByJndiName(jndiName);
            if (index <= 0) {
                // name not found... check if an object name was sent directly
                // Note: do not use the JNDI name since it has the leading '/' character stripped off
                index = deploymentIndex.getDeploymentIndex(req.getRequestString());
            }
            if (index > 0) {
                RpcEjbDeployment deployment = deploymentIndex.getDeployment(index);
                ProxyInfo info = deployment.getProxyInfo();

                res.setResponseCode(JNDI_EJBHOME);
                EJBMetaDataImpl metaData = new EJBMetaDataImpl(info.getHomeInterface(),
                        info.getRemoteInterface(),
                        info.getPrimaryKeyClass(),
                        info.getComponentType(),
                        info.getContainerID(),
                        index);
                res.setResult(metaData);
            } else {
                res.setResponseCode(JNDI_NOT_FOUND);
            }

        }
    }
    private void replyWithFatalError(ObjectOutputStream out, Throwable error, String message) {
        log.error(message, error);

        JNDIResponse res = new  JNDIResponse(JNDI_ERROR, error);
        try {
            res.writeExternal(out);
        } catch (java.io.IOException ie) {
            log.error("Failed to write JNDIResponse", ie);
        }
    }
}