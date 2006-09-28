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
 * This type is thrown when the EnterpriseBean throws a RuntimeException or 
 * system exception that results in the eviction of the bean instance.  The 
 * InvalidateReferenceException's nested exception will be a RemoteException
 * or possibly an ObjectNotFoundException.
 * 
 * The Application Server must catch the InvalidateReferenceException and its 
 * nested exception rethrown by the bean proxy. After the exception is 
 * re-thrown by the bean proxy, the bean proxy must be invalidated so that all
 * subsequent invocations by the client on that bean proxy throw a 
 * RemoteException. The proxy is made invalid. InvalidateReferenceException is
 * non-system exception; it does NOT indicate a problem with the container 
 * itself.
 * 
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */

public class InvalidateReferenceException extends ApplicationException{
    
    /**
     * Constructs an empty InvalidateReferenceException instance.
     */
    public InvalidateReferenceException( ){super();}
    
    /**
     * Constructs a InvalidateReferenceException with the specified message indicating 
     * the source of the problem that occurred.
     * 
     * @param message <code>String</code> identifying the source of the problem.
     */
    public InvalidateReferenceException(String message){
	    super(message);
    }
    
    /**
     * Constructs a InvalidateReferenceException with the source of the problem that occurred.
     * 
     * @param e
     */
    public InvalidateReferenceException(Exception e){
        super(e);
    }
    
    /**
     * Constructs a InvalidateReferenceException with the source of the problem that occurred.
     * 
     * @param e
     */
    public InvalidateReferenceException(Throwable t){
        super(t);
    }

    /**
     * Constructs a InvalidateReferenceException with the specified message indicating
     * the source of the problem that occurred and the original "root cause" exception
     * that was thrown when the problem occurred.
     * 
     * @param message <code>String</code> identifying the source of the problem.
     * @param e
     */
    public InvalidateReferenceException(String message, Exception e){
	    super(message,e);
    }
    
    
}
