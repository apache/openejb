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
package org.apache.openejb.deployment;

import javax.ejb.EnterpriseBean;
import javax.ejb.TimerService;

import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.timer.BasicTimerService;

/**
 * @version $Rev$ $Date$
 */
public class MockEJBInstanceContext implements EJBInstanceContext {

    public static final MockEJBInstanceContext INSTANCE = new MockEJBInstanceContext();

    public ExtendedEjbDeployment getDeployment() {
        return null;
    }

    public EnterpriseBean getInstance() {
        return null;
    }

    public void setOperation(EJBOperation operation) {
    }

    public EJBProxyFactory getProxyFactory() {
        return null;
    }

    public TimerService getTimerService() {
        return null;
    }

    public BasicTimerService getBasicTimerService() {
        return null;
    }

    public void setTimerServiceAvailable(boolean available) {
    }

    public boolean setTimerState(EJBOperation operation) {
        return false;
    }

    public EJBContextImpl getEJBContextImpl() {
        return null;
    }

    public Object getId() {
        return null;
    }

    public Object getContainerId() {
        return null;
    }

    public void associate() throws Throwable {
    }

    public void unassociate() throws Throwable {
    }

    public void flush() throws Throwable {
    }

    public void beforeCommit() throws Exception {
    }

    public void afterCommit(boolean status) throws Exception {
    }

    public void die() {
    }

    public boolean isDead() {
        return false;
    }

    public boolean isInCall() {
        return false;
    }

    public void enter() {
    }

    public void exit() {
    }

    public Object getConnectorInstanceData() {
        return null;
    }

    public void setConnectorInstanceData(Object connectorInstanceData) {
    }
}
