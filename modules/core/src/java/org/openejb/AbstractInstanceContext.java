/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.EnterpriseBean;

import org.openejb.proxy.EJBProxyFactory;


/**
 * Simple implementation of ComponentContext satisfying invariant.
 *
 * @version $Revision$ $Date$
 *
 * */
public abstract class AbstractInstanceContext implements EJBInstanceContext {

    private final Map connectionManagerMap = new HashMap();
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    //this sucks, but the CMP instance is not available until after the superclass constructor executes.
    protected EnterpriseBean instance;
    private final EJBProxyFactory proxyFactory;

    public AbstractInstanceContext(Set unshareableResources, Set applicationManagedSecurityResources, EnterpriseBean instance, EJBProxyFactory proxyFactory) {
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.instance = instance;
        this.proxyFactory = proxyFactory;
    }

    public Object getId() {
        return null;
    }

    public void setId(Object id) {
    }

    public Object getContainerId() {
        return null;
    }

    public void associate() throws Exception {
    }

    public void flush() throws Exception {
    }

    public void beforeCommit() throws Exception {
    }

    public void afterCommit(boolean status) throws Exception {
    }

    public Map getConnectionManagerMap() {
        return connectionManagerMap;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public EnterpriseBean getInstance() {
        return instance;
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

}
