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

package org.apache.openejb;

/**
 * --------------
 * EJB 2.0
 * 
 * 18.1.1 Application exceptions
 * 
 * An application exception is an exception defined in the throws clause of a
 * method of the enterprise bean’s home and component interfaces, other than
 * the java.rmi.RemoteException.
 * 
 * Enterprise bean business methods use application exceptions to inform the
 * client of abnormal application-level conditions, such as unacceptable values
 * of the input arguments to a business method. A client can typically recover
 * from an application exception.
 * 
 * Application exceptions are not intended for reporting system-level problems.
 * 
 * ---------------
 * 
 * 
 * This exception is thrown when a normal EnterpriseBean exception is thrown.  
 * 
 * The ApplicationException's nested exception will be either an EJB spec 
 * defined ApplicationException ( or a custom exception defined by the bean
 * developer) or a RemoteException.
 * 
 * The org.openejb.ApplicationException must be caught and its nested
 * exception rethrown by the bean proxy to the client.
 * 
 * The org.openejb.ApplicationException is non-system exception; it does NOT
 * indicate a problem with the contaienr itself.
 * 
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class ApplicationException extends OpenEJBException {

    /**
     * Constructs an empty ApplicationException instance.
     */
    public ApplicationException( ){super();}
    
    /**
     * Constructs a ApplicationException with the specified message indicating 
     * the source of the problem that occurred.
     * 
     * @param message <code>String</code> identifying the source of the problem.
     */
    public ApplicationException(String message){
        super(message);
    }

    /**
     * Constructs a ApplicationException with the source of the problem that occurred.
     * 
     * @param e
     */
    public ApplicationException(Exception e){
        super(e);
    }

    public ApplicationException(Throwable t){
        super(t);
    }
    
    /**
     * Constructs a ApplicationException with the specified message indicating
     * the source of the problem that occurred and the original "root cause" exception
     * that was thrown when the problem occurred.
     * 
     * @param message <code>String</code> identifying the source of the problem.
     * @param e
     */
    public ApplicationException(String message, Exception e){
	super(message,e);
    }
}