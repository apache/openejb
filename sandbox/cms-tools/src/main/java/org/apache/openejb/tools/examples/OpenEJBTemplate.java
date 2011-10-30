package org.apache.openejb.tools.examples;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.CommonsLogLogChute;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Romain Manni-Bucau
 */
public final class OpenEJBTemplate {
    private static final Logger LOGGER = Logger.getLogger(OpenEJBTemplate.class);

    private static final OpenEJBTemplate INSTANCE = new OpenEJBTemplate();
    private static final String LOG_TAG = OpenEJBTemplate.class.getName();
    private static final String BASE = ExamplesPropertiesManager.get().getProperty("velocity");

    // to be used by others classes
    public static final String USER_JAVASCRIPTS = ExamplesPropertiesManager.get().getProperty("javascripts");
    public static final String USER_CSS = ExamplesPropertiesManager.get().getProperty("css");

    private VelocityEngine engine;
    private Map<String, URL> resources = new HashMap<String, URL>();

    private OpenEJBTemplate() {
        // no-op
    }

    public synchronized void init() {
        if (engine != null) {
            return;
        }

        engine = new VelocityEngine();

        Properties properties = new Properties();
        properties.setProperty("file.resource.loader.cache", "true");
        properties.setProperty("resource.loader", "file, class");
        properties.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty("runtime.log.logsystem.class", CommonsLogLogChute.class.getName());
        properties.setProperty("runtime.log.logsystem.commons.logging.name", LOG_TAG);
        engine.init(properties);
    }

    private void evaluate(String template, Map<String, Object> mapContext, Writer writer) throws IOException {
        if (engine == null) {
            init();
        }

        if (!resources.containsKey(template)) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(BASE + template);
            resources.put(template, resource);
        }

        URL url = resources.get(template);
        if (url == null) {
            LOGGER.error("can't find template " + template);
            return;
        }

        VelocityContext context = new VelocityContext(mapContext);
        engine.evaluate(context, writer, LOG_TAG, new InputStreamReader(url.openStream()));
    }

    /**
     * generate a file from a velocity template.
     * <p/>
     * In error case (template not found...), only log with error level will be done (no exception).
     *
     * @param template   the template path in PREFIX resource folder
     * @param mapContext the parameters of the template
     * @param path       the output path
     */
    public void apply(String template, Map<String, Object> mapContext, String path) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
            evaluate(template, mapContext, writer);
        } catch (IOException ioe) {
            LOGGER.error("can't apply template " + template, ioe);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("can't flush file " + path, e);
                }
            }
        }
    }

    public String apply(String template, Map<String, Object> mapContext) {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            evaluate(template, mapContext, writer);
        } catch (IOException ioe) {
            LOGGER.error("can't apply template " + template, ioe);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("can't flush writer", e);
                }
            }
        }
        if (writer == null) {
            return "";
        }
        return writer.toString();
    }

    public static OpenEJBTemplate get() {
        return INSTANCE;
    }
}
