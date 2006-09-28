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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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

package org.apache.openejb.server.ejbd;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.openejb.DeploymentIndex;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.corba.ORBRef;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.EJBResponse;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.cluster.server.ClusteredInvocationResult;
import org.apache.openejb.proxy.BaseEJB;
import org.apache.openejb.proxy.ProxyInfo;


class EjbRequestHandler implements ResponseCodes, RequestMethods {

    private static final Log log = LogFactory.getLog(EjbRequestHandler.class);
    private final DeploymentIndex deploymentIndex;
    private final Collection orbRefs;


    EjbRequestHandler(DeploymentIndex deploymentIndex, Collection orbRefs) {
        this.orbRefs = orbRefs;

        if (deploymentIndex == null) {
            deploymentIndex = DeploymentIndex.getInstance();
        }
        this.deploymentIndex = deploymentIndex;
    }


    public void processRequest(ObjectInputStream input, ObjectOutputStream out) {

        EJBObjectInputStream in = (EJBObjectInputStream) input;

        ORBRef orbRef = null;
        if (orbRefs != null) {
            Iterator iterator = orbRefs.iterator();
            if (iterator.hasNext()) {
                orbRef = (ORBRef) iterator.next();
            }
        }
        EjbInvocationStream req = new EjbInvocationStream(orbRef);

        EJBResponse res = new EJBResponse();

        // TODO:2: This method can throw a large number of exceptions, we should
        // be prepared to handle them all.  Look in the ObejctOutputStream code
        // for a full list of the exceptions thrown.
        // java.io.WriteAbortedException  can be thrown containing a
        //
        try {
            req.readExternal(in);

            /*
                } catch (java.io.WriteAbortedException e){
                    if ( e.detail instanceof java.io.NotSerializableException){
                        //TODO:1: Log this warning better. Include information on what bean is to blame

                        throw new Exception("Client attempting to serialize unserializable object: "+ e.detail.getMessage());
                    } else {
                        throw e.detail;
                    }
                } catch (java.io.EOFException e) {
                    throw new Exception("Reached the end of the stream before the full request could be read");
                } catch (Throwable t){
                    throw new Exception("Cannot read client request: "+ t.getClass().getName()+" "+ t.getMessage());
                }
            */

        } catch (Throwable t) {
            replyWithFatalError(out, t, "Error caught during request processing");
            return;
        }

        CallContext call = null;
        RpcEjbDeployment container = null;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            try {
                container = getContainer(req);
                ClassLoader cl = container.getClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
                in.setClassLoader(cl);

                Method methodInstance = req.getMethodInstance();
                int methodIndex = container.getMethodIndex(methodInstance);
                req.setMethodIndex(methodIndex);

                /**
                 * The identification principal contains the subject id.  Use this
                 * id to obtain the registered subject.
                 */
                Subject subject = null;
                IdentificationPrincipal principal = (IdentificationPrincipal) req.getClientIdentity();
                if (principal != null && principal.getId() != null) {
                    subject = ContextManager.getRegisteredSubject(principal.getId());
                } else {
                    subject = container.getDefaultSubject();
                }

                if (subject != null) {
                    ContextManager.setCallers(subject, subject);
                }

                log.debug("setting cl=" + cl + " for " + container.getContainerId());
            } catch (RemoteException e) {
                replyWithFatalError
                        (out, e, "No such deployment");
                return;
                /*
                    logger.warn( req + "No such deployment: "+e.getMessage());
                    res.setResponse( EJB_SYS_EXCEPTION, e);
                    res.writeExternal( out );
                    return;
                */
            } catch (Throwable t) {
                replyWithFatalError(out, t, "unknown error occured while retrieving deployment");
                return;
            }

            try {
                call = CallContext.getCallContext();
                call.setEJBRequest(req);
                call.setContainer(container);
            } catch (Throwable t) {
                replyWithFatalError(out, t, "Unable to set the thread context for this request");
                return;
            }

            //logger.info( "EJB REQUEST : "+req );

            try {
                switch (req.getRequestMethod()) {
                    // Remote interface methods
                    case EJB_OBJECT_BUSINESS_METHOD:
                        doEjbObject_BUSINESS_METHOD(req, res);
                        break;

                        // Home interface methods
                    case EJB_HOME_METHOD:
                        doEjbHome_METHOD(req, res);
                        break;

                    case EJB_HOME_CREATE:
                        doEjbHome_CREATE(req, res);
                        break;

                    case EJB_HOME_FIND:
                        doEjbHome_FIND(req, res);
                        break;

                        // javax.ejb.EJBObject methods
                    case EJB_OBJECT_GET_EJB_HOME:
                        doEjbObject_GET_EJB_HOME(req, res);
                        break;

                    case EJB_OBJECT_GET_HANDLE:
                        doEjbObject_GET_HANDLE(req, res);
                        break;

                    case EJB_OBJECT_GET_PRIMARY_KEY:
                        doEjbObject_GET_PRIMARY_KEY(req, res);
                        break;

                    case EJB_OBJECT_IS_IDENTICAL:
                        doEjbObject_IS_IDENTICAL(req, res);
                        break;

                    case EJB_OBJECT_REMOVE:
                        doEjbObject_REMOVE(req, res);
                        break;

                        // javax.ejb.EJBHome methods
                    case EJB_HOME_GET_EJB_META_DATA:
                        doEjbHome_GET_EJB_META_DATA(req, res);
                        break;

                    case EJB_HOME_GET_HOME_HANDLE:
                        doEjbHome_GET_HOME_HANDLE(req, res);
                        break;

                    case EJB_HOME_REMOVE_BY_HANDLE:
                        doEjbHome_REMOVE_BY_HANDLE(req, res);
                        break;

                    case EJB_HOME_REMOVE_BY_PKEY:
                        doEjbHome_REMOVE_BY_PKEY(req, res);
                        break;
                }
            } catch (InvalidateReferenceException e) {
                res.setResponse(EJB_SYS_EXCEPTION, e.getCause());
            } catch (org.apache.openejb.ApplicationException e) {
                res.setResponse(EJB_APP_EXCEPTION, e.getCause());
            } catch (org.apache.openejb.SystemException e) {
                res.setResponse(EJB_ERROR, e.getCause());

                // TODO:2: This means a severe error occured in OpenEJB
                // we should restart the container system or take other
                // aggressive actions to attempt recovery.
                log.fatal(req + ": OpenEJB encountered an unknown system error in container: ", e);
            } catch (Throwable t) {
                //System.out.println(req+": unknown error in container: ");
                replyWithFatalError(out, t, "Unknown error in container");
                return;
            } finally {
                log.debug("EJB RESPONSE: " + res);
                try {
                    res.writeExternal(out);
                } catch (java.io.IOException e) {
                    if (e instanceof NotSerializableException && res.getResult() != null) {
                        log.fatal("Invocation result object is not serializable: " + res.getResult().getClass().getName(), e);
                    } else {
                        log.fatal("Couldn't write EjbResponse to output stream", e);
                    }
                }
                call.reset();
            }
        } finally {
            ContextManager.clearCallers();
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }


    private Object invoke(EJBRequest req, EJBResponse res) throws Throwable {

        CallContext call = CallContext.getCallContext();
        EjbDeployment container = call.getContainer();

        // Prepare yourself ...
        // for you are about to enter ...
        // the Twilight Zone.

        InvocationResult result = null;
        try {
            result = container.invoke((EjbInvocationStream) req);
        } catch (Throwable t) {
            RemoteException re;
            if (t instanceof RemoteException) {
                re = (RemoteException) t;
            } else {
                re = new RemoteException("The bean encountered a non-application exception. method", t);
            }

            throw new InvalidateReferenceException(re);

        }

        if (result instanceof ClusteredInvocationResult) {
            ClusteredInvocationResult clusteredResult = (ClusteredInvocationResult) result;
            res.setServers(clusteredResult.getServers());
        }

        if (result.isException()) {
            throw new org.apache.openejb.ApplicationException(result.getException());
        } else {
            return result.getResult();
        }

//        return container.invoke((EJBInvocationStream)req);
//        return container.invoke(req.getMethodInstance(), req.getMethodParameters(), req.getPrimaryKey());

    }


    protected void doEjbObject_BUSINESS_METHOD(EJBRequest req, EJBResponse res) throws Throwable {

        Object result = invoke(req, res);

        res.setResponse(EJB_OK, result);
    }



    // Home interface methods

    protected void doEjbHome_METHOD(EJBRequest req, EJBResponse res) throws Throwable {
        Object result = invoke(req, res);

        res.setResponse(EJB_OK, result);
    }

    protected void doEjbHome_CREATE(EJBRequest req, EJBResponse res) throws Throwable {

        Object result = invoke(req, res);

        if (result instanceof BaseEJB) {
            BaseEJB proxy = (BaseEJB) result;
            ProxyInfo info = proxy.getProxyInfo();
            res.setResponse(EJB_OK, info.getPrimaryKey());
        } else {
            // There should be no else, the entity should be found
            // or and exception should be thrown.
            //TODO:3: Localize all error messages in an separate file.
            result = new RemoteException("The bean is not EJB compliant.  The should be created or and exception should be thrown.");
            log.error(req + "The bean is not EJB compliant.  The should be created or and exception should be thrown.");
            res.setResponse(EJB_SYS_EXCEPTION, result);
        }
    }


    /**
     * EJB 1.1 --
     * 9.1.8 Finder method return type
     * <p/>
     * 9.1.8.1 Single-object finder
     * <p/>
     * Some finder methods (such as ejbFindByPrimaryKey) are designed to return
     * at most one entity object. For these single-object finders, the result type
     * of the find<METHOD>(...)method defined in the entity bean�s home interface
     * is the entity bean�s remote interface. The result type of the corresponding
     * ejbFind<METHOD>(...) method defined in the entity�s implementation class is
     * the entity bean�s primary key type.
     * <p/>
     * 9.1.8.2 Multi-object finders
     * <p/>
     * Some finder methods are designed to return multiple entity objects. For
     * these multi-object finders, the result type of the find<METHOD>(...)method
     * defined in the entity bean�s home interface is a col-lection of objects
     * implementing the entity bean�s remote interface. The result type of the
     * corresponding ejbFind<METHOD>(...) implementation method defined in the
     * entity bean�s implementation class is a collection of objects of the entity
     * bean�s primary key type.
     * <p/>
     * The Bean Provider can choose two types to define a collection type for a finder:
     * <p/>
     * � the JDK� 1.1 java.util.Enumeration interface
     * � the Java� 2 java.util.Collection interface
     * <p/>
     * A Bean Provider that wants to ensure that the entity bean is compatible
     * with containers and clients based on JDK TM 1.1 software must use the
     * java.util.Enumeration interface for the finder�s result type.
     */

    protected void doEjbHome_FIND(EJBRequest req, EJBResponse res) throws Throwable {

        Object result = invoke(req,res);

        /* Multiple instances found */
        if (result instanceof Collection) {

            Object[] objects = ((Collection) result).toArray();

            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof ProxyInfo) {
                    objects[i] = ((ProxyInfo) objects[i]).getPrimaryKey();
                } else if (objects[i] instanceof BaseEJB) {
                    ProxyInfo proxy = ((BaseEJB) objects[i]).getProxyInfo();
                    objects[i] = proxy.getPrimaryKey();
                }
            }
            res.setResponse(EJB_OK_FOUND_COLLECTION, objects);

        } else if (result instanceof java.util.Enumeration) {
            java.util.Enumeration resultAsEnum = (java.util.Enumeration) result;
            java.util.List listOfPKs = new java.util.ArrayList();
            while (resultAsEnum.hasMoreElements()) {
                Object pk = resultAsEnum.nextElement();
                if (pk instanceof ProxyInfo) {
                    pk = ((ProxyInfo) pk).getPrimaryKey();
                } else if (pk instanceof BaseEJB) {
                    ProxyInfo proxy = ((BaseEJB) pk).getProxyInfo();
                    pk = proxy.getPrimaryKey();
                }
                listOfPKs.add(pk);
            }

            res.setResponse(EJB_OK_FOUND_ENUMERATION, listOfPKs.toArray(new Object[listOfPKs.size()]));
            /* Single instance found */
        } else if (result instanceof ProxyInfo) {
            result = ((ProxyInfo) result).getPrimaryKey();
            res.setResponse(EJB_OK_FOUND, result);
        } else if (result instanceof BaseEJB) {
            BaseEJB proxy = (BaseEJB) result;
            ProxyInfo info = proxy.getProxyInfo();
            res.setResponse(EJB_OK_FOUND, info.getPrimaryKey());
        } else if (null == result) {
            res.setResponse(EJB_OK_FOUND, null);
        } else {
            //TODO:3: Localize all error messages in an separate file.
            // TODO:4: It should provide more info on the wrong method
            final String message = "The bean is not EJB compliant. " +
                    "The finder method [" + req.getMethodInstance().getName() + "] is declared " +
                    "to return neither Collection nor the Remote Interface, " +
                    "but [" + result.getClass().getName() + "]";

            result = new RemoteException(message);
            log.error(req + " " + message);
            res.setResponse(EJB_SYS_EXCEPTION, result);
        }
    }



    // javax.ejb.EJBObject methods

    private void doEjbObject_GET_EJB_HOME(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }


    private void doEjbObject_GET_HANDLE(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }


    private void doEjbObject_GET_PRIMARY_KEY(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }


    private void doEjbObject_IS_IDENTICAL(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }


    private void doEjbObject_REMOVE(EJBRequest req, EJBResponse res) throws Throwable {
        invoke(req, res);
        res.setResponse(EJB_OK, null);
    }



    // javax.ejb.EJBHome methods

    private void doEjbHome_GET_EJB_META_DATA(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }


    private void doEjbHome_GET_HOME_HANDLE(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }


    private void doEjbHome_REMOVE_BY_HANDLE(EJBRequest req, EJBResponse res) throws Throwable {
        invoke(req, res);
        res.setResponse(EJB_OK, null);
    }


    private void doEjbHome_REMOVE_BY_PKEY(EJBRequest req, EJBResponse res) throws Throwable {
        invoke(req, res);
        res.setResponse(EJB_OK, null);
    }


    private void checkMethodAuthorization(EJBRequest req, EJBResponse res) throws Exception {

        // Nothing to do here other than check to see if the client
        // is authorized to call this method
        // TODO:3: Keep a cache in the client-side handler of methods it can't access


//        SecurityService sec = OpenEJB.getSecurityService();
//        CallContext caller  = CallContext.getCallContext();
//        DeploymentInfo di   = caller.getDeploymentInfo();
//        String[] authRoles  = di.getAuthorizedRoles( req.getMethodInstance() );
//
//        if (sec.isCallerAuthorized( req.getClientIdentity(), authRoles )) {

        res.setResponse(EJB_OK, null);

//        } else {
//            this.daemon.logger.info(req + "Unauthorized Access by Principal Denied");
//            res.setResponse( EJB_APP_EXCEPTION , new RemoteException("Unauthorized Access by Principal Denied") );
//        }

    }


    private RpcEjbDeployment getContainer(EJBRequest req) throws RemoteException {

        RpcEjbDeployment container = null;

        if (req.getContainerCode() > 0) {
            container = deploymentIndex.getDeployment(req.getContainerCode());
            if (container == null) {
                throw new RemoteException("The deployement with this ID is null");
            }
            req.setContainerID((String) container.getContainerId());
            return container;
        }

        if (req.getContainerID() == null) {
            throw new RemoteException("Invalid deployment id and code: id=" + req.getContainerID() + ": code=" + req.getContainerCode());
        }


        int idCode = deploymentIndex.getDeploymentIndex(req.getContainerID());

        if (idCode == -1) {
            throw new RemoteException("No such deployment id and code: id=" + req.getContainerID() + ": code=" + req.getContainerCode());
        }

        req.setContainerCode(idCode);

        if (req.getContainerCode() < 0 || req.getContainerCode() >= deploymentIndex.length()) {
            throw new RemoteException("Invalid deployment id and code: id=" + req.getContainerID() + ": code=" + req.getContainerCode());
        }

        container = deploymentIndex.getDeployment(req.getContainerCode());
        if (container == null) {
            throw new RemoteException("The deployement with this ID is null");
        }

        return container;
    }


    private void replyWithFatalError(ObjectOutputStream out, Throwable error, String message) {

        log.fatal(message, error);
        error.printStackTrace();

        RemoteException re = new RemoteException("The server has encountered a fatal error: " + message + " " + error);
        EJBResponse res = new EJBResponse();
        res.setResponse(EJB_ERROR, re);
        try {
            res.writeExternal(out);
        } catch (java.io.IOException ie) {
            log.error("Failed to write to EJBResponse", ie);
        }
    }

}

