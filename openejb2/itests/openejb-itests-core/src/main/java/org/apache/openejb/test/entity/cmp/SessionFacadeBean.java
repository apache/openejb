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
package org.apache.openejb.test.entity.cmp;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.openejb.test.TestFailureException;

/**
 * 
 */
public class SessionFacadeBean implements javax.ejb.SessionBean {
    private SessionContext ejbContext;
    private InitialContext jndiContext;
    
    //=============================
    // Home interface methods
    //    
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //
    /**
     * execute a create-remove-create of the same CMP within a single TX.
     */
    public void invokeCreateRemoveCreateSameCMP() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                BasicCmpObject object = home.create(pk, "Same Bean");
                object.remove();
                home.create(pk, "Same Bean");
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    /**
     * execute a create-create of the same CMP within a single TX.
     */
    public void invokeCreateCreateSameCMP() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                home.create(pk, "Same Bean");
                home.create(pk, "Same Bean");
                Assert.fail("Should have thrown DuplicateKeyException");
            } catch (DuplicateKeyException e) {
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    /**
     * execute a create-find with force cache flush before find.
     */
    public void invokeCreateFindNoForceCacheFlush() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                home.create(pk, "Same Bean");
                home.findByPrimaryKey(pk);
            } catch (ObjectNotFoundException e) {
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    /**
     * execute a create-find without force cache flush before find.
     */
    public void invokeCreateFindForceCacheFlush() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                home.create(pk, "Same Bean");
                Collection objects = home.findByLastName("Bean");
                Assert.assertEquals(1, objects.size());
                BasicCmpObject object = (BasicCmpObject) objects.iterator().next();
                Assert.assertEquals(pk, object.getPrimaryKey());
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }
    //
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    public void ejbCreate() throws javax.ejb.CreateException{
        try {
            jndiContext = new InitialContext(); 
        } catch (Exception e){
            throw new CreateException("Can not get the initial context: "+e.getMessage());
        }
    }

    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }

    public void ejbRemove() throws EJBException,RemoteException {
    }

    public void ejbActivate() throws EJBException,RemoteException {
    }

    public void ejbPassivate() throws EJBException,RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================
    
}
