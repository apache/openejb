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


package org.openejb.util;


import java.text.*;
import java.util.*;

/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision$ $Date$
 */
public class MessagesBase
{
    static private Hashtable _rbBundles = new Hashtable();
    static private Hashtable _rbFormats = new Hashtable();
    static private Locale    _globalLocale;

    private ResourceBundle   _messages;
    private Hashtable        _formats;
    private Locale           _locale;
    private String           _resourceName = getResourceName();
    

    protected MessagesBase() {
	synchronized (MessagesBase.class) {
	    _locale = _globalLocale;
	    
	    ResourceBundle rb = (ResourceBundle)_rbBundles.get( _resourceName );
	    if ( rb == null ) {
		init();
	    } else {
		_messages = rb;
		_formats = (Hashtable)_rbFormats.get( _resourceName );
	    }
	}

    }

    protected void init() {
	try {
	    if ( _locale == null )
		_messages = ResourceBundle.getBundle( _resourceName );
	    else
		_messages = ResourceBundle.getBundle( _resourceName, _locale );
	} catch ( Exception except ) {
	    _messages = new EmptyResourceBundle();
	}

	_formats = new Hashtable();

	_rbBundles.put( _resourceName, _messages );
	_rbFormats.put( _resourceName, _formats );
    }

    protected String getResourceName() {
	String className = getClass().getName();

	int idxDot = className.lastIndexOf('.');
	
	return className.substring(0, idxDot) + ".resources" + className.substring(idxDot);
    }

    public String format( String message, Object arg1 )
    {
        return format( message, new Object[] { arg1 } );
    }


    public String format( String message, Object arg1, Object arg2 )
    {
        return format( message, new Object[] { arg1, arg2 } );
    }


    public String format( String message, Object arg1, Object arg2, Object arg3 )
    {
        return format( message, new Object[] { arg1, arg2, arg3 } );
    }


    public String format( String message, Object[] args )
    {
	if ( _locale != _globalLocale ) {
	    synchronized (MessagesBase.class) {
		init();
	    }
	}

        MessageFormat   mf;
        String          msg;

        try {
            mf = (MessageFormat) _formats.get( message );
            if ( mf == null ) {
                try {
                    msg = _messages.getString( message );
                } catch ( MissingResourceException except ) {
                    return message;
                }
                mf = new MessageFormat( msg );
                _formats.put( message, mf );
            }
            return mf.format( args );
        } catch ( Exception except ) {
            return "An internal error occured while processing message " + message;
        }
    }


    public String message( String message )
    {
	if ( _locale != _globalLocale ) {
	    synchronized (MessagesBase.class) {
		init();
	    }
	}
        
	try {
            return _messages.getString( message );
        } catch ( MissingResourceException except ) {
            return message;
        }
    }


    static public void setLocale( Locale locale )
    {
	synchronized (MessagesBase.class) {
	    _globalLocale = locale;
	    _rbBundles    = new Hashtable();
	    _rbFormats    = new Hashtable();
	}
    }


    static
    {
        setLocale( Locale.getDefault() );
    }


    private static final class EmptyResourceBundle
        extends ResourceBundle
        implements Enumeration
    {

        public Enumeration getKeys()
        {
            return this;
        }

        protected Object handleGetObject( String name )
        {
            return "[Missing message " + name + "]";
        }

        public boolean hasMoreElements()
        {
            return false;
        }

        public Object nextElement()
        {
            return null;
        }

    }

}
