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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.core;


import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;


/**
 * Implements the bean's {@link UserTransaction} interface into the
 * transaction monitor. A bean should only obtain access to this
 * interface if the transaction is bean managed. This interface
 * prevents the bean from marking the transaction as roll back only
 * as per the EJB specification.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @author <a href="richard@monson-haefel.com">Richard Monson-Haefel</a>
 * @version $Revision$ $Date$
 */
public class CoreUserTransaction
    implements javax.transaction.UserTransaction, java.io.Serializable
{


    /**
     * Holds a reference to the underlying transaction manager.  If the CoreUserTransction
     * is serialized during Stateful bean passivation, the reconstituted instance will simply
     * use the static _txManager, so serializablity per EJB 1.1 section 6.4.1 is assured.
     */
    private static TransactionManager  _txManager;


    /**
     * Private constructor for singlton.
     */
    public CoreUserTransaction(TransactionManager txMngr)
    {
        _txManager = txMngr;
    }
    public CoreUserTransaction( ){
    }
    
    private TransactionManager transactionManager(){
        if(_txManager == null){
            _txManager = org.openejb.OpenEJB.getTransactionManager();
        }
        return _txManager;
    }

    public void begin()
        throws NotSupportedException, SystemException
    {
        // The logic for user transaction is to never support nested
        // transactions, regardless of what the transaction server
        // can do.
        if ( transactionManager().getTransaction() != null )
            throw new NotSupportedException( "Nested transactions not supported in beans" );
        transactionManager().begin();
    }


    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
               SecurityException, IllegalStateException, SystemException
    {
        transactionManager().commit();
    }


    public void rollback()
        throws IllegalStateException, SecurityException, SystemException
    {
        transactionManager().rollback();
    }


    public int getStatus()
        throws SystemException
    {
        return transactionManager().getStatus();
    }


    public void setRollbackOnly()throws javax.transaction.SystemException
    {
        transactionManager().setRollbackOnly();
    }


    public void setTransactionTimeout( int seconds  )
        throws SystemException
    {
        transactionManager().setTransactionTimeout( seconds );
    }


}