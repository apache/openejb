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
 * $Id$
 */
package org.openejb.corba;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.spi.HandleDelegate;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.naming.reference.SimpleReference;


/**
 * See ejb spec 2.1, 19.5.5.1
 *
 * @version $Revision$ $Date$
 */
public class CORBAHandleDelegate implements HandleDelegate {

    /**
     * Called by home handles to deserialize stubs in any app server, including ones by other vendors.
     * The spec seems to imply that a simple cast of in.readObject() should work but in certain
     * orbs this does not seem to work and in.readObject returns a generic remote stub that needs
     * to be narrowed.  Although we think this is likely an orb bug this code with narrow will
     * work in both circumstances.
     * @param in
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public EJBHome readEJBHome(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Object o = in.readObject();
        EJBHome home = (EJBHome) PortableRemoteObject.narrow(o, EJBHome.class);
        return home;
    }

    /**
     * Called by handles to deserialize stubs in any app server.  See comment to readEJBHome.
     * @param in
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public EJBObject readEJBObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Object o = in.readObject();
        EJBObject object = (EJBObject) PortableRemoteObject.narrow(o, EJBObject.class);
        return object;
    }

    public void writeEJBHome(EJBHome ejbHome, ObjectOutputStream out) throws IOException {
        out.writeObject(ejbHome);
    }

    public void writeEJBObject(EJBObject ejbObject, ObjectOutputStream out) throws IOException {
        out.writeObject(ejbObject);
    }

}
