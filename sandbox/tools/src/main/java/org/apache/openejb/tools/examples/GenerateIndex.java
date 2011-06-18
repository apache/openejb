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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import static org.apache.openejb.tools.examples.FileHelper.extract;
import static org.apache.openejb.tools.examples.FileHelper.listFilesEndingWith;
import static org.apache.openejb.tools.examples.FileHelper.listFolders;
import static org.apache.openejb.tools.examples.FileHelper.mkdirp;
import static org.apache.openejb.tools.examples.ListBuilder.newList;
import static org.apache.openejb.tools.examples.MapBuilder.newMap;
import static org.apache.openejb.tools.examples.OpenEJBTemplate.USER_JAVASCRIPTS;
import static org.apache.openejb.tools.examples.ViewHelper.getAggregateClasses;
import static org.apache.openejb.tools.examples.ViewHelper.getAndUpdateApis;
import static org.apache.openejb.tools.examples.ViewHelper.getClassesByApi;
import static org.apache.openejb.tools.examples.ViewHelper.getLink;
import static org.apache.openejb.tools.examples.ViewHelper.removePrefix;

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

    private static final String EXTRACTED_EXAMPLES = "extracted";
    private static final String GENERATED_EXAMPLES = "generated";
    private static final String INDEX_HTML = "index.html";
    private static final String GLOSSARY_HTML = "glossary.html";
    private static final String README_MD = "README.md";
    private static final String POM_XML = "pom.xml";

    private static final MarkdownProcessor PROCESSOR = new MarkdownProcessor();

    private static final String TEMPLATE_COMMON_PROPERTIES = "generate-index/config.properties";
    private static final String MAIN_TEMPLATE = "index.vm";
    private static final String DEFAULT_EXAMPLE_TEMPLATE = "example.vm";
    private static final String EXTERNALE_TEMPLATE = "external.vm";
    private static final String GLOSSARY_TEMPLATE = "glossary.vm";

    private static final String TITLE = "title";
    private static final String BASE = "base";

    /**
     * Can be run in an IDE or via Maven like so:
     * <p/>
     * mvn clean install exec:java -Dexec.mainClass=org.apache.openejb.tools.examples.GenerateIndex
     *
     * @param args zip-location work-folder
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.info("Usage: <main> <examples-zip-location> <work-folder>");
            return;
        }

        Properties properties = new Properties();
        URL propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(TEMPLATE_COMMON_PROPERTIES);
        try {
            properties.load(propertiesUrl.openStream());
        } catch (IOException e) {
            LOGGER.error("can't read common properties, please put a " + TEMPLATE_COMMON_PROPERTIES + " file");
        }

        // will be used everywhere so keep it here
        String base = properties.getProperty(BASE);

        // working folder
        File extractedDir = new File(args[1], EXTRACTED_EXAMPLES);
        File generatedDir = new File(args[1], GENERATED_EXAMPLES);

        // crack open the examples zip file
        extract(args[0], extractedDir.getPath());

        // generate index.html by example
        Map<String, Set<String>> exampleLinksByKeyword = new TreeMap<String, Set<String>>();
        Map<String, String> nameByLink = new TreeMap<String, String>();
        Collection<File> examples = listFolders(extractedDir, POM_XML);
        for (File example : examples) {
            // create a directory for each example
            File generated = new File(generatedDir, example.getPath().replace(extractedDir.getPath(), ""));
            mkdirp(generated);

            File readme = new File(example, README_MD);
            String html = "";
            if (readme.exists()) {
                try {
                    html = PROCESSOR.markdown(FileUtils.readFileToString(readme));
                } catch (IOException e) {
                    LOGGER.warn("can't read readme file for example " + example.getName());
                }
            }

            File index = new File(generated, INDEX_HTML);
            nameByLink.put(getLink(generatedDir, index), example.getName());

            List<File> javaFiles = listFilesEndingWith(example, ".java");
            Map<String, Integer> apiCount = getAndUpdateApis(javaFiles, exampleLinksByKeyword, generatedDir, index);

            if (html.isEmpty()) {
                LOGGER.warn("no " + README_MD + " for example " + example.getName() + " [" + example.getPath() + "]");

                tpl(DEFAULT_EXAMPLE_TEMPLATE,
                    newMap(String.class, Object.class)
                        .add(TITLE, example.getName() + " example")
                        .add(BASE, base)
                        .add(OpenEJBTemplate.USER_JAVASCRIPTS, newList(String.class).add("prettyprint.js").list())
                        .add("apis", apiCount)
                        .add("files", removePrefix(extractedDir, javaFiles))
                        .map(),
                    index.getPath());
            } else {
                tpl(EXTERNALE_TEMPLATE,
                    newMap(String.class, Object.class)
                        .add(TITLE, example.getName() + " example")
                        .add(BASE, base)
                        .add(OpenEJBTemplate.USER_JAVASCRIPTS, newList(String.class).add("prettyprint.js").list())
                        .add("content", html)
                        .map(),
                    index.getPath());
            }
        }

        Map<String, String> classesByApi = getClassesByApi(exampleLinksByKeyword); // css class(es)
        Map<String, String> aggregatedClasses = getAggregateClasses(new ArrayList<String>(nameByLink.keySet()), exampleLinksByKeyword);

        // create a glossary page (OR search)
        tpl(GLOSSARY_TEMPLATE,
            newMap(String.class, Object.class)
                .add(TITLE, "OpenEJB Example Glossary")
                .add(BASE, base)
                .add(USER_JAVASCRIPTS, newList(String.class).add("glossary.js").list())
                .add("links", nameByLink)
                .add("classes", classesByApi)
                .add("exampleByKeyword", exampleLinksByKeyword)
                .add("examples", nameByLink)
                .add("aggregatedClasses", aggregatedClasses)
                .map(),
            new File(generatedDir, GLOSSARY_HTML).getPath());

        // create an index for all example directories
        tpl(MAIN_TEMPLATE,
            newMap(String.class, Object.class)
                .add(TITLE, "OpenEJB Example")
                .add(BASE, base)
                .add(USER_JAVASCRIPTS, newList(String.class).add("index.js").list())
                .add("examples", nameByLink)
                .add("classes", classesByApi)
                .add("aggregatedClasses", aggregatedClasses)
                .map(),
            new File(generatedDir, INDEX_HTML).getPath());
    }

    // just a shortcut
    private static void tpl(String template, Map<String, Object> mapContext, String path) {
        OpenEJBTemplate.get().apply(template, mapContext, path);
    }
}
