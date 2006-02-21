/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
