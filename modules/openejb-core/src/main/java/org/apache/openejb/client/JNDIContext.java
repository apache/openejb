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
package org.apache.openejb.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
import javax.naming.*;
import javax.naming.spi.InitialContextFactory;
/**
 * JNDI client
 *
 * @since 11/25/2001
 */
public class JNDIContext implements Serializable, InitialContextFactory, Context, RequestMethods, ResponseCodes {

    private transient String tail = "/";
    private transient ServerMetaData[] servers;
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
        this.servers = that.servers;
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
     */
    public void init(Hashtable environment) throws NamingException{
    }


    private JNDIResponse request(JNDIRequest req) throws Exception {
        RequestInfo reqInfo = new RequestInfo(req, servers);
        JNDIResponse res = new JNDIResponse();
        ResponseInfo resInfo = new ResponseInfo(res);
        Client.request(reqInfo, resInfo);
        return res;
    }

    public static void print(String s){
        //System.out.println();
    }
    public static void println(String s){
        //System.out.print(s+'\n');
    }

    //TODO:0:Write authentication module
    protected AuthenticationResponse requestAuthorization(AuthenticationRequest req) throws java.rmi.RemoteException {
        RequestInfo reqInfo = new RequestInfo(req, servers);
        AuthenticationResponse res = new AuthenticationResponse();
        ResponseInfo resInfo = new ResponseInfo(res);
        Client.request(reqInfo, resInfo);
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

//        if (userID == null) throw new ConfigurationException("Context property cannot be null: "+Context.SECURITY_PRINCIPAL);
//        if (psswrd == null) throw new ConfigurationException("Context property cannot be null: "+Context.SECURITY_CREDENTIALS);
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
            ServerMetaData server = new ServerMetaData();
            server.address = InetAddress.getByName( url.getHost() );
            server.port    = url.getPort();
            
            servers = new ServerMetaData[] {server};
        } catch (UnknownHostException  e){
            throw new ConfigurationException("Invalid provider URL:"+serverURL+": host unknown: "+e.getMessage());
        }

        //TODO:1: Either aggressively initiate authentication or wait for the
        //        server to send us an authentication challange.
        authenticate(userID, psswrd);

        return this;
    }


    public void authenticate(String userID, String psswrd) throws javax.naming.AuthenticationException{
        // TODO:1: Skip this if the identity hasn't been changed and
        // the user already has been authenticated.
        AuthenticationRequest  req = new AuthenticationRequest(userID, psswrd);
        AuthenticationResponse res = null;

        try {
            res = requestAuthorization(req);
        } catch (java.rmi.RemoteException e) {
            throw new javax.naming.AuthenticationException(e.getLocalizedMessage());
        }

        switch (res.getResponseCode()) {
            case AUTH_REDIRECT:
                ServerMetaData server = res.getServer();
                servers = new ServerMetaData[] {server};
                break;
            case AUTH_DENIED:
                throw new javax.naming.AuthenticationException("This principle is not authorized.");
        }
    }

    // Construct a new handler and proxy.
    public EJBHomeProxy createEJBHomeProxy(EJBMetaDataImpl ejbData){

        EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(ejbData, servers);
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

        JNDIRequest req = new JNDIRequest(JNDIRequest.JNDI_LOOKUP, name);

        JNDIResponse res = null;
        try{
            res = request(req);
        } catch (Exception e){
            throw (NamingException)new NamingException("Cannot lookup " + name).initCause(e);
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


