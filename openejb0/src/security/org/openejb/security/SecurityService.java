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

package org.openejb.security;

import java.lang.ThreadLocal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;

import org.openejb.util.SafeToolkit;
import org.openejb.util.Logger;
import org.openejb.security.spi.SecurityRealmSpi;
import org.openejb.security.castor.security.*;
import org.openejb.security.castor.securityjar.*;


/**
 *
 * @author  adc
 * @version 1.0
 */
public class SecurityService
    extends javax.security.auth.login.Configuration 
    implements org.openejb.alt.spi.SecurityService
{
    
    protected static Logger _logger = Logger.getInstance( "OpenEJB.Security", "org.openejb.security.util.resources" );

    static {
	org.openejb.util.JarUtils.setHandlerSystemProperty();
    }

    public final static String CONFIG_FILE = "openejb.securityservice.config.file";
    protected static SafeToolkit _toolkit = SafeToolkit.getToolkit("OpenEJB.SecurityService");
    protected ThreadLocal _threadStorage = new ThreadLocal();
    protected java.util.Properties _props;
    protected static HashMap _securityRealms = new HashMap();
    protected String _configLocation;

    /**
     * Initialize the OpenE security service. 
     *
     * @param props the properties used to initialize the security service
     */
    public void init( Properties props ) throws Exception {
        _props = props;

        _configLocation = _props.getProperty(CONFIG_FILE, "resource:/default.security.conf");

	Security security = ConfigUtils.readConfig( _configLocation );

        Properties properties;
        Realm realm;
        
        for (int i=0; i<security.getRealmCount(); i++) {
            realm = security.getRealm(i);
            
            String name = realm.getId();

	    RealmProvider provider = ConfigUtils.getRealmProvider( name );

            properties = ConfigUtils.assemblePropertiesFor( realm.getId(),
                                                            realm.getContent(),
                                                            _configLocation,
                                                            provider );

            Class realmClass = _toolkit.loadClass( provider.getClassName(), null );
            SecurityRealmSpi securityRealm = (SecurityRealmSpi)_toolkit.newInstance( realmClass );
            
	    securityRealm.setSecurityRealmName( realm.getName() );
            securityRealm.init( properties );
                        
            _securityRealms.put( realm.getName(), securityRealm );            
	}


	javax.security.auth.login.Configuration.setConfiguration( this );

	_logger.i18n.info( "security.startup" );
    }


    /** 
     * Check if securityIdentity is authorized to perform the specified action.
     * This is currently used by OpenEJB to check if a caller is authorized to
     * to assume at least one of a collection of roles, the roles authorized for
     * a particular method of a particular deployment.
     */   
    public boolean isCallerAuthorized( Object securityIdentity, String [] roleNames ) {

        if (securityIdentity == null) return false;

        if (roleNames == null) return true;

        Subject subject = (Subject)securityIdentity;
        Set principals = subject.getPrincipals();
        Iterator iter;
	for ( int i=0; i<roleNames.length; i++ ) {
	    System.err.print("roleName: " + roleNames[i] );
            
            iter = principals.iterator();
            while ( iter.hasNext() ) {
                Principal principal = (Principal)iter.next();
                String key = (String)_registeredPrincipals.get( principal );
                if ( key.equals( roleNames[i] ) ) {
                    System.err.println(" FOUND ");
                    return true;
                } else
                    System.err.println(" NOT FOUND ");
           }
	}

	return false;
        
    }

    public SecurityRealmSpi getSecurityRealm( String id ) {
        return (SecurityRealmSpi)_securityRealms.get( id );
    }

    public String mapToKey( String realm, String type, String name ) {
	SecurityRealmSpi sr = getSecurityRealm( realm );

	return "{"+realm+"}"+type+"#"+name;
    }
    private HashMap _registeredPrincipals = new HashMap();
    public String registerPrincipal( String realm, Principal principal ) {
        String value = principal.getName();
        _registeredPrincipals.put( principal, value );
        return value;
    }

    /**
     * Attempts to convert an opaque securityIdentity to a concrete target type.
     * This is currently used to obtain an java.security.Princiapl type which
     * must be returned by OpenEJB when a bean invokes EJBContext.getCallerPrincipal().
     * Conversion to a Principal type must be supported.
     *
     * It may also be used by JCX connectors to obtain the JAAS Subject of the caller,
     * support for translation to Subject type is currently optional.
     *
     */
    public Object translateTo( Object securityIdentity, Class type ) {
        
        if (securityIdentity == null) return null;

	Object retVal =null;
	if ( type == Principal.class ) {
	    Subject subject = (Subject)securityIdentity;
	    Set principals = subject.getPrincipals();
	    
	    retVal = principals.iterator().next();
	}
	return retVal;
    }
    
    /**
     * Associates a security identity object with the current thread. Setting 
     * this argument to null, will effectively dissociate the thread with a
     * security identity.  This is used when access enterprise beans through 
     * the global JNDI name space. Its not used when calling invoke on a 
     * RpcContainer object.
     */
    public void setSecurityIdentity( Object securityIdentity ) {
        _threadStorage.set(securityIdentity);
    }
    
    /**
     * Obtains the security identity associated with the current thread.
     * If there is no association, then null is returned. 
     */
    public Object getSecurityIdentity() {
        return _threadStorage.get();
    }


    // Configuration implmentation

    /**
     * Retrieve an array of AppConfigurationEntries which corresponds to
     *		the configuration of LoginModules for this application.
     *
     * <p>
     *
     * @param applicationName the name used to index the Configuration.
     * 
     * @return an array of AppConfigurationEntries which corresponds to
     *		the configuration of LoginModules for this
     *		application, or null if this application has no configured
     *		LoginModules.
     */
    public AppConfigurationEntry[] getAppConfigurationEntry( String id ) {
        SecurityRealmSpi securityRealm = getSecurityRealm( id );
        
        if (securityRealm == null) return null;
        return securityRealm.getAppConfigurationEntry();
    }


    /**
     * Refresh and reload the Configuration.
     *
     * <p> This method causes this object to refresh/reload its current
     * Configuration. This is implementation-dependent.
     * For example, if the Configuration object is stored
     * a file, calling <code>refresh</code> will cause the file to be re-read.
     *
     * <p>
     *
     * @exception SecurityException if the caller does not have permission
     *				to refresh the Configuration.
     */
    public void refresh() {
    }

}

