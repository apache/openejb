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

import org.omg.CosTransactions.TransactionFactory;

/**
 * This class is the OTS server class started as a local resource. It provides the ability to use
 * Tyrex as an OTS server. 
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version 1.0
 */
public class LocalTyrex
{
 
   /**
    * Reference to the transaction domain
    */
   private tyrex.tm.TransactionDomain txDomain = null;
    
   /**
    * Reference to the ORB
    */
   private org.omg.CORBA.ORB orb;
   
   /**
    * Tyrex domain name
    */
   private String domain;
   
   /**
    * Bind the Tyrex TM to the Naming Service or not
    */  
   private boolean doNamingBind = false; 
   
   /**
    * Constructor. DO the recovery and other initialization here,
    * so that other components can use it right away.
    */
   public LocalTyrex(  org.omg.CORBA.ORB orb, String domain, boolean doNamingBind ) throws org.openejb.OpenEJBException
   {
      this.orb = orb;
      this.domain = domain;
      this.doNamingBind = doNamingBind;

      TransactionFactory ots = createTransactionFactory(orb);

      // Bind the TransactionService into the ns
      if( doNamingBind )
          bind_to_naming_service( ots, orb );
   }
   
   /**
    * Creates the transaction factory
    */
   private TransactionFactory createTransactionFactory(org.omg.CORBA.ORB orb ) 
     throws org.openejb.OpenEJBException 
   {
     //
     // Gets the default transaction domain
     //
     try {
    	  txDomain = tyrex.tm.TransactionDomain.createDomain( domain );
     }
     catch( java.lang.Exception ex ) {
         throw new org.openejb.OpenEJBException(ex);
     }       

     //
     // Identifies the ORB
     //
     
     ((tyrex.tm.impl.TransactionDomainImpl)txDomain).identifyORB( orb, null, null );
     
     //
     // Check for recovery
     //
             
      try {
      	txDomain.recover();
      }
      catch ( tyrex.tm.RecoveryException ex ) {
	  System.out.println("LocalTyrex - Unable to complete the recovery : " + ex );
      }
         
     
     //
     // Gets the transaction factory
     //
     
     TransactionFactory ots = txDomain.getTransactionFactory();
     
     //
     // Connect the transaction factory object
     //
     
     orb.connect( ots );
     
     return ots;
    }
    
   /**
    * This operation binds the following object to naming service
    */
   private void bind_to_naming_service( org.omg.CORBA.Object tf, org.omg.CORBA.ORB orb )
   {
      org.omg.CosNaming.NamingContext naming = null;
      try
      {
         org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
         
         naming = org.omg.CosNaming.NamingContextHelper.narrow( obj );
      }
      catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
      {
         System.out.println("LocalTyrex - Unable to resolve NameService");
      }
      
      org.omg.CosNaming.NamingContext tyrex_ots = getNamingContext( naming, "Tyrex" );
      
      org.omg.CosNaming.NameComponent [] tf_name = new org.omg.CosNaming.NameComponent[ 1 ];
      
      tf_name[0] = new org.omg.CosNaming.NameComponent();
      tf_name[0].id = "TransactionFactory";
      tf_name[0].kind = "";
      
      try
      {
         tyrex_ots.resolve( tf_name );
      }
      catch ( java.lang.Exception ex )
      {
         try
         {
             tyrex_ots.bind( tf_name, tf );
         }
         catch ( java.lang.Exception exi )
         {
             System.out.println("LocalTyrex - Unable to bind the transaction factory to naming service.");
         }
         
         return;
      }
      
      try
      {
         tyrex_ots.rebind( tf_name, tf );
      }
      catch ( java.lang.Exception ex )
      {
         System.out.println("LocalTyrex - Unable to rebind the transaction factory to naming service.");
      }
   }
   
   /**
    * Returns a naming context. If this naming context does not exist, we create it.
    */
   private org.omg.CosNaming.NamingContext getNamingContext( org.omg.CosNaming.NamingContext parent, String id )
   {
      org.omg.CosNaming.NameComponent [] name = new org.omg.CosNaming.NameComponent[ 1 ];
      
      name[0] = new org.omg.CosNaming.NameComponent();
      name[0].id = id;
      name[0].kind = "";
      
      org.omg.CORBA.Object obj = null;
      try
      {
         obj = parent.resolve( name );
         
         return org.omg.CosNaming.NamingContextHelper.narrow( obj );
      }
      catch ( java.lang.Exception ex )
      {
         try
         {
             return parent.bind_new_context( name );
         }
         catch ( java.lang.Exception exi )
         {
             System.out.println("LocalTyrex - Unable to create a naming context.");
         }
      }
      
      return null;
   }
    
}
