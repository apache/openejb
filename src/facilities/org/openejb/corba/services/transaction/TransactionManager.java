/**
 * Redistribution and use of this software and associated
 * documentation ("Software"), with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements
 *    and notices.  Redistributions must also contain a copy of this
 *    document.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Inc.  For written permission, please
 *    contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Inc. Exolab is a registered trademark of
 *    Intalio Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Copyright 1999-2001 (c) Intalio,Inc. All Rights Reserved.
 *
 * $Id$
 *
 * Date         Author  Changes
 */
package org.openejb.corba.services.transaction;

import org.openejb.corba.util.Verbose;

/**
 * This class is an adaptator between the JTA API and the OTS API. This class is used by OpenEJB
 * to manage transactions, all invocations are transmit to an OTS implementation.
 * 
 * @author	Jerome Daniel ( jdaniel@intalio.com )
 */
public class TransactionManager implements javax.transaction.TransactionManager
{
	/**
	 * Suspended transactions
	 */
	private java.util.Hashtable _suspended;
	
	/**
	 * Reference to the ORB
	 */
	private org.omg.CORBA.ORB _orb;
	
	/**
	 * Reference to the POA
	 */
	private org.omg.PortableServer.POA _poa;
	 
	/**
	 * Constructor.
	 */
	public TransactionManager( org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa )
	{
		_orb = orb;
		_poa = poa;
		
		_suspended = new java.util.Hashtable();
	}

	/**
	 * Create a new transaction
	 */
	public void begin() throws javax.transaction.NotSupportedException, javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager::begin", "Begin a new transaction" );
		
		try
		{
			current().begin();
		
			
			}
		catch ( org.omg.CosTransactions.SubtransactionsUnavailable ex )
		{
			throw new javax.transaction.NotSupportedException();
		}
		catch ( java.lang.Exception ex )
		{
			 
			Verbose.exception( "TransactionManager::begin", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Commit the current transaction
	 */
	public void commit() throws javax.transaction.RollbackException, 
								javax.transaction.HeuristicMixedException,
								javax.transaction.HeuristicRollbackException,
								java.lang.SecurityException,
								java.lang.IllegalStateException,
								javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager::commit", "Commit a transaction" );
		
		try
		{
			current().commit( true );
		}
		catch ( org.omg.CosTransactions.HeuristicMixed ex )
		{
			throw new javax.transaction.HeuristicMixedException();
		}
		catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK ex )
		{
			throw new javax.transaction.RollbackException();
		}		
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::commit", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Return the transaction status.
	 */
	public int getStatus() throws javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager", "Get status" );
		
		try
		{
			int status = current().get_status().value();
		
			Verbose.print( "TransactionManager", "status = " + status );
			
			return status;
		}
		catch ( org.omg.CORBA.SystemException ex )
		{
	 		return org.omg.CosTransactions.Status._StatusNoTransaction;
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::getStatus", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Return the current transaction object.
	 */
	public javax.transaction.Transaction getTransaction() throws javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager", "Get current transaction" );
		
		try
		{
			if ( getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION )
				return null;

			return new org.openejb.corba.services.transaction.Transaction( current(), _poa, _orb );
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::getTransaction", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Resume a suspended transaction.
	 */
	public void resume( javax.transaction.Transaction t ) throws javax.transaction.InvalidTransactionException,
																 java.lang.IllegalStateException,
																 javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager::resume", "Resume a transaction" );
		
		try
		{
			org.omg.CosTransactions.Control ctrl = ( org.omg.CosTransactions.Control ) _suspended.get( t );
			
			if ( ctrl == null )
				throw new java.lang.IllegalStateException();
			
			current().resume( ctrl );
			
			_suspended.remove( ctrl );
						
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::resume", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Rollback a transaction
	 */
	public void rollback() throws java.lang.IllegalStateException,
								  java.lang.SecurityException,
								  javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager::rollback", "Rollback a transaction" );
		
		try
		{
			current().rollback();
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::rollback", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Set the current transaction to be only rolledback
	 */
	public void setRollbackOnly() throws java.lang.IllegalStateException,
										 javax.transaction.SystemException
	{
		try
		{
			current().rollback_only();
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::setRollbackOnly", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
				
	}
	
	/**
	 * Set the transaction timeout.
	 */
	public void setTransactionTimeout( int seconds ) throws javax.transaction.SystemException
	{
		try
		{
			current().set_timeout( seconds );
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::setTransactionTimeout", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Suspend the current transaction
	 */
	public javax.transaction.Transaction suspend() throws javax.transaction.SystemException
	{
		Verbose.print( "TransactionManager::suspend", "Suspend a transaction" );
		
		javax.transaction.Transaction t = getTransaction();
		
		try
		{
			org.omg.CosTransactions.Control ctrl = current().suspend();
			
			if ( ctrl == null )
				return null;
			
			_suspended.put( t, ctrl );
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "TransactionManager::suspend", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
		
			
		return t;
	}
	
	/**
	 * Return the current object ( a current object is associated to the calling thread ).
	 */
	private org.omg.CosTransactions.Current current()
	{
		try
		{
			return org.omg.CosTransactions.CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );
		}
		catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
		{
			Verbose.fatal( "TransactionManager::Current", "Unable to retrieve the transaction current object");
			
			return null;
		}
	}
}
 