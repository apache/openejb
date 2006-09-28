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
 * $Id: Simple.java 445472 2005-06-21 08:40:48Z dain $
 */
package org.apache.openejb.corba.compiler;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.openejb.corba.compiler.other.BlahEx;
import org.apache.openejb.corba.compiler.other.CheeseIDLEntity;
import org.apache.openejb.corba.compiler.other.Donkey;
import org.apache.openejb.corba.compiler.other.DonkeyEx;
import org.apache.openejb.corba.compiler.other.Generic$Interface;

/**
 * @version $Rev$ $Date$
 */
public interface Simple extends Remote, Special {
    public void invoke(boolean x0,
            char x1,
            byte x2,
            int x3,
            long x4,
            float x5,
            double x6,
            BigDecimal x7,
            Class x8,
            org.omg.CORBA.Object x9,
            org.omg.CORBA.Any x10,
            org.omg.CORBA.TypeCode x11,
            CheeseIDLEntity x12,
            Generic$Interface x13,
            BlahEx x14,
            BooException x15) throws RemoteException, RemoteException, BlahEx, BooException, DonkeyEx, Donkey;

    public int invokeInt() throws RemoteException;
    public String invokeString() throws RemoteException;
    public Generic$Interface invokeGeneric$Interface() throws RemoteException;
    public Foo invokeFoo() throws RemoteException;

}
