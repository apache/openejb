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
package org.openejb.core.stateless;

import javax.ejb.EJBHome;
import javax.naming.NameNotFoundException;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;

/*
  This class is a wrapper for an Intra-VM EJB or Connector references in the 
  JNDI ENC of a entity bean.  When the getObject( ) method is invoked the 
  Operation is checked to ensure that its is allowed for the bean's current state.
*/
public class EncReference extends org.openejb.core.ivm.naming.ENCReference{
    
    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some the OpenEJB global name space. The lookup name is provided, but not the context 
    * because it can be obtained dynamically using OpenEJB.getJNDIContext() method. The 
    * object is not resolved until it's requested.  This is primarily used when constructing
    * the JNDI ENC for a bean.
    */
    public EncReference(String jndiName){
        super(jndiName);
    }
    
    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some other JNDI name space. The context is provided and the lookup name, but the 
    * object is not resolved until it's requested. 
    */
    public EncReference(javax.naming.Context linkedContext, String jndiName){
        super(linkedContext, jndiName);
    }
    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some other JNDI name space, whose initial context is an element of the OpenEJB root 
    * context. To resolve the reference we must first look up the foreign context in the OpenEJB
    * root and then resolve the lookup on that.
    */
    public EncReference(String openEjbContext, String jndiName){
        super(openEjbContext, jndiName);
    }
    
    /*
    * This constructor is used when the object to be reference is available at the time 
    * the reference is created.
    */
    public EncReference(Object reference){
        super(reference);
    }
    
    /*
    * This method is invoked by the ENCReference super class each time its 
    * getObject() method is called within the container system.  This checkOperation
    * method ensures that the stateless bean is in the correct state before the super
    * class can return the requested reference object.
    */
    public void checkOperation(byte operation) throws NameNotFoundException{
	if ( ( jndiName != null ) && jndiName.startsWith("java:openejb") )
	     return;
        if(operation != Operations.OP_BUSINESS){
            throw new NameNotFoundException("Operation Not Allowed");
        }        
    }
    
}
