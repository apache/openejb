/*
 * Copyright  2002, Apple Computer, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1.  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.  
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.  
 * 3.  Neither the name of Apple Computer, Inc. ("Apple")
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY APPLE AND ITS CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL APPLE OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */


package org.openejb.corba.core;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.openejb.ProxyInfo;
import org.openejb.core.ivm.BaseEjbProxyHandler;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * This class is used to intercept the automatic remote object activation that will
 * be performed by the RMI/IIOP stubs. This class will call into the ApplicationServer
 * SPI to have object references marshalled correctly.
 *
 * @author Stefan Reich sreich@apple.com
 */
public class UtilDelegateImpl implements javax.rmi.CORBA.UtilDelegate {

    private static javax.rmi.CORBA.UtilDelegate _delegate;
    private static org.openejb.spi.ApplicationServer _server;
    
    private final static String propName= "javax.rmi.CORBA.UtilClass";
    private final static String delegateName= "org.openejb.corba.core.UtilDelegateClass";

    private static UtilDelegateImpl _instance;

    private final static org.openejb.util.Logger logger = org.openejb.util.Logger.getInstance("CORBA-Adapter", "org.openejb.util.resources");

    public UtilDelegateImpl() throws Exception {
        _instance=this;
        String value = (String) System.getProperty(propName);
        if(value.equals(getClass().getName())) {
            value=(String) System.getProperty(delegateName);
            if(value==null) {
                System.err.println("The property "+delegateName+" must be defined!");
            }
        }
        setDelegate((javax.rmi.CORBA.UtilDelegate)getClass().forName(value).newInstance());
    }

    public static void init() throws Exception {
        String value = System.getProperty(propName);
        // prevent infinite recursion if we have the right class already
        if(!value.equals(delegateName)) {
            System.setProperty(delegateName, value);
            System.setProperty(propName, "org.openejb.corba.core.UtilDelegateImpl");
        }
    }
    
    public static void setAppServer(org.openejb.spi.ApplicationServer server) {
        UtilDelegateImpl._server=server;
    }
    public static void setDelegate(javax.rmi.CORBA.UtilDelegate delegate) {
        _delegate=delegate;
    }
                               
    /**
        * handle activation
     */
    public org.omg.CORBA.Object handleRemoteObject(org.omg.CORBA.portable.OutputStream out, java.rmi.Remote remote){
        BaseEjbProxyHandler handler;
        if(remote instanceof javax.rmi.CORBA.Tie) {
            java.rmi.Remote delegate = ((javax.rmi.CORBA.Tie)remote).getTarget();
            handler = (BaseEjbProxyHandler)ProxyManager.getInvocationHandler(delegate);
        } else if(remote instanceof javax.rmi.CORBA.Stub) {
            // if its a stub there nothing to do...
            return (javax.rmi.CORBA.Stub)remote;
        }else if(ProxyManager.isProxyClass(remote.getClass())){
            handler = (BaseEjbProxyHandler)ProxyManager.getInvocationHandler(remote);
        } else {
	    logger.error("Encountered unknown object reference of type "+remote);
            throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
        }

        if(handler instanceof EjbHomeProxyHandler) {
            Class interf = handler.deploymentInfo.getHomeInterface();
            ProxyInfo info = new ProxyInfo(handler.deploymentInfo, handler.primaryKey, interf, handler.container);
            return (org.omg.CORBA.Object) _server.getEJBHome(info);
        } else if(handler instanceof EjbObjectProxyHandler) {
            Class interf = handler.deploymentInfo.getRemoteInterface();
            ProxyInfo info = new ProxyInfo(handler.deploymentInfo, handler.primaryKey, interf, handler.container);
            return (org.omg.CORBA.Object) _server.getEJBObject(info);
        } else {
	    logger.error("Encountered unknown local invocation handler of type "+handler.getClass()+":"+handler);
            throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
        }
    }

    public java.rmi.RemoteException mapSystemException(org.omg.CORBA.SystemException ex){
        return _delegate.mapSystemException(ex);
    }
    public void writeAny(org.omg.CORBA.portable.OutputStream stream, java.lang.Object obj){
        _delegate.writeAny(stream, obj);
    }
    public java.lang.Object readAny(org.omg.CORBA.portable.InputStream stream){
        return _delegate.readAny(stream);
    }
    public void writeRemoteObject(org.omg.CORBA.portable.OutputStream out, java.lang.Object obj){
        try{
	    if ( obj != null && obj instanceof java.rmi.Remote )
		out.write_Object( handleRemoteObject( out, ( java.rmi.Remote ) obj ) );
	    else if ( obj == null || obj instanceof org.omg.CORBA.Object )
		out.write_Object( ( org.omg.CORBA.Object ) obj );
	    else {
		logger.error("Encountered unknown object reference of type "+obj.getClass()+":"+obj);
                throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
	    }
        }catch(Throwable e) {
	    logger.error("Received unexpected exception while marshaling an object reference:", e);
            throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
        }
    }
    public void writeAbstractObject(org.omg.CORBA.portable.OutputStream stream, java.lang.Object obj){
        _delegate.writeAbstractObject(stream, obj);
    }
    public void registerTarget(javax.rmi.CORBA.Tie tie, java.rmi.Remote obj){
        _delegate.registerTarget(tie, obj);
    }

    public void unexportObject(java.rmi.Remote obj) {
	try{
	    _delegate.unexportObject(obj);
	}catch(Exception e) {
	    // this catch block exists, because SUN changed the method
	    // signature of this method between 1.3 and 1.4 to include an
	    // additional exception. We don't propagate it, but log it.
	    logger.error("Tried to unexport an object that wasn't activated", e);
	}
    }
    public javax.rmi.CORBA.Tie getTie(java.rmi.Remote obj){
        return _delegate.getTie(obj);
    }
    public javax.rmi.CORBA.ValueHandler createValueHandler(){
        return _delegate.createValueHandler();
    }
    public java.lang.String getCodebase(java.lang.Class clz){
        return _delegate.getCodebase(clz);
    }
    public java.lang.Class loadClass(java.lang.String className, java.lang.String remoteCodebase, java.lang.ClassLoader loader) throws java.lang.ClassNotFoundException{
        return _delegate.loadClass(className, remoteCodebase, loader);
    }
    public boolean isLocal(javax.rmi.CORBA.Stub stub) throws java.rmi.RemoteException{
        return _delegate.isLocal(stub);
    }
    public java.rmi.RemoteException wrapException(java.lang.Throwable t){
        return _delegate.wrapException(t);
    }
    public java.lang.Object copyObject(java.lang.Object obj, org.omg.CORBA.ORB orb) throws java.rmi.RemoteException{
        return _delegate.copyObject(obj, orb);
    }
    public java.lang.Object copyObjects(java.lang.Object[] objs, org.omg.CORBA.ORB orb)[] throws java.rmi.RemoteException{
        return _delegate.copyObjects(objs, orb);
    }
    
}
