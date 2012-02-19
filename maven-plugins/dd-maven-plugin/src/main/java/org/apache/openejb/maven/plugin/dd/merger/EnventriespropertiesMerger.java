package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class EnventriespropertiesMerger implements Merger<Properties> {
    private final Log log;

    public EnventriespropertiesMerger(final Log logger) {
        log = logger;
    }

    @Override
    public Properties merge(final Properties reference, final Properties toMerge) {
        for (Object key : toMerge.keySet()) {
            if (reference.containsKey(key)) {
                log.warn("property " + key + " found in multiple env-entries.properties, will be overriden");
            }
        }

        reference.putAll(toMerge);
        return reference;
    }

    @Override
    public Properties createEmpty() {
        return new Properties();
    }

    @Override
    public Properties read(URL url) {
        final Properties read = new Properties();
        try {
            read.load(new BufferedInputStream(url.openStream()));
        } catch (IOException e) {
            // ignored
        }
        return read;
    }
}
