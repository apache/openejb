package org.apache.openejb.arquillian.remote;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class RemoteTomEEEJBEnricherExtension implements RemoteLoadableExtension {
    @Override public void register(ExtensionBuilder builder) {
    	// only load if EJB is on ClassPath
        if(Validate.classExists("javax.ejb.EJB")) {
           builder.service(TestEnricher.class, org.apache.openejb.arquillian.remote.RemoteTomEEEnricher.class);         
        }
    }
}
