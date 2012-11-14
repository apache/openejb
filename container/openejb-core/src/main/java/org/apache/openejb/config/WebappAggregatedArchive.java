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
package org.apache.openejb.config;

import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WebappAggregatedArchive implements Archive, ScanConstants {
    private final Map<URL, List<String>> map = new HashMap<URL, List<String>>();
    private ScanUtil.ScanHandler handler = null;
    private boolean scanXmlExists = false; // faster than using an empty handler
    private Archive archive;

    public WebappAggregatedArchive(final Module module, final Iterable<URL> urls) {
        final List<Archive> archives = new ArrayList<Archive>();

        final URL scanXml = (URL) module.getAltDDs().get(ScanConstants.SCAN_XML_NAME);
        if (scanXml != null) {
            try {
                handler = ScanUtil.read(scanXml);
                scanXmlExists = true;
            } catch (IOException e) {
                // ignored, will not use filtering with scan.xml
            }
        }

        for (URL url : urls) {
            final List<String> classes = new ArrayList<String>();
            final Archive archive = new FilteredArchive(new ConfigurableClasspathArchive(module.getClassLoader(), Arrays.asList(url)), new ScanXmlSaverFilter(scanXmlExists, handler, classes));
            map.put(url, classes);
            archives.add(archive);
        }

        archive = new CompositeArchive(archives);
    }

    public WebappAggregatedArchive(final ClassLoader classLoader, final Map<String, Object> altDDs, ArrayList<URL> xmls) {
        this(new ConfigurableClasspathArchive.FakeModule(classLoader, altDDs), xmls);
    }

    public Map<URL, List<String>> getClassesMap() {
        return map;
    }

    @Override
    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
        return archive.getBytecode(className);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return archive.loadClass(className);
    }

    @Override
    public Iterator<Entry> iterator() {
        return archive.iterator();
    }

    public static class ScanXmlSaverFilter implements Filter {
        private boolean scanXmlExists;
        private final ScanUtil.ScanHandler handler;
        private final List<String> classes;

        public ScanXmlSaverFilter(boolean scanXmlExists, ScanUtil.ScanHandler handler, List<String> classes) {
            this.scanXmlExists = scanXmlExists;
            this.handler = handler;
            this.classes = classes;
        }

        @Override
        public boolean accept(String name) {
            if (scanXmlExists) {
                for (String packageName : handler.getPackages()) {
                    if (name.startsWith(packageName)) {
                        classes.add(name);
                        return true;
                    }
                }
                for (String className : handler.getClasses()) {
                    if (className.equals(name)) {
                        classes.add(name);
                        return true;
                    }
                }
                return false;
            }
            classes.add(name);
            return true;
        }
    }
}
