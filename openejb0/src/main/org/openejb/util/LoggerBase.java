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
 * Copyright 2002 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.util;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import java.lang.Throwable;
import java.util.HashMap;


/**
 * This is a wrapper class to the log4j facility.  In addition to the
 * internationalization of messages, it sets a default log4j configuration,
 * if one is not already set in the system properties.
 * <p>
 * If the log4j system complains that there is no configuration set, then it's
 * probably one of two things.  First, the config file does not exist.  Second,
 * and more likely, the OpenEJB URL handler has not been registered.  (Note
 * that the log4j.configuration default setting uses the protocol resource.)
 * <p>
 * @author <a href="mailto:adc@toolazydogs.com">Alan Cabrera</a>
 * @version $Revision$ $Date$
 */
public abstract class LoggerBase {

    protected Category _logger;

    static {
	// Set log4j's configuration (Note the URL's form)
	// It assumes that the OpenEJB URL handler has already been registered
	if( System.getProperty( "log4j.configuration" ) == null ) {
	    System.setProperty( "log4j.configuration", "resource:/default.logging.conf" );
	}
    }

    static protected HashMap _loggers  = new HashMap();
    protected MessagesBase   _messages = null;;

    /**
     * Returns a shared instance of Logger.
     * 
     * @param name   the name log4j category to use
     * 
     * @return Instance of logger.
     */
    static public Logger getInstance( String name ) {
	Logger l = (Logger)_loggers.get( name );

	if ( l == null ) {
	    synchronized (Logger.class) {
		l = (Logger)_loggers.get( name );
		if ( l == null ) {
		    l = new Logger( name );
		    _loggers.put( name, l );
		}
	    }
	}
	return l;
    }

    /**
     * Protected constructor.  Users must invoke getInstance() to
     * an instance of Logger.
     * 
     * @param name   the name of the log4j category to use
     * 
     * @see getInstance()
     */
    protected LoggerBase( String name ) {
	_logger = Category.getInstance( name );
	_messages = createMessagesBase();
    }
    
    protected abstract MessagesBase createMessagesBase();

    /**
     * Wrapper function for log4j's isDebugEnabled() method.
     * 
     * @return if debug is enabled.
     */
    public boolean isDebugEnabled() {
	return _logger.isDebugEnabled();
    }

    /**
     * Check to see if error messages are enabled.
     * 
     * @return if error messages are enabled.
     */
    public boolean isErrorEnabled() {
	return _logger.isEnabledFor( Level.ERROR );
    }

    /**
     * Check to see if fatal messages are enabled.
     * 
     * @return if fatal messages are enabled.
     */
    public boolean isFatalEnabled() {
	return _logger.isEnabledFor( Level.FATAL );
    }

    /**
     * Wrapper function for log4j's isInfoEnabled() method.
     * 
     * @return if info messages are enabled.
     */
    public boolean isInfoEnabled() {
	return _logger.isInfoEnabled();
    }
    

    /**
     * Check to see if warning messages are enabled.
     * 
     * @return if warning messages are enabled.
     */
    public boolean isWarningEnabled() {
	return _logger.isEnabledFor( Level.WARN );
    }
    
    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized.
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code ) {
	if ( isInfoEnabled() ) _logger.info( _messages.message( code ) );
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized.
     * @param t      the exception to log, including its stack trace
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t ) {
	if ( isInfoEnabled() ) _logger.info( _messages.message( code ), t );
    }


    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object arg0 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0 };
	    info( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t, Object arg0 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0 };
	    info( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object arg0, Object arg1 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    info( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t, Object arg0, Object arg1 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    info( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object arg0, Object arg1, Object arg2 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    info( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    info( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    info( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    info( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    info( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    info( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    info( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	if ( isInfoEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    info( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Object[] args ) {
	_logger.info( _messages.format( code, args ) );
    }

    /**
     * An internationalized wrapper call to log4j's info method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void info( String code, Throwable t,  Object[] args ) {
	    _logger.info( _messages.format( code, args ), t );
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized.
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code ) {
	if ( isWarningEnabled() ) _logger.warn( _messages.message( code ) );
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized.
     * @param t      the exception to log, including its stack trace
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t ) {
	if ( isWarningEnabled() ) _logger.warn( _messages.message( code ), t );
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object arg0 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0 };
	    warning( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object arg0 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0 };
	    warning( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object arg0, Object arg1 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    warning( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object arg0, Object arg1 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    warning( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object arg0, Object arg1, Object arg2 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    warning( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    warning( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    warning( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    warning( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	if ( isWarningEnabled() ) {    
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    warning( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    warning( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    warning( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	if ( isWarningEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    warning( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Object[] args ) {
	_logger.warn( _messages.format( code, args ) );
    }

    /**
     * An internationalized wrapper call to log4j's warning method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void warning( String code, Throwable t, Object[] args ) {
	_logger.warn( _messages.format( code, args ), t );
    }


    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized.
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code ) {
	if ( isErrorEnabled() ) _logger.error( _messages.message( code ) );
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized.
     * @param t      the exception to log, including its stack trace
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t ) {
	if ( isErrorEnabled() ) _logger.error( _messages.message( code ), t );
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object arg0 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0 };
	    error( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object arg0 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0 };
	    error( code, t, args ); 
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object arg0, Object arg1 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    error( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object arg0, Object arg1 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    error( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object arg0, Object arg1, Object arg2 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    error( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    error( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    error( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    error( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    error( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    error( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    error( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	if ( isErrorEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    error( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Object[] args ) {
	_logger.error( _messages.format( code, args ) );
    }

    /**
     * An internationalized wrapper call to log4j's error method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void error( String code, Throwable t, Object[] args ) {
	_logger.error( _messages.format( code, args ), t );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized.
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code ) {
	_logger.fatal( _messages.message( code ) );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized.
     * @param t      the exception to log, including its stack trace
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t ) {
	_logger.fatal( _messages.message( code ), t );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object arg0 ) {
	Object[] args = { arg0 };
	fatal( code, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object arg0 ) {
	Object[] args = { arg0 };
	fatal( code, t, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object arg0, Object arg1 ) {
        Object[] args = { arg0, arg1 };
	fatal( code, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object arg0, Object arg1 ) {
        Object[] args = { arg0, arg1 };
	fatal( code, t, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object arg0, Object arg1, Object arg2 ) {
        Object[] args = { arg0, arg1, arg2 };
	fatal( code, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
        Object[] args = { arg0, arg1, arg2 };
	fatal( code, t, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        Object[] args = { arg0, arg1, arg2, arg3 };
	fatal( code, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        Object[] args = { arg0, arg1, arg2, arg3 };
	fatal( code, t, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
        Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	fatal( code, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
        Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	fatal( code, t, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
        Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	fatal( code, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
        Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	fatal( code, t, args );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Object[] args ) {
	_logger.fatal( _messages.format( code, args ) );
    }

    /**
     * An internationalized wrapper call to log4j's fatal method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void fatal( String code, Throwable t, Object[] args ) {
	_logger.fatal( _messages.format( code, args ), t );
    }


    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized.
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code ) {
	if ( isDebugEnabled() ) _logger.debug( _messages.message( code ) );
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized.
     * @param t      the exception to log, including its stack trace
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t ) {
	if ( isDebugEnabled() ) _logger.debug( _messages.message( code ), t );
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object arg0 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0 };
	    debug( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object arg0 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0 };
	    debug( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object arg0, Object arg1 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    debug( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object arg0, Object arg1 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1 };
	    debug( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object arg0, Object arg1, Object arg2 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    debug( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2 };
	    debug( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    debug( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    debug( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    debug( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    debug( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    debug( code, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param arg0   First argument to the i18n message
     * @param arg1   Second argument to the i18n message
     * @param arg2   Third argument to the i18n message
     * @param arg3   Fourth argument to the i18n message
     * @param arg4   Fifth argument to the i18n message
     * @param arg5   Sixth argument to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
        if ( isDebugEnabled() ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    debug( code, t, args );
	}
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Object[] args ) {
	_logger.debug( _messages.format( code, args ) );
    }

    /**
     * An internationalized wrapper call to log4j's debug method
     * 
     * @param code   The code to be internationalized
     * @param t      the exception to log, including its stack trace
     * @param args   An array of arguments to the i18n message
     * 
     * @see org.openejb.util.Messages
     */
    public void debug( String code, Throwable t, Object[] args ) {
	_logger.debug( _messages.format( code, args ), t );
    }
}
