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
 * $Id: 
 */
package org.openejb.sp.tyrex;


import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.NamingException;

import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.core.ivm.naming.NameNode;
import org.openejb.core.ivm.naming.ParsedName;

import tyrex.resource.Resource;
import tyrex.resource.Resources;
import tyrex.tm.TransactionDomain;


/**
 * jndi name as the tyrex resource id 
 * 
 * @author David Blevins
 * @author Richard Monson-Haefel
 */
public class TyrexResourceFactory implements javax.naming.spi.InitialContextFactory {

    public final static String TX_DOMAIN = "tyrex/tm/TransactionDomain/DOMAIN_FILE";
    
    static IvmContext defaultDomain;

    // Turn into a static class
    public Context getInitialContext(Hashtable env) throws NamingException {
        if (defaultDomain == null) init(env);
        return defaultDomain;
    }

    private void init(Hashtable env)  throws NamingException {
        
        defaultDomain = new IvmContext(new NameNode(null, new ParsedName("default"),null));
        
        // need to create the domain if it does not already exist.
        if(TransactionDomain.getDomain("default") == null ){
            String domainPath = (String)env.get(TX_DOMAIN);
            if(domainPath==null){
                domainPath  = System.getProperty(TX_DOMAIN);
            }
            if(domainPath!=null){
                try{
                TransactionDomain.createDomain(domainPath);
                }catch(tyrex.tm.DomainConfigurationException dce){
                    throw new NamingException ("Failed to create a namespace for Tyrex resource.  Although the "+TX_DOMAIN+" property was set the domain could not be created");
                }
            }
        }

        TransactionDomain td = TransactionDomain.getDomain("default");
        if(td == null){
            //FIXME: log no domain path
            throw new RuntimeException("Failed to create a namespace for Tyrex resource.  The Tyrex \"default\" was not set.");
        }
            
        Resources resources = td.getResources();
        Iterator list = resources.listResources();
        
        while (list.hasNext()) {
            try{
            String resourceName = (String)list.next();
            Resource resource = resources.getResource(resourceName);

            TyrexResourceReference resourceRef = new TyrexResourceReference(resource);
            defaultDomain.bind( resourceName , resourceRef);
            } catch (tyrex.resource.ResourceException re){
                // FIXME: Log this
                re.printStackTrace();
            }
        }
    }
}



class TyrexResourceReference implements org.openejb.core.ivm.naming.Reference {
    
    Resource resource;

    public TyrexResourceReference(Resource resource){
         this.resource = resource;
    }
    
    public Object getObject( ) throws javax.naming.NamingException {
        return resource.getClientFactory();
    }
}
