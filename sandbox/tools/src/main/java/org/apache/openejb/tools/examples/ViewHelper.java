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
    private static final String JAVAX_PREFIX = "javax.";
    private static final String IMPORT_START = "import ";

    private ViewHelper() {
        // no-op
    }

    public static Map<String, String> getAggregateClasses(List<String> links, Map<String, Set<String>> exampleLinksByKeyword) {
        Map<String, String> classes = new HashMap<String, String>();
        for (String link: links) {
            classes.put(link, getHTMLClass(exampleLinksByKeyword, link));
        }
        return classes;
    }

    public static Map<String, String> getClassesByApi(Map<String, Set<String>> exampleLinksByKeyword) {
        Map<String, String> classes = new TreeMap<String, String>();
        for (String api : exampleLinksByKeyword.keySet()) {
            classes.put(api, api.replace(".", "-"));
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

    private static String getHTMLClass(Map<String, Set<String>> exampleLinksByKeyword, String value) {
        StringBuilder clazz = new StringBuilder("example");
        for (Entry<String, Set<String>> links : exampleLinksByKeyword.entrySet()) {
            for (String link : links.getValue()) {
                if (value.equals(link)) {
                    clazz.append(" ").append(links.getKey().replace('.', '-'));
                    break;
                }
            }
        }
        return clazz.toString();
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
