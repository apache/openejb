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
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
public class MockTransaction implements Transaction {
    public boolean committed;
    public boolean rolledBack;
    public boolean rollbackOnly;

    public void clear() {
        committed = false;
        rolledBack = false;
        rollbackOnly = false;
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
        committed = true;
    }

    public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
        return false;
    }

    public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
        return false;
    }

    public int getStatus() throws SystemException {
        if (rollbackOnly) {
            return Status.STATUS_MARKED_ROLLBACK;
        } else if (committed || rolledBack) {
            return Status.STATUS_NO_TRANSACTION;
        } else {
            return Status.STATUS_ACTIVE;
        }
    }

    public void registerSynchronization(Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
    }

    public void rollback() throws IllegalStateException, SystemException {
        rolledBack = true;
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        rollbackOnly = true;
    }
}
