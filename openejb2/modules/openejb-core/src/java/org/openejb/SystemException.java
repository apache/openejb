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
package org.openejb;

/**
 * This exception is thrown when the container has encountered an unresolvable
 * system exception that make this Container unable to process requests.
 * A breakdown in communication with one of the primary services or a
 * RuntimeException thrown within the container (not by a bean) is are good
 * examples.
 * 
 * The org.openejb.SystemException represents a serious problem with the
 * Container.  The Container should be shut down and not used for any more
 * processing.
 * 
 * 
 * NOTE: This exception bears no resemblence to the unchecked exceptions and
 * errors that an enterprise bean instance may throw during the
 * execution of a session or entity bean business method, a message-driven bean
 * onMessage method, or a container callback method (e.g. ejbLoad).  
 * See InvalidateReferenceException for this.
 * 
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class SystemException extends OpenEJBException {

    /**
     * Constructs an empty SystemException instance.
     */
    public SystemException() {
        super();
    }

    /** 
     * Constructs a SystemException with the specified message indicating the source of the problem that occurred.
     *
     * @param message <code>String</code> identifying the source of the problem.
     */
    public SystemException(String message){
	    super(message);
    }

    /** 
     * Constructs a SystemException with the source of the problem that occurred.
     *
     * @param rootCause <code>Throwable</code> root cause of problem.
     */
    public SystemException(Throwable rootCause){
        super(rootCause);
    }

    /**
     * Constructs a SystemException with the specified message indicating 
     * the source of the problem that occurred and the original "root cause" exception
     * that was thrown when the problem occurred.
     * 
     * @param message   <code>String</code> identifying the source of the problem.
     * @param rootCause <code>Throwable</code> root cause of problem.
     */
    public SystemException(String message, Throwable rootCause){
	    super(message, rootCause);
    }

}
