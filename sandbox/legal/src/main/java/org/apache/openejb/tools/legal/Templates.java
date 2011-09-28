/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tools.legal;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.CommonsLogLogChute;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Templates {
    private static final Logger LOGGER = Logger.getLogger(Templates.class);

    private static final Templates INSTANCE = new Templates();
    private static final String LOG_TAG = Templates.class.getName();
    private static final String BASE = "legal/";

    private VelocityEngine engine;
    private Map<String, URL> resources = new HashMap<String, URL>();

    private Templates() {
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

    public static Templates get() {
        return INSTANCE;
    }

    public static Builder template(String name) {
        return get().new Builder(name);
    }

    public class Builder {
        private final String template;
        private final Map<String, Object> map = new HashMap<String, Object>();

        public Builder(String template) {
            this.template = template;
        }

        public Builder add(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Builder addAll(Map<String, Object> map) {
            this.map.putAll(map);
            return this;
        }

        public String apply() {
            return Templates.this.apply(template, map);
        }

        public File write(File file) throws IOException {
            IOUtil.writeString(file, apply());
            return file;
        }
    }
}
