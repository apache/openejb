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


import org.openejb.util.Logger;
import org.openejb.util.Messages;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;


/**
 *
 * @author  adc
 * @version 
 */
public class PropertiesFileLoginModule implements javax.security.auth.spi.LoginModule {

    private static Logger _logger = Logger.getInstance( "OpenEJB.Security", "org.openejb.security.util.resources" );
    private static Messages _messages =  new Messages( "org.openejb.security.util.resources" );

    private Subject _subject;
    private CallbackHandler _callbackHandler;

    private Map _sharedState;
    private Map _options;

    // the authentication status
    private boolean _succeeded = false;
    private boolean _commitSucceeded = false;

    // username and password
    private String _username;
    private String _password;

    private PropertiesFilePrincipalUser _userPrincipal;
    private LinkedList _groupPrincipals = new LinkedList();

    private Properties _users = new Properties();
    private Properties _groups = new Properties();


    public PropertiesFileLoginModule() {
    }

    public boolean abort() throws LoginException {

	_logger.i18n.debug( "propFileLoginModule.abort" );
        
        if ( _succeeded == false ) {
            return false;
        } else if ( _succeeded == true && _commitSucceeded == false ) {
            _succeeded = false;
            _username = null;
            _password = null;
            _userPrincipal = null;
	    _groupPrincipals.clear();
        } else {
            logout();
        }
        return true;
    }
    
    public boolean commit() throws LoginException {

	_logger.i18n.debug( "propFileLoginModule.commit" );
        
	if (_succeeded == false) {
            return false;
        } else {
            if ( !_subject.getPrincipals().contains(_userPrincipal ) ) {
		_subject.getPrincipals().add( _userPrincipal );
	    }

	    Iterator iter = _groupPrincipals.iterator();
	    while ( iter.hasNext() ) {
		PropertiesFilePrincipalGroup groupPrincipal = (PropertiesFilePrincipalGroup)iter.next();

		if ( !_subject.getPrincipals().contains( groupPrincipal ) ) {
		    _subject.getPrincipals().add( groupPrincipal );
		}
	     }

            // in any case, clean out state
            _username = null;
            _password = null;

            _commitSucceeded = true;

            return true;
        }
    }
    
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options ) {

	_logger.i18n.debug( "propFileLoginModule.initialize" );

        _subject = subject;
        _callbackHandler = callbackHandler;
        _sharedState = sharedState;
        _options = options;

	String userFileName = (String)_options.get( new String( "user_file_name" ) );
	String groupFileName = (String)_options.get( new String( "group_file_name" ) );
	_logger.i18n.debug( "propFileLoginModule.user_file_name", userFileName );
	_logger.i18n.debug( "propFileLoginModule.group_file_name", groupFileName );

	_users = new Properties();
	try {
	    _users.load( new FileInputStream( userFileName ) );
	} catch( FileNotFoundException fnfe ) {
	    _users = null;
	} catch( IOException ioe ) {
	    _users = null;
	}
    
	try {
	    _groups.load( new FileInputStream( groupFileName ) );
	} catch( FileNotFoundException fnfe ) {
	    _groups = null;
	} catch( IOException ioe ) {
	    _groups = null;
	}
    }
    
    public boolean login() throws LoginException {

	_logger.debug( "propFileLoginModule.login" );
        
        if (_callbackHandler == null) {
            String msg = _messages.message( "propFileLoginModule.noCallbackHandler" );

	    _logger.error( msg );
            throw new LoginException( msg );
	}
        
	if (_users == null) {
            String msg = _messages.message( "propFileLoginModule.unableLoadUsers" );

	    _logger.error( msg );
            throw new LoginException( msg );
	}

        
	if (_groups == null){
            String msg = _messages.message( "propFileLoginModule.unableLoadGroups" );

	    _logger.error( msg );
            throw new LoginException( msg );
	}

            
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback( _messages.message( "propFileLoginModule.username" ) );
        callbacks[1] = new PasswordCallback( _messages.message( "propFileLoginModule.password" ), false );

        try {

            _callbackHandler.handle(callbacks);

            _username = ((NameCallback)callbacks[0]).getName();
            _password = new String(((PasswordCallback)callbacks[1]).getPassword());
            ((PasswordCallback)callbacks[1]).clearPassword();
        } catch (java.io.IOException ioe) {
	    _logger.error( ioe.toString() );

            throw new LoginException( ioe.toString() );
        } catch (UnsupportedCallbackException uce) {
	    String msg = _messages.format( "propFileLoginModule.unsupportedCallbackException", uce.getCallback().toString() );
	    _logger.error( msg );
            
	    throw new LoginException( msg );
        }
        
        if ( !_users.containsKey(_username) ) {
	    String msg = _messages.format( "propFileLoginModule.userDoesNotExist", _username );
	    _logger.error( msg );
            
	    throw new LoginException( msg );
	}

        // verify the username/password
        if ( _users.getProperty(_username).equals( _password ) ) {
            // authentication succeeded!!!
            _succeeded = true;

            _userPrincipal = new PropertiesFilePrincipalUser( _username );
        } else {

            // authentication failed -- clean out state
            _succeeded = false;
            _username = null;
            _password = null;

	    String msg = _messages.format( "propFileLoginModule.passwordIncorrect", _username );
	    _logger.error( msg );
            
	    throw new FailedLoginException( msg );
        }

	Enumeration enum = _groups.keys();
	while ( enum.hasMoreElements() ) {
	    String groupName = (String)enum.nextElement();
	    String groupMembers = _groups.getProperty( groupName );
	    StringTokenizer tokens = new StringTokenizer( groupMembers, "," );

	    while ( tokens.hasMoreTokens() ) {
		String member = tokens.nextToken();

		if ( member.equals( _username ) ) {
		    _groupPrincipals.add( new PropertiesFilePrincipalGroup( groupName ) );
		}
	    }

	}

	return true;
    }
    
    public boolean logout() throws javax.security.auth.login.LoginException {
        _subject.getPrincipals().remove( _userPrincipal );

        _succeeded = false;
        _succeeded = _commitSucceeded;
        _username = null;
        _password = null;
        _userPrincipal = null;
	_groupPrincipals.clear();

        return true;
    }
    
}
