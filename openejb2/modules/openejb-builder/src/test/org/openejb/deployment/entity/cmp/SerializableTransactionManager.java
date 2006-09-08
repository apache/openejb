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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * $Id: file,v 1.1 2005/02/18 23:22:00 user Exp $
 */
package org.openejb.deployment.entity.cmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * DO NOT USE THIS FOR A REAL TRANSACTION MANAGER!!!!!!
 *
 * @version $Revision$ $Date$
 */
public class SerializableTransactionManager implements TransactionManager, Serializable {
    private static final long serialVersionUID = 4066474294074568165L;
    private static final List transactionManagers = new ArrayList();

    private transient final TransactionManager transactionManager;
    private final int index;

    public SerializableTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        synchronized (SerializableTransactionManager.transactionManagers) {
            index = SerializableTransactionManager.transactionManagers.size();
            SerializableTransactionManager.transactionManagers.add(transactionManager);
        }
    }

    public void begin() throws NotSupportedException, SystemException {
        transactionManager.begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        transactionManager.commit();
    }

    public int getStatus() throws SystemException {
        return transactionManager.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return transactionManager.getTransaction();
    }

    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        transactionManager.resume(transaction);
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        transactionManager.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        transactionManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int i) throws SystemException {
        transactionManager.setTransactionTimeout(i);
    }

    public Transaction suspend() throws SystemException {
        return transactionManager.suspend();
    }

    protected Object readResolve() {
        synchronized (SerializableTransactionManager.transactionManagers) {
            TransactionManager transactionManager = (TransactionManager) SerializableTransactionManager.transactionManagers.get(index);
            return new SerializableTransactionManager(transactionManager);
        }
    }
}
