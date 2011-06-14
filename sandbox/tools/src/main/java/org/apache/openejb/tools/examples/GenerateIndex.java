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
package org.apache.openejb.tools.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 * Most the examples do not have any documentation.
 * <p/>
 * There are some wiki pages for some of the examples, but these are hard to create and maintain.  The examples change frequently enough that we really should have documentation that goes with each version of the examples.
 * <p/>
 * If we put a README.md file in each example and use Markdown which is a really simple text format that has many tools capable of generating html, we could probably generate a web page for each example.  Then we could generate a index for all the examples.
 * <p/>
 * We could then take this all and upload it to the website
 * <p/>
 * Something kind of like this, but nicer looking, with breadcrumbs and links for navigating around to other examples: http://people.apache.org/~dblevins/simple-stateless.html
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * IDEAS FOR AFTER SOMETHING IS WORKING
 * <p/>
 * Perhaps at some point some xref style processing of the source and links to the source
 * <p/>
 * Perhaps at some point use ASM to see what annotations and API classes were used and make an index that lists examples by which APIs are used
 * <p/>
 * Perhaps at some point use Swizzle stream to do a sort of SNIPPET thing like the Confluence plugin, so we wouldn't have to copy source into the example
 *
 * @version $Rev$ $Date$
 */
public class GenerateIndex {
    private static final Logger LOGGER = Logger.getLogger(GenerateIndex.class);
    private static final int BUFFER_SIZE = 1024;
    private static final String EXTRACTED_EXAMPLES = "extracted";
    private static final String GENERATED_EXAMPLES = "generated";
    private static final String README_MD = "README.md";
    private static final String POM_XML = "pom.xml";
    private static final String INDEX_HTML = "index.html";

    // A couple possible markdown processors in Java
    //   http://code.google.com/p/markdownj/wiki/Maven
    //   http://code.google.com/p/doxia-module-markdown/wiki/Usage


    // Syntax highlighting can be done with this:
    //   http://code.google.com/p/google-code-prettify

    /**
     * Can be run in an IDE or via Maven like so:
     * <p/>
     * mvn clean install exec:java -Dexec.mainClass=org.apache.openejb.tools.examples.GenerateIndex
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            LOGGER.info("Usage: <main> <examples-zip-location> <output-folder>");
            return;
        }

        File extractedDir = new File(args[1], EXTRACTED_EXAMPLES);
        File generatedDir = new File(args[1], GENERATED_EXAMPLES);

        // crack open the examples zip file
        extract(args[0], extractedDir.getPath());

        Collection<File> examples = listFolders(extractedDir, POM_XML);
        for (File example : examples) {
            // create a directory for each example
            File generated = new File(generatedDir, example.getPath().replace(extractedDir.getPath(), ""));
            generated.mkdirs();

            File readme = new File(example, README_MD);
            if (readme.exists()) {
                // use the README.md markdown file to generate an index.html page

                // TODO
            } else {
                // If there is no README.md we should just generate a basic page
                // maybe something that includes the FooTest.java code and shows
                // shows that with links to other classes in the example
                LOGGER.warn("no " + README_MD + " for example " + example.getName() + " [" + example.getPath() + "]");

                // TODO
            }
        }

        // create an index for all example directories
        Collection<File> indexes = listFolders(extractedDir, INDEX_HTML);
        // TODO
    }

    private static Collection<File> listFolders(File extractedDir, String name) {
        Collection<File> examples = new ArrayList<File>();
        for (File file : extractedDir.listFiles()) {
            if (file.isDirectory()) {
                examples.addAll(listFolders(file, name));
            } else if (name.equals(file.getName())) {
                examples.add(file.getParentFile());
            }
        }
        return examples;
    }

    public static void extract(String filename, String output) {
        File extractHere = new File(output);
        if (!extractHere.exists()) {
            extractHere.mkdirs();
        }

        try {
            // we'll read everything so ZipFile is useless
            ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
            byte[] buf = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(output + File.separator + entry.getName()).mkdirs();
                } else {
                    int count;
                    File file = new File(output + File.separator + entry.getName());
                    FileOutputStream fos = new FileOutputStream(file);
                    while ((count = zip.read(buf, 0, BUFFER_SIZE)) != -1) {
                       fos.write(buf, 0, count);
                    }
                    fos.flush();
                    fos.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("can't unzip examples", e);
            throw new RuntimeException("can't unzip " + filename);
        }
    }
}
