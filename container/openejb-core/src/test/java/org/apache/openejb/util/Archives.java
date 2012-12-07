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
package org.apache.openejb.util;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


/**
 * @version $Rev$ $Date$
 */
public class Archives {

    public static File fileArchive(Class[] classes) throws IOException {
        return fileArchive(new HashMap<String, String>(), classes);
    }

    public static File fileArchive(Map<String, String> entries, Class... classes) throws IOException {
        return fileArchive(entries, Collections.EMPTY_LIST, classes);
    }

    public static File fileArchive(Map<String, String> entries, final List<String> parts, Class... classes) throws IOException {

        ClassLoader loader = Archives.class.getClassLoader();

        File classpath = File.createTempFile("test", "archive");
        Files.deleteOnExit(classpath);
        assertTrue(classpath.delete());
        assertTrue(classpath.mkdirs());

        for (String part : parts) {
            classpath = new File(classpath, part);
        }

        System.out.println("Archive file path:" + classpath.getCanonicalPath());

        for (Class clazz : classes) {
            String name = clazz.getName().replace('.', File.separatorChar) + ".class";
            File file = new File(classpath, name);

            File d = file.getParentFile();

            if (!d.exists()) assertTrue(d.getAbsolutePath(), d.mkdirs());

            URL resource = loader.getResource(name);
            assertNotNull(resource);

            IO.copy(IO.read(resource), file);
        }

        for (Map.Entry<String, String> entry : entries.entrySet()) {

            final String key = entry.getKey().replace('/', File.separatorChar);

            final File file = new File(classpath, key);

            File d = file.getParentFile();

            if (!d.exists()) assertTrue(d.getAbsolutePath(), d.mkdirs());

            IO.copy(entry.getValue().getBytes(), file);
        }

        return classpath;
    }

    public static File jarArchive(Class... classes) throws IOException {
        return jarArchive(new HashMap<String, String>(), "temp", classes);
    }


    public static File jarArchive(Map<String, ?> entries, String archiveNamePrefix, Class... classes) throws IOException {

        File classpath = File.createTempFile(archiveNamePrefix, ".jar");
        classpath.deleteOnExit();

        return jarArchive(classpath, entries, classes);
    }

    public static File jarArchive(File archive, Map<String, ?> entries, Class... classes) throws IOException {
        final ClassLoader loader = Archives.class.getClassLoader();

        // Create the ZIP file
        final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive)));

        for (Class clazz : classes) {
            String name = clazz.getName().replace('.', File.separatorChar) + ".class";

            URL resource = loader.getResource(name);
            assertNotNull(resource);

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(name));

            InputStream in = new BufferedInputStream(resource.openStream());

            int i = -1;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            // Complete the entry
            in.close();
            out.closeEntry();
        }

        for (Map.Entry<String, ?> entry : entries.entrySet()) {

            out.putNextEntry(new ZipEntry(entry.getKey()));

            final Object value = entry.getValue();

            if (value instanceof String) {

                String s = (String) value;
                out.write(s.getBytes());

            } else if (value instanceof File) {

                final File file = (File) value;
                if (file.isDirectory()) throw new IllegalArgumentException(entry.getKey() + " is a directory, not a file.");
                IO.copy(file, out);

            } else if (value instanceof URL) {

                IO.copy((URL)value, out);

            }

            out.closeEntry();
        }

        // Complete the ZIP file
        out.close();

        return archive;
    }
}