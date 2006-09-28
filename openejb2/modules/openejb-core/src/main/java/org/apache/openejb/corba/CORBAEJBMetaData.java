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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: CORBAEJBMetaData.java 446193 2006-05-05 17:55:38Z dblevins $
 */
package org.apache.openejb.corba;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;

/**
 * @version $Revision$ $Date$
 */
public class CORBAEJBMetaData implements EJBMetaData, java.io.Serializable {

    private static final long serialVersionUID = 8085488135161906381L;

    public final static byte ENTITY = 1;
    public final static byte STATEFUL = 2;
    public final static byte STATELESS = 3;

    /**
     * The Class of the bean's home interface.
     */
    private final Class homeInterface;

    /**
     * The Class of the bean's remote interface.
     */
    private final Class remoteInterface;

    /**
     * The Class of the bean's primary key or null if the
     * bean is of a type that does not require a primary key.
     */
    private final Class primaryKeyClass;

    /**
     * The EJBHome stub/proxy for this bean deployment.
     */
    private final EJBHome ejbHome;

    /**
     * The type of bean that this MetaData implementation represents.
     *
     * @see #ENTITY
     * @see #STATEFUL
     * @see #STATELESS
     */
    private final byte ejbType;

    public CORBAEJBMetaData(EJBHome ejbHome, byte ejbType, Class homeInterface, Class remoteInterface, Class primaryKeyClass) {
        if (homeInterface == null) {
            throw new IllegalArgumentException("Home interface is null");
        }
        if (remoteInterface == null) {
            throw new IllegalArgumentException("Remote interface is null");
        }
        if (ejbType == ENTITY && primaryKeyClass == null) {
            throw new IllegalArgumentException("Entity bean must have a primary key class");
        }
        if (ejbType != ENTITY && primaryKeyClass != null) {
            throw new IllegalArgumentException("Session bean must have a primary key class");
        }
        this.ejbHome = ejbHome;
        this.ejbType = ejbType;
        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.primaryKeyClass = primaryKeyClass;
    }

    public Class getHomeInterfaceClass() {
        return homeInterface;
    }

    public Class getRemoteInterfaceClass() {
        return remoteInterface;
    }

    public Class getPrimaryKeyClass() {
        if (ejbType == ENTITY) {
            return primaryKeyClass;
        } else {
            throw new UnsupportedOperationException("Session objects are private resources and do not have primary keys");
        }
    }

    public boolean isSession() {
        return (ejbType == STATEFUL || ejbType == STATELESS);
    }

    public boolean isStatelessSession() {
        return ejbType == STATELESS;
    }

    public EJBHome getEJBHome() {
        return (EJBHome) PortableRemoteObject.narrow(ejbHome, EJBHome.class);
    }
}
