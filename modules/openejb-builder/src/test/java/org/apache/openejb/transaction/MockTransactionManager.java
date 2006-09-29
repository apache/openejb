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
package org.apache.openejb.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @version $Revision$ $Date$
 */
public class MockTransactionManager implements TransactionManager {
    private MockTransaction transaction;
    private MockTransaction lastTransaction;

    public boolean wasLastTxCommitted() {
        if (lastTransaction == null) throw new IllegalStateException("lastTransaction is null");
        return lastTransaction.committed;
    }

    public boolean wasLastTxRolledBack() {
        if (lastTransaction == null) throw new IllegalStateException("lastTransaction is null");
        return lastTransaction.rolledBack;
    }

    public void clear() {
        transaction = null;
    }

    public void begin() throws NotSupportedException, SystemException {
        if (transaction != null) throw new NotSupportedException("Transacion in progress");
        transaction = new MockTransaction();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        if (transaction == null) throw new IllegalStateException("No transacion in progress");
        transaction.commit();
        lastTransaction = transaction;
        transaction = null;
    }

    public int getStatus() throws SystemException {
        if (transaction == null) return Status.STATUS_NO_TRANSACTION;
        return transaction.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return transaction;
    }

    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        if (transaction == null) {
            throw new InvalidTransactionException("transaction is null");
        }
        if (!(transaction instanceof MockTransaction)) {
            throw new InvalidTransactionException("Expected instance of MockTransaction but was " + transaction.getClass().getName());
        }
        if (this.transaction != null) throw new IllegalStateException("Transacion in progress");
        this.transaction = (MockTransaction) transaction;
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (transaction == null) throw new IllegalStateException("No transacion in progress");
        transaction.rollback();
        lastTransaction = transaction;
        transaction = null;
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (transaction == null) throw new IllegalStateException("No transacion in progress");
        transaction.setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
    }

    public Transaction suspend() throws SystemException {
        MockTransaction transaction = this.transaction;
        this.transaction = null;
        return transaction;
    }
}
