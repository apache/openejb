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
package org.apache.openejb.spi;


public interface SecurityService extends Service{
    
   /**
    * Check if securityIdentity is authorized to perform the specified action. 
    * This is currently used by OpenEJB to check if a caller is authorized to 
    * to assume at least one of a collection of roles, the roles authorized for 
    * a particular method of a particular deployment.
    */   
    public boolean isCallerAuthorized(Object securityIdentity,String [] roleNames);

    /**
    * Attempts to convert an opaque securityIdentity to a concrete target type.
    * This is currently used to obtain an java.security.Princiapl type which
    * must be returned by OpenEJB when a bean invokes EJBContext.getCallerPrincipal().
    * Conversion to a Principal type must be supported.
    *
    * It may also be used by JCX connectors to obtain the JAAS Subject of the caller,
    * support for translation to Subject type is currently optional.
    *
    */
    public Object translateTo(Object securityIdentity, Class type);
    
    /*
     * Associates a security identity object with the current thread. Setting 
     * this argument to null, will effectively dissociate the thread with a
     * security identity.  This is used when access enterprise beans through 
     * the global JNDI name space. Its not used when calling invoke on a 
     * RpcContainer object.
    */
    public void setSecurityIdentity(Object securityIdentity);
    
    /*
    * Obtains the security identity associated with the current thread.
    * If there is no association, then null is returned. 
    */
    public Object getSecurityIdentity( );
}