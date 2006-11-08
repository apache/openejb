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
package org.openejb.test.interop.slsb;

import java.rmi.RemoteException;
import javax.ejb.EJBLocalObject;


/**
 * @version $Revision$ $Date$
 */
public interface LocalInterop extends EJBLocalObject {

    public String callNoAccess(String argument1) throws RemoteException;

    public String callLowAccess(String argument1) throws RemoteException;

    public String callMedAccess(String argument1) throws RemoteException;

    public String callHighAccess(String argument1) throws RemoteException;

    public String callAllAccess(String argument1) throws RemoteException;

}
