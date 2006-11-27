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
 * The org.apache.openejb.ApplicationException must be caught and its nested
 * exception rethrown by the bean proxy to the client.
 * 
 * The org.apache.openejb.ApplicationException is non-system exception; it does NOT
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