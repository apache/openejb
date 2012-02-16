package org.apache.openejb.tools.doc.property;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class DocTemplate {
    private static final Logger LOGGER = Logger.getLogger(DocTemplate.class);

    private static final DocTemplate INSTANCE = new DocTemplate();
    private static final String LOG_TAG = DocTemplate.class.getName();
    private static final String BASE = "doc/";

    private VelocityEngine engine;
    private Map<String, URL> resources = new HashMap<String, URL>();

    private DocTemplate() {
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
        properties.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
        properties.setProperty("runtime.log.logsystem.commons.logging.name", LOG_TAG);
        engine.init(properties);
    }

    private void evaluate(String template, final AnnotationInfo key, final List<AnnotationInstanceInfo> value, StringWriter writer) throws IOException {
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

        VelocityContext context = new VelocityContext(context(key, value));
        engine.evaluate(context, writer, LOG_TAG, new InputStreamReader(new BufferedInputStream(url.openStream())));
    }

    private Map context(AnnotationInfo key, List<AnnotationInstanceInfo> value) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", key);
        map.put("values", value);
        map.put("columns", value.isEmpty() ? new ArrayList<String>() : value.iterator().next().getInfo().keySet());
        return map;
    }

    public String apply(String template, AnnotationInfo key, List<AnnotationInstanceInfo> value) {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            evaluate(template, key, value, writer);
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
        return writer.toString();
    }

    public static DocTemplate get() {
        return INSTANCE;
    }
}
