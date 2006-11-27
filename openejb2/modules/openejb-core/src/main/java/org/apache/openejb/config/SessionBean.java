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
package org.apache.openejb.config;

import org.apache.openejb.config.ejb11.*;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class SessionBean implements Bean {

    Session bean;
    String type;

    SessionBean(Session bean) {
        this.bean = bean;
        if (bean.getSessionType().equals("Stateful")) {
            type = STATEFUL;
        } else {
            type = STATELESS;
        }
    }

    public String getType() {
        return type;
    }

    public Object getBean() {
        return bean;
    }

    public String getEjbName() {
        return bean.getEjbName();
    }

    public String getEjbClass() {
        return bean.getEjbClass();
    }

    public String getHome() {
        return bean.getHome();
    }

    public String getRemote() {
        return bean.getRemote();
    }

    public EjbLocalRef[] getEjbLocalRef() {
        return bean.getEjbLocalRef();
    }

    public String getLocal() {
        return bean.getLocal();
    }

    public String getLocalHome() {
        return bean.getLocalHome();
    }

    public EjbRef[] getEjbRef() {
        return bean.getEjbRef();
    }

    public EnvEntry[] getEnvEntry() {
        return bean.getEnvEntry();
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef();
    }

    public SecurityRoleRef[] getSecurityRoleRef() {
        return bean.getSecurityRoleRef();
    }
}

