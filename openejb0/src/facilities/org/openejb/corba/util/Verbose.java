/**
 * Redistribution and use of this software and associated
 * documentation ("Software"), with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements
 *    and notices.  Redistributions must also contain a copy of this
 *    document.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Inc.  For written permission, please
 *    contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Inc. Exolab is a registered trademark of
 *    Intalio Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Copyright 1999-2001 (c) Intalio,Inc. All Rights Reserved.
 *
 * $Id$
 *
 * Date         Author  Changes
 */

package org.openejb.corba.util;

/**
 * @author	Jerome Daniel ( jdaniel@intalio.com )
 */
public class Verbose
{
	private static boolean verbose = false;
	
	private static java.io.PrintStream out = System.out;

	/**
	 * Enable trace
	 */
	public static void enable( String file_name )
	{
		if ( file_name != null )
		{
		}
		
		verbose = true;
	}
	
	/**
	 * Disable trace
	 */
	public static void disable()
	{
		verbose = false;
	}
	
	/**
	 * Display a log message
	 */
	public static void print( String className, String message )
	{
		if ( verbose )
			out.println( className + " : " + message );
	}

	/**
	 * Display a log message for an exception
	 */
	public static void exception( String className, String message, java.lang.Throwable ex )
	{
		if ( verbose )
		{
			out.println("==> Exception in " + className + " : " + message );
			
			ex.printStackTrace( out );
			
			out.println("<==");
		}
	}
	
	/**
	 * Assert mode
	 */
	public static void fatal( String className, String message )
	{
		out.println("***************************************************************************************");
		out.println("Fatal error in " + className + " : " );
		out.println("\t" + message);
		out.println();
		
		try 
		{
			java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
			java.io.PrintWriter pw = new java.io.PrintWriter(bos);
			(new Error()).printStackTrace(pw);
			pw.close();
			
			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(bos.toString()));
			System.out.println(reader.readLine());
			reader.readLine();

			out.println("Stack trace:");
			String line = reader.readLine();
			while(line != null) {
				out.println(line);
				line = reader.readLine();
			}
			out.println();
		}
		catch(java.io.IOException ex) {
			System.out.println(ex);
		}
	        // The following code was removed on behalf of bugzilla 988	
                // At this point, the error must be printed but the application must not
                // terminate. Only the request was unable to complete. 
                // System.exit(1);
	
		out.println("***************************************************************************************");
		// out.println();
		// out.println("Please send this message by mail to bugreport@openorg.org");
		// out.println();
	}
}
