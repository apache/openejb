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
package org.openejb.corba.compiler;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.openejb.corba.compiler.other.BlahEx;
import org.openejb.corba.compiler.other.CheeseIDLEntity;
import org.openejb.corba.compiler.other.Generic$Interface;

/**
 * @version $Rev$ $Date$
 */
public interface BeanProperties extends Remote {
    public void setBooleanabcdef(boolean x) throws RemoteException;
    public void setCharabcdef(char x) throws RemoteException;
    public void setByteabcdef(byte x) throws RemoteException;
    public void setIntabcdef(int x) throws RemoteException;
    public void setLongabcdef(long x) throws RemoteException;
    public void setFloatabcdef(float x) throws RemoteException;
    public void setDoubleabcdef(double x) throws RemoteException;
    public void setBigDecimalabcdef(BigDecimal x) throws RemoteException;
    public void setClassObjectabcdef(Class x) throws RemoteException;
    public void setCORBA_Objectabcdef(org.omg.CORBA.Object x) throws RemoteException;
    public void setCORBA_Anyabcdef(org.omg.CORBA.Any x) throws RemoteException;
    public void setCORBA_TypeCodeabcdef(org.omg.CORBA.TypeCode x) throws RemoteException;
    public void setCheeseIDLEntityabcdef(CheeseIDLEntity x) throws RemoteException;
    public void setGenericInterfaceabcdef(Generic$Interface x) throws RemoteException;
    public void setBlahExceptionabcdef(BlahEx x) throws RemoteException;
    public void setBooExceptionabcdef(BooException x) throws RemoteException;

    public boolean isBooleanabcdef() throws RemoteException;
    public char getCharabcdef() throws RemoteException;
    public byte getByteabcdef() throws RemoteException;
    public int getIntabcdef() throws RemoteException;
    public long getLongabcdef() throws RemoteException;
    public float getFloatabcdef() throws RemoteException;
    public double getDoubleabcdef() throws RemoteException;
    public BigDecimal getBigDecimalabcdef() throws RemoteException;
    public Class getClassObjectabcdef() throws RemoteException;
    public org.omg.CORBA.Object getCORBA_Objectabcdef() throws RemoteException;
    public org.omg.CORBA.Any getCORBA_Anyabcdef() throws RemoteException;
    public org.omg.CORBA.TypeCode getCORBA_TypeCodeabcdef() throws RemoteException;
    public CheeseIDLEntity getCheeseIDLEntityabcdef() throws RemoteException;
    public Generic$Interface getGenericInterfaceabcdef() throws RemoteException;
    public BlahEx getBlahExceptionabcdef() throws RemoteException;
    public BooException getBooExceptionabcdef() throws RemoteException;


    // special
    public int getWithArgumentabcdef(int x) throws RemoteException;

    public int getWithSetReturningabcdef() throws RemoteException;
    public int setWithSetReturningabcdef(int x) throws RemoteException;

    public int getWithSetOfDifferentTypeabcdef() throws RemoteException;
    public void setWithSetOfDifferentTypeabcdef(long x) throws RemoteException;

    public int getThrowsUserExceptionabcdef() throws RemoteException, Exception;
    public void setThrowsUserExceptionabcdef(int x) throws RemoteException, Exception;

    public int getOverridenSetabcdef() throws RemoteException;
    public void setOverridenSetabcdef(int x) throws RemoteException;
    public void setOverridenSetabcdef(long x) throws RemoteException;

    public void setOnlyabcdef(int x) throws RemoteException;

    public int getOverridenGetabcdef() throws RemoteException;
    public int getOverridenGetabcdef(int x) throws RemoteException;

    public int getUPPERCASEabcdef() throws RemoteException;

    public int get() throws RemoteException;

    public int get_collisionabcdef() throws RemoteException;

    public int getA() throws RemoteException;
}
