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
 * $Id$
 */
package org.openejb.corba.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.StubDelegate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.ORB;
import org.openejb.corba.ClientContext;
import org.openejb.corba.ClientContextManager;

/**
 * @version $Revision$ $Date$
 */
public class StubDelegateImpl implements StubDelegate {
    private static final Log log = LogFactory.getLog(StubDelegateImpl.class);
    private final static String DELEGATE_NAME = "org.openejb.corba.StubDelegateClass";
    private final StubDelegate delegate;
    private ClientContext clientContext;

    private static final Map stubToDelegateMap = Collections.synchronizedMap(new WeakHashMap());

    public StubDelegateImpl() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String value = System.getProperty(DELEGATE_NAME);
        if (value == null) {
            log.error("No delegate specfied via " + DELEGATE_NAME);
            throw new IllegalStateException("The property " + DELEGATE_NAME + " must be defined!");
        }

        if (log.isDebugEnabled()) log.debug("Set delegate " + value);
        delegate = (StubDelegate) StubDelegateImpl.class.getClassLoader().loadClass(value).newInstance();
    }

    public static StubDelegateImpl getDelegateForStub(Stub stub) {
        return (StubDelegateImpl) stubToDelegateMap.get(stub);
    }

    public int hashCode(Stub self) {
        return delegate.hashCode(self);
    }

    public boolean equals(Stub self, Object obj) {
        return delegate.equals(self, obj);
    }

    public String toString(Stub self) {
        return delegate.toString(self);
    }

    public ClientContext getClientContext() {
        return clientContext;
    }

    public void connect(Stub self, ORB orb) throws RemoteException {
        delegate.connect(self, orb);
        clientContext = ClientContextManager.getClientContext();
        stubToDelegateMap.put(self, this);
    }

    public void readObject(Stub self, ObjectInputStream s) throws IOException, ClassNotFoundException {
        ClientContext oldClientContext = ClientContextManager.getClientContext();
        try {
            ClientContextManager.setClientContext(clientContext);
            delegate.readObject(self, s);
        } finally {
            ClientContextManager.setClientContext(oldClientContext);
        }
    }

    public void writeObject(Stub self, ObjectOutputStream s) throws IOException {
        delegate.writeObject(self, s);
    }
}
