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

/**
 * This class is a coordinator that manages XA Resources.
 * 
 * @author	Jerome DANIEL
 * @version	1.0
 */
public class XACoordinator extends org.omg.CosTransactions.ResourcePOA
{
	/**
	 * Reference to the POA
	 */
	private org.omg.PortableServer.POA _poa;
									   
	/**
	 * XA resources list
	 */
	private java.util.Vector _xa;
	
	/**
	 * Reference to the XA XID
	 */
	private javax.transaction.xa.Xid _xid;
	
	/**
	 * Prepared XA resources
	 */
	private java.util.Vector _prepared;
	
	/**
	 * Heuristic XA resources
	 */
	private java.util.Vector _heuristic;
	
	/**
	 * Is the transaction prepared
	 */
	private boolean _prepared_transaction;
	
	/**
	 * Constructor
	 */
	public XACoordinator( org.omg.PortableServer.POA poa,  org.omg.CosTransactions.otid_t ots_xid )
	{
		_poa = poa;
		
		_xa = new java.util.Vector();
		
		_xid = new XID( ots_xid );				
		
		_prepared = new java.util.Vector();
		
		_heuristic = new java.util.Vector();
		
		_prepared_transaction = false;
	}
	
	/**--------
	//
	// Resource interface implementation
	//
	/**--------
		
	/**
	 * First phase of the 2PC
	 */
	public org.omg.CosTransactions.Vote prepare()
		throws org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
	{
		javax.transaction.xa.XAResource res = null;
		boolean error = false;
		
		org.omg.CosTransactions.Vote global_vote = org.omg.CosTransactions.Vote.VoteReadOnly;
				
		_prepared.removeAllElements();
		
		for ( int i=0; i<_xa.size(); i++ )
		{
			res = ( javax.transaction.xa.XAResource ) _xa.elementAt( i );
			
			if ( error )
			{
				try
				{
					res.rollback( _xid );
				}
				catch ( javax.transaction.xa.XAException ex )
				{
					// Nothing to do...
				}
			}
			else
			{
				try
				{
					switch ( res.prepare( _xid ) )
					{
					case javax.transaction.xa.XAResource.XA_OK :
						_prepared.addElement( res );
						global_vote = org.omg.CosTransactions.Vote.VoteCommit;
						break;
					case javax.transaction.xa.XAResource.XA_RDONLY :
						// Nothing to do...
						break;
					}
				}
				catch ( javax.transaction.xa.XAException ex )
				{
					error = true;
					global_vote = org.omg.CosTransactions.Vote.VoteRollback;		
				}
			}

		}
		
		_prepared_transaction = true;
		
		if ( global_vote.value() == org.omg.CosTransactions.Vote._VoteRollback )
		{
			try
			{			
				rollback_after_prepare();
			}
			catch ( org.omg.CosTransactions.HeuristicCommit ex )
			{ 
				throw new org.omg.CosTransactions.HeuristicMixed();
			}
		}
		
		return global_vote;
	}

	/**
	 * Rollback the transaction ( 2nd phase of 2PC )
	 */
	public void rollback()
		throws org.omg.CosTransactions.HeuristicCommit, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
	{
		javax.transaction.xa.XAResource res = null;
		boolean heuristic_commit = false;
		
		_prepared_transaction = false;
		
		_heuristic.removeAllElements();
		
		for ( int i=0; i<_xa.size(); i++ )
		{
			res = ( javax.transaction.xa.XAResource ) _xa.elementAt( i );
			
			try
			{
				res.rollback( _xid );
			}
			catch ( javax.transaction.xa.XAException ex )
			{
				if ( ex.errorCode == javax.transaction.xa.XAException.XA_HEURCOM )
				{
					heuristic_commit = true;
					_heuristic.addElement( res );
				}
			}
		}				
		
		if ( heuristic_commit )
			throw new org.omg.CosTransactions.HeuristicCommit();			
		
		complete();
	}

	/**
	 * Commit a transaction ( 2nd phase of 2PC )
	 */
	public void commit()
		throws org.omg.CosTransactions.NotPrepared, org.omg.CosTransactions.HeuristicRollback, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
	{
		javax.transaction.xa.XAResource res = null;
		boolean error = false;				
		boolean committed = false;
		boolean rolledback = false;
		
		if ( !_prepared_transaction )
			throw new org.omg.CosTransactions.NotPrepared();
		
		for ( int i=0; i<_prepared.size(); i++ )
		{
			res = ( javax.transaction.xa.XAResource ) _prepared.elementAt( i );
			
			if ( ( error ) && ( !committed ) )
			{
				try
				{
					res.rollback( _xid );
					
					rolledback = true;
				}
				catch ( javax.transaction.xa.XAException ex )
				{
					_heuristic.addElement( res );
					
					if ( rolledback == false )
						committed = true;										
				}
			}
			else
			{
				try
				{
					res.commit( _xid, false );
					committed = true;
				}
				catch ( javax.transaction.xa.XAException ex )
				{
					_heuristic.addElement( res );
					
					error = true;						
				}
			}

		}
		
		_prepared.removeAllElements();				
		
		if ( ( committed ) && ( rolledback ) )
			throw new org.omg.CosTransactions.HeuristicMixed();
		
		if ( rolledback )	
			throw new org.omg.CosTransactions.HeuristicRollback();
		
		if ( error )
			throw new org.omg.CosTransactions.HeuristicHazard();
		
		complete();
	}

	/**
	 * Commit one phase
	 */
	public void commit_one_phase()
		throws org.omg.CosTransactions.HeuristicHazard
	{
		_prepared_transaction = false;
		
		if ( _xa.size() > 0 )
		{
			javax.transaction.xa.XAResource res = ( javax.transaction.xa.XAResource ) _xa.elementAt( 0 );
			
			try
			{
				res.commit( _xid, true );
			}
			catch ( javax.transaction.xa.XAException ex )
			{ 
				_heuristic.addElement( res );
				
				throw new org.omg.CosTransactions.HeuristicHazard();
			}
		}
				
		complete();
	}

	/**
	 * Forget a transaction
	 */
	public void forget()
	{
		_prepared_transaction = false;		
		
		javax.transaction.xa.XAResource res = null;						
		
		for ( int i=0; i<_heuristic.size(); i++ )
		{
			res = ( javax.transaction.xa.XAResource ) _heuristic.elementAt( i );
			
			try
			{
				res.rollback( _xid );
			}
			catch ( javax.transaction.xa.XAException ex )
			{
				// Nothing to do !
			}
		}
		
		_heuristic.removeAllElements();		
		
		complete();
	}
	
	/**--------
	//
	// Implementation specific operations
	//
	/**--------
	
	/**
	 * This operation is used to rollback all prepared XA resources.
	 */
	public void rollback_after_prepare()
		throws org.omg.CosTransactions.HeuristicCommit, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
	{
		javax.transaction.xa.XAResource res = null;
		boolean heuristic_commit = false;
		
		_prepared_transaction = false;
		
		for ( int i=0; i<_prepared.size(); i++ )
		{
			res = ( javax.transaction.xa.XAResource ) _prepared.elementAt( i );
			
			try
			{
				res.rollback( _xid );
			}
			catch ( javax.transaction.xa.XAException ex )
			{
				if ( ex.errorCode == javax.transaction.xa.XAException.XA_HEURCOM )
				{
					heuristic_commit = true;
					_heuristic.addElement( res );
				}
			}
		}
		
		_prepared.removeAllElements();
		
		if ( heuristic_commit )
			throw new org.omg.CosTransactions.HeuristicCommit();			
		
		complete();
	}
	
	/**
	 * Add an XA resource.
	 */
	public void registerXAResource( javax.transaction.xa.XAResource resource )
	{
		boolean already = false;
		
		if ( !_xa.contains( resource ) )
			_xa.addElement( resource );
		else
			already = true;
		
		//
		// Send XA START to the resource
		//
		try
		{
			if  ( already )
				resource.start( _xid, javax.transaction.xa.XAResource.TMJOIN );
			else
				resource.start( _xid, javax.transaction.xa.XAResource.TMNOFLAGS );
		}
		catch ( javax.transaction.xa.XAException ex )
		{
			System.out.println("OpenORB OTS warning XACoordinator : " + ex.toString() );
			
			throw new org.omg.CORBA.INTERNAL();
		}
	}
	
	/**
	 * End the usage of an XA resource
	 */
	public void endXAResource( javax.transaction.xa.XAResource resource, boolean success )
	{
		try
		{
			if ( success )
				resource.end( _xid, javax.transaction.xa.XAResource.TMSUCCESS );
			else
				resource.end( _xid, javax.transaction.xa.XAResource.TMFAIL );
		}
		catch ( javax.transaction.xa.XAException ex )
		{
			System.out.println("OpenORB OTS warning XACoordinator : " + ex.toString() );
			
			throw new org.omg.CORBA.INTERNAL();
		}
	}
	
	/**
	 * Unregister the XA coordinator from POA
	 */
	private void complete()
	{
		try
		{
			byte [] id = _poa.servant_to_id( this );
			
			_poa.deactivate_object( id );
		}
		catch ( java.lang.Exception ex )
		{
			System.out.println("OpenORB OTS warning XACoordinator : " + ex.toString() );
			
			throw new org.omg.CORBA.INTERNAL();
		}
	}
						  
}
