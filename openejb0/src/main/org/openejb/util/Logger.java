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

package org.openejb.util;

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
public class Logger extends org.openejb.util.LoggerBase {

    /**
     * Returns a shared instance of Logger.
     * 
     * @param name   the name of the log4j category to use
     * 
     * @return Instance of logger.
     */
    static public Logger getInstance( String name ) {
	return (Logger)org.openejb.util.LoggerBase.getInstanceProtected( Logger.class, name );
    }

    /**
     * Constructor.  Users must invoke getInstance() to
     * an instance of Logger.
     * 
     * @see getInstance()
     */
    public Logger() {
	super();
    }
        
    protected org.openejb.util.MessagesBase createMessagesBase() {
	return new Messages();
    }
}
