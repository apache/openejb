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
 * $Id$
 */
package org.openejb.webadmin.main;

import java.io.Serializable;

import org.exolab.castor.xml.ValidationException;
import org.openejb.util.StringUtilities;

/**
 * A simple data class for database data
 */
public class DatabaseData implements Serializable {
	private String fileName;
	private String dbEngine;
	private String jndiName;
	private String driverClass;
	private String driverUrl;
	private String username;
	private String password;

	public DatabaseData() {
		this.fileName = "";
		this.dbEngine = "";
		this.jndiName = "";
		this.driverClass = "";
		this.driverUrl = "";
		this.username = "";
		this.password = "";
	}

	public String getDbEngine() {
		return this.dbEngine;
	}

	public String getDriverClass() {
		return this.driverClass;
	}

	public String getDriverUrl() {
		return this.driverUrl;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getJndiName() {
		return this.jndiName;
	}

	public String getPassword() {
		return this.password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setDbEngine(String string) {
		this.dbEngine = string;
	}

	public void setDriverClass(String string) {
		this.driverClass = string;
	}

	public void setDriverUrl(String string) {
		this.driverUrl = string;
	}

	public void setFileName(String string) {
		this.fileName = string;
	}

	public void setJndiName(String string) {
		this.jndiName = string;
	}

	public void setPassword(String string) {
		this.password = string;
	}

	public void setUsername(String string) {
		this.username = string;
	}

	/**
	 * validates that the required fields are filled out
	 */
	public void validate() throws ValidationException {
		StringBuffer errorMsg = new StringBuffer(50);
		if (StringUtilities.checkNullBlankString(this.dbEngine))
			errorMsg.append("DB Engine is required<br>");
		if (StringUtilities.checkNullBlankString(this.driverClass))
			errorMsg.append("Driver Class is required<br>");
		if(StringUtilities.checkNullBlankString(this.driverUrl))
			errorMsg.append("Driver URL is required<br>");
		if(StringUtilities.checkNullBlankString(this.fileName))
			errorMsg.append("File Names is required<br>");
		if(StringUtilities.checkNullBlankString(this.jndiName))
			errorMsg.append("JNDI Name is required<br>");
		
		//if the length of the buffer is greater than zero
		//throw the the exception
		if(errorMsg.length() > 0)
			throw new ValidationException(errorMsg.toString());
	}
}
