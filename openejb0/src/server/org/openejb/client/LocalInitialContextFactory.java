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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.client;

import java.io.IOException;
import java.util.Hashtable;
import javax.naming.*;
import javax.naming.spi.InitialContextFactory;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.Subject;

import org.openejb.util.proxy.InvocationHandler;
import org.openejb.core.ivm.IntraVmProxy;
import org.openejb.loader.Loader;
import org.openejb.util.proxy.ProxyManager;


/**
 * LocalInitialContextFactory
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 10/5/2002
 */
public class LocalInitialContextFactory implements InitialContextFactory {

    static Context _intraVmContext;

    
    public Context getInitialContext( Hashtable environment ) throws NamingException 
    {
        if (environment == null) throw new NamingException("Invalid Argument, hashtable cannot be null.");

        final String userID    = (String) environment.get(Context.SECURITY_PRINCIPAL);
        final String psswrd    = (String) environment.get(Context.SECURITY_CREDENTIALS);
        String realm     = (String) environment.get("org.openenb.security.realm");
        Object serverURL = environment.get(Context.PROVIDER_URL);

        if (userID == null) throw new ConfigurationException("Context property cannot be null: "+Context.SECURITY_PRINCIPAL);
        if (psswrd == null) throw new ConfigurationException("Context property cannot be null: "+Context.SECURITY_CREDENTIALS);
        if (serverURL == null) throw new ConfigurationException("Context property cannot be null: "+Context.PROVIDER_URL);
        
	if (realm == null) {
	    realm = "Pseudo Realm";
	}


        LoginContext lc = null;

        try {
	    lc = new LoginContext( realm, new CallbackHandler()  {
		    public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
		    {
			for (int i = 0; i < callbacks.length; i++) {
                            if (callbacks[i] instanceof NameCallback) {
                                NameCallback nc = (NameCallback)callbacks[i];
 
                                nc.setName(userID);

                            } else if (callbacks[i] instanceof PasswordCallback) {
                                PasswordCallback pc = (PasswordCallback)callbacks[i];
                                pc.setPassword(psswrd.toCharArray());
 
                            } else {
                                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                            }
			}
		    }
		} );
	    lc.login();
        } catch( javax.security.auth.login.FailedLoginException fle ) {
            throw new javax.naming.AuthenticationException( "This principle is not authorized." );
        } catch( javax.security.auth.login.LoginException le ) {
            throw new javax.naming.AuthenticationException( "This principle is not authorized." );
        }

	if ( _intraVmContext == null ) {
	    try { 
		    getLoader(environment).load(environment);
	    } catch( Exception e ) {
                throw new  NamingException("Attempted to load OpenEJB. "+e.getMessage());
            }
            _intraVmContext = getIntraVmContext(environment);
        }

        return new LocalInitialContext(environment, lc.getSubject());
    }

    private Loader getLoader(Hashtable env) throws Exception {
        Loader loader = null;
        String type = (String)env.get("openejb.loader");

        try{
            if (type == null || type.equals("context")) {
                loader = instantiateLoader("org.openejb.loader.EmbeddingLoader");                
            } else if ( type.equals("embed")) {
                loader = instantiateLoader("org.openejb.loader.EmbeddingLoader");                
            } else if ( type.equals("system")) {
                loader = instantiateLoader("org.openejb.loader.SystemLoader");                
            } else if ( type.equals("bootstrap")) {
                loader = instantiateLoader("org.openejb.loader.SystemLoader");                
            } else if ( type.equals("noload")) {
                loader = instantiateLoader("org.openejb.loader.EmbeddedLoader");                
            } else if ( type.equals("embedded")) {
                loader = instantiateLoader("org.openejb.loader.EmbeddedLoader");                
            } // other loaders here
        } catch (Exception e){
            throw new Exception( "Loader "+type+". "+ e.getMessage() );
        }
        return loader;
    }

    private ClassLoader getClassLoader(){
        try{
            return Thread.currentThread().getContextClassLoader();
        } catch (Exception e){
            //e.printStackTrace();
        }
        return null;
    }

    private Loader instantiateLoader(String loaderName) throws Exception{
        Loader loader = null;
        try{
            ClassLoader cl = getClassLoader();
            Class loaderClass = Class.forName(loaderName, true, cl );
            loader = (Loader)loaderClass.newInstance();
        } catch (Exception e){
            throw new Exception(
                "Could not instantiate the Loader "+loaderName+". Exception "+
                e.getClass().getName()+" "+ e.getMessage());
        } 
        return loader;
    }
    
    
    private Context getIntraVmContext( Hashtable env ) throws NamingException {
        Context context = null;
        try{
            InitialContextFactory factory = null;
            ClassLoader cl = getClassLoader();
            Class ivmFactoryClass = Class.forName( "org.openejb.core.ivm.naming.InitContextFactory", true, cl );
            
            factory = (InitialContextFactory)ivmFactoryClass.newInstance();
            context = factory.getInitialContext( env );
        } catch (Exception e){
            throw new NamingException( 
                "Cannot instantiate an IntraVM InitialContext. Exception: "+
                e.getClass().getName()+" "+ e.getMessage());
        }

        return context;
    }

    class LocalInitialContext implements Context {
        private transient ClientMetaData _client = new ClientMetaData();
        private transient Hashtable _env;
        
        public LocalInitialContext(Hashtable environment, Object data) {
            _env = (Hashtable)environment.clone();
            _client.setClientIdentity(data);
        }

        /**
         * Retrieves the named object.
         * If <tt>name</tt> is empty, returns a new instance of this context
         * (which represents the same naming context as this context, but its
         * environment may be modified independently and it may be accessed
         * concurrently).
         *
         * @param name
         *		the name of the object to look up
         * @return	the object bound to <tt>name</tt>
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #lookup(String)
         * @see #lookupLink(Name)
         */
        public Object lookup(Name name) throws NamingException {
            Object result = _intraVmContext.lookup(name);
            
            if (result instanceof IntraVmProxy) {
                InvocationHandler handler = new SecurityCtxInvocationHandler(result, _client);
                Class[] interfaces = result.getClass().getInterfaces();
    
                try {
                    result = ProxyManager.newProxyInstance(interfaces, handler);
                } catch(IllegalAccessException e) {
                    throw new NamingException("Unable to create proxy");
                }
            }
    
            return result;
        }
        
        /**
         * Retrieves the named object.
         * See {@link #lookup(Name)} for details.
         * @param name
         *		the name of the object to look up
         * @return	the object bound to <tt>name</tt>
         * @throws	NamingException if a naming exception is encountered
         */
        public Object lookup(String name) throws NamingException {
            Object result = _intraVmContext.lookup(name);
            
            if (result instanceof IntraVmProxy) {
                InvocationHandler handler = new SecurityCtxInvocationHandler(result, _client);
                Class[] interfaces = result.getClass().getInterfaces();
    
                try {
                    result = ProxyManager.newProxyInstance(interfaces, handler);
                } catch(IllegalAccessException e) {
                    throw new NamingException("Unable to create proxy");
                }
            }
    	
        	return result;
        }
        
        /**
         * Throws a OperationNotSupportedException.
         *
         * Clients are not allowed to add beans to the name space.
         *
         * @param name
         * @param obj
         * @exception NamingException
         */
        public void bind(String name, Object obj) throws NamingException {
        	_intraVmContext.bind(name, obj);
        }
    
        /**
         * Throws a OperationNotSupportedException.
         *
         * Clients are not allowed to add beans to the name space.
         *
         * @param name
         * @param obj
         * @exception NamingException
         */
        public void bind(Name name, Object obj) throws NamingException {
            _intraVmContext.bind(name, obj);
        }
        /**
         * Binds a name to an object, overwriting any existing binding.
         * All intermediate contexts and the target context (that named by all
         * but terminal atomic component of the name) must already exist.
         *
         * <p> If the object is a <tt>DirContext</tt>, any existing attributes
         * associated with the name are replaced with those of the object.
         * Otherwise, any existing attributes associated with the name remain
         * unchanged.
         *
         * @param name
         *		the name to bind; may not be empty
         * @param obj
         *		the object to bind; possibly null
         * @throws	javax.naming.directory.InvalidAttributesException
         *	 	if object did not supply all mandatory attributes
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #rebind(String, Object)
         * @see #bind(Name, Object)
         * @see javax.naming.directory.DirContext#rebind(Name, Object,
         *		javax.naming.directory.Attributes)
         * @see javax.naming.directory.DirContext
         */
        public void rebind(Name name, Object obj) throws NamingException {
            _intraVmContext.rebind(name, obj);
        }
    
        /**
         * Binds a name to an object, overwriting any existing binding.
         * See {@link #rebind(Name, Object)} for details.
         *
         * @param name
         *		the name to bind; may not be empty
         * @param obj
         *		the object to bind; possibly null
         * @throws	javax.naming.directory.InvalidAttributesException
         *	 	if object did not supply all mandatory attributes
         * @throws	NamingException if a naming exception is encountered
         */
        public void rebind(String name, Object obj) throws NamingException {
            _intraVmContext.rebind(name, obj);
        }
    
        /**
         * Unbinds the named object.
         * Removes the terminal atomic name in <code>name</code>
         * from the target context--that named by all but the terminal
         * atomic part of <code>name</code>.
         *
         * <p> This method is idempotent.
         * It succeeds even if the terminal atomic name
         * is not bound in the target context, but throws
         * <tt>NameNotFoundException</tt>
         * if any of the intermediate contexts do not exist.
         *
         * <p> Any attributes associated with the name are removed.
         * Intermediate contexts are not changed.
         *
         * @param name
         *		the name to unbind; may not be empty
         * @throws	NameNotFoundException if an intermediate context does not exist
         * @throws	NamingException if a naming exception is encountered
         * @see #unbind(String)
         */
        public void unbind(Name name) throws NamingException {
            _intraVmContext.unbind(name);
        }
    
        /**
         * Unbinds the named object.
         * See {@link #unbind(Name)} for details.
         *
         * @param name
         *		the name to unbind; may not be empty
         * @throws	NameNotFoundException if an intermediate context does not exist
         * @throws	NamingException if a naming exception is encountered
         */
        public void unbind(String name) throws NamingException {
            _intraVmContext.unbind(name);
        }
    
        /**
         * Binds a new name to the object bound to an old name, and unbinds
         * the old name.  Both names are relative to this context.
         * Any attributes associated with the old name become associated
         * with the new name.
         * Intermediate contexts of the old name are not changed.
         *
         * @param oldName
         *		the name of the existing binding; may not be empty
         * @param newName
         *		the name of the new binding; may not be empty
         * @throws	NameAlreadyBoundException if <tt>newName</tt> is already bound
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #rename(String, String)
         * @see #bind(Name, Object)
         * @see #rebind(Name, Object)
         */
        public void rename(Name oldName, Name newName) throws NamingException {
            _intraVmContext.rename(oldName, newName);
        }
    
        /**
         * Binds a new name to the object bound to an old name, and unbinds
         * the old name.
         * See {@link #rename(Name, Name)} for details.
         *
         * @param oldName
         *		the name of the existing binding; may not be empty
         * @param newName
         *		the name of the new binding; may not be empty
         * @throws	NameAlreadyBoundException if <tt>newName</tt> is already bound
         * @throws	NamingException if a naming exception is encountered
         */
        public void rename(String oldName, String newName) throws NamingException {
            _intraVmContext.rename(oldName, newName);
        }
    
        /**
         * Enumerates the names bound in the named context, along with the
         * class names of objects bound to them.
         * The contents of any subcontexts are not included.
         *
         * <p> If a binding is added to or removed from this context,
         * its effect on an enumeration previously returned is undefined.
         *
         * @param name
         *		the name of the context to list
         * @return	an enumeration of the names and class names of the
         *		bindings in this context.  Each element of the
         *		enumeration is of type <tt>NameClassPair</tt>.
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #list(String)
         * @see #listBindings(Name)
         * @see NameClassPair
         */
        public NamingEnumeration list(Name name) throws NamingException {
            return _intraVmContext.list(name);
        }
    
        /**
         * Enumerates the names bound in the named context, along with the
         * class names of objects bound to them.
         * See {@link #list(Name)} for details.
         *
         * @param name
         *		the name of the context to list
         * @return	an enumeration of the names and class names of the
         *		bindings in this context.  Each element of the
         *		enumeration is of type <tt>NameClassPair</tt>.
         * @throws	NamingException if a naming exception is encountered
         */
        public NamingEnumeration list(String name) throws NamingException {
            return _intraVmContext.list(name);
        }
    
        /**
         * Enumerates the names bound in the named context, along with the
         * objects bound to them.
         * The contents of any subcontexts are not included.
         *
         * <p> If a binding is added to or removed from this context,
         * its effect on an enumeration previously returned is undefined.
         *
         * @param name
         *		the name of the context to list
         * @return	an enumeration of the bindings in this context.
         *		Each element of the enumeration is of type
         *		<tt>Binding</tt>.
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #listBindings(String)
         * @see #list(Name)
         * @see Binding
          */
        public NamingEnumeration listBindings(Name name) throws NamingException {
            return _intraVmContext.listBindings(name);
        }
    
        /**
         * Enumerates the names bound in the named context, along with the
         * objects bound to them.
         * See {@link #listBindings(Name)} for details.
         *
         * @param name
         *		the name of the context to list
         * @return	an enumeration of the bindings in this context.
         *		Each element of the enumeration is of type
         *		<tt>Binding</tt>.
         * @throws	NamingException if a naming exception is encountered
         */
        public NamingEnumeration listBindings(String name) throws NamingException {
            return _intraVmContext.listBindings(name);
        }
    
        /**
         * Destroys the named context and removes it from the namespace.
         * Any attributes associated with the name are also removed.
         * Intermediate contexts are not destroyed.
         *
         * <p> This method is idempotent.
         * It succeeds even if the terminal atomic name
         * is not bound in the target context, but throws
         * <tt>NameNotFoundException</tt>
         * if any of the intermediate contexts do not exist.
         *
         * <p> In a federated naming system, a context from one naming system
         * may be bound to a name in another.  One can subsequently
         * look up and perform operations on the foreign context using a
         * composite name.  However, an attempt destroy the context using
         * this composite name will fail with
         * <tt>NotContextException</tt>, because the foreign context is not
         * a "subcontext" of the context in which it is bound.
         * Instead, use <tt>unbind()</tt> to remove the
         * binding of the foreign context.  Destroying the foreign context
         * requires that the <tt>destroySubcontext()</tt> be performed
         * on a context from the foreign context's "native" naming system.
         *
         * @param name
         *		the name of the context to be destroyed; may not be empty
         * @throws	NameNotFoundException if an intermediate context does not exist
         * @throws	NotContextException if the name is bound but does not name a
         *		context, or does not name a context of the appropriate type
         * @throws	ContextNotEmptyException if the named context is not empty
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #destroySubcontext(String)
         */
        public void destroySubcontext(Name name) throws NamingException {
            _intraVmContext.destroySubcontext(name);
        }
    
        /**
         * Destroys the named context and removes it from the namespace.
         * See {@link #destroySubcontext(Name)} for details.
         *
         * @param name
         *		the name of the context to be destroyed; may not be empty
         * @throws	NameNotFoundException if an intermediate context does not exist
         * @throws	NotContextException if the name is bound but does not name a
         *		context, or does not name a context of the appropriate type
         * @throws	ContextNotEmptyException if the named context is not empty
         * @throws	NamingException if a naming exception is encountered
         */
        public void destroySubcontext(String name) throws NamingException {
            _intraVmContext.destroySubcontext(name);
        }
    
        /**
         * Creates and binds a new context.
         * Creates a new context with the given name and binds it in
         * the target context (that named by all but terminal atomic
         * component of the name).  All intermediate contexts and the
         * target context must already exist.
         *
         * @param name
         *		the name of the context to create; may not be empty
         * @return	the newly created context
         *
         * @throws	NameAlreadyBoundException if name is already bound
         * @throws	javax.naming.directory.InvalidAttributesException
         *		if creation of the subcontext requires specification of
         *		mandatory attributes
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #createSubcontext(String)
         * @see javax.naming.directory.DirContext#createSubcontext
         */
        public Context createSubcontext(Name name) throws NamingException {
            return _intraVmContext.createSubcontext(name);
        }
    
        /**
         * Creates and binds a new context.
         * See {@link #createSubcontext(Name)} for details.
         *
         * @param name
         *		the name of the context to create; may not be empty
         * @return	the newly created context
         *
         * @throws	NameAlreadyBoundException if name is already bound
         * @throws	directory.InvalidAttributesException
         *		if creation of the subcontext requires specification of
         *		mandatory attributes
         * @throws	NamingException if a naming exception is encountered
         */
        public Context createSubcontext(String name) throws NamingException {
            return _intraVmContext.createSubcontext(name);
        }
    
        /**
         * Retrieves the named object, following links except
         * for the terminal atomic component of the name.
         * If the object bound to <tt>name</tt> is not a link,
         * returns the object itself.
         *
         * @param name
         *		the name of the object to look up
         * @return	the object bound to <tt>name</tt>, not following the
         *		terminal link (if any).
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #lookupLink(String)
         */
        public Object lookupLink(Name name) throws NamingException {
            return _intraVmContext.lookupLink(name);
        }
    
        /**
         * Retrieves the named object, following links except
         * for the terminal atomic component of the name.
         * See {@link #lookupLink(Name)} for details.
         *
         * @param name
         *		the name of the object to look up
         * @return	the object bound to <tt>name</tt>, not following the
         *		terminal link (if any)
         * @throws	NamingException if a naming exception is encountered
         */
        public Object lookupLink(String name) throws NamingException {
            return _intraVmContext.lookupLink(name);
        }
    
        /**
         * Retrieves the parser associated with the named context.
         * In a federation of namespaces, different naming systems will
         * parse names differently.  This method allows an application
         * to get a parser for parsing names into their atomic components
         * using the naming convention of a particular naming system.
         * Within any single naming system, <tt>NameParser</tt> objects
         * returned by this method must be equal (using the <tt>equals()</tt>
         * test).
         *
         * @param name
         *		the name of the context from which to get the parser
         * @return	a name parser that can parse compound names into their atomic
         *		components
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #getNameParser(String)
         * @see CompoundName
         */
        public NameParser getNameParser(Name name) throws NamingException {
            return _intraVmContext.getNameParser(name);
        }
    
        /**
         * Retrieves the parser associated with the named context.
         * See {@link #getNameParser(Name)} for details.
         *
         * @param name
         *		the name of the context from which to get the parser
         * @return	a name parser that can parse compound names into their atomic
         *		components
         * @throws	NamingException if a naming exception is encountered
         */
        public NameParser getNameParser(String name) throws NamingException {
            return _intraVmContext.getNameParser(name);
        }
    
        /**
         * Composes the name of this context with a name relative to
         * this context.
         * Given a name (<code>name</code>) relative to this context, and
         * the name (<code>prefix</code>) of this context relative to one
         * of its ancestors, this method returns the composition of the
         * two names using the syntax appropriate for the naming
         * system(s) involved.  That is, if <code>name</code> names an
         * object relative to this context, the result is the name of the
         * same object, but relative to the ancestor context.  None of the
         * names may be null.
         * <p>
         * For example, if this context is named "wiz.com" relative
         * to the initial context, then
         * <pre>
         *	composeName("east", "wiz.com")	</pre>
         * might return <code>"east.wiz.com"</code>.
         * If instead this context is named "org/research", then
         * <pre>
         *	composeName("user/jane", "org/research")	</pre>
         * might return <code>"org/research/user/jane"</code> while
         * <pre>
         *	composeName("user/jane", "research")	</pre>
         * returns <code>"research/user/jane"</code>.
         *
         * @param name
         *		a name relative to this context
         * @param prefix
         *		the name of this context relative to one of its ancestors
         * @return	the composition of <code>prefix</code> and <code>name</code>
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #composeName(String, String)
         */
        public Name composeName(Name name, Name prefix) throws NamingException {
            return _intraVmContext.composeName(name, prefix);
        }
    
        /**
         * Composes the name of this context with a name relative to
         * this context.
         * See {@link #composeName(Name, Name)} for details.
         *
         * @param name
         *		a name relative to this context
         * @param prefix
         *		the name of this context relative to one of its ancestors
         * @return	the composition of <code>prefix</code> and <code>name</code>
         * @throws	NamingException if a naming exception is encountered
         */
        public String composeName(String name, String prefix) throws NamingException {
            return _intraVmContext.composeName(name, prefix);
        }
    
        /**
         * Adds a new environment property to the environment of this
         * context.  If the property already exists, its value is overwritten.
         * See class description for more details on environment properties.
         *
         * @param propName
         *		the name of the environment property to add; may not be null
         * @param propVal
         *		the value of the property to add; may not be null
         * @return	the previous value of the property, or null if the property was
         *		not in the environment before
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #getEnvironment()
         * @see #removeFromEnvironment(String)
         */
        public Object addToEnvironment(String propName, Object propVal) throws NamingException {
            return _intraVmContext.addToEnvironment(propName, propVal);
        }
    
        /**
         * Removes an environment property from the environment of this
         * context.  See class description for more details on environment
         * properties.
         *
         * @param propName
         *		the name of the environment property to remove; may not be null
         * @return	the previous value of the property, or null if the property was
         *		not in the environment
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #getEnvironment()
         * @see #addToEnvironment(String, Object)
         */
        public Object removeFromEnvironment(String propName) throws NamingException {
            return _intraVmContext.removeFromEnvironment(propName);
        }
    
        /**
         * Retrieves the environment in effect for this context.
         * See class description for more details on environment properties.
         *
         * <p> The caller should not make any changes to the object returned:
         * their effect on the context is undefined.
         * The environment of this context may be changed using
         * <tt>addToEnvironment()</tt> and <tt>removeFromEnvironment()</tt>.
         *
         * @return	the environment of this context; never null
         * @throws	NamingException if a naming exception is encountered
         *
         * @see #addToEnvironment(String, Object)
         * @see #removeFromEnvironment(String)
         */
        public Hashtable getEnvironment() throws NamingException {
            return _intraVmContext.getEnvironment();
        }
    
        /**
         * Closes this context.
         * This method releases this context's resources immediately, instead of
         * waiting for them to be released automatically by the garbage collector.
         *
         * <p> This method is idempotent:  invoking it on a context that has
         * already been closed has no effect.  Invoking any other method
         * on a closed context is not allowed, and results in undefined behaviour.
         *
         * @throws	NamingException if a naming exception is encountered
         */
        public void close() throws NamingException {
            _intraVmContext.close();
        }
    
        /**
         * Retrieves the full name of this context within its own namespace.
         *
         * <p> Many naming services have a notion of a "full name" for objects
         * in their respective namespaces.  For example, an LDAP entry has
         * a distinguished name, and a DNS record has a fully qualified name.
         * This method allows the client application to retrieve this name.
         * The string returned by this method is not a JNDI composite name
         * and should not be passed directly to context methods.
         * In naming systems for which the notion of full name does not
         * make sense, <tt>OperationNotSupportedException</tt> is thrown.
         *
         * @return	this context's name in its own namespace; never null
         * @throws	OperationNotSupportedException if the naming system does
         *		not have the notion of a full name
         * @throws	NamingException if a naming exception is encountered
         *
         * @since 1.3
         */
        public String getNameInNamespace() throws NamingException {
            return _intraVmContext.getNameInNamespace();
        }
    }
}

 
