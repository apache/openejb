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
 * The OpenEJBException is the standard exception thrown by all methods in all type in the
 * Container Provider Interface (CPI).  The OpenEJBException has 3 subtypes each serving a different
 * purpose.  The CPI will always thrown one of these subtype and never the OpenEJBException itself.
 * <ul>
 * <li><b>org.openejb.ApplicationException</b><br>
 *     This type is thrown when a normal EnterpriseBean exception is thrown.  The ApplicationException's nested
 *     Exception will be either an EJB ApplicationException ( a custom exception defined by the bean developer)
 *     or a RemoteException.  The org.openejb.ApplicationException must be caught and its nested exception rethrown
 *     by the bean proxy to the client.  The org.openejb.ApplicationException is non-system exception; it does NOT
 *     indicate a problem with the contaienr itself.
 * <li><b>org.openejb.InvalidateReferenceException</b><br>
 *     This type is thrown when the EnterpriseBean throws a RuntimeException or system exception that results in the
 *     eviction of the bean instance.  The InvalidateReferenceException's nested exception will be a RuntimeException,
 *     which must be converted to a RemoteException or the nested exception will be a RemoteException.  The
 *     Application Server must catch the InvalidateReferenceException and its nested exception rethrown by the bean proxy
 *     (if the nested exception is a RuntimeException it must first be converted to a RemoteException).
 *     After the exception is re-thrown by the bean proxy, the bean proxy must be invalidated so that all subsequent invocations by
 *     the client on that bean proxy throw a RemoteException. The proxy is made invalid. InvalidateReferenceException is non-system
 *     exception; it does NOT indicate a problem with the container itself.
 * <li><b>org.openejb.SystemException</b><br>
 *     This type is thrown when the container has encountered an unresolvable system exception that make this Container
 *     unable to process requests.  A breakdown in communication with one of the primary services or a RuntimeException
 *     thrown within the container (not by a bean) is are good examples.  The org.openejb.SystemException represents a
 *     serious problem with the Container.  The Container should be shut down and not used for any more processing.
 * </ul>
 *
 * @version $Revision$ $Date$
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class OpenEJBException extends Exception {

    /** Error code for unknown errors */
//    private String message = "error.unknown";

    /** Stored <code>Exception</code> for root cause */
//    private Throwable rootCause;

    /**
     * <p>
     *  Default constructor, which simply delegates exception
     *    handling up the inheritance chain to <code>Exception</code>.
     * </p>
     */
    public OpenEJBException() {
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
    public OpenEJBException(String message) {
        super(message);
//        this.message = message;
    }

    /**
     * <p>
     *  This constructor allows a "root cause" exception to be supplied,
     *    which may later be used by the wrapping application.
     * </p>
     *
     * @param rootCause <code>Throwable</code> that triggered the problem.
     */
    public OpenEJBException(Throwable rootCause) {
        super(rootCause);
//        this.rootCause = rootCause;
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
    public OpenEJBException(String message, Throwable rootCause) {
        super(message, rootCause);
//        this.rootCause = rootCause;
    }

    /**
     * <p>
     *  This returns the message for the <code>Exception</code>. If there is
     *    a root cause, the message associated with the root cause
     *    is appended.
     * </p>
     *
     * @return <code>String</code> - message for this <code>Exception</code>.
     */
//    public String getMessage() {
//        if (rootCause != null) {
//            return super.getMessage() + ": " + rootCause.getMessage();
//        } else {
//            return super.getMessage();
//        }
//    }

    /**
     * <p>
     *  This prints the stack trace of the <code>Exception</code>. If there is
     *    a root cause, the stack trace of the root <code>Exception</code>
     *    is printed right after.
     * </p>
     */
//    public void printStackTrace() {
//        super.printStackTrace();
//        if (rootCause != null) {
//            System.err.println("Root cause: ");
//            rootCause.printStackTrace();
//        }
//    }

    /**
     * <p>
     *  This prints the stack trace of the <code>Exception</code>. If there is
     *    a root cause, the stack trace of the root <code>Exception</code>
     *    is printed right after.
     * </p>
     *
     * @param stream <code>PrintStream</code> to print stack trace to.
     */
//    public void printStackTrace(PrintStream stream) {
//        super.printStackTrace(stream);
//        if (rootCause != null) {
//            stream.print("Root cause: ");
//            rootCause.printStackTrace(stream);
//        }
//    }

    /**
     * <p>
     *  This prints the stack trace of the <code>Exception</code>. If there is
     *    a root cause, the stack trace of the root <code>Exception</code>
     *    is printed right after.
     * </p>
     *
     * @param writer <code>PrintWriter</code> to print stack trace to.
     */
//    public void printStackTrace(PrintWriter writer) {
//        super.printStackTrace(writer);
//        if (rootCause != null) {
//            writer.print("Root cause: ");
//            rootCause.printStackTrace(writer);
//        }
//    }

    /**
     * <p>
     *  This will return the root cause <code>Throwable</code>, or
     *    <code>null</code> if one does not exist.
     * </p>
     *
     * @return <code>Throwable</code> - the wrapped <code>Throwable</code>.
     */
//    public Throwable getRootCause() {
//        super.getCause();
//        return rootCause;
//    }

}
