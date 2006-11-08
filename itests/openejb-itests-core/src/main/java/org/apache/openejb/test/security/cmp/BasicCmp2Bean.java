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
package org.apache.openejb.test.security.cmp;

import java.util.Hashtable;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;


/**
 * @version $Revision$ $Date$
 */
public abstract class BasicCmp2Bean implements EntityBean {

    public static int key = 1000;

    public EntityContext entityContext;
    public Hashtable allowedOperationsTable = new Hashtable();

    public abstract Integer getId();

    public abstract void setId(Integer primaryKey);

    public abstract String getFirstName();

    public abstract void setFirstName(String firstName);

    public abstract String getLastName();

    public abstract void setLastName(String lastName);

    public String noAccessMethod(String argument1) {
        return argument1;
    }

    public String noAccessMethod(String argument1, String argument2) {
        return argument1 + argument2;
    }

    public String highSecurityMethod(String argument1) {
        return argument1;
    }

    public String highSecurityMethod(String argument1, String argument2) {
        return argument1 + argument2;
    }

    public String mediumSecurityMethod(String argument1) {
        return argument1;
    }

    public String mediumSecurityMethod(String argument1, String argument2) {
        return argument1 + argument2;
    }

    public String lowSecurityMethod(String argument1) {
        return argument1;
    }

    public String lowSecurityMethod(String argument1, String argument2) {
        return argument1 + argument2;
    }

    public String allAccessMethod(String argument1) {
        return argument1;
    }

    public String allAccessMethod(String argument1, String argument2) {
        return argument1 + argument2;
    }

    public String unassignedMethod(String argument1) {
        return argument1;
    }

    public String unassignedMethod(String argument1, String argument2) {
        return argument1 + argument2;
    }

    public boolean isInRole(String roleName) {
        return entityContext.isCallerInRole(roleName);
    }

    public Integer ejbCreate(String firstName, String lastName) throws CreateException {
        setId(new Integer(key++));
        setFirstName(firstName);
        setLastName(lastName);

        return null;
    }

    public void ejbPostCreate(String firstName, String lastName) {
    }

    public void ejbLoad() {
    }

    public void setEntityContext(EntityContext ctx) {
        entityContext = ctx;
    }

    public void unsetEntityContext() {
        entityContext = null;
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

}
