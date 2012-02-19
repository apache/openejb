package org.apache.openejb.maven.plugins.properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @goal copy-settings-proxy
 */
public class ProxySettingsPropertiesMojo extends AbstractMojo {
    /**
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Collection<String> alreadySetProtocols = new ArrayList<String>();
        for (Proxy proxy : settings.getProxies()) {
            if (alreadySetProtocols.contains(proxy.getProtocol())) {
                getLog().info("ignoring proxy " + toString(proxy) + " because another one is already set for this protocol");
                continue;
            }

            if (proxy.isActive()) {
                setProxy(proxy);
                alreadySetProtocols.add(proxy.getProtocol());
            }
        }
    }

    private void setProxy(final Proxy proxy) {
        final String prefix;
        if (proxy.getProtocol() == null) {
            prefix = "";
        } else {
            prefix = proxy.getProtocol() + ".";
        }

        setPropertyIfNotNull(prefix + "proxyHost", proxy.getHost());
        setPropertyIfNotNull(prefix + "proxyPort", Integer.toString(proxy.getPort()));
        setPropertyIfNotNull(prefix + "proxyPassword", proxy.getPassword());
        setPropertyIfNotNull(prefix + "proxyUser", proxy.getUsername());
        setPropertyIfNotNull(prefix + "nonProxyHosts", proxy.getNonProxyHosts());

        getLog().info("set proxy " + toString(proxy));
    }

    protected void setProperty(final String key, final String value) {
        session.getUserProperties().setProperty(key, value);
    }

    protected void setPropertyIfNotNull(final String key, final String value) {
        if (value != null) {
            setProperty(key, value);
        }
    }

    private static String toString(final Proxy proxy) {
        return new StringBuilder(proxy.getId())
                .append("[")
                .append(proxy.getProtocol()).append("://")
                .append(proxy.getHost()).append(":").append(proxy.getPort())
                .append("]")
                .toString();
    }
}
