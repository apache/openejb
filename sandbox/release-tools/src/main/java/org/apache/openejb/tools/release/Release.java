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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tools.release;

import org.apache.openejb.tools.release.util.Files;
import org.apache.openejb.tools.release.util.ObjectMap;
import org.apache.openejb.tools.release.util.Options;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class Release {

    public static String openejbVersion = "4.0.0-beta-2";
    public static String tomeeVersion = "1.0.0-beta-2";

    public static String openejbVersionName = "openejb-${openejbVersion}";
    public static String tomeeVersionName = "tomee-${tomeeVersion}";

    public static String trunk = "https://svn.apache.org/repos/asf/openejb/trunk/openejb/";
    public static String branches = "https://svn.apache.org/repos/asf/openejb/branches/";
    public static String tags = "https://svn.apache.org/repos/asf/openejb/tags/";
    public static String tckBranches = "https://svn.apache.org/repos/tck/openejb-tck/branches/";
    public static String tckTrunk = "https://svn.apache.org/repos/tck/openejb-tck/trunk";

    public static String staging = "https://repository.apache.org/content/repositories/orgapacheopenejb-${build}";
    public static String build = "075";


    public static String builddir = "/tmp/downloads";
    public static String workdir = "/tmp/release";

    public static String mavenOpts = "-Xmx2048m -XX:MaxPermSize=1024m";

    public static String user = System.getProperty("user.name");
    public static String to = "dev@openejb.apache.org";
    public static String from = "${user}@apache.org";

    public static String lastReleaseDate = "2011-10-05";

    private static final Pattern PATTERN = Pattern.compile("(\\$\\{)(\\w+)(})");


    static {
        final File public_html = Files.file(System.getProperty("user.home"), "public_html");

        if (public_html.exists()) {
            builddir = public_html.getAbsolutePath();
        }

        final Options options = new Options(System.getProperties());

        final Map<String, Object> map = map();

        boolean interpolating = true;
        while (interpolating) {
            interpolating = false;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                final Object value = options.get(entry.getKey(), entry.getValue());

                final String raw = value.toString();
                final String formatted = format(raw, map);
                if (!raw.equals(formatted)) interpolating = true;

                entry.setValue(formatted);
            }
        }
    }

    public static void main(String[] args) {
        for (Map.Entry<String, Object> entry : map().entrySet()) {
            System.out.printf("%s = %s\n", entry.getKey(), entry.getValue());
        }
    }

    static String format(String input, Map<String, Object> map) {
        Matcher matcher = PATTERN.matcher(input);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(2);
            Object value = map.get(key);
            if (value != null) {
                try {
                    matcher.appendReplacement(buf, value.toString());
                } catch (Exception e) {
                }
            }
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    public static Map<String, Object> map() {
        return new ObjectMap(Release.class);



//        @Override
//        public int size() {
//            return clazz.getFields().length;
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return false;
//        }
//
//        @Override
//        public boolean containsKey(Object key) {
//            try {
//                clazz.getField(key.toString());
//                return true;
//            } catch (NoSuchFieldException e) {
//                return false;
//            }
//        }
//
//        @Override
//        public boolean containsValue(Object value) {
//            return false;
//        }
//
//        @Override
//        public Object get(Object key) {
//            try {
//                final Field field = clazz.getField(key.toString());
//                return field.get(object);
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//        @Override
//        public Object put(String key, Object value) {
//            try {
//                final Object original = get(key);
//
//                final Field field = clazz.getField(key);
//                field.set(object, value);
//
//                return original;
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//        @Override
//        public String remove(Object key) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public void putAll(Map<? extends String, ? extends Object> m) {
//            for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
//                put(entry.getKey(), entry.getValue());
//            }
//        }
//
//        @Override
//        public void clear() {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public Set<String> keySet() {
//            return null;
//        }
//
//        @Override
//        public Collection<Object> values() {
//            return null;
//        }
//
//        @Override
//        public Set<Map.Entry<String, Object>> entrySet() {
//            return null;
//        }
//
//        public static class FieldSet implements Set<Map.Entry<String, Object>> {
//
//        }
    }

}
