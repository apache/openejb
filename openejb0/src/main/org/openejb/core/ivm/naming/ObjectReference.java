package org.openejb.core.ivm.naming;

import javax.naming.NamingException;

/**
 * This class is used when the object to be reference is available at the time
 * the reference is created.
 */
public class ObjectReference implements Reference{
    
    private Object obj;

    public ObjectReference(Object obj){
        this.obj = obj;
    }
    
    public Object getObject( ) throws NamingException{
        return obj;
    }
}
