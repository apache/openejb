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
package org.apache.openejb.test.security.sfsb;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;


/**
 * @version $Revision$ $Date$
 */
public class BasicStatefulBean implements SessionBean {

    private String name;
    private SessionContext sessionContext;

    public String noAccessMethod(String argument1) {
        return name + argument1;
    }

    public String noAccessMethod(String argument1, String argument2) {
        return name + argument1 + argument2;
    }

    public String highSecurityMethod(String argument1) {
        return name + argument1;
    }

    public String highSecurityMethod(String argument1, String argument2) {
        return name + argument1 + argument2;
    }

    public String mediumSecurityMethod(String argument1) {
        return name + argument1;
    }

    public String mediumSecurityMethod(String argument1, String argument2) {
        return name + argument1 + argument2;
    }

    public String lowSecurityMethod(String argument1) {
        return name + argument1;
    }

    public String lowSecurityMethod(String argument1, String argument2) {
        return name + argument1 + argument2;
    }

    public String allAccessMethod(String argument1) {
        return name + argument1;
    }

    public String allAccessMethod(String argument1, String argument2) {
        return name + argument1 + argument2;
    }

    public String unassignedMethod(String argument1) {
        return name + argument1;
    }

    public String unassignedMethod(String argument1, String argument2) {
        return name + argument1 + argument2;
    }

    public boolean isInRole(String roleName) {
        return sessionContext.isCallerInRole(roleName);
    }

    public void ejbCreate(String name) {
        this.name = name;
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    public void ejbRemove() throws EJBException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException {
        this.sessionContext = sessionContext;
    }
}
