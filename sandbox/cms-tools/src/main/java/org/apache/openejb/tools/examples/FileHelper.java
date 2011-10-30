package org.apache.openejb.tools.examples;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public final class FileHelper {
    private static final Logger LOGGER = Logger.getLogger(FileHelper.class);

    public static final List<String> EXCLUDED_FOLDERS = Arrays.asList(
            ExamplesPropertiesManager.get().getProperty("excluded").split(","));

    private FileHelper() {
        // no-op
    }

    public static Collection<File> listFolders(File extractedDir, String name) {
        Collection<File> examples = new ArrayList<File>();
        for (File file : extractedDir.listFiles()) {
            if (file.isDirectory()) {
                examples.addAll(listFolders(file, name));
            } else if (!EXCLUDED_FOLDERS.contains(file.getParentFile().getName()) && name.equals(file.getName())
                    && !file.getParentFile().getName().startsWith("openejb-")) {
                examples.add(file.getParentFile());
            }
        }
        return examples;
    }

    public static List<File> listFilesEndingWith(File extractedDir, String end) {
        Collection<String> extensions = Arrays.asList(end.split(","));
        List<File> examples = new ArrayList<File>();
        for (File file : extractedDir.listFiles()) {
            if (file.isDirectory() && !EXCLUDED_FOLDERS.contains(file.getName())) {
                examples.addAll(listFilesEndingWith(file, end));
            } else if (matches(file.getName(), extensions)) {
                examples.add(file);
            }
        }
        return examples;
    }

    private static boolean matches(String name, Collection<String> extensions) {
        for (String ext : extensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static void mkdirp(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LOGGER.warn("can't create folder " + file.getPath());
            }
        }
    }
}
