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

package org.openejb.tyrex;

public class TyrexDeploymentInfo extends org.openejb.core.DeploymentInfo {
    
    private tyrex.tm.RuntimeContext runtimeContext;
    
    private void newRuntimeContext(javax.naming.Context jndiEnc){
        try{
          tyrex.naming.MemoryContextFactory factory = new tyrex.naming.MemoryContextFactory();
          
          javax.naming.Context tyrexEnc = factory.getInitialContext( new java.util.Hashtable() );
          
          tyrexEnc.bind( "java:comp", jndiEnc.lookup("java:comp") );
          tyrexEnc.bind( "comp", jndiEnc.lookup("java:comp") );
          runtimeContext = tyrex.tm.RuntimeContext.newRuntimeContext(tyrexEnc, null);

        
        }catch(javax.naming.NamingException ne){
            ne.printStackTrace();
            // serious problem here. Can't set the JNDI ENC, can't run the program.
            throw new RuntimeException("Can not set the JNDI context on the DeploymentInfo = "+this.getDeploymentID());
        }
    }
    
    public TyrexDeploymentInfo(javax.naming.Context jndiEnc, Object did, String homeClassName, String remoteClassName, String beanClassName, String pkClassName, byte componentType)
    throws org.openejb.SystemException{
        
        super(did,homeClassName,remoteClassName, beanClassName, pkClassName, componentType);
      
        newRuntimeContext(jndiEnc);
    }
    
    public TyrexDeploymentInfo(javax.naming.Context jndiEnc, Object did, Class homeClass, Class remoteClass, Class beanClass, Class pkClass, byte componentType)
    throws org.openejb.SystemException{
        
        super(did,homeClass,remoteClass, beanClass, pkClass, componentType);
      
        newRuntimeContext(jndiEnc);
    }
    
    public TyrexDeploymentInfo(javax.naming.Context jndiEnc,Object did, String homeClassName, String remoteClassName, String beanClassName, String pkClassName, byte componentType, ClassLoader loader)
    throws org.openejb.SystemException{
        super(did,homeClassName,remoteClassName, beanClassName, pkClassName, componentType, loader);
        newRuntimeContext(jndiEnc);
    }
    public tyrex.tm.RuntimeContext getRuntimeContext(){
        return runtimeContext;
    }
    public javax.naming.Context getJndiEnc( ){
        return runtimeContext.getEnvContext();
    }
    public void setJndiEnc(javax.naming.Context cntx){
        throw new UnsupportedOperationException("The runtime context is set automatically when TyrexDeploymentInfo is create");
    }
    
}
