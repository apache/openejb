package org.openejb.tyrex;
import org.openejb.core.ivm.naming.ENCReference;
import tyrex.resource.Resource;

public class TyrexReference implements org.openejb.core.ivm.naming.Reference {
    ENCReference encReference;
    public TyrexReference(ENCReference ref){
        encReference = ref;
    }
    
    public Object getObject( ) throws javax.naming.NamingException {
	synchronized ( encReference )
	{
		encReference.setChecking(false);
        	Resource resource = (Resource)encReference.getObject();
		encReference.setChecking(true);
        	return resource.getClientFactory();
	}
    }
    
    
}