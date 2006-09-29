/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb;


/**
 * The OpenEJBException is the standard exception thrown by all methods in all type in the
 * Container Provider Interface (CPI).  The OpenEJBException has 3 subtypes each serving a different
 * purpose.  The CPI will always thrown one of these subtype and never the OpenEJBException itself.
 * <ul>
 * <li><b>org.apache.openejb.ApplicationException</b><br>
 *     This type is thrown when a normal EnterpriseBean exception is thrown.  The ApplicationException's nested
 *     Exception will be either an EJB ApplicationException ( a custom exception defined by the bean developer)
 *     or a RemoteException.  The org.apache.openejb.ApplicationException must be caught and its nested exception rethrown
 *     by the bean proxy to the client.  The org.apache.openejb.ApplicationException is non-system exception; it does NOT
 *     indicate a problem with the contaienr itself.
 * <li><b>org.apache.openejb.InvalidateReferenceException</b><br>
 *     This type is thrown when the EnterpriseBean throws a RuntimeException or system exception that results in the
 *     eviction of the bean instance.  The InvalidateReferenceException's nested exception will be a RuntimeException,
 *     which must be converted to a RemoteException or the nested exception will be a RemoteException.  The
 *     Application Server must catch the InvalidateReferenceException and its nested exception rethrown by the bean proxy
 *     (if the nested exception is a RuntimeException it must first be converted to a RemoteException).
 *     After the exception is re-thrown by the bean proxy, the bean proxy must be invalidated so that all subsequent invocations by
 *     the client on that bean proxy throw a RemoteException. The proxy is made invalid. InvalidateReferenceException is non-system
 *     exception; it does NOT indicate a problem with the container itself.
 * <li><b>org.apache.openejb.SystemException</b><br>
 *     This type is thrown when the container has encountered an unresolvable system exception that make this Container
 *     unable to process requests.  A breakdown in communication with one of the primary services or a RuntimeException
 *     thrown within the container (not by a bean) is are good examples.  The org.apache.openejb.SystemException represents a
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
