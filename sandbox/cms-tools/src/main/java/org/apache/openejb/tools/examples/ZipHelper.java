package org.apache.openejb.tools.examples;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.apache.openejb.tools.examples.FileHelper.mkdirp;

/**
 * @author Romain Manni-Bucau
 */
public final class ZipHelper {
    private static final Logger LOGGER = Logger.getLogger(ZipHelper.class);
    private static final int BUFFER_SIZE = Integer.parseInt(ExamplesPropertiesManager.get().getProperty("zip.buffer"));

    private ZipHelper() {
        // no-op
    }

    public static void extract(String filename, String output) {
        File extractHere = new File(output);
        mkdirp(extractHere);

        try {
            // we'll read everything so ZipFile is useless
            ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
            byte[] buf = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    mkdirp(new File(output + File.separator + entry.getName()));
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

    public static void zipDirectory(File dir, File zipName, String skip) throws IOException, IllegalArgumentException {
        if (!dir.isDirectory()) {
            LOGGER.error(dir.getPath() + " is not a directory, skipping");
            return;
        }

        String[] entries = dir.list();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipName));
        for (String entry : entries) {
            File f = new File(dir, entry);
            zipFile(out, f, skip);
        }
        out.close();
    }

    private static void zipFile(ZipOutputStream out, File f, String skip) throws IOException {
        if (f.isDirectory()) {
            if (FileHelper.EXCLUDED_FOLDERS.contains(f.getName())) {
                return;
            } else {
                for (File child : f.listFiles()) {
                    zipFile(out, child, skip);
                }
            }
        } else {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            String path = f.getPath();
            if (path.startsWith(skip)) {
                path = path.substring(skip.length(), path.length());
                if (path.startsWith(File.separator)) {
                    path = path.substring(1);
                }
            }

            FileInputStream in = new FileInputStream(f);
            ZipEntry entry = new ZipEntry(path);
            out.putNextEntry(entry);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
        }
    }
}
