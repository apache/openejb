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
package org.openejb.core.ivm.naming;

import org.openejb.core.ivm.naming.Reference;
import javax.naming.NameNotFoundException;
import org.openejb.core.ThreadContext;
import org.openejb.core.Operations;
import javax.naming.Context;

/*
  This class is a wrapper for an Intra-VM EJB or Connector references in the 
  JNDI ENC of a entity, stateful and stateless beans.  When the getObject( ) method is invoked the 
  Operation is checked to ensure that its is allowed for the bean's current state.
  
  This class is subclassed by ENCReference in the entity, stateful and stateless packages 
  of org.openejb.core.
*/
public abstract class ENCReference implements Reference{
    
    protected Reference ref = null;
    protected boolean checking = true;
    
    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some the OpenEJB global name space. The lookup name is provided, but not the context 
    * because it can be obtained dynamically using OpenEJB.getJNDIContext() method. The 
    * object is not resolved until it's requested.  This is primarily used when constructing
    * the JNDI ENC for a bean.
    */
    public ENCReference(String jndiName){
        this.ref = new IntraVmJndiReference( jndiName );
    }
    
    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some other JNDI name space. The context is provided and the lookup name, but the 
    * object is not resolved until it's requested. 
    */
    public ENCReference(javax.naming.Context linkedContext, String jndiName){
        this.ref = new JndiReference( linkedContext, jndiName );
    }
 
    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some other JNDI name space, whose initial context is an element of the OpenEJB root 
    * context. To resolve the reference we must first look up the foreign context in the OpenEJB
    * root and then resolve the lookup on that.
    */
    public ENCReference(String linkedContextName, String jndiName){
        try{
        javax.naming.Context linkedContext = (javax.naming.Context)org.openejb.OpenEJB.getJNDIContext().lookup(linkedContextName);
        this.ref = new JndiReference( linkedContext, jndiName );
        } catch (javax.naming.NamingException e){
            throw new RuntimeException("The linked context cannot be looked up from the OpenEJB JNDI namespace: Received exception: "+e.getClass().getName() + " : "+e.getMessage());
        }
    }
    
    /*
    * This constructor is used when the object to be reference is available at the time 
    * the reference is created.
    */
    public ENCReference(Object reference){
        this.ref = new ObjectReference( reference );
    }
    
    public void setChecking( boolean value ) {
	checking = value;
    }
    
    /*
    * Obtains the referenced object.
    */
    public Object getObject( ) throws javax.naming.NamingException{
        if( ThreadContext.isValid() ){
            ThreadContext cntx = ThreadContext.getThreadContext();
            byte operation = cntx.getCurrentOperation();
            checkOperation(operation);
        }
        return ref.getObject();
    }
    
    public abstract void checkOperation(byte opertionType) throws NameNotFoundException;
}
