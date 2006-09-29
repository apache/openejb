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
package org.openejb.test.entity.cmp;

import java.rmi.RemoteException;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;


public interface BasicCmpHome extends javax.ejb.EJBHome {
    public BasicCmpObject create(Integer primarykey, String name) throws CreateException, RemoteException;

    public BasicCmpObject create(String name) throws CreateException, RemoteException;

    public BasicCmpObject findByPrimaryKey(Integer primarykey) throws FinderException, RemoteException;

    public Collection findEmptyCollection() throws FinderException, RemoteException;

    public Collection findByLastName(String lastName) throws FinderException, RemoteException;

    public int sum(int x, int y) throws RemoteException;
}
