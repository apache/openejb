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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: CORBAHandle.java 446193 2006-05-05 17:55:38Z dblevins $
 */
package org.apache.openejb.corba;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.spi.HandleDelegate;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;


/**
 * EJB v2.1 spec, section 19.5.5.1
 * <p/>
 * The <code>javax.ejb.spi.HandleDelegate</code> service provider interface
 * defines methods that enable portable implementations of <code>Handle</code>
 * and <code>HomeHandle</code> that are instantiated in a different vendor’s
 * container to serialize and deserialize EJBObject and EJBHome references.
 * The <code>HandleDelegate</code> interface is not used by enterprise beans
 * or J2EE application components directly.
 *
 * @version $Revision$ $Date$
 */
public class CORBAHandle implements Handle, Serializable {

    private static final long serialVersionUID = -3390719015323727224L;

    private String ior;
    private Object primaryKey;

    public CORBAHandle(String ior, Object primaryKey) {
        this.ior = ior;
        this.primaryKey = primaryKey;
    }

    public EJBObject getEJBObject() throws RemoteException {
        try {
            return (EJBObject) PortableRemoteObject.narrow(getOrb().string_to_object(ior), EJBObject.class);
        } catch (Exception e) {
            throw new RemoteException("Unable to convert IOR into object", e);
        }
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        HandleDelegate handleDelegate = getHandleDelegate();
        handleDelegate.writeEJBObject(getEJBObject(), out);
        out.writeObject(primaryKey);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        HandleDelegate handleDelegate = getHandleDelegate();
        EJBObject obj = handleDelegate.readEJBObject(in);
        primaryKey = in.readObject();

        try {
            ior = getOrb().object_to_string((org.omg.CORBA.Object) obj);
        } catch (Exception e) {
            throw new RemoteException("Unable to convert object to IOR", e);
        }
    }

    private static ORB getOrb() {
        try {
            Context context = new InitialContext();
            ORB orb = (ORB) context.lookup("java:comp/ORB");
            return orb;
        } catch (Throwable e) {
            throw new org.omg.CORBA.MARSHAL("Cound not find ORB in jndi at java:comp/ORB", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }

    private static HandleDelegate getHandleDelegate() {
        try {
            Context context = new InitialContext();
            HandleDelegate handleDelegate = (HandleDelegate) context.lookup("java:comp/HandleDelegate");
            return handleDelegate;
        } catch (Throwable e) {
            throw new org.omg.CORBA.MARSHAL("Cound not find handle delegate in jndi at java:comp/HandleDelegate", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }
}
