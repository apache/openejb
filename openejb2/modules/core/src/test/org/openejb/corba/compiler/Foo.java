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
import org.openejb.corba.compiler.other.Donkey;
import org.openejb.corba.compiler.other.DonkeyEx;
import org.openejb.corba.compiler.other.Generic$Interface;

/**
 * @version $Rev$ $Date$
 */
public interface Foo extends Remote {

    public void abcdefg_nothing() throws RemoteException;

    public void abcdefg_pass_boolean(boolean x) throws RemoteException;
    public void abcdefg_pass_char(char x) throws RemoteException;
    public void abcdefg_pass_byte(byte x) throws RemoteException;
    public void abcdefg_pass_int(int x) throws RemoteException;
    public void abcdefg_pass_long(long x) throws RemoteException;
    public void abcdefg_pass_float(float x) throws RemoteException;
    public void abcdefg_pass_double(double x) throws RemoteException;
    public void abcdefg_pass_BigDecimal(BigDecimal x) throws RemoteException;
    public void abcdefg_pass_Class(Class x) throws RemoteException;
    public void abcdefg_pass_CORBA_Object(org.omg.CORBA.Object x) throws RemoteException;
    public void abcdefg_pass_CORBA_Any(org.omg.CORBA.Any x) throws RemoteException;
    public void abcdefg_pass_CORBA_TypeCode(org.omg.CORBA.TypeCode x) throws RemoteException;
    public void abcdefg_pass_CheeseIDLEntity(CheeseIDLEntity x) throws RemoteException;
    public void abcdefg_pass_GenericInterface(Generic$Interface x) throws RemoteException;
    public void abcdefg_pass_BlahException(BlahEx x) throws RemoteException;
    public void abcdefg_pass_BooException(BooException x) throws RemoteException;

    public boolean abcdefg_return_boolean() throws RemoteException;
    public char abcdefg_return_char() throws RemoteException;
    public byte abcdefg_return_byte() throws RemoteException;
    public int abcdefg_return_int() throws RemoteException;
    public long abcdefg_return_long() throws RemoteException;
    public float abcdefg_return_float() throws RemoteException;
    public double abcdefg_return_double() throws RemoteException;
    public BigDecimal abcdefg_return_BigDecimal() throws RemoteException;
    public Class abcdefg_return_Class() throws RemoteException;
    public org.omg.CORBA.Object abcdefg_return_CORBA_Object() throws RemoteException;
    public org.omg.CORBA.Any abcdefg_return_CORBA_Any() throws RemoteException;
    public org.omg.CORBA.TypeCode abcdefg_return_CORBA_TypeCode() throws RemoteException;
    public CheeseIDLEntity abcdefg_return_CheeseIDLEntity() throws RemoteException;
    public Generic$Interface abcdefg_return_GenericInterface() throws RemoteException;
    public BlahEx abcdefg_return_BlahException() throws RemoteException;
    public BooException abcdefg_return_BooException() throws RemoteException;

    public boolean abcdefg_pass_return_boolean(boolean x) throws RemoteException;
    public char abcdefg_pass_return_char(char x) throws RemoteException;
    public byte abcdefg_pass_return_byte(byte x) throws RemoteException;
    public int abcdefg_pass_return_int(int x) throws RemoteException;
    public long abcdefg_pass_return_long(long x) throws RemoteException;
    public float abcdefg_pass_return_float(float x) throws RemoteException;
    public double abcdefg_pass_return_double(double x) throws RemoteException;
    public BigDecimal abcdefg_pass_return_BigDecimal(BigDecimal x) throws RemoteException;
    public Class abcdefg_pass_return_Class(Class x) throws RemoteException;
    public org.omg.CORBA.Object abcdefg_pass_return_CORBA_Object(org.omg.CORBA.Object x) throws RemoteException;
    public org.omg.CORBA.Any abcdefg_pass_return_CORBA_Any(org.omg.CORBA.Any x) throws RemoteException;
    public org.omg.CORBA.TypeCode abcdefg_pass_return_CORBA_TypeCode(org.omg.CORBA.TypeCode x) throws RemoteException;
    public CheeseIDLEntity abcdefg_pass_return_CheeseIDLEntity(CheeseIDLEntity x) throws RemoteException;

    public void abcdefg_pass_boolean_arr(boolean[] x) throws RemoteException;
    public void abcdefg_pass_char_arr(char[] x) throws RemoteException;
    public void abcdefg_pass_byte_arr(byte[] x) throws RemoteException;
    public void abcdefg_pass_int_arr(int[] x) throws RemoteException;
    public void abcdefg_pass_long_arr(long[] x) throws RemoteException;
    public void abcdefg_pass_float_arr(float[] x) throws RemoteException;
    public void abcdefg_pass_double_arr(double[] x) throws RemoteException;
    public void abcdefg_pass_BigDecimal_arr(BigDecimal[] x) throws RemoteException;
    public void abcdefg_pass_Class_arr(Class[] x) throws RemoteException;
    public void abcdefg_pass_CORBA_Object_arr(org.omg.CORBA.Object[] x) throws RemoteException;
    public void abcdefg_pass_CORBA_Any_arr(org.omg.CORBA.Any[] x) throws RemoteException;
    public void abcdefg_pass_CORBA_TypeCode_arr(org.omg.CORBA.TypeCode[] x) throws RemoteException;
    public void abcdefg_pass_CheeseIDLEntity_arr(CheeseIDLEntity[] x) throws RemoteException;
    public void abcdefg_pass_GenericInterface_arr(Generic$Interface[] x) throws RemoteException;
    public void abcdefg_pass_BlahException_arr(BlahEx[] x) throws RemoteException;
    public void abcdefg_pass_BooException_arr(BooException[] x) throws RemoteException;

    public boolean[] abcdefg_return_boolean_arr() throws RemoteException;
    public char[] abcdefg_return_char_arr() throws RemoteException;
    public byte[] abcdefg_return_byte_arr() throws RemoteException;
    public int[] abcdefg_return_int_arr() throws RemoteException;
    public long[] abcdefg_return_long_arr() throws RemoteException;
    public float[] abcdefg_return_float_arr() throws RemoteException;
    public double[] abcdefg_return_double_arr() throws RemoteException;
    public BigDecimal[] abcdefg_return_BigDecimal_arr() throws RemoteException;
    public Class[] abcdefg_return_Class_arr() throws RemoteException;
    public org.omg.CORBA.Object[] abcdefg_return_CORBA_Object_arr() throws RemoteException;
    public org.omg.CORBA.Any[] abcdefg_return_CORBA_Any_arr() throws RemoteException;
    public org.omg.CORBA.TypeCode[] abcdefg_return_CORBA_TypeCode_arr() throws RemoteException;
    public CheeseIDLEntity[] abcdefg_return_CheeseIDLEntity_arr() throws RemoteException;
    public Generic$Interface[] abcdefg_return_GenericInterface_arr() throws RemoteException;
    public BlahEx[] abcdefg_return_BlahException_arr() throws RemoteException;
    public BooException[] abcdefg_return_BooException_arr() throws RemoteException;

    public void abcdefg_overload() throws RemoteException;

    public void abcdefg_overload(boolean x) throws RemoteException;
    public void abcdefg_overload(char x) throws RemoteException;
    public void abcdefg_overload(byte x) throws RemoteException;
    public void abcdefg_overload(int x) throws RemoteException;
    public void abcdefg_overload(long x) throws RemoteException;
    public void abcdefg_overload(float x) throws RemoteException;
    public void abcdefg_overload(double x) throws RemoteException;
    public void abcdefg_overload(String x) throws RemoteException;
    public void abcdefg_overload(BigDecimal x) throws RemoteException;
    public void abcdefg_overload(Class x) throws RemoteException;
    public void abcdefg_overload(Object x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.Object x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.Any x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.TypeCode x) throws RemoteException;
    public void abcdefg_overload(CheeseIDLEntity x) throws RemoteException;
    public void abcdefg_overload(Generic$Interface x) throws RemoteException;
    public void abcdefg_overload(BlahEx x) throws RemoteException;
    public void abcdefg_overload(BooException x) throws RemoteException;

    public void abcdefg_overload(boolean[] x) throws RemoteException;
    public void abcdefg_overload(char[] x) throws RemoteException;
    public void abcdefg_overload(byte[] x) throws RemoteException;
    public void abcdefg_overload(int[] x) throws RemoteException;
    public void abcdefg_overload(long[] x) throws RemoteException;
    public void abcdefg_overload(float[] x) throws RemoteException;
    public void abcdefg_overload(double[] x) throws RemoteException;
    public void abcdefg_overload(String[] x) throws RemoteException;
    public void abcdefg_overload(BigDecimal[] x) throws RemoteException;
    public void abcdefg_overload(Class[] x) throws RemoteException;
    public void abcdefg_overload(Object[] x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.Object[] x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.Any[] x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.TypeCode[] x) throws RemoteException;
    public void abcdefg_overload(CheeseIDLEntity[] x) throws RemoteException;
    public void abcdefg_overload(Generic$Interface[] x) throws RemoteException;
    public void abcdefg_overload(BlahEx[] x) throws RemoteException;
    public void abcdefg_overload(BooException[] x) throws RemoteException;

    public void abcdefg_overload(boolean[][] x) throws RemoteException;
    public void abcdefg_overload(char[][] x) throws RemoteException;
    public void abcdefg_overload(byte[][] x) throws RemoteException;
    public void abcdefg_overload(int[][] x) throws RemoteException;
    public void abcdefg_overload(long[][] x) throws RemoteException;
    public void abcdefg_overload(float[][] x) throws RemoteException;
    public void abcdefg_overload(double[][] x) throws RemoteException;
    public void abcdefg_overload(String[][] x) throws RemoteException;
    public void abcdefg_overload(BigDecimal[][] x) throws RemoteException;
    public void abcdefg_overload(Class[][] x) throws RemoteException;
    public void abcdefg_overload(Object[][] x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.Object[][] x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.Any[][] x) throws RemoteException;
    public void abcdefg_overload(org.omg.CORBA.TypeCode[][] x) throws RemoteException;
    public void abcdefg_overload(CheeseIDLEntity[][] x) throws RemoteException;
    public void abcdefg_overload(Generic$Interface[][] x) throws RemoteException;
    public void abcdefg_overload(BlahEx[][] x) throws RemoteException;
    public void abcdefg_overload(BooException[][] x) throws RemoteException;

    public void abcdefg_throw_exception() throws RemoteException, BlahEx, BooException, DonkeyEx, Donkey;
    public void abcdefg_pass_throw_exception(String x) throws RemoteException, BlahEx, BooException, DonkeyEx, Donkey;
    public String abcdefg_return_throw_exception() throws RemoteException, BlahEx, BooException, DonkeyEx, Donkey;
    public String abcdefg_pass_return_throw_exception(String x) throws RemoteException, BlahEx, BooException, DonkeyEx, Donkey;
}
