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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
package org.openejb.proxy;


public class ProxyInfo {
    
    private final int componentType;
    private final Object containerId;
    private final Object primaryKey;
    
    private final Class remoteInterface;
    private final Class homeInterface;
    private final Class localObjectInterface;
    private final Class localHomeInterface;
    private final Class primaryKeyClass;
    

    public ProxyInfo(ProxyInfo info, Object primaryKey) {
        this(info.componentType,info.containerId, info.homeInterface, info.remoteInterface, info.primaryKeyClass, primaryKey);
    }

    public ProxyInfo(int componentType, Object containerId, Class homeInterface, Class remoteInterface, Class primaryKeyClass, Object primaryKey) {
        this.componentType = componentType;
        this.containerId = containerId;
        this.primaryKey = primaryKey;
        this.homeInterface = homeInterface;
        this.primaryKeyClass = primaryKeyClass;
        this.remoteInterface = remoteInterface;
        this.localHomeInterface = null; //TODO: add these to constructor
        this.localObjectInterface = null; //TODO: add these to constructor
    }
    
    public ProxyInfo(
            int componentType,
            Object deploymentID,
            Class homeInterface,
            Class remoteInterface,
            Class localHomeInterface,
            Class localObjectInterface,
            Class primaryKeyClass) {

        this.componentType = componentType;
        this.containerId = deploymentID;
        this.primaryKey = null;
        this.homeInterface = homeInterface;
        this.primaryKeyClass = primaryKeyClass;
        this.remoteInterface = remoteInterface;
        this.localHomeInterface = localHomeInterface;
        this.localObjectInterface = localObjectInterface;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }
    
    public Class getRemoteInterface() {
        return remoteInterface;
    }
    
    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localObjectInterface;
    }

    public Class getPrimaryKeyClass() {
        return primaryKeyClass;
    }
    
    public int getComponentType() {
        return componentType;
    }
    
    public Object getContainerID() {
        return containerId;
    }

    // TODO: Kill this method
    public Object getPrimaryKey() {
        return primaryKey;
    }

}
