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
