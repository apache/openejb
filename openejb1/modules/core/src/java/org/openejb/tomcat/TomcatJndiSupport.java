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
 * $Id: TomcatJndiSupport.java 2106 2005-08-26 21:04:51Z dblevins $
 */

package org.openejb.tomcat;

import org.openejb.OpenEJBException;
import org.openejb.RpcContainer;
import org.openejb.core.RpcContainerWrapper;
import org.openejb.core.DeploymentInfo;

import javax.naming.Context;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

/**
 * @version $Revision: 2106 $ $Date: 2005-08-26 14:04:51 -0700 (Fri, 26 Aug 2005) $
 */
public class TomcatJndiSupport extends RpcContainerWrapper {
    private final Class contextBindings;
    private final Method bindContext;
    private final Method bindThread;
    private final Method unbindThread;

    public TomcatJndiSupport(RpcContainer container) throws OpenEJBException {
        super(container);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            contextBindings = classLoader.loadClass("org.apache.naming.ContextBindings");
            bindContext = contextBindings.getMethod("bindContext", new Class[]{Object.class, Context.class, Object.class});
            bindThread = contextBindings.getMethod("bindThread", new Class[]{Object.class, Object.class});
            unbindThread = contextBindings.getMethod("unbindThread", new Class[]{Object.class, Object.class});
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Unable to setup Tomcat JNDI support.  Support requires the org.apache.naming.ContextBindings class to be available.");
        } catch (NoSuchMethodException e) {
            throw new OpenEJBException("Unable to setup Tomcat JNDI support.  Method of org.apache.naming.ContextBindings was not found:"+e.getMessage());
        }
    }

    public void init(Object containerId, HashMap deployments, Properties properties) throws OpenEJBException {
        super.init(containerId, deployments, properties);
        Collection collection = deployments.values();
        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
            DeploymentInfo deployment = (DeploymentInfo) iterator.next();

            setupDeployment(deployment);
        }
    }

    public void deploy(Object deploymentID, org.openejb.DeploymentInfo info) throws OpenEJBException {
        super.deploy(deploymentID, info);
        setupDeployment((DeploymentInfo)info);
    }

    public static Map contexts = new HashMap();

    private void setupDeployment(DeploymentInfo deployment) {
        // Set this container as the deployment's container
        // so calls from proxies created by the deploymentinfo
        // class come here first.
        deployment.setContainer(this);

        // Register this deployment's JNDI namespace in
        // Tomcat's list of context objects
        Object deploymentID = deployment.getDeploymentID();
        Context jndiEnc = deployment.getJndiEnc();
        bindContext(deploymentID, jndiEnc);
        contexts.put(deploymentID, jndiEnc);
    }

    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        try {
            // Tell tomcat that this deployment's context should
            // be used for this call
            bindThread(deployID);
            return super.invoke(deployID, callMethod, args, primKey, securityIdentity);
        } finally {
            unbindThread(deployID);
        }
    }

    public void bindContext(Object name, Context context) {
        try {
            bindContext.invoke(null, new Object[]{name, context, name});
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "bindContext");
        }
    }

    public void bindThread(Object name) {
        try {
            bindThread.invoke(null, new Object[]{name, name});
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "bindThread");
        }
    }

    public void unbindThread(Object name) {
        try {
            unbindThread.invoke(null, new Object[]{name, name});
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "unbindThread");
        }
    }

    private RuntimeException convertToRuntimeException(Throwable e, String methodName) {
        if (e instanceof InvocationTargetException){
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException){
                return (RuntimeException) cause;
            } else {
                e = cause;
            }
        }
        return new RuntimeException("ContextBindings."+methodName, e);
    }
}
