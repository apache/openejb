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

import com.petebevin.markdown.MarkdownProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
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
    private static final String GLOSSARY_HTML = "glossary.html";
    private static final String HEAD = getTemplate("head.frag.html");
    private static final String FOOT = getTemplate("foot.frag.html");
    private static final String DEFAULT = getTemplate("default-index.frag.html");
    private static final String HEAD_MAIN = getTemplate("main-head.frag.html");
    private static final String FOOT_MAIN = getTemplate("main-foot.frag.html");
    private static final String TITLE = "TITLE";
    private static final String JAVAX_PREFIX = "javax.";
    private static final String IMPORT_START = "import ";
    private static final MarkdownProcessor PROCESSOR = new MarkdownProcessor();
    private static final List<String> EXCLUDED_FOLDERS = new ArrayList<String>() {{
        add("examples");
        add(".svn");
    }};

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
     * @param args zip-location work-folder
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.info("Usage: <main> <examples-zip-location> <output-folder>");
            return;
        }

        File extractedDir = new File(args[1], EXTRACTED_EXAMPLES);
        File generatedDir = new File(args[1], GENERATED_EXAMPLES);

        // crack open the examples zip file
        extract(args[0], extractedDir.getPath());

        Map<String, Set<String>> exampleLinksByKeyword = new HashMap<String, Set<String>>();
        Collection<File> examples = listFolders(extractedDir, POM_XML);
        List<File> generatedIndexHtml = new ArrayList<File>();
        for (File example : examples) {
            // create a directory for each example
            File generated = new File(generatedDir, example.getPath().replace(extractedDir.getPath(), ""));
            if (!generated.mkdirs()) {
                LOGGER.warn("can't create folder " + generated.getPath());
            }

            File readme = new File(example, README_MD);
            String html = "";
            if (readme.exists()) {
                // use the README.md markdown file to generate an index.html page

                try {
                    html = PROCESSOR.markdown(FileUtils.readFileToString(readme));

                    // if readme keeps small it will be ok
                    html = html.replace("<code>", "<code class=\"prettyprint\">");
                    html = new StringBuilder(HEAD.replace(TITLE, example.getName() + " example"))
                        .append(html).append(FOOT).toString();
                } catch (IOException e) {
                    LOGGER.warn("can't read readme file for example " + example.getName());
                }
            }
            if (html.isEmpty()) {
                // If there is no README.md we should just generate a basic page
                // maybe something that includes the FooTest.java code and shows
                // shows that with links to other classes in the example
                LOGGER.warn("no " + README_MD + " for example " + example.getName() + " [" + example.getPath() + "]");

                html = new StringBuilder(HEAD.replace(TITLE, example.getName() + " example"))
                    .append(DEFAULT).append(FOOT).toString();
            }

            File index;
            try {
                index = new File(generated, INDEX_HTML);
                FileUtils.writeStringToFile(index, html);
                generatedIndexHtml.add(index);
            } catch (IOException e) {
                LOGGER.error("can't write index file for example " + example.getName());
                continue;
            }

            Collection<File> javaFiles = listFilesEndingWith(example, ".java");
            for (File file : javaFiles) {
                try {
                    Set<String> imports = getImports(file);
                    if (imports != null) {
                        for (String name : imports) {
                            if (name.startsWith(JAVAX_PREFIX)) {
                                if (!exampleLinksByKeyword.containsKey(name)) {
                                    exampleLinksByKeyword.put(name, new HashSet<String>());
                                }
                                exampleLinksByKeyword.get(name).add(getLink(generatedDir, index));
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("can't read " + file.getPath());
                }
            }
        }

        // create a glossary page
        StringBuilder glossaryContent = new StringBuilder(HEAD.replace(TITLE, "OpenEJB Example Glossary"))
                                                        .append("<h2>Glossary</h2>\n");
        glossaryContent.append("<ul>\n");
        for (Entry<String, Set<String>> clazz : exampleLinksByKeyword.entrySet()) {
            glossaryContent.append("<li>").append(clazz.getKey()).append("\n<ul>\n");
            for (String link : clazz.getValue()) {
                String name = link;
                int idx = name.lastIndexOf('/');
                int idxBefore = name.lastIndexOf('/', idx - 1);
                if (idx >= 0 && idxBefore >= 0) {
                    name = name.substring(idxBefore + 1, idx);
                }
                glossaryContent.append("<li><a href=\"").append(link).append("\">").append(name).append("</a></li>");
            }
            glossaryContent.append("</ul>");
        }
        glossaryContent.append("</ul></li>\n")
            .append(FOOT);
        File glossary;
        try {
            glossary = new File(generatedDir, GLOSSARY_HTML);
            FileUtils.writeStringToFile(glossary, glossaryContent.toString());
        } catch (IOException e) {
            LOGGER.error("can't write glossary file");
        }


        // create an index for all example directories
        StringBuilder mainIndex = new StringBuilder(HEAD.replace(TITLE, "OpenEJB Example"));
        mainIndex.append(HEAD_MAIN);
        mainIndex.append("<ul><li><a href=\"").append(GLOSSARY_HTML).append("\">").append("Glossary").append("</a></li></ul>");
        mainIndex.append("    <ul>\n");
        Collections.sort(generatedIndexHtml);
        for (File example : generatedIndexHtml) {
            String name = getLink(generatedDir, example);
            mainIndex.append("      <li>\n").append("        <a href=\"")
                .append(name)
                .append("\">").append(example.getParentFile().getName()).append("</a>\n")
                .append("      </li>\n");
        }
        mainIndex.append("    </ul>\n");
        mainIndex.append(FOOT_MAIN).append(FOOT);
        try {
            FileUtils.writeStringToFile(new File(generatedDir, INDEX_HTML), mainIndex.toString());
        } catch (IOException e) {
            LOGGER.error("can't write main index file.");
        }
    }

    private static Set<String> getImports(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        Set<String> imports = new HashSet<String>();
        while ((line = in.readLine()) != null) {
            if (line.startsWith(IMPORT_START)) {
                String clazz = line.replace(IMPORT_START, "");
                imports.add(clazz.substring(0, clazz.length() - 1));
            }
        }
        in.close();
        return imports;
    }

    private static String getLink(File generatedDir, File example) {
        return example.getPath().replace(generatedDir.getPath(), "")
                            .replaceFirst(File.separator, "/").replaceFirst("/", "");
    }

    private static Collection<File> listFolders(File extractedDir, String name) {
        Collection<File> examples = new ArrayList<File>();
        for (File file : extractedDir.listFiles()) {
            if (file.isDirectory()) {
                examples.addAll(listFolders(file, name));
            } else if (!EXCLUDED_FOLDERS.contains(file.getParentFile().getName()) && name.equals(file.getName())) {
                examples.add(file.getParentFile());
            }
        }
        return examples;
    }

    private static Collection<File> listFilesEndingWith(File extractedDir, String end) {
        Collection<File> examples = new ArrayList<File>();
        for (File file : extractedDir.listFiles()) {
            if (file.isDirectory() && !EXCLUDED_FOLDERS.contains(file.getName())) {
                examples.addAll(listFilesEndingWith(file, end));
            } else if (file.getName().endsWith(end)) {
                examples.add(file);
            }
        }
        return examples;
    }

    public static void extract(String filename, String output) {
        File extractHere = new File(output);
        if (!extractHere.exists()) {
            if (!extractHere.mkdirs()) {
                LOGGER.warn("can't create folder " + extractHere.getPath());
            }
        }

        try {
            // we'll read everything so ZipFile is useless
            ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
            byte[] buf = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File file = new File(output + File.separator + entry.getName());
                    if (!file.mkdirs()) {
                        LOGGER.warn("can't create folder " + file.getPath());
                    }
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

    private static String getTemplate(String file) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("generate-index/" + file);
        try {
            File f = new File(url.toURI());
            return FileUtils.readFileToString(f);
        } catch (URISyntaxException e) {
            LOGGER.error("can't get template " + file);
        } catch (IOException e) {
            LOGGER.error("can't read template " + file);
        }
        return "";
    }
}
