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
package org.openejb.test.interop.slsb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.openejb.test.security.slsb.BasicStateless;
import org.openejb.test.security.slsb.BasicStatelessHome;


/**
 * @version $Revision$ $Date$
 */
public class InteropBean implements SessionBean {

    private SessionContext sessionContext;

    public String callNoAccess(String argument1) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
            BasicStateless bean = home.create();

            return bean.noAccessMethod(argument1);
        } catch (NamingException e) {
            throw new RemoteException("Unable to lookup java:comp/env/ejb/interop/InteropBean", e);
        } catch (CreateException e) {
            throw new RemoteException("Unable to create BasicStateless EJB", e);
        }
    }

    public String callLowAccess(String argument1) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
            BasicStateless bean = home.create();

            return bean.lowSecurityMethod(argument1);
        } catch (NamingException e) {
            throw new RemoteException("Unable to lookup java:comp/env/ejb/interop/InteropBean", e);
        } catch (CreateException e) {
            throw new RemoteException("Unable to create BasicStateless EJB", e);
        }
    }

    public String callMedAccess(String argument1) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
            BasicStateless bean = home.create();

            return bean.mediumSecurityMethod(argument1);
        } catch (NamingException e) {
            throw new RemoteException("Unable to lookup java:comp/env/ejb/interop/InteropBean", e);
        } catch (CreateException e) {
            throw new RemoteException("Unable to create BasicStateless EJB", e);
        }
    }

    public String callHighAccess(String argument1) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
            BasicStateless bean = home.create();

            return bean.highSecurityMethod(argument1);
        } catch (NamingException e) {
            throw new RemoteException("Unable to lookup java:comp/env/ejb/interop/InteropBean", e);
        } catch (CreateException e) {
            throw new RemoteException("Unable to create BasicStateless EJB", e);
        }
    }

    public String callAllAccess(String argument1) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
            BasicStateless bean = home.create();

            return bean.allAccessMethod(argument1);
        } catch (NamingException e) {
            e.printStackTrace();
            throw new RemoteException("Unable to lookup java:comp/env/ejb/interop/InteropBean", e);
        } catch (CreateException e) {
            e.printStackTrace();
            throw new RemoteException("Unable to create BasicStateless EJB", e);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RemoteException("Throwable", t);
        }

    }

     public String callAllAccessTx(String argument1) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
            BasicStateless bean = home.create();

            bean.allAccessMethod(argument1);

        } catch (NamingException e) {
            e.printStackTrace();
            throw new RemoteException("Unable to lookup java:comp/env/ejb/interop/InteropBean", e);
        } catch (CreateException e) {
            e.printStackTrace();
            throw new RemoteException("Unable to create BasicStateless EJB", e);
        } catch (RemoteException e) {
            //expected
            System.out.println("SUCCESS, got RemoteException: ");
            e.printStackTrace();
            return "SUCCESS";
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RemoteException("Throwable", t);
        }
         throw new RemoteException("NO EXCEPTION THROWN");

    }

    public boolean isInRole(String roleName) {
        return sessionContext.isCallerInRole(roleName);
    }

    public void ejbCreate() throws CreateException {
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
