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
 * $Id: OpenEJBErrorHandler.java 444624 2004-03-01 07:17:26Z dblevins $
 */
package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.apache.openejb.OpenEJBException;


public class OpenEJBErrorHandler {

    private static Logger _logger = Logger.getInstance( "OpenEJB", "org.openejb" );
    private static Messages _messages = new Messages( "org.openejb" );

    /**
     * This method is only intended for situations where an unknown error
     * or exception may occur and have fatal results.
     *
     * Example use:
     * <pre>
     * public ContainerSystem build() throws AssemblerException{
     *     try{
     *         return (org.openejb.ContainerSystem)assembleContainerSystem(config);
     *     }catch(AssemblerException ae){
     *         // AssemblerExceptions contain useful information and are debbugable.
     *         // Let the exception pass through to the top and be logged.
     *          throw ae;
     *     }catch(Exception e){
     *         // General Exceptions at this level are too generic and difficult to debug.
     *         // These exceptions are considered unknown bugs and are fatal.
     *         OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
     *     }
     * }
     * </pre>
     *
     * Creates and logs an OpenEJBException with the following message:
     *
     * "FATAL ERROR: Unknown error in {0}.  Please send the following stack trace and this message to openejb-bugs@exolab.org :\n {1}"}
     *      {0} is the part of the system that the error occurred.
     *
     * @param error                 the unknown Throwable that occurred.
     * @param systemLocation        replaces {0} in the error message.
     */
    public static void handleUnknownError(Throwable error, String systemLocation){

        // Collect the stack trace {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        error.printStackTrace(pw);
        pw.flush();
        pw.close();
        // }

        _logger.i18n.error("ge0001", systemLocation, new String(baos.toByteArray()));

        /*
         * An error broadcasting system is under development.
         * At this point an appropriate error would be broadcast to all listeners.
         */
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "The required properties object needed by {0} is null ."
     *      {1} is the part of the system that needs the properties object.
     *
     * @param systemLocation        replaces {0} in the error message.
     */
    public static void propertiesObjectIsNull(String systemLocation) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0002", systemLocation ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "Properties file '{0}' for {1} not found."
     *      {0} is the properties file name
     *      {1} is the part of the system that needs the properties file.
     *
     * @param propertyfileName      replaces {0} in the error message.
     * @param systemLocation        replaces {1} in the error message.
     */
    public static void propertyFileNotFound(String propertyfileName, String systemLocation) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0003", propertyfileName, systemLocation ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "Environment entry '{0}' not found in {1}."
     *      {0} is the property name
     *      {1} is the properties file name.
     *
     * @param propertyName          replaces {0} in the error message.
     * @param propertyfileName      replaces {1} in the error message.
     */
    public static void propertyNotFound(String propertyName, String propertyfileName) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0004", propertyName, propertyfileName ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "Environment entry '{0}' contains illegal value {1}."
     *      {0} is the property name
     *      {1} is the illegal value.
     *
     * @param propertyName          replaces {0} in the error message.
     * @param value                 replaces {1} in the error message.
     */
    public static void propertyValueIsIllegal(String propertyName, String value) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0005", propertyName, value ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "Environment entry '{0}' contains illegal value {1}. {2}"
     *      {0} is the property name
     *      {1} is the illegal value.
     *      {2} an additional message.
     *
     * @param propertyName          replaces {0} in the error message.
     * @param value                 replaces {1} in the error message.
     * @param message               replaces {2} in the error message.
     */
    public static void propertyValueIsIllegal(String propertyName, String value, String message) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0006", propertyName, value, message ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "The {0} cannot find and load the class '{1}'."
     *      {0} part of the system that needs the class
     *      {1} class that cannot be found.
     *
     * @param systemLocation        replaces {0} in the error message.
     * @param className             replaces {1} in the error message.
     */
    public static void classNotFound(String systemLocation, String className) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0007", systemLocation, className ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "The {0} cannot instaniate the class '{1}', the class or initializer is not accessible."
     *      {0} part of the system that needs the class
     *      {1} class that cannot be accessed.
     *
     * @param systemLocation        replaces {0} in the error message.
     * @param className             replaces {1} in the error message.
     */
    public static void classNotAccessible(String systemLocation, String className) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0008", systemLocation, className ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "The {0} cannot instaniate the class '{1}', the class may be abstract or an interface."
     *      {0} part of the system that needs the class
     *      {1} class that cannot be accessed.
     *
     * @param systemLocation        replaces {0} in the error message.
     * @param className             replaces {1} in the error message.
     */
    public static void classNotIntantiateable(String systemLocation, String className) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0009", systemLocation, className ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "The {0} cannot instaniate the class {1}:  Recieved exception {2}: {3}"
     *      {0} part of the system that needs the class
     *      {1} class that cannot be accessed.
     *      {2} name of caught exception
     *      {3} message from caught exception
     *
     * @param systemLocation        replaces {0} in the error message.
     * @param className             replaces {1} in the error message.
     * @param exceptionClassName    replaces {2} in the error message.
     * @param message               replaces {3} in the error message.
     */
    public static void classNotIntantiateableForUnknownReason(String systemLocation, String className, String exceptionClassName, String message) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0011", systemLocation, className, exceptionClassName, message ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "The {0} cannot instaniate the class {1} loaded from codebase {2}:  Recieved exception {3}: {4}"
     *      {0} part of the system that needs the class
     *      {1} class that cannot be accessed.
     *      {2} codebase the class was loaded from
     *      {3} name of caught exception
     *      {4} message from caught exception
     *
     * @param systemLocation        replaces {0} in the error message.
     * @param className             replaces {1} in the error message.
     * @param codeBase              replaces {2} in the error message.
     * @param exceptionClassName    replaces {3} in the error message.
     * @param message               replaces {4} in the error message.
     */
    public static void classNotIntantiateableFromCodebaseForUnknownReason(String systemLocation, String className, String codebase, String exceptionClassName, String message)
	throws OpenEJBException
    {
        throw new OpenEJBException( _messages.format( "ge0012", systemLocation, className, codebase, exceptionClassName, message ) );
    }

    /**
     * The {0} cannot locate the class {1}, the codebase '{2}' cannot
     * be accessed. Received message: {3}"
     *
     * @param systemLocation
     *                  replaces {0} in the error message.
     * @param className replaces {1} in the error message.
     * @param codebase  replaces {2} in the error message.
     *
     * @param e         e.getMessage() replaces {3} in the error message.
     *
     * @exception OpenEJBException
     */
    public static void classCodebaseNotFound(String systemLocation, String className, String codebase, Exception e) throws OpenEJBException{

        throw new OpenEJBException( _messages.format( "ge0010", systemLocation, className, codebase, e.getMessage() ) );
    }

    /**
     * Creates and throws an OpenEJBException with the following message:
     *
     * "Error in XML configuration file.  Received {0} from the parser
     *  stating '{1}' at line {2} column {3}."},
     *      {0} the type of message.
     *      {1} the error message from the parser.
     *      {2} the line number.
     *      {3} the column number.
     *
     * @param messageType           replaces {0} in the error message.
     * @param message               replaces {1} in the error message.
     * @param line                  replaces {2} in the error message.
     * @param column                replaces {3} in the error message.
     */
    public static void configurationParsingError(String messageType, String message, String line, String column){

        _logger.i18n.error( "as0001", messageType, message, line, column );
        /*
         * An error broadcasting system is under development.
         * At this point an appropriate error would be broadcast to all listeners.
         */
    }

}
