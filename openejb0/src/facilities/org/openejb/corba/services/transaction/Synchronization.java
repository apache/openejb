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
 * This class is a coordinator that manages a synchronization object.
 * 
 * @author	Olivier MODICA
 * @version	1.0
 */
public class Synchronization extends org.omg.CosTransactions.SynchronizationPOA
{
	
        
        /**
	 * Reference to the POA
	 */
	private org.omg.PortableServer.POA _poa;
									   
	/**
	 * Reference to the javax.transaction.Synchronization
	 */
	private javax.transaction.Synchronization _synch;
	
	/**
	 * Constructor
	 */
	public Synchronization( org.omg.PortableServer.POA poa,  javax.transaction.Synchronization synch )
	{
		_poa = poa;
		
		_synch = synch;
		
        }
	
	/**--------
	//
	// Resource interface implementation
	//
	/**--------
		
	/**
	 * BeforeCompletion
	 */
	public void before_completion()
        {
          _synch.beforeCompletion();
        }

	/**
	 * AfterCompletion
	 */
	public void after_completion( org.omg.CosTransactions.Status status )
        {
	  _synch.afterCompletion( fromOTSStatus(status) );
		
	}
        
         /**
     * Convert OTS transaction statuc code into JTA transaction code.
     */
    static int fromOTSStatus( org.omg.CosTransactions.Status status )
    {
        switch ( status.value() ) {
        case org.omg.CosTransactions.Status._StatusActive:
            return javax.transaction.Status.STATUS_ACTIVE;
        case org.omg.CosTransactions.Status._StatusMarkedRollback:
            return javax.transaction.Status.STATUS_MARKED_ROLLBACK;
        case org.omg.CosTransactions.Status._StatusCommitting:
            return javax.transaction.Status.STATUS_COMMITTING;
        case org.omg.CosTransactions.Status._StatusCommitted:
            return javax.transaction.Status.STATUS_COMMITTED;
        case org.omg.CosTransactions.Status._StatusRollingBack:
            return javax.transaction.Status.STATUS_ROLLING_BACK;    
        case org.omg.CosTransactions.Status._StatusRolledBack:
            return javax.transaction.Status.STATUS_ROLLEDBACK;  
        case org.omg.CosTransactions.Status._StatusPrepared:
            return javax.transaction.Status.STATUS_PREPARED;  
        case org.omg.CosTransactions.Status._StatusPreparing:
            return javax.transaction.Status.STATUS_PREPARING; 
        case org.omg.CosTransactions.Status._StatusNoTransaction:
            return javax.transaction.Status.STATUS_NO_TRANSACTION; 
        case org.omg.CosTransactions.Status._StatusUnknown:
        default:
          return javax.transaction.Status.STATUS_UNKNOWN; 
      }
    }
                                                            							  
}
