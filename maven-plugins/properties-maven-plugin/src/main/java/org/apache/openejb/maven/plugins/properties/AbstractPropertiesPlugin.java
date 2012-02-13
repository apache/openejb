package org.apache.openejb.maven.plugins.properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;

public abstract class AbstractPropertiesPlugin extends AbstractMojo {
    /**
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    protected void setProperty(final String key, final String value) {
        session.getUserProperties().setProperty(key, value);
    }

    protected void setPropertyIfNotNull(final String key, final String value) {
        if (value != null) {
            setProperty(key, value);
        }
    }
}
