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
package org.openejb.ri.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.ejb.EJBHome;

public class RiMetaData implements javax.ejb.EJBMetaData, java.io.Externalizable {
    
    final static byte ENTITY = (byte)1;
    final static byte STATEFUL = (byte)2;
    final static byte STATELESS = (byte)3;

    protected Class homeClass;
    protected Class remoteClass;
    protected Class keyClass;
    protected EJBHome homeStub;

    protected byte type;

    /** Public no-arg constructor required by Externalizable API */
    public RiMetaData() {}

    public RiMetaData(Class homeInterface, Class remoteInterface, byte typeOfBean) {
        type = typeOfBean;
        homeClass = homeInterface;
        remoteClass = remoteInterface;
    }
    
    public RiMetaData(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean) {
        if ( type==ENTITY ) {
            keyClass = primaryKeyClass;
        }
    }
    public Class getHomeInterfaceClass( ) {
        return homeClass;
    }
    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }
    public Class getPrimaryKeyClass( ) {
        if ( type == ENTITY )
            return keyClass;
        else
            throw new java.lang.UnsupportedOperationException();
    }
    public boolean isSession( ) {
        return(type == STATEFUL || type ==STATELESS);
    }
    
    public boolean isStatelessSession() {
        return type == STATELESS;
    }
    
    protected void setEJBHome(EJBHome home) {
        homeStub = home;
    }
    
    public javax.ejb.EJBHome getEJBHome() {
        return homeStub;
    }

    //========================================
    // Externalizable object implementation
    //
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(homeClass);
        out.writeObject(remoteClass);
        out.writeObject(keyClass);
        out.writeObject(homeStub);
        out.writeByte(type);
    }

    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        homeClass = (Class) in.readObject();
        remoteClass = (Class) in.readObject();
        keyClass = (Class) in.readObject();
        homeStub = (EJBHome) in.readObject();
        type = in.readByte();
    }

}