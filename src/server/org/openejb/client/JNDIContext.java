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
package org.openejb.client;

import java.io.Serializable;
import javax.naming.*;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import java.io.IOException;
import java.io.*;
import java.util.Hashtable;
import java.net.URL;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import org.openejb.client.proxy.ProxyManager;
/**
 * JNDI client
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class JNDIContext implements Serializable, InitialContextFactory, Context, RequestMethods, ResponseCodes {
    
    private transient String tail = "/";
    private transient ServerMetaData server;
    private transient ClientMetaData client;
    private transient Hashtable env;

    /**
     * Constructs this JNDI context for the client.
     *
     * Opens a socket connection with the NamingServer.
     * Initializes object output/input streams for writing/reading 
     * objects with the naming server.
     * Authenticates the user's information with the NamingServer
     *
     * @param environment
     * @exception NamingException
     * @see NamingServer
     */
    JNDIContext(Hashtable environment) throws NamingException{
        init( environment );
    }

    public JNDIContext(){
    }
    
    /*
     * A neater version of clone
     */
    public JNDIContext(JNDIContext that){
        this.tail   = that.tail;    
        this.server = that.server;
        this.client = that.client;
        this.env    = (Hashtable)that.env.clone();
    }

    /**
     * Initializes this JNDI context for the client.
     *
     * Opens a socket connection with the NamingServer.
     * Initializes object output/input streams for writing/reading 
     * objects with the naming server.
     * Authenticates the user's information with the NamingServer
     *
     * @param environment
     * @exception NamingException
     * @see NamingServer
     */
    public void init(Hashtable environment) throws NamingException{
    }


    private JNDIResponse request(JNDIRequest req) throws Exception {
        // TODO:  Think of a better exception type and message
        if ( server == null ) throw new Exception("No server");
        
        Socket socket = null;
        OutputStream socketOut = null;        
        InputStream  socketIn  = null;        
        
        ObjectOutput objectOut = null;        
        ObjectInput  objectIn  = null;        
        
        JNDIResponse res       = null;

        try{
            /*-----------------------*/
            /* Open socket to server */
            /*-----------------------*/
            try{
                // TODO:3: Look for optimizations in opening sockets
                socket = new Socket(server.address, server.port);
                // TODO:2: Implement connection pooling with a max poolsize
                // and a max time to keep unused connections alive.
            } catch (IOException e){
                throw new IOException("Cannot access server: "+server.address+":"+server.port+" Exception: "+ e.getMessage());
            
            } catch (SecurityException e){
                throw new IOException("Cannot access server: "+server.address+":"+server.port+" due to security restrictions in the current VM: "+ e.getMessage());
            }
            
            /*----------------------------------*/
            /* Openning output streams          */
            /*----------------------------------*/
            try{
                
                socketOut = socket.getOutputStream();
                objectOut = new ObjectOutputStream(socketOut);
            
            } catch (IOException e){
                throw new IOException("Cannot open output stream to server: " + e.getMessage());
            
            } catch (Throwable e){
                throw new IOException("Cannot open output stream to server: " + e.getMessage());
            } 
            
            
            /*----------------------------------*/
            /* Write request                    */
            /*----------------------------------*/
            try{
                // Let the server know what type of client is making 
                // a request
                objectOut.writeByte( JNDI_REQUEST );
            
                // Write the request data.
                req.writeExternal( objectOut );
                objectOut.flush();
            
            } catch (java.io.NotSerializableException e){
                //TODO:2: This doesn't seem to work in the OpenEJB test suite
                // run some other test to see if the exception reaches the client.
                throw new IllegalArgumentException("Object is not serializable: "+ e.getMessage());
            
            } catch (IOException e){
                throw new IOException("Cannot open output stream to server: " + e.getMessage());
            
            } catch (Throwable e){
                throw new IOException("Cannot open output stream to server: " + e.getMessage());
            } 
            
            /*----------------------------------*/
            /* Open input streams               */
            /*----------------------------------*/
            try{
                
                socketIn = socket.getInputStream();
                objectIn = new ObjectInputStream(socketIn);
            
            } catch (StreamCorruptedException e){
                throw new IOException("Cannot open input stream to server, the stream has been corrupted: " + e.getMessage());
            
            } catch (IOException e){
                throw new IOException("Cannot open input stream to server: " + e.getMessage());
            
            } catch (Throwable e){
                throw new IOException("Cannot open output stream to server: " + e.getMessage());
            } 
            
            /*----------------------------------*/
            /* Read response                    */
            /*----------------------------------*/
            try{
                // Read the response from the server
                res = new JNDIResponse();
                res.readExternal( objectIn );
            } catch (ClassNotFoundException e){
                throw new IOException("Cannot read the response from the server.  The class for an object being returned is not located in this system:" + e.getMessage());
    
            } catch (IOException e){
                throw new IOException("Cannot read the response from the server." + e.getMessage());
            
            } catch (Throwable e){
                throw new IOException("Error reading response from server: " + e.getMessage());
            } 
        
        } catch ( Exception error ) {
            throw error;
        
        } finally {
            try {
                if (objectOut != null) objectOut.close();
                if (socketOut != null) socketOut.close();
                if (objectIn  != null) objectIn.close();
                if (socketIn  != null) socketIn.close();
                if (socket    != null) socket.close();
            } catch (Throwable t){
                //TODO:2: Log this
                System.out.println("Error closing connection with server: "+t.getMessage());
            }
        }
        return res;
    }

    public static void print(String s){
        //System.out.println();
    }
    public static void println(String s){
        //System.out.print(s+'\n');
    }

    //TODO:0:Write authentication module
    protected AuthenticationResponse requestAuthorization(AuthenticationRequest req){
        // TODO:0: Remove this temporary Fix
        AuthenticationResponse res = new AuthenticationResponse();
        res.setResponseCode( AUTH_GRANTED );
        res.setIdentity( new ClientMetaData("authenicatedUser") );
        return res;
    }

    //-------------------------------------------------------------//
    //  InitialContextFactory implementation                       //
    //-------------------------------------------------------------//
    
    /**
      * Creates an Initial Context for beginning name resolution.
      * Special requirements of this context are supplied
      * using <code>environment</code>.
      *<p>
      * The environment parameter is owned by the caller.
      * The implementation will not modify the object or keep a reference
      * to it, although it may keep a reference to a clone or copy.
      *
      * @param environment The possibly null environment
      * 		specifying information to be used in the creation 
      * 		of the initial context.
      * @return A non-null initial context object that implements the Context
      *		interface.
      * @exception NamingException If cannot create an initial context.
      */
    public Context getInitialContext(Hashtable environment) throws NamingException{
        if ( environment == null )
            throw new NamingException("Invalid Argument, hashtable cannot be null.");
        else
            env = (Hashtable)environment.clone();

        String userID    = (String) env.get(Context.SECURITY_PRINCIPAL);
        String psswrd    = (String) env.get(Context.SECURITY_CREDENTIALS);
        Object serverURL = env.get(Context.PROVIDER_URL);

        if (userID == null) throw new ConfigurationException("Context property cannot be null: "+Context.SECURITY_PRINCIPAL);
        if (psswrd == null) throw new ConfigurationException("Context property cannot be null: "+Context.SECURITY_CREDENTIALS);
        if (serverURL == null) throw new ConfigurationException("Context property cannot be null: "+Context.PROVIDER_URL);
        
        URL url;
        if ( serverURL instanceof String ) {
            try {
                url = new URL( "http://"+serverURL );
            } catch (Exception e){
                e.printStackTrace();
                throw new ConfigurationException("Invalid provider URL: "+serverURL);
            }
        } else if ( serverURL instanceof URL ) {
            url = (URL)serverURL;
        } else {
            throw new ConfigurationException("Invalid provider URL: "+serverURL);
        }
        
        try {
            server = new ServerMetaData();
            server.address = InetAddress.getByName( url.getHost() );
            server.port    = url.getPort();
        } catch (UnknownHostException  e){
            throw new ConfigurationException("Invalid provider URL:"+serverURL+": host unkown: "+e.getMessage());
        }
        
        //TODO:1: Either aggressively initiate authentication or wait for the 
        //        server to send us an authentication challange.
        client = new ClientMetaData("unauthenticated");
        return this;
    }
    
    
    public void authenticate(String userID, String psswrd) throws javax.naming.AuthenticationException{
        // TODO:1: Skip this if the identity hasn't been changed and
        // the user already has been authenticated.
        AuthenticationRequest  req = new AuthenticationRequest(userID, psswrd);
        AuthenticationResponse res = requestAuthorization(req);
        
        switch (res.getResponseCode()) {
            case AUTH_GRANTED:
                client = res.getIdentity();
                break;
            case AUTH_REDIRECT:
                client = res.getIdentity();
                server = res.getServer();
                break;
            case AUTH_DENIED:
                throw new javax.naming.AuthenticationException("This principle is not authorized.");
        }
    }
    
    // Construct a new handler and proxy.
    public EJBHomeProxy createEJBHomeProxy(EJBMetaDataImpl ejbData){
        
        EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(ejbData, server, client);
        EJBHomeProxy proxy = handler.createEJBHomeProxy();
        handler.ejb.ejbHomeProxy = proxy;
        
        return proxy;
    
    }
    
    //-------------------------------------------------------------//
    // Context implementation                                      //
    //-------------------------------------------------------------//
    
    //----------------------------------------------------------------------//
    //   Supportted methods                                                 //
    //----------------------------------------------------------------------//

    public Object lookup(String name) throws NamingException {

        if ( name == null ) throw new InvalidNameException("The name cannot be null");
        else if ( name.equals("") ) return new JNDIContext(this);
        else if ( !name.startsWith("/") ) name = tail+name;

        JNDIRequest req = new JNDIRequest();
        req.setRequestMethod( JNDIRequest.JNDI_LOOKUP );
        req.setRequestString( name );

        JNDIResponse res = null;
        try{
            res = request(req);
        } catch (Exception e){
            // TODO:1: Better exception handling
            throw new javax.naming.NamingException("Cannot lookup "+name+": Received error: "+e.getMessage());
        }

        switch ( res.getResponseCode() ) {
            case JNDI_EJBHOME:
                // Construct a new handler and proxy.
                return createEJBHomeProxy( (EJBMetaDataImpl)res.getResult() );
            
            case JNDI_OK:    
                return res.getResult();
            
            case JNDI_CONTEXT:
                JNDIContext subCtx = new JNDIContext(this);
                if (!name.endsWith("/")) name += '/';
                subCtx.tail = name;
                return subCtx;
            
            case JNDI_NOT_FOUND:    
                throw new NameNotFoundException(name + " not found");
            
            case JNDI_NAMING_EXCEPTION:
                throw (NamingException) res.getResult();
            
            case JNDI_RUNTIME_EXCEPTION:
                throw (RuntimeException) res.getResult();
            
            case JNDI_ERROR:
                throw (Error) res.getResult();
            default:
                throw new RuntimeException("Invalid response from server :"+res.getResponseCode());
        }
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public NamingEnumeration list(String name) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }


    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public Object addToEnvironment(String key, Object value) throws NamingException {
        return env.put(key, value);
    }

    public Object removeFromEnvironment(String key) throws NamingException {
        return env.remove(key);
    }

    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable)env.clone();
    }

    public String getNameInNamespace() throws NamingException {
        return "";
    }

    public void close() throws NamingException {
    }

    //==============================
    // Unsupported Context methods
    //

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to add beans to the name space.
     *
     * @param name
     * @param obj
     * @exception NamingException
     */
    public void bind(String name, Object obj) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to add beans to the name space.
     *
     * @param name
     * @param obj
     * @exception NamingException
     */
    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to add beans to the name space.
     *
     * @param name
     * @param obj
     * @exception NamingException
     */
    public void rebind(String name, Object obj) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();

    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to add beans to the name space.
     *
     * @param name
     * @param obj
     * @exception NamingException
     */
    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to remove beans from the name space.
     *
     * @param name
     * @exception NamingException
     */
    public void unbind(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();

    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to remove beans from the name space.
     *
     * @param name
     * @exception NamingException
     */
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to rename beans.
     *
     * @param oldname
     * @param newname
     * @exception NamingException
     */
    public void rename(String oldname, String newname)
    throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to rename beans.
     *
     * @param oldname
     * @param newname
     * @exception NamingException
     */
    public void rename(Name oldname, Name newname)
    throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to remove a sub-context from the name space.
     *
     * @param name
     * @exception NamingException
     */
    public void destroySubcontext(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to remove a sub-context from the name space.
     *
     * @param name
     * @exception NamingException
     */
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to add a sub-context to the name space.
     *
     * @param name
     * @return
     * @exception NamingException
     */
    public Context createSubcontext(String name)
    throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    /**
     * Throws a javax.naming.OperationNotSupportedException.
     *
     * Clients are not allowed to add a sub-context to the name space.
     *
     * @param name
     * @return
     * @exception NamingException
     */
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    //
    // Unsupported Context methods
    //==============================
}


