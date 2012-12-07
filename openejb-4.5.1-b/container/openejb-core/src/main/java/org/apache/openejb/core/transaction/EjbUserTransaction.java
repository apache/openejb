/*
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
package org.apache.openejb.core.transaction;

import java.io.Serializable;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.openejb.core.ThreadContext;

public class EjbUserTransaction implements UserTransaction, Serializable {
    private static final long serialVersionUID = 8369364873055306924L;

    public void begin() throws NotSupportedException, SystemException {
        getUserTransaction().begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        getUserTransaction().commit();
    }

    public int getStatus() throws SystemException {
        return getUserTransaction().getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        getUserTransaction().rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        getUserTransaction().setRollbackOnly();
    }

    public void setTransactionTimeout(int i) throws SystemException {
        getUserTransaction().setTransactionTimeout(i);
    }

    private UserTransaction getUserTransaction() throws SystemException {
        // get UserTransaction from BeanTransactionEnvironment in ThreadContext
        ThreadContext callContext = ThreadContext.getThreadContext();
        if (callContext != null) {
            TransactionPolicy txPolicy = callContext.getTransactionPolicy();
            if (txPolicy != null && txPolicy instanceof BeanTransactionPolicy) {
                BeanTransactionPolicy beanTxEnv = (BeanTransactionPolicy) txPolicy;
                return beanTxEnv.getUserTransaction();
            }
        }

        // not in a BeanTransactionEnvironment
        throw new SystemException("Current thread context does not contain a bean-managed transaction environment");
    }
}
