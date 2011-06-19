package org.apache.openejb.tools.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 * @author Romain Manni-Bucau
 */
public final class FileHelper {
    public static final List<String> EXCLUDED_FOLDERS = ListBuilder.newList(String.class)
        .add("examples").add(".svn").add("target").add(".git").add(".settings")
        .list();

    private static final Logger LOGGER = Logger.getLogger(FileHelper.class);

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
