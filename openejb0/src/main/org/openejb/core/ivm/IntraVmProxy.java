package org.openejb.core.ivm;

import java.io.ObjectStreamException;

/**
 * All proxies that are created for IntraVM EJBObject and EJBHome implementations
 * must implement this interface.  This interface will result in the handler being notified
   when the proxy is being serialized.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface IntraVmProxy extends java.io.Serializable {

    /**
     * If the proxy is being  copied between bean instances in a RPC
     * call we use the IntraVmArtifact
     * 
     * If the proxy is referenced by a stateful bean that is  being
     * passivated by the container we allow this object to be serialized.
     * 
     * If the proxy is serialized outside the core container system,
     * we allow the application server to handle it.
     * 
     * @return 
     * @exception ObjectStreamException
     */
    public Object writeReplace() throws ObjectStreamException;

}
