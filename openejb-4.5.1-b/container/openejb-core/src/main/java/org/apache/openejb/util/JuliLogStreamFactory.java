/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.log.ConsoleColorHandler;
import org.apache.openejb.log.SingleLineFormatter;
import org.apache.webbeans.logger.JULLoggerFactory;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;

/**
 * default conf = jre conf
 * user conf used transparently
 */
public class JuliLogStreamFactory implements LogStreamFactory {
    public static final String OPENEJB_LOG_COLOR_PROP = "openejb.log.color";

    private static String consoleHandlerClazz;

    public LogStream createLogStream(LogCategory logCategory) {
        return new JuliLogStream(logCategory);
    }

    static {
        final boolean tomee = is("org.apache.tomee.catalina.TomcatLoader");
        final boolean embedded = is("org.apache.tomee.embedded.Container");

        // if embedded case enhance a bit logging if not set
        if ((!tomee || embedded) && System.getProperty("java.util.logging.manager") == null) {
            System.setProperty("java.util.logging.manager", OpenEJBLogManager.class.getName());
            if (SystemInstance.get().getOptions().get(OPENEJB_LOG_COLOR_PROP, false) && isNotIDE()) {
                consoleHandlerClazz = ConsoleColorHandler.class.getName();
            } else {
                consoleHandlerClazz = OpenEJBSimpleLayoutHandler.class.getName();
            }

            try { // check it will not fail later (case when a framework change the JVM classloading)
                ClassLoader.getSystemClassLoader().loadClass(consoleHandlerClazz);
            } catch (ClassNotFoundException e) {
                consoleHandlerClazz = ConsoleHandler.class.getName();
            }
        }

        try {
            if (SystemInstance.get().getOptions().get("openjpa.Log", (String) null) == null) {
                JuliLogStreamFactory.class.getClassLoader().loadClass("org.apache.openjpa.lib.log.LogFactoryAdapter");
                System.setProperty("openjpa.Log", "org.apache.openejb.openjpa.JULOpenJPALogFactory");
            }
        } catch (Exception ignored) {
            // no-op: openjpa is not at the classpath so don't trigger it loading with our logger
        }

        System.setProperty("openwebbeans.logging.factory", "org.apache.webbeans.logger.JULLoggerFactory");
    }

    public static boolean isNotIDE() {
        return !System.getProperty("java.class.path").contains("idea_rt"); // TODO: eclipse, netbeans
    }

    // TODO: mange conf by classloader? see tomcat log manager
    public static class OpenEJBLogManager extends LogManager {
        static {
            final LogManager mgr = LogManager.getLogManager();
            if (mgr instanceof OpenEJBLogManager) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        ((OpenEJBLogManager) mgr).forceReset();
                    }
                });
            }
        }

        public void forceReset() {
            super.reset();
        }

        @Override
        public void reset() throws SecurityException {
            // no-op
        }

        @Override
        public String getProperty(final String name) {
            final String parentValue = super.getProperty(name);

            if (SystemInstance.get().getProperties().containsKey(name)) {
                return SystemInstance.get().getProperty(name);
            }

            final String propertyKeyValue = "logging" + reverseProperty(name);
            if (SystemInstance.get().getProperties().containsKey(propertyKeyValue)) {
                return SystemInstance.get().getProperty(propertyKeyValue);
            }

            // if it is one of ours loggers and no value is defined let set our nice logging style
            if (OpenEJBLogManager.class.getName().equals(System.getProperty("java.util.logging.manager")) // custom logging
                    && isOverridableLogger(name) // managed loggers
                    && parentValue == null) { // not already defined
                if (name.endsWith(".handlers")) {
                    return consoleHandlerClazz;
                } else if (name.endsWith(".useParentHandlers")) {
                    return "false";
                }
            }
            return parentValue;
        }

        private static String reverseProperty(String name) {
            if (name.contains(".") && !name.endsWith(".")) {
                int idx = name.lastIndexOf('.');
                return name.substring(idx) + "." + name.substring(0, idx);
            }
            return name;
        }

        private static boolean isOverridableLogger(String name) {
            return name.toLowerCase().contains("openejb")
                    || name.toLowerCase().contains("transaction")
                    || name.toLowerCase().contains("cxf")
                    || name.toLowerCase().contains("timer")
                    || name.startsWith("org.apache.")
                    || name.startsWith("openjpa.")
                    || name.startsWith("net.sf.ehcache.")
                    || name.startsWith("org.quartz.")
                    || name.startsWith("org.hibernate.");
        }
    }

    public static class OpenEJBSimpleLayoutHandler extends ConsoleHandler {
        public OpenEJBSimpleLayoutHandler() {
            setFormatter(new SingleLineFormatter());
            setOutputStream(System.out);
        }
    }

    private static boolean is(String classname) {
        try {
            JuliLogStreamFactory.class.getClassLoader().loadClass(classname);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
