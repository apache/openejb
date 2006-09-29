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

import org.codehaus.wadi.Session;

/**
 * 
 * @version $Revision$ $Date$
 */
public class EJBSessionUtil {
    private static final String ENTERPRISE_BEAN = "enterpriseBean";
    private static final String ID = "id";
    private static final String CONTAINER_ID = "containerId";
    
    private final Session session;
    
    public EJBSessionUtil(Session session) {
        this.session = session;
    }
    
    public EnterpriseBean getEnterpriseBean() {
        EnterpriseBean bean = (EnterpriseBean) session.getAttribute(ENTERPRISE_BEAN);
        if (null == bean) {
            throw new IllegalStateException("No EnterpriseBean defined by session.");
        }
        return bean;
    }
    
    public void setEnterpriseBean(EnterpriseBean bean) {
        session.setAttribute(ENTERPRISE_BEAN, bean);
    }
    
    public Object getId() {
        Object id = session.getAttribute(ID);
        if (null == id) {
            throw new IllegalStateException("No ID defined by session.");
        }
        return id;
    }
    
    public void setId(Object id) {
        session.setAttribute(ID, id);
    }
    
    public void setContainerId(Object containerId) {
        session.setAttribute(CONTAINER_ID, containerId);
    }

    public Object getContainerId() {
        return session.getAttribute(CONTAINER_ID);
    }
}