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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;

import javax.ejb.TransactionAttribute;
import javax.ejb.EJB;
import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import javax.ejb.RemoteHome;
import static javax.ejb.TransactionAttributeType.*;
import javax.naming.InitialContext;

import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.rmi.RemoteException;

/**
 * @version $Rev$ $Date$
 */
public class StatefulTransactionLockingTest extends TestCase {


    @Override
    protected void setUp() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        
        StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        statefulContainerInfo.properties.setProperty("AccessTimeout", "0 milliseconds");

        // containers
        assembler.createContainer(statefulContainerInfo);

        // Setup the descriptor information

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(BlueStatelessBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean(RedStatefulBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean(LegacyStatefulBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    public void testCompetingTransactions() throws Exception {
        final BlueStateless blueStateless = (BlueStateless) new InitialContext().lookup("BlueStatelessBeanLocal");
        final RedStateful redStateful = (RedStateful) new InitialContext().lookup("RedStatefulBeanLocal");

        Tx tx1 = new Tx(new RunTransaction(blueStateless, redStateful));
        Tx tx2 = new Tx(new RunTransaction(blueStateless, redStateful));
        Tx tx3 = new Tx(new RunTransaction(blueStateless, redStateful));

        tx1.thread.start();
        tx2.thread.start();
        tx3.thread.start();

        // Start the first transaction and wait for it to enlist the stateful bean
        tx1.begin.countDown();
        tx1.progress.await();

        // Start the second transaction, it should fail
        tx2.begin.countDown();

        assertTrue("TX 2 should fail", tx2.fail.await(5, TimeUnit.SECONDS));

        // end tx 1
        tx1.commit.countDown();

        // give it a second to commit
        Thread.sleep(1000);
        
        // Now we should be able to cleanly enlist the stateful bean in a new transaction
        tx3.begin.countDown();

        tx3.progress.await(5, TimeUnit.SECONDS);

    }

    public void testLeavingTransaction() throws Exception {
        BlueStateless blueStateless = (BlueStateless) new InitialContext().lookup("BlueStatelessBeanLocal");
        RedStateful redStateful = (RedStateful) new InitialContext().lookup("RedStatefulBeanLocal");

        blueStateless.leaveTransaction1(redStateful);
        
        blueStateless.leaveTransaction2(redStateful);
    }

    public void testNestingTransaction() throws Exception {
        BlueStateless blueStateless = (BlueStateless) new InitialContext().lookup("BlueStatelessBeanLocal");
        RedStateful redStateful = (RedStateful) new InitialContext().lookup("RedStatefulBeanLocal");

        blueStateless.nestingTransaction(redStateful);
    }

    public void testCreatedInTransaction() throws Exception {
        BlueStateless blueStateless = (BlueStateless) new InitialContext().lookup("BlueStatelessBeanLocal");
        blueStateless.testCreatedInTransaction(new Tx(new RunTransaction(null, null)));
    }

    public void testLegacyRemoveOutOfTransaction() throws Exception {
        LegacyHome home = (LegacyHome) new InitialContext().lookup("LegacyStatefulBeanRemoteHome");
        LegacyObject legacyObject = home.create();

        legacyObject.txRequired();
        legacyObject.remove();
    }

    public static class Tx {
        private final CountDownLatch begin = new CountDownLatch(1);
        private final CountDownLatch progress = new CountDownLatch(1);
        private final CountDownLatch commit = new CountDownLatch(1);
        private final CountDownLatch fail = new CountDownLatch(1);
        private final Thread thread;

        public Tx(TxRunnable runnable) {
            runnable.tx = this;
            this.thread = new Thread(runnable);
        }
    }

    public abstract static class TxRunnable implements Runnable {
        protected Tx tx;
    }
    
    public static class RunTransaction extends TxRunnable {
        private final BlueStateless blueStateless;
        private final RedStateful redStateful;

        public RunTransaction(BlueStateless blueStateless, RedStateful redStateful) {
            this.blueStateless = blueStateless;
            this.redStateful = redStateful;
        }

        public void run() {
            try {
                blueStateless.runTransaction(redStateful, tx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EJB(name = "red", beanName = "RedStatefulBean", beanInterface = RedStateful.class)
    public static class BlueStatelessBean implements BlueStateless {

        @TransactionAttribute(REQUIRED)
        public void runTransaction(RedStateful stateful, Tx tx) throws Exception {

            try {
                tx.begin.await();

                stateful.txRequired();

                tx.progress.countDown();

                stateful.txRequired();

                stateful.txRequired();

                tx.commit.await();
            } catch (Exception e) {
                tx.fail.countDown();
            }
        }

        @TransactionAttribute(REQUIRED)
        public void leaveTransaction1(RedStateful stateful) throws Exception {
            stateful.txRequired();
            stateful.txNotSupported();
            stateful.txRequired();
            stateful.txNotSupported();
            stateful.txRequired();
        }

        @TransactionAttribute(REQUIRED)
        public void leaveTransaction2(RedStateful stateful) throws Exception {
            stateful.txNotSupported();
            stateful.txRequired();
            stateful.txNotSupported();
            stateful.txRequired();
        }

        @TransactionAttribute(REQUIRED)
        public void nestingTransaction(RedStateful stateful) throws Exception {
            stateful.txRequired();
            stateful.txRequiresNew();
            stateful.txRequired();
            stateful.txRequiresNew();
            stateful.txRequired();
        }

        @TransactionAttribute(REQUIRED)
        public void testCreatedInTransaction(Tx tx) throws Exception {
            RedStateful targetBean = (RedStateful) new InitialContext().lookup("java:comp/env/red");
            targetBean.txRequired();
            targetBean.txRequired();
            targetBean.txRequired();
        }
    }

    public static interface BlueStateless {

        void testCreatedInTransaction(Tx tx) throws Exception;

        void runTransaction(RedStateful redStateful, Tx tx) throws Exception;

        void leaveTransaction1(RedStateful redStateful) throws Exception;
        void leaveTransaction2(RedStateful redStateful) throws Exception;

        void nestingTransaction(RedStateful stateful) throws Exception;

    }

    

    public static class RedStatefulBean implements RedStateful {

        @TransactionAttribute(REQUIRED)
        public void txRequired() {}

        @TransactionAttribute(REQUIRES_NEW)
        public void txRequiresNew() {}

        @TransactionAttribute(NOT_SUPPORTED)
        public void txNotSupported() {}
    }

    public static interface RedStateful {
        public void txRequired();

        public void txNotSupported();

        public void txRequiresNew();

    }

    @RemoteHome(LegacyHome.class)
    public static class LegacyStatefulBean extends RedStatefulBean implements SessionBean {

        public void ejbCreate() {
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
        }
    }

    public static interface LegacyObject extends EJBObject, RedStateful {
    }

    public static interface LegacyHome extends EJBHome {
        LegacyObject create() throws RemoteException, CreateException;
    }

}
