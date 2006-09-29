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
package org.apache.openejb.mdb;

import javax.ejb.MessageDrivenBean;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.openejb.AbstractInstanceContext;
import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.MdbContainer;
import org.apache.openejb.cache.InstancePool;

/**
 * Wrapper for a MDB.
 *
 * @version $Revision$ $Date$
 */
public final class MdbInstanceContext extends AbstractInstanceContext {
    private final MdbContainer mdbContainer;
    private final MdbContext mdbContext;

    private InstancePool pool;

    public MdbInstanceContext(MdbDeployment mdbDeployment,
            MdbContainer mdbContainer,
            MessageDrivenBean instance) {
        super(mdbDeployment, instance, null);

        this.mdbContainer = mdbContainer;

        TransactionManager transactionManager = mdbContainer.getTransactionManager();

        UserTransaction userTransaction;
        if (mdbDeployment.isBeanManagedTransactions()) {
            userTransaction = mdbContainer.getUserTransaction();
        } else {
            userTransaction = null;
        }

        this.mdbContext = new MdbContext(this, transactionManager, userTransaction);
    }

    public void flush() {
        throw new AssertionError("Cannot flush a MDB Context");
    }

    public InstancePool getPool() {
        return pool;
    }

    public void setPool(InstancePool pool) {
        this.pool = pool;
    }

    public void die() {
        if (pool != null) {
            pool.remove(this);
            pool = null;
        }
        super.die();
    }

    public void exit() {
        if (pool != null) {
            pool.release(this);
        }
        super.exit();
    }

    public MdbContext getMessageDrivenContext() {
        return mdbContext;
    }

    public void setOperation(EJBOperation operation) {
        mdbContext.setState(operation);
    }

    public boolean setTimerState(EJBOperation operation) {
        return mdbContext.setTimerState(operation);
    }

    public EJBContextImpl getEJBContextImpl() {
        return mdbContext;
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        mdbContainer.setContext(this, mdbContext);
    }

    public void ejbCreate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        assert(getInstance() != null);
        mdbContainer.ejbCreate(this);
    }

    public void ejbRemove() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        assert(getInstance() != null);
        mdbContainer.ejbRemove(this);
    }
}
