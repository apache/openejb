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
package org.openejb.test.stateless;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.openejb.test.object.ObjectGraph;

/**
 * 
 */
public interface RmiIiopStatelessObject extends javax.ejb.EJBObject{
    
    public String returnStringObject(String data) throws RemoteException; 

    public String[] returnStringObjectArray(String[] data) throws RemoteException; 

    public Character returnCharacterObject(Character data) throws RemoteException; 

    public char returnCharacterPrimitive(char data) throws RemoteException; 

    public Character[] returnCharacterObjectArray(Character[] data) throws RemoteException; 

    public char[] returnCharacterPrimitiveArray(char[] data) throws RemoteException; 

    public Boolean returnBooleanObject(Boolean data) throws RemoteException; 

    public boolean returnBooleanPrimitive(boolean data) throws RemoteException; 

    public Boolean[] returnBooleanObjectArray(Boolean[] data) throws RemoteException; 

    public boolean[] returnBooleanPrimitiveArray(boolean[] data) throws RemoteException; 

    public Byte returnByteObject(Byte data) throws RemoteException; 

    public byte returnBytePrimitive(byte data) throws RemoteException; 

    public Byte[] returnByteObjectArray(Byte[] data) throws RemoteException; 

    public byte[] returnBytePrimitiveArray(byte[] data) throws RemoteException; 

    public Short returnShortObject(Short data) throws RemoteException; 

    public short returnShortPrimitive(short data) throws RemoteException; 

    public Short[] returnShortObjectArray(Short[] data) throws RemoteException; 

    public short[] returnShortPrimitiveArray(short[] data) throws RemoteException; 

    public Integer returnIntegerObject(Integer data) throws RemoteException; 

    public int returnIntegerPrimitive(int data) throws RemoteException; 

    public Integer[] returnIntegerObjectArray(Integer[] data) throws RemoteException; 

    public int[] returnIntegerPrimitiveArray(int[] data) throws RemoteException; 

    public Long returnLongObject(Long data) throws RemoteException; 

    public long returnLongPrimitive(long data) throws RemoteException; 

    public Long[] returnLongObjectArray(Long[] data) throws RemoteException; 

    public long[] returnLongPrimitiveArray(long[] data) throws RemoteException; 

    public Float returnFloatObject(Float data) throws RemoteException; 

    public float returnFloatPrimitive(float data) throws RemoteException; 

    public Float[] returnFloatObjectArray(Float[] data) throws RemoteException; 

    public float[] returnFloatPrimitiveArray(float[] data) throws RemoteException; 

    public Double returnDoubleObject(Double data) throws RemoteException; 

    public double returnDoublePrimitive(double data) throws RemoteException; 

    public Double[] returnDoubleObjectArray(Double[] data) throws RemoteException; 

    public double[] returnDoublePrimitiveArray(double[] data) throws RemoteException; 

    public EJBHome returnEJBHome(EJBHome data) throws RemoteException; 

    public EJBHome returnEJBHome() throws RemoteException; 

    public ObjectGraph returnNestedEJBHome() throws RemoteException; 

    public EJBHome[] returnEJBHomeArray(EJBHome[] data) throws RemoteException; 

    public EJBObject returnEJBObject(EJBObject data) throws RemoteException; 

    public EJBObject returnEJBObject() throws RemoteException; 

    public ObjectGraph returnNestedEJBObject() throws RemoteException; 

    public EJBObject[] returnEJBObjectArray(EJBObject[] data) throws RemoteException; 

    public EJBMetaData returnEJBMetaData(EJBMetaData data) throws RemoteException; 

    public EJBMetaData returnEJBMetaData() throws RemoteException; 

    public ObjectGraph returnNestedEJBMetaData() throws RemoteException; 

    public EJBMetaData[] returnEJBMetaDataArray(EJBMetaData[] data) throws RemoteException; 

    public Handle returnHandle(Handle data) throws RemoteException; 

    public Handle returnHandle() throws RemoteException; 

    public ObjectGraph returnNestedHandle() throws RemoteException; 

    public Handle[] returnHandleArray(Handle[] data) throws RemoteException; 

    public ObjectGraph returnObjectGraph(ObjectGraph data) throws RemoteException; 

    public ObjectGraph[] returnObjectGraphArray(ObjectGraph[] data) throws RemoteException; 

}
