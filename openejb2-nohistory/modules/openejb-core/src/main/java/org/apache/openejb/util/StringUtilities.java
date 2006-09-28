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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: StringUtilities.java 444624 2004-03-01 07:17:26Z dblevins $
 */
package org.apache.openejb.util;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

/**
 * A simple string utilities class.
 * 
 */
public class StringUtilities {
	/** the CRLF for use in String manipulation */
	public static final String CRLF = "\r\n";

	//we don't want anyone creating new instances
	private StringUtilities() {}

	/**
	 * Gets the last token in a StringTokenizer.
	 * @param tokenString - the string to get the last token from
	 * @param delimeter - the delimeter of the string
	 * @return the last token or null if there are none
	 */
	public static String getLastToken(String tokenString, String delimeter) {
		StringTokenizer token = new StringTokenizer(tokenString, delimeter);

		String returnValue = null;
		while (token.hasMoreTokens()) {
			returnValue = token.nextToken();
		}

		return returnValue;
	}

	/**
	 * Checks a String to see if it's value is null, 
	 * and if so returns a blank string.
	 * @param stringToCheckForNull - the string to check for null
	 * @return the checked string
	 */
	public static String nullToBlankString(String stringToCheckForNull) {
		return (stringToCheckForNull == null) ? "" : stringToCheckForNull;
	}
	
	/**
	 * Checks a String to see if it's value is null or blank
	 * @param stringToCheck - the string to check for blank or null
	 * @return whether blank or null
	 */
	public static boolean checkNullBlankString(String stringToCheck) {
		return (stringToCheck == null || "".equals(stringToCheck.trim()));
	}
	
	/**
	 * Checks a String to see if it's blank, 
	 * and if so returns null (the opposite of <code>nullToBlankString</code>.
	 * @param stringToCheckForNull - the string to check for blank
	 * @return the checked string or null
	 */
	public static String blankToNullString(String stringToCheckForBlank) {
		if(stringToCheckForBlank != null) stringToCheckForBlank.trim();
		return ("".equals(stringToCheckForBlank)) ? null : stringToCheckForBlank;
	}

	/**
	 * Checks a String to see if it's value is null or blank, 
	 * and if so returns a non-breaking space.
	 * @param stringToCheckForNull - the string to check for null or blank 
	 * @return the checked string
	 */
	public static String replaceNullOrBlankStringWithNonBreakingSpace(String stringToCheckForNull) {
		if ((stringToCheckForNull == null) || (stringToCheckForNull.equals(""))) {
			return "&nbsp;";
		} else {
			return stringToCheckForNull;
		}
	}

	/**
	 * Creates a string representation of a reflection method for example
	 * <br>
	 * <code>
	 * myMethod(String, String) throws Exception
	 * </code> 
	 * <br>
	 * @param method - the reflection method
	 * @param lineBreak - the type of line break usually \n or &lt;br&gt;
	 * @return the string representation of the method
	 */
	public static String createMethodString(Method method, String lineBreak) {
		Class[] parameterList = method.getParameterTypes();
		Class[] exceptionList = method.getExceptionTypes();
		StringBuffer methodString = new StringBuffer();

		methodString.append(method.getName()).append("(");

		for (int j = 0; j < parameterList.length; j++) {
			methodString.append(StringUtilities.getLastToken(parameterList[j].getName(), "."));

			if (j != (parameterList.length - 1)) {
				methodString.append(", ");
			}
		}
		methodString.append(") ");

		if (exceptionList.length > 0) {
			methodString.append(lineBreak);
			methodString.append("throws ");
		}

		for (int j = 0; j < exceptionList.length; j++) {
			methodString.append(StringUtilities.getLastToken(exceptionList[j].getName(), "."));

			if (j != (exceptionList.length - 1)) {
				methodString.append(", ");
			}
		}

		return methodString.toString();
	}

	/**
	 * Changes a string array into a comma delimted list
	 * @param stringArray - The string array to be converted
	 * @return the comma delimted list
	 */
	public static String stringArrayToCommaDelimitedStringList(String[] stringArray) {
		StringBuffer stringList = new StringBuffer();
		for (int i = 0; i < stringArray.length; i++) {
			stringList.append(stringArray[i]);
			if (i != (stringArray.length - 1)) {
				stringList.append(",");
			}
		}

		return stringList.toString();
	}

}
