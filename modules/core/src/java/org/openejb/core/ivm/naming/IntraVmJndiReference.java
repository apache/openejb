package org.openejb.core.ivm.naming;

import javax.naming.NamingException;

import org.openejb.OpenEJB;

/**
 * This class is used when the object to be referenced is accessible through
 * the OpenEJB global name space. 
 * 
 * The lookup name is provided, but not the context because it can be obtained
 * dynamically using OpenEJB.getJNDIContext() method. 
 * 
 * The object is not resolved until it's requested.  
 * 
 * This is primarily used when constructing the JNDI ENC for a bean.
 */
public class IntraVmJndiReference implements Reference{
    
    private String    jndiName;
    
    public IntraVmJndiReference(String jndiName){
        this.jndiName = jndiName;
    }
    
    public Object getObject( ) throws NamingException{
        return OpenEJB.getJNDIContext().lookup( jndiName );
    }
}
