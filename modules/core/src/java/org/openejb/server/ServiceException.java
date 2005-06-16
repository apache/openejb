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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.server;

import org.openejb.OpenEJBException;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ServiceException extends OpenEJBException {

    /** 
     * <p>
     *  Default constructor, which simply delegates exception
     *    handling up the inheritance chain to <code>Exception</code>.
     * </p>
     */
    public ServiceException() {
        super();
    }

    /** 
     * <p>
     *  This constructor allows a message to be supplied indicating the source
     *    of the problem that occurred.
     * </p>
     *
     * @param message <code>String</code> identifying the cause of the problem.
     */
    public ServiceException(String message) {
        super( message );
    }

    /**
     * <p>
     *  This constructor allows a "root cause" exception to be supplied,
     *    which may later be used by the wrapping application.
     * </p>
     *
     * @param rootCause <code>Throwable</code> that triggered the problem.
     */
    public ServiceException(Throwable rootCause) {
        super( rootCause );
    }

    /**
     * This constructor allows both a message identifying the
     * problem that occurred as well as a "root cause" exception
     * to be supplied, which may later be used by the wrapping
     * application.
     * 
     * @param message   <code>String</code> identifying the cause of the problem.
     * @param rootCause <code>Throwable</code> that triggered this problem.
     */
    public ServiceException(String message, Throwable rootCause) {
        super( message, rootCause );
    }

}

