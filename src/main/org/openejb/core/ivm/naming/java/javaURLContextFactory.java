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


package org.openejb.core.ivm.naming.java;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

import org.openejb.core.DeploymentInfo;
import org.openejb.core.ThreadContext;
/**
 * Implements a URL context factory for the <tt>java:</tt> URL. Exposes
 * the environment naming context (<tt>java:/comp</tt>) as a read-only
 * context as per the J2EE container requirements.
 * <p>
 * To use this context factory the JNDI properties file must include
 * the following property:
 * <pre>
 * java.naming.factory.url.pkgs=org.openejb.naming
 * </pre>
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class javaURLContextFactory implements ObjectFactory,  InitialContextFactory {
    
    public Context getInitialContext(Hashtable env) throws NamingException {
        return getContext();
    }
    public Object getObjectInstance( Object obj, Name name, Context nameCtx, Hashtable env )
	throws NamingException {
	    if(obj == null){
	        /* 
	           A null obj ref means the NamingManager is requesting 
	           a Context that can resolve the 'java:' schema
	        */
	        return getContext();
    	}else if(obj instanceof java.lang.String){
    	    String string = (String)obj;
    	    if(string.startsWith("java:comp")||string.startsWith("java:openejb")){
    	        /*
    	         If the obj is a URL String with the 'java:' schema 
    	         resolve the URL in the context of this threads JNDI ENC
    	         */
    	        string = string.substring(string.indexOf(':'));
    	        Context encRoot = getContext();
    	        return encRoot.lookup(string);
    	    }
    	}
    	return null;
    }
    public Object getObjectInstance( Object obj, Hashtable env )
	throws NamingException {
	    return getContext();
	}
	    
    public Context getContext(){
        Context jndiCtx = null;
        
    	if( !ThreadContext.isValid() ){
            return org.openejb.OpenEJB.getJNDIContext();
        }
    	    
        DeploymentInfo di = ThreadContext.getThreadContext().getDeploymentInfo();
        if ( di != null ) {
            return di.getJndiEnc();
        } else {
            return org.openejb.OpenEJB.getJNDIContext();
        }
    }
}
