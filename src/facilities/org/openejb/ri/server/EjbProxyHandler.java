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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.openejb.ri.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.Socket;
import java.rmi.NoSuchObjectException;

import javax.ejb.EJBHome;

import org.openejb.InvalidateReferenceException;
import org.openejb.util.Messages;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContaer manager for more details.
 */
public class EjbProxyHandler implements InvocationHandler, Serializable {

    transient Socket socket = null;
    transient RPCMessage message;
    transient ObjectOutputStream oos;
    transient ObjectInputStream ois;

    int port;
    String ip;
    Serializable deploymentID;
    Serializable primaryKey;
    boolean inProxyMap = false;
    boolean isInvalidReference = false;
    String securityToken;

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );


    /** Public no-arg constructor required by Externalizable API */
    public EjbProxyHandler() {}

    public EjbProxyHandler(int port, String ip, Object pk, Object depID, String securityToken) {
        this.port = port;
        this.ip = ip;
        this.primaryKey = (Serializable)pk;
        this.deploymentID = (Serializable)depID;
        this.securityToken = securityToken;
    }

    private void invalidateReference(){
        //FIXME: Replace with socket pooling (mapped to ip:port)
        try {
            oos.close();
            ois.close();
            socket.close();
        } catch ( Throwable t ) {
        } finally {
            oos = null;
            ois = null;
            socket = null;
            message = null;
            ip = null;
            deploymentID = null;
            primaryKey = null;
            securityToken = null;
            isInvalidReference = true;
        }
    }

    // The EJBObject stub is synchronized to prevent multiple client threads from accessing the
    // stub concurrently.  This is required by session beans (6.5.6 Serializing session bean methods) but
    // not required by entity beans. Implementing synchronization on the stub prohibits multiple threads from currently
    // invoking methods on the same stub, but doesn't prohibit a client from having multiple references to the same
    // entity identity. Synchronizing the stub is a simple and elegant solution to a difficult problem.
    //
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
        if (isInvalidReference) throw new NoSuchObjectException( _messages.message( "ejbProxyHandler.referenceIsInvalid" ) );

        if(method.getDeclaringClass().getName().equals("java.lang.Object")) {
            if(method.getName().equals("equals")) {
                return new Boolean(proxy == args[0]);
            } else if(method.getName().equals("hashCode")) {
                return new Integer(hashCode());
            } else if(method.getName().equals("toString")) {
                return "Proxy for "+deploymentID;
            }
        }

        if(method.getDeclaringClass()==javax.ejb.EJBObject.class && method.getName().equals("isIdentical")){
            EjbProxyHandler otherHandler = (EjbProxyHandler)ProxyManager.getInvocationHandler(proxy);
            if(deploymentID.equals(otherHandler.deploymentID)){
                if(primaryKey != null){
                    if(primaryKey.equals(otherHandler.primaryKey)){
                        return new Boolean(true);
                    }else
                        return new Boolean(false);
                }else
                    return new Boolean(true);
            }

        }
        //FIXME: Replace with socket pooling (mapped to ip:port); be careful about sync issues
        if ( socket==null ) {
            socket = new Socket(ip, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        }


        /*
        Note: Do not attempt to optimize be reuse the message instance.
        an attempt was made to reuses the message object, but the serialization mechanism
        reuses its buffered instance of the object and so changes to its fields (like arguments)
        are not written to the stream; the original instance is simply written again.
        */
        message = new RPCMessage(primaryKey, deploymentID, method, args, securityToken);

        // FIXME: set transaction id and security info on message.context object
        Object retval = null;
        try {
            oos.writeObject(message);
            oos.flush();
            oos.reset();
            retval = ois.readObject();
        } catch(IOException e) {
            throw new java.rmi.RemoteException( _messages.format( "ejbProxyHandler.communicationBreakdown", e) );
        }

        if ( retval instanceof InvalidateReferenceException ) {
            // The ire is thrown by the container system and propagated by the server to the stub.
            // invalidate the reference

            invalidateReference();

            InvalidateReferenceException ire = (InvalidateReferenceException)retval;

            if ( ire.getRootCause()!=null )
                throw ire.getRootCause();
            else
                return null;
        } else if ( retval instanceof Throwable ) {
            throw (Throwable)retval;
        } else if ( retval instanceof RiMetaData ) {
            ((RiMetaData)retval).setEJBHome((EJBHome)proxy);
        } else if ( retval instanceof RiBaseHandle ) {
            ((RiBaseHandle)retval).setProxy(proxy);
        }

        return retval;
    }

    // Catch the System.exit and close connections.
    protected void finalize() throws Throwable {

    }
}
