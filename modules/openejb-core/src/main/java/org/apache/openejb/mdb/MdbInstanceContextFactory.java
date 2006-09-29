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
package org.apache.openejb.mdb;

import javax.ejb.MessageDrivenBean;

import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInstanceFactory;
import org.apache.openejb.EJBInstanceFactoryImpl;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.MdbContainer;

/**
 * @version $Revision$ $Date$
 */
public class MdbInstanceContextFactory implements InstanceContextFactory {
    private final MdbDeployment mdbDeploymentContext;
    private final MdbContainer mdbContainer;
    private final EJBInstanceFactory instanceFactory;

    public MdbInstanceContextFactory(MdbDeployment mdbDeploymentContext, MdbContainer mdbContainer) {
        this.mdbDeploymentContext = mdbDeploymentContext;
        this.mdbContainer = mdbContainer;
        this.instanceFactory = new EJBInstanceFactoryImpl(mdbDeploymentContext.getBeanClass());
    }

    public EJBInstanceContext newInstance() throws Exception {
        return new MdbInstanceContext(mdbDeploymentContext,
                mdbContainer,
                (MessageDrivenBean) instanceFactory.newInstance()
        );
    }
}
