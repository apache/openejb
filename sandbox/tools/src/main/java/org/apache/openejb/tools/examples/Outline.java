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
package org.apache.openejb.tools.examples;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.openejb.tools.util.Files;
import org.apache.openejb.tools.util.IO;
import org.apache.openejb.tools.util.Join;
import org.codehaus.swizzle.stream.StreamLexer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class Outline {

    static {
        Logger root = Logger.getRootLogger();

        root.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
        root.setLevel(Level.INFO);
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Outline.class);

    public void run(String... args) throws Exception {

        for (String arg : args) {
            final File file = new File(arg);

            if (!file.exists()) {
                log.error("Does not exist: " + file.getAbsolutePath());
                continue;
            }

            if (!file.isDirectory()) {
                log.error("Not a directory: " + file.getAbsolutePath());
                continue;
            }

            generate(file);
        }
    }

    private void generate(File dir) throws Exception {
        final File readme = new File(dir, "README.md");
        final PrintStream out = new PrintStream(IO.write(readme));

        out.print("Title: ");
        out.println(title(dir));
        out.println();
        out.println("*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/" + dir.getName() + ") or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/" + dir.getName() + "). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*");
        final File main = Files.file(dir, "src", "main");
        final File test = Files.file(dir, "src", "test");

        printJavaFiles(out, collect(main, Pattern.compile(".*\\.java")));
        printXmlFiles(out, collect(main, Pattern.compile(".*\\.xml")));

        printJavaFiles(out, collect(test, Pattern.compile(".*\\.java")));
        printXmlFiles(out, collect(test, Pattern.compile(".*\\.xml")));

        printBuildOutput(out, new File("/Users/dblevins/examples/"+dir.getName()+"/build.log"));

        out.close();
    }

    private void printBuildOutput(PrintStream out, File file) throws IOException {
        if (!file.exists()) return;

        final String content = IO.slurp(file);
        out.println();
        out.println("# Running");
        out.println();

        for (String line : content.split("\n")) {

            if (line.startsWith("[")) continue;
            out.println(indent(line));
        }
    }

    private String title(File dir) {
        String[] strings = dir.getName().split("-");
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];

            if (string.matches("and|or|to|from|in|on|with|out|the")) continue;

            if (string.matches("ejb|cdi|jsf|jsp|mdb|jstl|xml|jpa|jms|ear|rest")) {
                strings[i] = strings[i].toUpperCase();
                continue;
            }

            StringBuilder sb = new StringBuilder(string);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            strings[i] = sb.toString();

        }
        return Join.join(" ", strings);
    }

    private void printJavaFiles(PrintStream out, List<File> mainJavaFiles) throws IOException {
        for (File javaFile : mainJavaFiles) {

            out.println();
            out.println("## " + javaFile.getName().replace(".java", ""));
            out.println();

            InputStream read = IO.read(javaFile);
            StreamLexer lexer = new StreamLexer(read);

            String contents = lexer.read("\npackage", "\n}");
            contents = "package" + contents + "\n}";
            contents = contents.replaceAll("\n *(\n *})", "$1");

            contents = indent(contents);
            out.println(contents);
        }
    }

    private void printXmlFiles(PrintStream out, List<File> files) throws IOException {
        for (File file : files) {
            String xml = "\n" + IO.slurp(file);

            final List<Integer> indexes = indexesOf(xml, "\n<");

            if (indexes.size() == 0) continue;
            if (indexes.size() > 1) {
                xml = xml.substring(indexes.get(indexes.size() - 2), xml.length());
            }

            if (xml.startsWith("\n")) xml = xml.substring(1);

            if (xml.matches("<[a-zA-Z-]/>")) continue;

            out.println();
            out.println("## " + file.getName());
            out.println();
            out.println(indent(xml));
        }
    }

    private static List<Integer> indexesOf(String xml, String str) {
        final List<Integer> indexes = new ArrayList<Integer>();
        int index = -1;
        while ((index = xml.indexOf(str, index + 1)) != -1) {
            indexes.add(index);
        }
        return indexes;
    }

    private String indent(String contents) {
        String indent = "    ";
        return indent + contents.replaceAll("\n", "\n" + indent);
    }


    /*
     * Mainstreet
     */

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
//        new Outline().run(args);
        process(new File("/Users/dblevins/work/all/trunk/openejb/examples/"));
        process(new File("/Users/dblevins/work/all/trunk/openejb/examples/webapps/"));
    }

    private static void process(File dir) throws Exception {
        for (File file : dir.listFiles()) {

            if (!file.isDirectory()) continue;
            if (!Files.file(file, "src").exists()) continue;

            new Outline().run(file.getAbsolutePath());
        }
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
