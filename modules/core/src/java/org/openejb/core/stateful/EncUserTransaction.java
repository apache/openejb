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
package org.openejb.core.stateful;

import javax.naming.NameNotFoundException;

import org.openejb.core.Operations;
import org.openejb.core.ivm.naming.ObjectReference;

/*
  This class is a wrapper for CoreUserTransaction reference in the 
  JNDI ENC of a stateful bean.  When the getObject( ) method is invoked the 
  Operation is checked to ensure that its is allowed for the bean's current state.
*/
public class EncUserTransaction extends org.openejb.core.ivm.naming.ENCReference{
    
    /*
    * This constructor take a new CoreUserTransaction object as the object reference
    */
    public EncUserTransaction(org.openejb.core.CoreUserTransaction reference){
        super( new ObjectReference(reference) );
    }
    
    /*
    * This method is invoked by the ENCReference super class each time its 
    * getObject() method is called within the container system.  This checkOperation
    * method ensures that the stateful bean is in the correct state before the super
    * class can return the requested reference object.
    */
    public void checkOperation(byte operation) throws NameNotFoundException{
        if(operation == Operations.OP_SET_CONTEXT || operation == Operations.OP_AFTER_COMPLETION || operation == Operations.OP_BEFORE_COMPLETION ){
            throw new NameNotFoundException("Operation Not Allowed");
        }
        
    }
    
}
