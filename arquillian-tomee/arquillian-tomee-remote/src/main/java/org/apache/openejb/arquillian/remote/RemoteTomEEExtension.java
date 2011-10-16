package org.apache.openejb.arquillian.remote;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class RemoteTomEEExtension implements LoadableExtension {
    @Override public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, RemoteTomEEContainer.class)
            .service(TestEnricher.class, RemoteTomEEEnricher.class);
    }
}
