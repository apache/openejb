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
package org.apache.openejb.tools.cms;

import org.codehaus.swizzle.stream.StreamLexer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.apache.openejb.tools.cms.Join.join;

/**
 * @version $Rev$ $Date$
 */
public class Imports {

    //private static String base = "http://download.oracle.com/javaee/6/api/javax/ejb/AccessTimeout.html";
    private static String link = "<li><a href=\"http://download.oracle.com/javaee/6/api/%s.html\">%s</a></li>\n";

    public void run(String... args) throws Exception {

        for (String arg : args) {
            final File file = new File(arg);

            if (!file.exists()) {
                System.err.println("Does not exist: " + file.getAbsolutePath());
                continue;
            }

            if (!file.isDirectory()) {
                System.err.println("Not a directory: " + file.getAbsolutePath());
                continue;
            }

            final Set<String> imports = imports(file);

            Iterator<String> iterator = imports.iterator();
            while (iterator.hasNext()) {
                String i = iterator.next();

                final String include = "javax\\..*";

                if (!i.matches(include)) iterator.remove();
            }

            System.out.println("<ul>");
            for (String i : imports) {
                System.out.printf(link, i.replace('.', '/'), i);
            }
            System.out.println("</ul>");
        }
    }

    private Set<String> imports(File dir) throws IOException {
        Set<String> imports = new TreeSet<String>();

        for (File file : collect(dir, Pattern.compile(".*\\.java"))) {
            final InputStream stream = IO.read(file);
            final StreamLexer lexer = new StreamLexer(stream);

            String statement;
            while ((statement = lexer.read("\nimport ", ";")) != null) {
                statement = statement.trim();

                if (statement.startsWith("static ")) {
                    List<String> parts = new ArrayList<String>(Arrays.asList(statement.split("[ .]")));
                    if (parts.get(0).equals("static")) {
                        parts.remove(0);
                        parts.remove(parts.size() - 1);
                        statement = join(".", parts);
                    }
                }

                imports.add(statement);
            }

            stream.close();
        }
        return imports;
    }

    public static List<File> collect(final File dir, final Pattern pattern) {
        return collect(dir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                return pattern.matcher(file.getAbsolutePath()).matches();
            }
        });
    }

    public static List<File> collect(File dir, FileFilter filter) {
        final List<File> accepted = new ArrayList<File>();
        if (filter.accept(dir)) accepted.add(dir);

        final File[] files = dir.listFiles();
        if (files != null) for (File file : files) {
            accepted.addAll(collect(file, filter));
        }

        return accepted;
    }

    public static void main(String[] args) throws Exception {
        args = processSystemProperties(args);
        new Imports().run(args);
    }

    public static String[] processSystemProperties(String[] args) {
        final ArrayList<String> list = new ArrayList<String>();

        // Read in and apply the properties specified on the command line
        for (String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String value = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(name, value);
            } else {
                list.add(arg);
            }
        }

        return (String[]) list.toArray(new String[list.size()]);
    }


}
