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
package org.openejb.corba.services.transaction;
import org.omg.PortableInterceptor.Current;

/**
 * This class is used to keep an associtaion between a
 * thread and a transaction. This is required when 
 * underlying communication use different threads for
 * processing invocations. 
 *
 * @version $Revision$ $Date$
 */
public class ThreadTxAssociation
{
  /**
   * Use flag. If set to false, then nothing is done.
   * If set to true, an ORB is in use.
   */ 
  public static boolean useFlag = false;

  /**             
   * OpenORB Initializer Info
   */    
  private static org.omg.CORBA.ORB orb; 

  /**
   * The slotid.
   */
  private static int slotId;

  /**
   * Set the parateters.
   */
  public static void setParams(org.omg.CORBA.ORB _orb, int _slotId)
  {
    orb = _orb;

    slotId  = _slotId;
  } 

  public static int getSlotId()
  {
    return slotId;
  }

  /**
   * Set an association between a SlotId, and a transaction xid.
   */
  public static void setAssociation(javax.transaction.Transaction tx)
  {
      if ( !useFlag )
	 return;

      try
      {
        org.omg.CORBA.Any pctx_any = orb.create_any();
        
        pctx_any.insert_string( tx.toString() );
        
        Current piCurrent = (Current)orb.resolve_initial_references("PICurrent");
       
        piCurrent.set_slot( getSlotId(), pctx_any);
      }
      catch(java.lang.Throwable t)
      {
        t.printStackTrace();
      } 
  }

  /**
   * Free an association between a SlotId, and a transaction xid.
   */
  public static void freeAssociation()
  {
      if ( !useFlag )
         return;
      try
      {
        javax.transaction.TransactionManager txMngr = 
          ((org.openejb.core.TransactionManagerWrapper)org.openejb.OpenEJB.getTransactionManager()).getTxManager();           

        String domainName = (String)org.openejb.OpenEJB.getInitProps().getProperty("domain"); 

        if(domainName==null) domainName = "default";

        tyrex.tm.impl.TransactionDomainImpl txDomain =
          (tyrex.tm.impl.TransactionDomainImpl)tyrex.tm.TransactionDomain.getDomain(domainName);  
 
        javax.transaction.Transaction tx = ((tyrex.tm.TyrexTransactionManager)txMngr).getTransaction();

	if ( tx != null )
	   txMngr.suspend();

        org.omg.CORBA.Any pctx_any = orb.create_any();

        Current piCurrent = (Current)orb.resolve_initial_references("PICurrent");

        piCurrent.set_slot( getSlotId(), pctx_any);
      }
      catch(java.lang.Throwable t)
      {
        t.printStackTrace();
      }
  }

  /**
   * Get a Tx xid from a SlotId.
   * @return null if the SlotId is not found.
   */ 
  public static void getAssociation()
  {
      if ( !useFlag )
         return;
      try
      {
        Current piCurrent = (Current)orb.resolve_initial_references("PICurrent");
        
        org.omg.CORBA.Any pctx_any = piCurrent.get_slot( getSlotId());
        
        if ( ( pctx_any.type().kind().value() == org.omg.CORBA.TCKind._tk_null ) ||
           ( pctx_any.type().kind().value() == org.omg.CORBA.TCKind._tk_void ) )
           return;
 
	String tid = pctx_any.extract_string();
        javax.transaction.TransactionManager txMngr = 
          ((org.openejb.core.TransactionManagerWrapper)org.openejb.OpenEJB.getTransactionManager()).getTxManager();           

        String domainName = (String)org.openejb.OpenEJB.getInitProps().getProperty("domain"); 

        if(domainName==null) domainName = "default";

        tyrex.tm.impl.TransactionDomainImpl txDomain =
          (tyrex.tm.impl.TransactionDomainImpl)tyrex.tm.TransactionDomain.getDomain(domainName);  
 
        javax.transaction.Transaction tx = ((tyrex.tm.TyrexTransactionManager)txMngr).getTransaction(tid);

        if(tx==null)
          return;

        // Enlist the thread
	txMngr.resume( tx ); 
      }
      catch( Exception ex )
      {
        ex.printStackTrace();
      }
  }

}
