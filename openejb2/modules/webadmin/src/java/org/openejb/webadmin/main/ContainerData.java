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

/**
 * A simple data object for container information.
 * 
 */
public class ContainerData implements Serializable {
	private String id;
	private String jar;
	private String provider;
	private String poolSize;
	private String globalTxDatabase;
	private String localTxDatabase;
	private String passivator;
	private String timeOut;
	private String bulkPassivate;
	private String strictPooling;
	private String containerType;
	private int index;
	private boolean edit;
	
	public ContainerData() {
		this.id = "";
		this.jar = "";
		this.provider = "";
		this.poolSize = "";
		this.globalTxDatabase = "";
		this.localTxDatabase = "";
		this.passivator = "";
		this.timeOut = "";
		this.bulkPassivate = "";
		this.strictPooling = "true";
		this.index = -1;
		this.containerType = "";
	}
	
	public String getBulkPassivate() {
		return bulkPassivate;
	}

	public String getContainerType() {
		return containerType;
	}

	public String getGlobalTxDatabase() {
		return globalTxDatabase;
	}

	public String getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	public String getJar() {
		return jar;
	}

	public String getLocalTxDatabase() {
		return localTxDatabase;
	}

	public String getPassivator() {
		return passivator;
	}

	public String getPoolSize() {
		return poolSize;
	}

	public String getProvider() {
		return provider;
	}

	public String isStrictPooling() {
		return strictPooling;
	}

	public String getTimeOut() {
		return timeOut;
	}

	public void setBulkPassivate(String string) {
		bulkPassivate = string;
	}

	public void setContainerType(String string) {
		containerType = string;
	}

	public void setGlobalTxDatabase(String string) {
		globalTxDatabase = string;
	}

	public void setId(String string) {
		id = string;
	}

	public void setIndex(int i) {
		index = i;
	}

	public void setJar(String string) {
		jar = string;
	}

	public void setLocalTxDatabase(String string) {
		localTxDatabase = string;
	}

	public void setPassivator(String string) {
		passivator = string;
	}

	public void setPoolSize(String string) {
		poolSize = string;
	}

	public void setProvider(String string) {
		provider = string;
	}

	public void setStrictPooling(String string) {
		strictPooling = string;
	}

	public void setTimeOut(String string) {
		timeOut = string;
	}

	public boolean isEdit() {
		return edit;
	}

	public String getStrictPooling() {
		return strictPooling;
	}

	public void setEdit(boolean b) {
		edit = b;
	}
}