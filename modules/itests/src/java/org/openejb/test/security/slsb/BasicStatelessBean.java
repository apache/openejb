/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.openejb.test.security.slsb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;


/**
 * @version $Revision$ $Date$
 */
public class BasicStatelessBean implements javax.ejb.SessionBean {

    private SessionContext sessionContext;

    public String noAccessMethod(String argument1) throws RemoteException {
        return argument1;
    }

    public String noAccessMethod(String argument1, String argument2) throws RemoteException {
        return argument1 + argument2;
    }

    public String highSecurityMethod(String argument1) throws RemoteException {
        return argument1;
    }

    public String highSecurityMethod(String argument1, String argument2) throws RemoteException {
        return argument1 + argument2;
    }

    public String mediumSecurityMethod(String argument1) throws RemoteException {
        return argument1;
    }

    public String mediumSecurityMethod(String argument1, String argument2) throws RemoteException {
        return argument1 + argument2;
    }

    public String lowSecurityMethod(String argument1) throws RemoteException {
        return argument1;
    }

    public String lowSecurityMethod(String argument1, String argument2) throws RemoteException {
        return argument1 + argument2;
    }

    public String allAccessMethod(String argument1) throws RemoteException {
        return argument1;
    }

    public String allAccessMethod(String argument1, String argument2) throws RemoteException {
        return argument1 + argument2;
    }

    public String unassignedMethod(String argument1) throws RemoteException {
        return argument1;
    }

    public String unassignedMethod(String argument1, String argument2) throws RemoteException {
        return argument1 + argument2;
    }

    public boolean isInRole(String roleName) throws RemoteException {
        return sessionContext.isCallerInRole(roleName);
    }

    public void ejbCreate() throws CreateException {
    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
        this.sessionContext = sessionContext;
    }
}
