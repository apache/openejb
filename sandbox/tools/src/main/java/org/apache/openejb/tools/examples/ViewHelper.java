package org.apache.openejb.tools.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * @author Romain Manni-Bucau
 */
public final class ViewHelper {
    private static final Logger LOGGER = Logger.getLogger(ViewHelper.class);

    public static final char REPLACED_CHAR = '_';

    private static final String JAVAX_PREFIX = "javax.";
    private static final String IMPORT_START = "import ";

    private ViewHelper() {
        // no-op
    }

    public static Map<String, String> getAggregateClasses(List<String> links, Map<String, Set<String>> exampleLinksByKeyword) {
        Map<String, String> classes = new HashMap<String, String>();
        for (String link: links) {
            classes.put(link, concatHTMLClasses(getApisForExample(exampleLinksByKeyword, link), "example", '.', '_'));
        }
        return classes;
    }

    public static Map<String, String> getExamplesClassesByApi(Map<String, Set<String>> exampleLinksByKeyword) {
        Map<String, String> classes = new TreeMap<String, String>();
        for (Entry<String, Set<String>> entry : exampleLinksByKeyword.entrySet()) {
            Set<String> examples = new HashSet<String>();
            for (String example : entry.getValue()) {
                examples.add(example.substring(0, example.length() - GenerateIndex.INDEX_HTML.length() - 1));
            }
            classes.put(entry.getKey(), concatHTMLClasses(examples, "button", '/', REPLACED_CHAR));
        }
        return classes;
    }

    public static Map<String, String> getClassesByApi(Map<String, Set<String>> exampleLinksByKeyword, char toReplace, char replaced) {
        Map<String, String> classes = new TreeMap<String, String>();
        for (String api : exampleLinksByKeyword.keySet()) {
            classes.put(api, api.replace(toReplace, replaced));
        }
        return classes;
    }

    public static List<String> removePrefix(File path, List<File> files) {
        List<String> processed = new ArrayList<String>();
        for (File file : files) {
            processed.add(getLink(path, file));
        }
        return processed;
    }

    public static List<String> startFromPrefix(String prefix, List<File> files) {
        List<String> processed = new ArrayList<String>();
        for (File file : files) {
            int index = file.getPath().indexOf(prefix);
            processed.add(file.getPath().substring(index));
        }
        return processed;
    }

    public static Map<String, Integer> getAndUpdateApis(List<File> javaFiles, Map<String, Set<String>> exampleLinksByKeyword, File generatedDir, File index) {
        Map<String, Integer> apiCount = new TreeMap<String, Integer>();
        Collections.sort(javaFiles);
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
                        if (!apiCount.containsKey(name)) {
                            apiCount.put(name, 1);
                        } else {
                            apiCount.put(name, apiCount.get(name) + 1);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("can't read " + file.getPath());
            }
        }
        return apiCount;
    }

    private static String concatHTMLClasses(Set<String> values, String defaultClass, char toReplace, char replaced) {
        StringBuilder htmlClass = new StringBuilder(defaultClass);
        for (String value : values) {
            htmlClass.append(' ').append(value.replace(toReplace, replaced));
        }
        return htmlClass.toString().trim();
    }

    private static Set<String> getApisForExample(Map<String, Set<String>> exampleLinksByKeyword, String example) {
        Set<String> set = new HashSet<String>();
        for (Entry<String, Set<String>> links : exampleLinksByKeyword.entrySet()) {
            for (String link : links.getValue()) {
                if (example.equals(link)) {
                    set.add(links.getKey());
                    break;
                }
            }
        }
        return set;
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

    public static String getLink(File generatedDir, File example) {
        return example.getPath().replace(generatedDir.getPath(), "")
            .replace(File.separator, "/").replaceFirst("/", "");
    }
}
