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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    private static final java.util.Random _random = new java.util.Random();

    private static FileUtils openejbHomeUtils = new FileUtils("openejb.home", "user.dir");
    private static FileUtils openejbBaseUtils = new FileUtils("openejb.base", "openejb.home");

    private File home;

    private FileUtils(String homeDir, String defaultDir) {
        String homePath = null;
        try {
            homePath = (String) System.getProperty(homeDir);
            if (homePath == null) {
                homePath = System.getProperty(defaultDir);
                System.setProperty(homeDir, homePath);
            }

            home = new File(homePath);

            if (!home.exists() || (home.exists() && !home.isDirectory())) {
                homePath = System.getProperty("user.dir");
                System.setProperty(homeDir, homePath);
                home = new File(homePath);
            }

            home = home.getAbsoluteFile();
        } catch (SecurityException e) {
            //throw new IOException("Cannot resolve the directory: "+homeDir+" : "+e.getMessage());
        }
    }

    public static FileUtils getBase() {
        return openejbBaseUtils;
    }

    public static FileUtils getHome() {
        return openejbHomeUtils;
    }

    /**
	 * @see #getDirectory(String, boolean)
	 */
    public File getDirectory(String path) throws IOException {
        return getDirectory(path, false);
    }

    /**
	 * Resolves the specified path relative to the home directory; create it if requested
	 * 
	 * @param path
	 *            relative path to the home directory
	 * @param create
	 *            shall the directory be created if it doesn't exist?
	 * @return directory
	 * @throws IOException
	 */
    public File getDirectory(String path, boolean create) throws IOException {
        File dir = null;

        dir = new File(home, path);
        dir = dir.getCanonicalFile();

        if (!dir.exists() && create) {
            try {
                if (!dir.mkdirs())
                    throw new IOException("Cannot create the directory " + dir.getPath());
            } catch (SecurityException e) {
                throw new IOException(
                    "Permission denied: Cannot create the directory " + dir.getPath() + " : " + e.getMessage());
            }
        } else if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("The path specified is not a valid directory: " + dir.getPath());
        }

        return dir;
    }

    public File getDirectory() {
        return home;
    }

    public File getFile(String path) throws java.io.FileNotFoundException, java.io.IOException {
        return getFile(path, true);
    }

    public File getFile(String path, boolean validate) throws java.io.FileNotFoundException, java.io.IOException {
        File file = null;

        file = new File(path);

        if (!file.isAbsolute()) {
            file = new File(home, path);
        }

        if (validate && !file.exists()) {
            throw new FileNotFoundException("The path specified is not a valid file: " + file.getPath());
        } else if (validate && file.isDirectory()) {
            throw new FileNotFoundException("The path specified is a directory, not a file: " + file.getPath());
        }

        return file;
    }

    /**
	 * Creates a string for a temporary directory
	 * 
	 * @param pathPrefix
	 *            the path prefix to for the directory, e.g. /tmp/openejb @returns the file object associated with the
	 *            unique name
	 * @throws java.io.IOException
	 *             if it can't find a unique directory name after many iterations
	 */
    public static File createTempDirectory(String pathPrefix) throws java.io.IOException {
        for (int maxAttempts = 100; maxAttempts > 0; --maxAttempts) {
            String path = pathPrefix + _random.nextLong();
            java.io.File tmpDir = new java.io.File(path);
            if (tmpDir.exists()) {
                continue;
            } else {
                tmpDir.mkdir();
                return tmpDir;
            }
        }
        throw new java.io.IOException("Can't create temporary directory.");
    }

    /**
	 * Creates a string for a temporary directory The path prefix is chosen from the system property "java.io.tmpdir"
	 * plus a file separator plus the string "openejb" @returns the file object associated with the unique name
	 * 
	 * @throws java.io.IOException
	 *             if it can't find a unique directory name after many iterations
	 */
    public static File createTempDirectory() throws java.io.IOException {
        String prefix = System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator + "openejb";
        return createTempDirectory(prefix);
    }

    /**
	 * Copies the contents of one file to another.
	 * 
	 * @param destination
	 *            Destination file
	 * @param source
	 *            Source file
	 * 
	 * @exception java.io.IOException
	 *                Thrown if there is an error copying the file.
	 */
    public static void copyFile(File destination, File source) throws java.io.IOException {
        copyFile(destination, source, false);
    }

    /**
	 * Copies the contents of one file to another.
	 * 
	 * @param destination
	 *            Destination file
	 * @param source
	 *            Source file
	 * @param deleteSourceFile
	 *            whether or not to delete the source file
	 * 
	 * @exception java.io.IOException
	 *                Thrown if there is an error copying the file.
	 */
    public static void copyFile(File destination, File source, boolean deleteSourceFile) throws java.io.IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);

            int len;
            byte[] buffer = new byte[4096];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
            throw e;
        } finally {
            in.close();
            out.close();
        }

        if (deleteSourceFile) {
            source.delete();
        }
    }

    /**
     * Recursively delete a file and all its contents.
     *
     * @param root the root to delete
     */
    public static void recursiveDelete(File root) {
        if (root == null) {
            return;
        }

        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        recursiveDelete(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        root.delete();
    }
}
