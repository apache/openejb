package org.apache.openejb.arquillian.embedded;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * @author rmannibucau
 */
public class EmbeddedTomEEExtension implements LoadableExtension {
    @Override public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, EmbeddedTomEEContainer.class)
            .service(TestEnricher.class, EmbeddedTomEEEnricher.class);
    }
}
