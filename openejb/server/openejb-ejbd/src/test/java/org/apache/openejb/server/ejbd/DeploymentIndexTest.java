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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.SystemException;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.InterfaceType;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class DeploymentIndexTest {

    private Method method;
    private BeanContext beanContext;
    private DeploymentIndex deploymentIndex;

    @Before
    public void setUp() throws SystemException {
        method = Method.class.getMethods()[0];
        beanContext = new BeanContext("aDeploymentId", null, new ModuleContext("", null, "", new AppContext("", SystemInstance.get(), null, null, null, false), null, null), DeploymentIndexTest.class, null, null, null, null, null, null, null, null, null, null, false);
        deploymentIndex = new DeploymentIndex(new BeanContext[]{beanContext, beanContext});
    }

    @Test
    public void testGetDeploymentEJBRequest() throws RemoteException {
        final EJBMetaDataImpl ejbMetadataWithId = new EJBMetaDataImpl(null, null, null, null, null, 1, InterfaceType.BUSINESS_REMOTE, null, null);
        final EJBRequest request = new EJBRequest(null, ejbMetadataWithId, method, null, null);
        final BeanContext info = deploymentIndex.getDeployment(request);

        Assert.assertEquals(beanContext, info);
        Assert.assertEquals(request.getDeploymentId(), info.getDeploymentID());
    }

    @Test(expected = RemoteException.class)
    public void testGetDeploymentEJBRequestRemoteException() throws RemoteException {
        // 0 causes DeploymentIndex to move further
        final EJBMetaDataImpl ejbMetadata = new EJBMetaDataImpl(null, null, null, null, null, 0, InterfaceType.BUSINESS_REMOTE, null, null);
        final EJBRequest request = new EJBRequest(null, ejbMetadata, method, null, null);
        deploymentIndex.getDeployment(request);
    }

}
