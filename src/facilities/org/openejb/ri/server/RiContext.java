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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.openejb.util.Messages;


/**
 * The initial Context implementation that is returned from the client's first JNDI lookup.
 */
public class RiContext implements Context, DynamicContext {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    Hashtable myEnv;
    private Hashtable bindings = new Hashtable(11);
    private Socket mySocket;
    static NameParser myParser = new RiNameParser();
    final static Byte LIST = new Byte((byte)1);
    final static Byte BINDINGS = new Byte((byte)2);
    final static Byte CLOSE_CONNECTION = new Byte((byte)4);

    ObjectOutputStream oos;
    ObjectInputStream ois;


    /**
     * Constructs this JNDI context for the client.
     *
     * Opens a socket connection with the NamingServer.
     * Initializes object output/input streams for writing/reading objects with the naming server.
     * Authenticates the user's information with the NamingServer
     *
     * @param environment
     * @exception NamingException
     * @see NamingServer
     */
    RiContext(Hashtable environment) throws NamingException{
        init( environment );
    }

    public RiContext(){
    }

    protected void finalize() throws Throwable{
        oos.writeObject(CLOSE_CONNECTION);
        oos.flush();
        oos.close();
        ois.close();
        mySocket.close();
        mySocket = null;
        myEnv = null;
        bindings = null;
    }

    /**
     * Initializes this JNDI context for the client.
     *
     * Opens a socket connection with the NamingServer.
     * Initializes object output/input streams for writing/reading objects with the naming server.
     * Authenticates the user's information with the NamingServer
     *
     * @param environment
     * @exception NamingException
     * @see NamingServer
     */
    public void init(Hashtable environment) throws NamingException{

        if ( environment ==null )
            throw new NamingException( _messages.message( "riContext.invalidArgument" ) );
        else
            myEnv = (Hashtable)environment.clone();

        String userID = (String)myEnv.get(Context.SECURITY_PRINCIPAL);
        String psswrd = (String)myEnv.get(Context.SECURITY_CREDENTIALS);
        AuthenticationRequest authRequest = new AuthenticationRequest(userID, psswrd);
        try {
            URL url;
            Object providerUrl = myEnv.get(Context.PROVIDER_URL);
            if ( providerUrl instanceof String ) {
                url = new URL((String)providerUrl);
            } else {
                url =  (URL)providerUrl;
            }
            mySocket = new Socket(url.getHost(), url.getPort());
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());

            request(authRequest);
            /*
            ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
            oos.writeObject(authRequest);
            oos.flush();*/

        } catch ( NamingException ne ) {
            System.out.println( _messages.message( "riContext.cannotInitialize" ) );
            //ne.printStackTrace();
            throw ne;
        }

        catch ( Exception e ) {
            System.out.println( _messages.message( "riContext.cannotInitialize" ) );
            //e.printStackTrace();
            throw new NamingException(e.getMessage());
        }
    }

    public Object request(Object req) throws NamingException {
        // This is required so that the deserialized proxy can find the
        // classes for the interfaces it is supposed to implement.
        // Since the deserialization is performed by classes loaded from
        // the system classpath, they don't have access to bean interfaces
        // loaded separately (e.g. by downloading from the server)
        if(Thread.currentThread().getContextClassLoader() != RiContext.class.getClassLoader()) {
            Thread.currentThread().setContextClassLoader(RiContext.class.getClassLoader());
        }
        try {

            oos.writeObject(req);
            oos.flush();

            Object answer = ois.readObject();

            if ( answer instanceof NamingException ) {
                System.out.println( _messages.format( "riContext.serverReturnedNamingException", answer ) );
                throw (NamingException)answer;
            } else
                return answer;


        } catch ( Exception e ) {
            System.out.println( _messages.format( "riContext.operationTerminated", req ) );
            //e.printStackTrace();
            throw new NamingException(e.getMessage());
        }
    }

    public Object lookup(String name) throws NamingException {
        if ( name.equals("") ) {
            // Asking to look up this context itself. Create and return
            // a new instance with its own independent environment.
            return(new RiContext(myEnv));
        }
        Object answer = request(name);
        if ( answer == null ) {
            throw new NameNotFoundException( _messages.format( "riContext.nameNotFound", name ) );
        }
        return answer;
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public NamingEnumeration list(String name) throws NamingException {
        if ( name.equals("") ) {

            return new RiNamingEnum((Vector)request(LIST));
        }
        throw new NotContextException( _messages.format( "riContext.nameNotListed", name ) );
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        if ( name.equals("") ) {

            return new RiNamingEnum((Vector)request(BINDINGS));
        }
        throw new NotContextException( _messages.format( "riContext.nameNotListed", name ) );
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }


    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix) throws NamingException {
        Name result = composeName(new CompositeName(name),
                                  new CompositeName(prefix));
        return result.toString();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name)(prefix.clone());
        result.addAll(name);
        return result;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        if ( myEnv == null ) {
            myEnv = new Hashtable(5, 0.75f);
        }
        return myEnv.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        if ( myEnv == null )
            return null;
        return myEnv.remove(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        if ( myEnv == null ) {
            // Must return non-null
            return new Hashtable(3, 0.75f);
        } else {
            return(Hashtable)myEnv.clone();
        }
    }

    public String getNameInNamespace() throws NamingException {
        return "";
    }

    public void close() throws NamingException {
        myEnv = null;
        bindings = null;
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


    //===============================================
    // Inner class for enumerating name/class pairs
    //

    class RiNamingEnum implements NamingEnumeration {
        Enumeration names;

        RiNamingEnum (Vector pairs) {
            names = pairs.elements();
        }
        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }
        public boolean hasMore() throws NamingException {
            return names.hasMoreElements();
        }
        public Object nextElement() {
            return names.nextElement();
        }
        public Object next() throws NamingException {
            return names.nextElement();
        }
        public void close() {
        }
    }

    //
    // Inner class for enumerating name/class pairs
    //===============================================
}
