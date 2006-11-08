/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;

/**
 * 
 */
public interface SoapEncodedStatelessEndpoint extends java.rmi.Remote {

    public String returnStringObject(String data) throws RemoteException;
    public Boolean returnBooleanObject(Boolean data) throws RemoteException;
    public boolean returnBooleanPrimitive(boolean data) throws RemoteException;
    public Byte returnByteObject(Byte data) throws RemoteException;
    public byte returnBytePrimitive(byte data) throws RemoteException;
    public Short returnShortObject(Short data) throws RemoteException;
    public short returnShortPrimitive(short data) throws RemoteException;
    public Integer returnIntegerObject(Integer data) throws RemoteException;
    public int returnIntegerPrimitive(int data) throws RemoteException;
    public Long returnLongObject(Long data) throws RemoteException;
    public long returnLongPrimitive(long data) throws RemoteException;
    public Float returnFloatObject(Float data) throws RemoteException;
    public float returnFloatPrimitive(float data) throws RemoteException;
    public Double returnDoubleObject(Double data) throws RemoteException;
    public double returnDoublePrimitive(double data) throws RemoteException;

}
