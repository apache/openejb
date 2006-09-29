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
package org.apache.openejb.cluster.server;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;

import org.codehaus.wadi.Context;
import org.codehaus.wadi.InvocationContext;
import org.codehaus.wadi.PoolableInvocationWrapper;
import org.codehaus.wadi.Session;
import org.apache.openejb.EJBInstanceContext;

/**
 * 
 * @version $Revision$ $Date$
 */
public class EJBInvocationWrapper implements PoolableInvocationWrapper {
    private final RecreatorSelector recreatorSelector;
    private EJBInstanceContext instanceContext;
    private Session session;

    public EJBInvocationWrapper(RecreatorSelector factorySelector) {
        this.recreatorSelector = factorySelector;
    }

    public void init(InvocationContext invocationContext, Context context) {
        if (!(context instanceof Session)) {
            throw new IllegalArgumentException(Session.class +
                    " is expected");
        }
        session = (Session) context;

        EJBSessionUtil sessionUtil = new EJBSessionUtil(session);

        EnterpriseBean enterpriseBean = sessionUtil.getEnterpriseBean();
        if (!(enterpriseBean instanceof EnterpriseBean)) {
            throw new IllegalArgumentException(EnterpriseBean.class +
                    " is expected");
        }
        SessionBean sessionBean = (SessionBean) enterpriseBean;
        Object id = sessionUtil.getId();
        Object containerId = sessionUtil.getContainerId();

        EJBInstanceContextRecreator recreator = recreatorSelector.select(containerId);
        instanceContext = recreator.recreate(id, sessionBean);
    }

    public void destroy() {
        // Do nothing.
    }

    public EJBInstanceContext getInstanceContext() {
        return instanceContext;
    }

    public Session getSession() {
        return session;
    }
}