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
package org.apache.openejb.test.entity.cmp2;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.NamedTestCase;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.cmp2.cmrmapping.CMRMappingFacadeHome;
import org.apache.openejb.test.entity.cmp2.cmrmapping.CMRMappingFacadeRemote;


/**
 * @version $Revision$ $Date$
 */
public class CMRMappingTests extends NamedTestCase {
    private InitialContext initialContext;
    private CMRMappingFacadeHome ejbHome;
    private CMRMappingFacadeRemote facade;

    public CMRMappingTests() {
        super("CMRMappingTests.");
    }

    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        ejbHome = (CMRMappingFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/CMRMappingFacade"), CMRMappingFacadeHome.class);
        facade = ejbHome.create();
    }

    public void testOneToManyDoNotSetCMR() throws RemoteException, TestFailureException {
        facade.testOneToManyDoNotSetCMR();
    }

    public void testOneToManySetCMROnInverseSide() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnInverseSide();
    }

    public void testOneToManySetCMROnInverseSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnInverseSideResetPK();
    }

    public void testOneToManySetCMROnOwningSide() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnOwningSide();
    }

    public void testOneToManySetCMROnOwningSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnOwningSideResetPK();
    }

    public void testOneToOneDoNotSetCMR() throws RemoteException, TestFailureException {
        facade.testOneToOneDoNotSetCMR();
    }

    public void testOneToOneSetCMROnInverseSide() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnInverseSide();
    }

    public void testOneToOneSetCMROnInverseSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnInverseSideResetPK();
    }

    public void testOneToOneSetCMROnOwningSide() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnOwningSide();
    }

    public void testOneToOneSetCMROnOwningSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnOwningSideResetPK();
    }
}