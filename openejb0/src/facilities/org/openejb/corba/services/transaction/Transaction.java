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
 * This class represents a transaction. It gives the ability to register or unregister an XA resource, but also the 
 * possibility to demarcate a transaction.
 * 
 * Restriction : to be able to manage XA resource from this class, it is required to use OpenORB OTS that provides
 * a JTA compatibility ( support of javax.transaction.xa.XAResource )
 * 
 * @author	Jerome Daniel ( jdaniel@intalio.com )
 */
public class Transaction implements javax.transaction.Transaction
{
	/**
	 * Reference to the current object
	 */
	private org.omg.CosTransactions.Current current;
	
	/**
	 * Reference to the POA
	 */
	private org.omg.PortableServer.POA poa;

	/**
	 * Reference to the ORB
	 */
	private org.omg.CORBA.ORB orb;
	
	/**
	 * Reference to the XA Coordinator
	 */
	private XACoordinator coordinator;
        
     	/**
	 * Constructor
	 */
	public Transaction( org.omg.CosTransactions.Current curr, org.omg.PortableServer.POA poa, org.omg.CORBA.ORB orb )
	{
		current = curr;
	
		this.poa = poa;
		
		this.orb = orb;
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
		Verbose.print("Transaction", "commit");
		
		try
		{
		  current.commit( true );
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
			Verbose.exception( "Transaction::commit", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Delist a resource from the current transaction
	 */
	public boolean delistResource( javax.transaction.xa.XAResource resource, int flag ) throws java.lang.IllegalStateException,
																							javax.transaction.SystemException
	{
		try
		{
			if ( coordinator == null )
				throw new java.lang.IllegalStateException();
			
			if ( flag == javax.transaction.xa.XAResource.TMSUCCESS )
				coordinator.endXAResource( resource, true );
			else
				coordinator.endXAResource( resource, false );
			
			return true;
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "Transaction::delistResource", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Enlist a new resource for the current transaction.
	 */
	public boolean enlistResource( javax.transaction.xa.XAResource resource ) throws  javax.transaction.RollbackException,
																				      java.lang.IllegalStateException,
																				      javax.transaction.SystemException
	{
		try
		{
			if ( coordinator == null )
			{
				coordinator = new XACoordinator( poa, current.get_control().get_coordinator().get_txcontext().current.otid );
				
				poa.activate_object( coordinator );
				
				org.omg.CosTransactions.Resource coordinator_ref = org.omg.CosTransactions.ResourceHelper.narrow( poa.servant_to_reference( coordinator ) );
				
				current.get_control().get_coordinator().register_resource( coordinator_ref );
			}
			
			coordinator.registerXAResource( resource );
			
			return true;
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "Transaction::enlistResource", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Return the transaction status.
	 */
	public int getStatus() throws javax.transaction.SystemException
	{		
		try
		{
			return current.get_status().value();
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "Transaction::getStatus", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Register a new synchronization object
	 */
	public void registerSynchronization( javax.transaction.Synchronization synchro ) throws javax.transaction.RollbackException,
																							java.lang.IllegalStateException,
																							javax.transaction.SystemException
	{
          try {
            Synchronization s = new Synchronization( poa, synchro );
            
            poa.activate_object( s );
				
            org.omg.CosTransactions.Synchronization s_ref = org.omg.CosTransactions.SynchronizationHelper.narrow( poa.servant_to_reference( s ) );
	    
            current.get_control().get_coordinator().register_synchronization( s_ref );
          }
          catch(Exception ex) { throw new javax.transaction.SystemException(ex.getMessage()); }			
	  
        }  
        
        	
	/**
	 * Rollback a transaction
	 */
	public void rollback() throws java.lang.IllegalStateException,
								  java.lang.SecurityException,
								  javax.transaction.SystemException
	{
		Verbose.print("Transaction", "rollback");
		
		try
		{
                        current.rollback();
        
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "Transaction::rollback", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
	}
	
	/**
	 * Set the current transaction to be only rolledback
	 */
	public void setRollbackOnly() throws java.lang.IllegalStateException,
										 javax.transaction.SystemException
	{
		Verbose.print("Transaction", "setRollbackOnly");
		
		try
		{
			current.rollback_only();
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "Transaction::setRollbackOnly", "Unexpected exception", ex );
			
			throw new javax.transaction.SystemException();
		}
				
	}
	
	public boolean equals( java.lang.Object obj ) {
	 
	  try {
			  Transaction T = (Transaction)obj;
			  
			  return current.get_control().get_coordinator().is_same_transaction( T.current.get_control().get_coordinator() );
		 }
		 catch ( java.lang.Throwable ex ) {}
		 
		 return false;
	 
	} 

}
