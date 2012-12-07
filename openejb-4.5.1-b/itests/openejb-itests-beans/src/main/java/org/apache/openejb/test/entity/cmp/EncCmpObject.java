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
package org.apache.openejb.test.entity.cmp;

import java.rmi.RemoteException;

import org.apache.openejb.test.TestFailureException;

public interface EncCmpObject extends javax.ejb.EJBObject{
    
    public void lookupEntityBean()    throws TestFailureException, RemoteException;
    public void lookupStatefulBean()  throws TestFailureException, RemoteException;
    public void lookupStatelessBean() throws TestFailureException, RemoteException;

    public void lookupStatelessBusinessLocal() throws TestFailureException, RemoteException;
    public void lookupStatelessBusinessRemote() throws TestFailureException, RemoteException;
    public void lookupStatefulBusinessLocal() throws TestFailureException, RemoteException;
    public void lookupStatefulBusinessRemote() throws TestFailureException, RemoteException;

    public void lookupResource() throws TestFailureException, RemoteException;
    public void lookupJMSConnectionFactory() throws TestFailureException, RemoteException;
    public void lookupPersistenceUnit() throws TestFailureException, RemoteException;
    public void lookupPersistenceContext() throws TestFailureException, RemoteException;

    public void lookupStringEntry()  throws TestFailureException, RemoteException;
    public void lookupDoubleEntry()  throws TestFailureException, RemoteException;
    public void lookupLongEntry()    throws TestFailureException, RemoteException;
    public void lookupFloatEntry()   throws TestFailureException, RemoteException;
    public void lookupIntegerEntry() throws TestFailureException, RemoteException;
    public void lookupShortEntry()   throws TestFailureException, RemoteException;
    public void lookupBooleanEntry() throws TestFailureException, RemoteException;
    public void lookupByteEntry()    throws TestFailureException, RemoteException;
    public void lookupCharacterEntry()    throws TestFailureException, RemoteException;

}
