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
package org.apache.openejb.corba.compiler;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.openejb.corba.compiler.other.Generic$Interface;
import org.apache.openejb.corba.compiler.other._Something;
import org.apache.openejb.corba.compiler.other.inout;

/**
 * @version $Rev$ $Date$
 */
public interface Special extends Remote {
    // J_underscore
    public void _underscore() throws RemoteException;

    public void _underscoreOverload() throws RemoteException;
    public void _underscoreOverload(_Something x) throws RemoteException;
    public void _underscoreOverload(_Something[] x) throws RemoteException;

    // special characters
    public void dollar$() throws RemoteException;
    public void $dollar() throws RemoteException;

    // this doesn't work in rmic either although the spec says it's legal
//    public void unicode_øçœ¥πåßƒΩçµ() throws RemoteException;

    // innerclass
    public void innerClass(Generic$Interface.Generic$InnerClass x, int y) throws RemoteException;
    public void innerClass(Generic$Interface.Generic$InnerClass x[], int y) throws RemoteException;

    // class collision
    public void special() throws RemoteException;

    // difer by case only
    public void differByCase() throws RemoteException;
    public void differByCASE() throws RemoteException;
    public void differByCaseOverload() throws RemoteException;
    public void differByCASEOverload() throws RemoteException;
    public void differByCASEOverload(int x) throws RemoteException;

    // keywords
    public void keyword() throws RemoteException;
    public void keyword(inout x) throws RemoteException;
    public void ABSTRACT() throws RemoteException;
    public void ABSTRACT(int x) throws RemoteException;

    public void any() throws RemoteException;
    public void attribute() throws RemoteException;
    public void BOOLEAN() throws RemoteException;
    public void CASE() throws RemoteException;
    public void CHAR() throws RemoteException;
    public void CONST() throws RemoteException;
    public void context() throws RemoteException;
    public void custom() throws RemoteException;
    public void DEFAULT() throws RemoteException;
    public void DOUBLE() throws RemoteException;
    public void enum() throws RemoteException;
    public void exception() throws RemoteException;
    public void factory() throws RemoteException;
    public void FALSE() throws RemoteException;
    public void fixed() throws RemoteException;
    public void FLOAT() throws RemoteException;
    public void in() throws RemoteException;
    public void inout() throws RemoteException;
    public void INTERFACE() throws RemoteException;
    public void LONG() throws RemoteException;
    public void module() throws RemoteException;
    public void NATIVE() throws RemoteException;
    public void OBJECT() throws RemoteException;
    public void octet() throws RemoteException;
    public void oneway() throws RemoteException;
    public void out() throws RemoteException;
    public void PRIVATE() throws RemoteException;
    public void PUBLIC() throws RemoteException;
    public void raises() throws RemoteException;
    public void readonly() throws RemoteException;
    public void sequence() throws RemoteException;
    public void SHORT() throws RemoteException;
    public void string() throws RemoteException;
    public void struct() throws RemoteException;
    public void supports() throws RemoteException;
    public void SWITCH() throws RemoteException;
    public void TRUE() throws RemoteException;
    public void truncatable() throws RemoteException;
    public void typedef() throws RemoteException;
    public void union() throws RemoteException;
    public void unsigned() throws RemoteException;
    public void ValueBase() throws RemoteException;
    public void valuetype() throws RemoteException;
    public void VOID() throws RemoteException;
    public void wchar() throws RemoteException;
    public void wstring() throws RemoteException;
}
