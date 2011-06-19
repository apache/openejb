package org.apache.openejb.tools.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

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
            } else if (!EXCLUDED_FOLDERS.contains(file.getParentFile().getName()) && name.equals(file.getName())) {
                examples.add(file.getParentFile());
            }
        }
        return examples;
    }

    public static List<File> listFilesEndingWith(File extractedDir, String end) {
        List<File> examples = new ArrayList<File>();
        for (File file : extractedDir.listFiles()) {
            if (file.isDirectory() && !EXCLUDED_FOLDERS.contains(file.getName())) {
                examples.addAll(listFilesEndingWith(file, end));
            } else if (file.getName().endsWith(end)) {
                examples.add(file);
            }
        }
        return examples;
    }

    public static void mkdirp(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LOGGER.warn("can't create folder " + file.getPath());
            }
        }
    }
}
