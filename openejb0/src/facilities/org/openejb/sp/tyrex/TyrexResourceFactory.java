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
