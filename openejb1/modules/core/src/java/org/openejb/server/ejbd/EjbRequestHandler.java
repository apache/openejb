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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.client.EJBRequest;
import org.openejb.client.EJBResponse;
import org.openejb.client.RequestMethods;
import org.openejb.client.ResponseCodes;
import org.openejb.spi.SecurityService;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
class EjbRequestHandler  implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;
    /**
     * @param EjbDaemon
     */
    EjbRequestHandler(EjbDaemon daemon) {
        this.daemon = daemon;
        // TODO Auto-generated constructor stub
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        EJBRequest req = new EJBRequest();
        EJBResponse res = new EJBResponse();

        // TODO:2: This method can throw a large number of exceptions, we should
        // be prepared to handle them all.  Look in the ObejctOutputStream code
        // for a full list of the exceptions thrown.
        // java.io.WriteAbortedException  can be thrown containing a
        //
        try {
            req.readExternal( in );

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
            replyWithFatalError
            (out, t, "Error caught during request processing");
            return;
        }

        CallContext  call = null;
        DeploymentInfo di = null;
        RpcContainer    c = null;;

        try {
            di = this.daemon.getDeployment(req);
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
        } catch ( Throwable t ) {
            replyWithFatalError
            (out, t, "Unkown error occured while retrieving deployment");
            return;
        }

        try {
            call = CallContext.getCallContext();
            call.setEJBRequest( req );
            call.setDeploymentInfo( di );
        } catch ( Throwable t ) {
            replyWithFatalError
            (out, t, "Unable to set the thread context for this request");
            return;
        }

        //logger.info( "EJB REQUEST : "+req );

        try {
            switch (req.getRequestMethod()) {
            // Remote interface methods
            case EJB_OBJECT_BUSINESS_METHOD:
                doEjbObject_BUSINESS_METHOD( req, res );
                break;

                // Home interface methods
            case EJB_HOME_CREATE:
                doEjbHome_CREATE( req, res );
                break;

            case EJB_HOME_FIND:
                doEjbHome_FIND( req, res );
                break;

                // javax.ejb.EJBObject methods
            case EJB_OBJECT_GET_EJB_HOME:
                doEjbObject_GET_EJB_HOME( req, res );
                break;

            case EJB_OBJECT_GET_HANDLE:
                doEjbObject_GET_HANDLE( req, res );
                break;

            case EJB_OBJECT_GET_PRIMARY_KEY:
                doEjbObject_GET_PRIMARY_KEY( req, res );
                break;

            case EJB_OBJECT_IS_IDENTICAL:
                doEjbObject_IS_IDENTICAL( req, res );
                break;

            case EJB_OBJECT_REMOVE:
                doEjbObject_REMOVE( req, res );
                break;

                // javax.ejb.EJBHome methods
            case EJB_HOME_GET_EJB_META_DATA:
                doEjbHome_GET_EJB_META_DATA( req, res );
                break;

            case EJB_HOME_GET_HOME_HANDLE:
                doEjbHome_GET_HOME_HANDLE( req, res );
                break;

            case EJB_HOME_REMOVE_BY_HANDLE:
                doEjbHome_REMOVE_BY_HANDLE( req, res );
                break;

            case EJB_HOME_REMOVE_BY_PKEY:
                doEjbHome_REMOVE_BY_PKEY( req, res );
                break;
            }


        } catch (org.openejb.InvalidateReferenceException e) {
            res.setResponse(EJB_SYS_EXCEPTION, e.getRootCause());
        } catch (org.openejb.ApplicationException e) {
            res.setResponse(EJB_APP_EXCEPTION, e.getRootCause());
        } catch (org.openejb.SystemException e) {
            res.setResponse(EJB_ERROR, e.getRootCause());
            // TODO:2: This means a severe error occured in OpenEJB
            // we should restart the container system or take other
            // aggressive actions to attempt recovery.
            this.daemon.logger.fatal( req+": OpenEJB encountered an unknown system error in container: ", e);
        } catch (java.lang.Throwable t) {
            //System.out.println(req+": Unkown error in container: ");
            replyWithFatalError
            (out, t, "Unknown error in container");
            return;
        } finally {
            this.daemon.logger.info( "EJB RESPONSE: "+res );
            try {
                res.writeExternal( out );
            } catch (java.io.IOException ie) {
                this.daemon.logger.fatal("Couldn't write EjbResponse to output stream", ie);
            }
            call.reset();
        }
    }

    protected void doEjbObject_BUSINESS_METHOD( EJBRequest req, EJBResponse res ) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());

        if (result instanceof ProxyInfo) {
            ProxyInfo info = (ProxyInfo)result;

            if ( EJBObject.class.isAssignableFrom(info.getInterface()) ) {
                result = this.daemon.clientObjectFactory._getEJBObject(call, info);
            } else if ( EJBHome.class.isAssignableFrom(info.getInterface()) ) {
                result = this.daemon.clientObjectFactory._getEJBHome(call, info);
            } else {
                // Freak condition
                //TODO:3: Localize all error messages in an separate file.
                result = new RemoteException("The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: "+info.getInterface());
                this.daemon.logger.error( req + "The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: "+info.getInterface());
                res.setResponse( EJB_SYS_EXCEPTION, result);
                return;
            }
        }

        res.setResponse( EJB_OK, result);
    }


    // Home interface methods
    protected void doEjbHome_CREATE( EJBRequest req, EJBResponse res ) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());

        if (result instanceof ProxyInfo) {
            ProxyInfo info = (ProxyInfo)result;
            res.setResponse(EJB_OK, info.getPrimaryKey());
        } else {
            // There should be no else, the entity should be found
            // or and exception should be thrown.
            //TODO:3: Localize all error messages in an separate file.
            result = new RemoteException("The bean is not EJB compliant.  The should be created or and exception should be thrown.");
            this.daemon.logger.error( req + "The bean is not EJB compliant.  The should be created or and exception should be thrown.");
            res.setResponse( EJB_SYS_EXCEPTION, result);
        }
    }

    /**
     * EJB 1.1 --
     * 9.1.8 Finder method return type
     *
     * 9.1.8.1 Single-object finder
     *
     * Some finder methods (such as ejbFindByPrimaryKey) are designed to return
     * at most one entity object. For these single-object finders, the result type
     * of the find<METHOD>(...)method defined in the entity bean’s home interface
     * is the entity bean’s remote interface. The result type of the corresponding
     * ejbFind<METHOD>(...) method defined in the entity’s implementation class is
     * the entity bean’s primary key type.
     *
     * 9.1.8.2 Multi-object finders
     *
     * Some finder methods are designed to return multiple entity objects. For
     * these multi-object finders, the result type of the find<METHOD>(...)method
     * defined in the entity bean’s home interface is a col-lection of objects
     * implementing the entity bean’s remote interface. The result type of the
     * corresponding ejbFind<METHOD>(...) implementation method defined in the
     * entity bean’s implementation class is a collection of objects of the entity
     * bean’s primary key type.
     *
     * The Bean Provider can choose two types to define a collection type for a finder:
     * • the JDK™ 1.1 java.util.Enumeration interface
     * • the Java™ 2 java.util.Collection interface
     *
     * A Bean Provider that wants to ensure that the entity bean is compatible
     * with containers and clients based on JDK TM 1.1 software must use the
     * java.util.Enumeration interface for the finder’s result type.
     * </P>
     *
     * @param req
     * @param in
     * @param out
     * @exception Exception
     */
    protected void doEjbHome_FIND( EJBRequest req, EJBResponse res ) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());


        /* Multiple instances found */
        if ( result instanceof Collection ) {

            Object [] primaryKeys = ((Collection)result).toArray();

            for (int i=0; i < primaryKeys.length; i++) {
                primaryKeys[i] = ((ProxyInfo)primaryKeys[i]).getPrimaryKey();
            }

            res.setResponse( EJB_OK_FOUND_COLLECTION , primaryKeys );

        } else if (result instanceof java.util.Enumeration ) {
            
            java.util.Enumeration resultAsEnum = (java.util.Enumeration) result;
            java.util.List listOfPKs = new java.util.ArrayList();
            while ( resultAsEnum.hasMoreElements() ) {
                listOfPKs.add( ((ProxyInfo)resultAsEnum.nextElement()).getPrimaryKey() );
            }
            
            res.setResponse( EJB_OK_FOUND_ENUMERATION , listOfPKs.toArray( new Object[listOfPKs.size()]) );
        /* Single instance found */
        } else if (result instanceof ProxyInfo) {
            result = ((ProxyInfo)result).getPrimaryKey();
            res.setResponse( EJB_OK_FOUND , result );

        } else {
            // There should be no else, the entity should be found
            // or an exception should be thrown.
            //TODO:3: Localize all error messages in an separate file.
            // TODO:4: It should provide more info on the wrong method
            final String message = "The bean is not EJB compliant. " +
                "The finder method ["+req.getMethodInstance().getName()+"] is declared " +
                "to return neither Collection nor the Remote Interface, " +
                "but [" +result.getClass().getName()+ "]";
            result = new RemoteException( message );
            this.daemon.logger.error( req + " " + message);
            res.setResponse( EJB_SYS_EXCEPTION, result);
        }
    }

    // javax.ejb.EJBObject methods
    protected void doEjbObject_GET_EJB_HOME( EJBRequest req, EJBResponse res ) throws Exception {
        checkMethodAuthorization( req, res );
    }

    protected void doEjbObject_GET_HANDLE( EJBRequest req, EJBResponse res ) throws Exception {
        checkMethodAuthorization( req, res );
    }

    protected void doEjbObject_GET_PRIMARY_KEY( EJBRequest req, EJBResponse res ) throws Exception {
        checkMethodAuthorization( req, res );
    }

    protected void doEjbObject_IS_IDENTICAL( EJBRequest req, EJBResponse res ) throws Exception {
        checkMethodAuthorization( req, res );
    }

    protected void doEjbObject_REMOVE( EJBRequest req, EJBResponse res ) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());

        res.setResponse( EJB_OK, null);
    }

    // javax.ejb.EJBHome methods
    protected void doEjbHome_GET_EJB_META_DATA( EJBRequest req, EJBResponse res ) throws Exception {
        checkMethodAuthorization( req, res );
    }

    protected void doEjbHome_GET_HOME_HANDLE( EJBRequest req, EJBResponse res ) throws Exception {
        checkMethodAuthorization( req, res );
    }

    protected void doEjbHome_REMOVE_BY_HANDLE( EJBRequest req, EJBResponse res ) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());

        res.setResponse( EJB_OK, null);
    }

    protected void doEjbHome_REMOVE_BY_PKEY( EJBRequest req, EJBResponse res ) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());

        res.setResponse( EJB_OK, null);
    }

    protected void checkMethodAuthorization( EJBRequest req, EJBResponse res ) throws Exception {
        // Nothing to do here other than check to see if the client
        // is authorized to call this method
        // TODO:3: Keep a cache in the client-side handler of methods it can't access

        SecurityService sec = OpenEJB.getSecurityService();
        CallContext caller  = CallContext.getCallContext();
        DeploymentInfo di   = caller.getDeploymentInfo();
        String[] authRoles  = di.getAuthorizedRoles( req.getMethodInstance() );

        if (sec.isCallerAuthorized( req.getClientIdentity(), authRoles )) {
            res.setResponse( EJB_OK, null );
        } else {
            this.daemon.logger.info(req + "Unauthorized Access by Principal Denied");
            res.setResponse( EJB_APP_EXCEPTION , new RemoteException("Unauthorized Access by Principal Denied") );
        }
    }

    private void replyWithFatalError(ObjectOutputStream out,Throwable error,String message) {
        this.daemon.logger.fatal(message, error);
        RemoteException re = new RemoteException
                             ("The server has encountered a fatal error: "+message+" "+error);
        EJBResponse res = new EJBResponse();
        res.setResponse(EJB_ERROR, re);
        try {
            res.writeExternal(out);
        } catch (java.io.IOException ie) {
            this.daemon.logger.error("Failed to write to EJBResponse", ie);
        }
    }
}
