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
package org.openejb.server;

import java.util.Collection;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.NotSerializableException;
import java.io.WriteAbortedException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.*;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import org.openejb.client.*;
import org.openejb.client.proxy.*;
import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.EnvProps;
import org.openejb.spi.SecurityService;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.FileUtils;

/** 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EjbDaemon implements Runnable, org.openejb.spi.ApplicationServer, ResponseCodes, RequestMethods {
    
    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");
    
    Category logger = Category.getInstance( "OpenEJB" );

    Vector           clientSockets  = new Vector();
    ServerSocket     serverSocket   = null;
    ServerMetaData   sMetaData      = null;
    DeploymentInfo[] deployments    = null;
    HashMap          deploymentsMap = null;

    // The EJB Server Port
    int    port = 4201;
    String ip   = "127.0.0.1";
    Properties props;


    public EjbDaemon() {
    }

    public void init(Properties props) throws Exception{

        System.out.println("====------------------------====");
        System.out.println("Starting EJB Server");
        System.out.println(" |");
        
        props.putAll(System.getProperties());

        SafeProperties safeProps = toolkit.getSafeProperties(props);
        
        port = safeProps.getPropertyAsInt("openejb.server.port");
        ip   = safeProps.getProperty("openejb.server.ip");

        
        sMetaData = new ServerMetaData(ip, port);

        System.out.println(" +--ip: "+ip+" ");
        System.out.println(" +--port: "+port+" ");
        serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
        
        System.out.println(" |");
        System.out.println(" +--Starting OpenEJB");
        System.out.println(" |  |");
        System.out.println(" |  + initializing container system");
        OpenEJB.init(props, this);
        System.out.println(" |");
        System.out.println(" +--Creating deployment registry");
        
        clientJndi = (javax.naming.Context)OpenEJB.getJNDIContext().lookup("openejb/ejb");

        DeploymentInfo[] ds = OpenEJB.deployments();
        
        // This intentionally has the 0 index as null. The 0 index is the 
        // default value of an unset deploymentCode.
        deployments = new DeploymentInfo[ ds.length +1 ];

        System.arraycopy( ds, 0, deployments, 1, ds.length);
        
        deploymentsMap = new HashMap( deployments.length );
        for (int i=1; i < deployments.length; i++){
            deploymentsMap.put( deployments[i].getDeploymentID(), new Integer(i));
        }

        System.out.println(" |");
        System.out.println("Ready!");
        System.out.println("====------------------------====");
    }

    // This class doesn't use its own namespace, it uses the 
    // jndi context of OpenEJB

    boolean stop = false;
    

    public void run( ) {
        
        Socket socket = null;
        /**
         * The ObjectInputStream used to receive incoming messages from the client.
         */
        ObjectInputStream ois = null;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        ObjectOutputStream oos = null;
        InetAddress cleintIP = null;
        while ( !stop ) {
            try {
                socket = serverSocket.accept();
                cleintIP = socket.getInetAddress();
                Thread.currentThread().setName(cleintIP.getHostAddress());
                
                ois = new ObjectInputStream(  socket.getInputStream() );
                oos = new ObjectOutputStream( socket.getOutputStream() );
                
                byte requestType = ois.readByte();
    
                switch (requestType) {
                    case EJB_REQUEST:  processEjbRequest(ois, oos); break;
                    case JNDI_REQUEST: processJndiRequest(ois, oos);break;
                    case AUTH_REQUEST: processAuthRequest(ois, oos);break;
                }
    
                oos.flush();
                // Exceptions should not be thrown from these methods
                // They should handle their own exceptions and clean
                // things up with the client accordingly.
            } catch ( Throwable e ) {
                logger.error( "Unexpected error", e );
                //System.out.println("ERROR: "+cleintIP.getHostAddress()+": " +e.getMessage());
            } finally {
                try {
                    if ( oos != null ) oos.close();
                    if ( ois != null ) ois.close();
                    if ( socket != null ) socket.close();
                } catch ( Throwable t ){
                    logger.error("Encountered problem while closing connection with client: "+t.getMessage());
                }
            }
        }

    }

    private DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {
        // This logic could probably be cleaned up quite a bit.
        
        DeploymentInfo info = null;
        
        if (req.getDeploymentCode() > 0 && req.getDeploymentCode() < deployments.length) {
            info = deployments[ req.getDeploymentCode() ];
            if ( info == null ) {
                throw new RemoteException("The deployement with this ID is null");
            }
            req.setDeploymentId((String) info.getDeploymentID() );
            return info;
        } 
        
        if ( req.getDeploymentId() == null ) {
            throw new RemoteException("Invalid deployment id and code: id="+req.getDeploymentId()+": code="+req.getDeploymentCode());
        }

        Integer idCode = (Integer)deploymentsMap.get( req.getDeploymentId() );

        if ( idCode == null ) {
            throw new RemoteException("No such deployment id and code: id="+req.getDeploymentId()+": code="+req.getDeploymentCode());
        }
        
        req.setDeploymentCode( idCode.intValue() );

        if (req.getDeploymentCode() < 0 || req.getDeploymentCode() >= deployments.length){
            throw new RemoteException("Invalid deployment id and code: id="+req.getDeploymentId()+": code="+req.getDeploymentCode());
        }
        
        info = deployments[ req.getDeploymentCode() ];
        if ( info == null ) {
            throw new RemoteException("The deployement with this ID is null");
        }
        return info;
    }
    
    public void processEjbRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        
        EJBRequest req = new EJBRequest();
        EJBResponse res = new EJBResponse();

        // TODO:2: This method can throw a large number of exceptions, we should 
        // be prepared to handle them all.  Look in the ObejctOutputStream code
        // for a full list of the exceptions thrown.
        // java.io.WriteAbortedException  can be thrown containing a
        //
        try {
            req.readExternal( in );
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

        CallContext  call = null;
        DeploymentInfo di = null;
        RpcContainer    c = null;;
        
        try{
            di = getDeployment(req);
        } catch (RemoteException e){
            logger.warn( req + "No such deployment: "+e.getMessage());
            res.setResponse( EJB_SYS_EXCEPTION, e);
            res.writeExternal( out );
            return;
        
        } catch ( Throwable t ){
            String message = "Unkown error occured while retreiving deployment:"+t.getMessage();
            logger.fatal( req + message, t);
            
            RemoteException e = new RemoteException("The server has encountered a fatal error and must be restarted."+message);
            res.setResponse( EJB_ERROR , e );
            res.writeExternal( out );
            return;            
        }
            
        try {
            call = CallContext.getCallContext();
            call.setEJBRequest( req );
            call.setDeploymentInfo( di );
        } catch ( Throwable t ){
            logger.fatal( req + "Unable to set the thread context for this request: ", t);
            
            RemoteException e = new RemoteException("The server has encountered a fatal error and must be restarted.");
            res.setResponse( EJB_ERROR , e );
            res.writeExternal( out );
            return;            
        }
        
        logger.info( "EJB REQUEST : "+req );

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
        } catch (org.openejb.SystemException e){
            res.setResponse(EJB_ERROR, e.getRootCause());
            // TODO:2: This means a severe error occured in OpenEJB
            // we should restart the container system or take other
            // aggressive actions to attempt recovery.
            logger.fatal( req+": OpenEJB encountered an unknown system error in container: ", e);
        } catch (java.lang.Throwable t){
            //System.out.println(req+": Unkown error in container: ");
            res.setResponse(EJB_ERROR, t);
            logger.fatal( req+": Unkown error in container: ", t);
        } finally {
            logger.info( "EJB RESPONSE: "+res );
            res.writeExternal( out );
            call.reset();
        }
    }
    
    static javax.naming.Context clientJndi;

    public void processJndiRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        JNDIRequest  req = new JNDIRequest();
        JNDIResponse res = new JNDIResponse();
        req.readExternal( in );
        
        // We are assuming that the request method is JNDI_LOOKUP
        // TODO: Implement the JNDI_LIST and JNDI_LIST_BINDINGS methods

        //Object result = clientJndi.lookup( req.getRequestString() );
        String name = req.getRequestString();
        if ( name.startsWith("/") ) name = name.substring(1);

        ///DeploymentInfo deployment = OpenEJB.getDeploymentInfo( name );
        Integer idNum = (Integer)deploymentsMap.get( name );

        DeploymentInfo deployment = null;
        Object obj = null;

        if ( idNum != null ) {
            deployment = deployments[idNum.intValue()];
        }
        
        if (deployment == null) {
            try {
                obj = clientJndi.lookup(name);    

                if ( obj instanceof Context ) {
                    res.setResponseCode( JNDI_CONTEXT );
                } else res.setResponseCode( JNDI_NOT_FOUND );

            } catch (NameNotFoundException e) {
                res.setResponseCode(JNDI_NOT_FOUND);
            } catch (NamingException e){
                res.setResponseCode(JNDI_NAMING_EXCEPTION);
                res.setResult( e );
            }
        } else {
            res.setResponseCode( JNDI_EJBHOME );
            EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                           deployment.getRemoteInterface(),
                                                           deployment.getPrimaryKeyClass(),
                                                           deployment.getComponentType(),
                                                           deployment.getDeploymentID().toString(),
                                                           idNum.intValue());
            res.setResult( metaData );
        }

        res.writeExternal( out );
    }
    
    public void processAuthRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        //TODO   
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
                result = _getEJBObject(call, info);
            } else if ( EJBHome.class.isAssignableFrom(info.getInterface()) ) {
                result = _getEJBHome(call, info);
            } else {
                // Freak condition
                //TODO:3: Localize all error messages in an separate file.
                result = new RemoteException("The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: "+info.getInterface());
                logger.error( req + "The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: "+info.getInterface());
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
            logger.error( req + "The bean is not EJB compliant.  The should be created or and exception should be thrown.");
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
            
            for (int i=0; i < primaryKeys.length; i++){
                primaryKeys[i] = ((ProxyInfo)primaryKeys[i]).getPrimaryKey();
            }

            res.setResponse( EJB_OK_FOUND_MULTIPLE , primaryKeys );

        /* Single intance found */
        } else if (result instanceof ProxyInfo) {
            result = ((ProxyInfo)result).getPrimaryKey();
            res.setResponse( EJB_OK_FOUND , result );

        } else {
            // There should be no else, the entity should be found
            // or and exception should be thrown.
            //TODO:3: Localize all error messages in an separate file.
            result = new RemoteException("The bean is not EJB compliant.  The should be found or and exception should be thrown.");
            logger.error( req + "The bean is not EJB compliant.  The should be found or and exception should be thrown.");
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
            logger.info(req + "Unauthorized Access by Principal Denied");
            res.setResponse( EJB_APP_EXCEPTION , new RemoteException("Unauthorized Access by Principal Denied") );
        }
    }
    
    
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------

    //=============================================================
    //  ApplicationServer interface methods
    //=============================================================
    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info){
        CallContext call = CallContext.getCallContext();
        return _getEJBMetaData(call, info);
    }
    
    public javax.ejb.Handle getHandle(ProxyInfo info){
        CallContext call = CallContext.getCallContext();
        return _getHandle(call, info);
    }
    
    public javax.ejb.EJBObject getEJBObject(ProxyInfo info){
        CallContext call = CallContext.getCallContext();
        return _getEJBObject(call, info);
    }
    
    public javax.ejb.EJBHome getEJBHome(ProxyInfo info){
        CallContext call = CallContext.getCallContext();
        return _getEJBHome(call, info);
    }
    
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
    //=============================================================
    //  ApplicationServer interface methods
    //=============================================================
    private javax.ejb.EJBMetaData _getEJBMetaData(CallContext call, ProxyInfo info){
        
        DeploymentInfo deployment = info.getDeploymentInfo();
        Integer idCode = (Integer)deploymentsMap.get( deployment.getDeploymentID() );
        
        EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                       deployment.getRemoteInterface(),
                                                       deployment.getPrimaryKeyClass(),
                                                       deployment.getComponentType(),
                                                       deployment.getDeploymentID().toString(),
                                                       idCode.intValue());
        return metaData;
    }
    
    private javax.ejb.Handle _getHandle(CallContext call, ProxyInfo info){
        DeploymentInfo deployment = info.getDeploymentInfo();
        
        Integer idCode = (Integer)deploymentsMap.get( deployment.getDeploymentID() );

        ClientMetaData  cMetaData = new ClientMetaData(call.getEJBRequest().getClientIdentity());
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                        deployment.getRemoteInterface(),
                                                        deployment.getPrimaryKeyClass(),
                                                        deployment.getComponentType(),
                                                        deployment.getDeploymentID().toString(),
                                                        idCode.intValue());
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,sMetaData,cMetaData,primKey);

        return new EJBObjectHandle( hanlder.createEJBObjectProxy() );
    }
    
    private javax.ejb.EJBObject _getEJBObject(CallContext call, ProxyInfo info){
        DeploymentInfo deployment = info.getDeploymentInfo();
        
        Integer idCode = (Integer)deploymentsMap.get( deployment.getDeploymentID() );

        ClientMetaData  cMetaData = new ClientMetaData(call.getEJBRequest().getClientIdentity());
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                        deployment.getRemoteInterface(),
                                                        deployment.getPrimaryKeyClass(),
                                                        deployment.getComponentType(),
                                                        deployment.getDeploymentID().toString(),
                                                        idCode.intValue());
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,sMetaData,cMetaData,primKey);

        return hanlder.createEJBObjectProxy();
    }
    
    private javax.ejb.EJBHome _getEJBHome(CallContext call, ProxyInfo info){
        DeploymentInfo deployment = info.getDeploymentInfo();
  
        Integer idCode = (Integer)deploymentsMap.get( deployment.getDeploymentID() );
  
        ClientMetaData  cMetaData = new ClientMetaData(call.getEJBRequest().getClientIdentity());
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                        deployment.getRemoteInterface(),
                                                        deployment.getPrimaryKeyClass(),
                                                        deployment.getComponentType(),
                                                        deployment.getDeploymentID().toString(),
                                                        idCode.intValue());

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,sMetaData,cMetaData);
  
        //EJBHomeProxyHandle handle = new EJBHomeProxyHandle( hanlder );
        
        return hanlder.createEJBHomeProxy();
    }

//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------
    static String OpenEJB_Home;

    public static void main(String [] args) {
        try {
            
            // Set up a simple configuration that logs on the console.
            //BasicConfigurator.configure();
                    
            Properties props = System.getProperties();
            
            // -- Set Defaults -- //
            props.put("openejb.home",              System.getProperty("user.dir"));
            props.put("openejb.server.ip",         "127.0.0.1");
            props.put("openejb.server.port",       "4201");
            props.put("openejb.server.threads",    "20");

            //TODO:0: Remove this hack.  OpenEJB container system related properties
            // should be resolved by org.openejb.OpenEJB
            props.put("log4j.configuration",       "file:conf/default.logging.conf");


            for (int i=0; i < args.length; i++){
                if (args[i].equals("-h")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.server.ip", args[++i]);
                    }
                } else if (args[i].equals("-p")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.server.port", args[++i]);
                    }
                } else if (args[i].equals("-t")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.server.threads", args[++i]);
                    }
                } else if (args[i].equals("-c")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.configuration", args[++i]);
                    }
                } else if (args[i].equals("-l")){
                    if (args.length > i+2 ) {
                        System.setProperty("log4j.configuration", args[++i]);
                    }
                } else if (args[i].equals("-d")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.home", args[++i]);
                    }
                }
            }
            
            if ( args.length == 1 ) {
                File propsFile = new File(args[0]);
                FileInputStream input = new FileInputStream(propsFile.getAbsoluteFile());
                props.load(input);
            }

            props.setProperty("org/openejb/configuration_factory", "org.openejb.alt.config.ConfigurationFactory");            
            

            EjbDaemon ejbd = new EjbDaemon();
            ejbd.init(props);

            int threads = Integer.parseInt( (String)props.get("openejb.server.threads") );
            for (int i=0; i < threads; i++){
                Thread d = new Thread(ejbd);
                d.setName("EJB Daemon ["+i+"]");
                d.start();
            }
            
            // dont allow the server to exit
            while ( true ) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch ( InterruptedException e ) {}
            }
        
        } catch ( Exception re ) {
            System.err.println("[EJB Server] FATAL ERROR: "+ re.getMessage());
            re.printStackTrace();
            System.exit(-1);
        }
    }
    //
    //  Inner class for sending commands to the server at runtime
    //=============================================================
}
