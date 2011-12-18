package org.apache.openejb.tools.examples;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public final class ExamplesPropertiesManager {
    private static final Logger LOGGER = Logger.getLogger(ExamplesPropertiesManager.class);
    private static final String TEMPLATE_COMMON_PROPERTIES = "generate-index/config.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        URL propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(TEMPLATE_COMMON_PROPERTIES);
        try {
            PROPERTIES.load(propertiesUrl.openStream());
        } catch (IOException e) {
            LOGGER.error("can't read common properties, please put a " + TEMPLATE_COMMON_PROPERTIES + " file");
        }
    }

    private ExamplesPropertiesManager() {
        // no-op
    }

    public static Properties get() {
        return PROPERTIES;
    }
}
