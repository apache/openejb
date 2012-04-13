/*
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
package org.apache.openejb.tools.release.cmd;

import org.apache.openejb.tools.release.util.Exec;
import org.apache.openejb.tools.release.util.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @version $Rev$ $Date$
 */
public class CompareLibraries {

    public static void main(String... args) throws IOException {
        final File repository = Files.file(System.getProperty("user.home"), ".m2", "repository", "org", "apache", "openejb");

        // /Users/dblevins/.m2/repository/org/apache/openejb/apache-tomee/1.0.0/apache-tomee-1.0.0-webprofile.zip

        diff(repository, "apache-tomee", "1.0.0-beta-2", "1.0.0", "webprofile");

        diff(repository, "apache-tomee", "1.0.0-beta-2", "1.0.0", "plus");

        diff(repository, "openejb-standalone", "4.0.0-beta-2", "4.0.0", null);


    }

    private static void diff(File repository, final String artifactId, final String versionA, final String versionB, final String classifier) throws IOException {
        System.out.printf("\n%s %s %s\n\n", artifactId, versionB, (classifier == null) ? "" : classifier);

        final File previous = artifact(repository, artifactId, versionA, classifier);
        final File current = artifact(repository, artifactId, versionB, classifier);

        final Map<String, File> a = libraries(previous);
        final Map<String, File> b = libraries(current);

        for (String key : a.keySet()) {
            if (b.containsKey(key)) continue;
            System.out.printf("  D %s\n", path(a, key));
        }

        for (String key : b.keySet()) {
            if (a.containsKey(key)) continue;
            System.out.printf("  A %s\n", path(b, key));
        }

        System.out.println();
        final long change = current.length() - previous.length();
        System.out.printf("  change: %s%.2f MB\n", change > 0 ? "+" : "", toMB(change));
        System.out.printf("  total : %.2f MB\n\n", toMB(current.length()));

    }

    private static double toMB(final double length) {
        return length / 1024 / 1024;
    }

    private static String path(Map<String, File> b, String key) {
        return b.get(key).getName();
//        return b.get(key).getAbsolutePath().replace(new File("").getAbsolutePath(),"");
    }

    private static Map<String, File> libraries(final File file) throws IOException {

        final Map<String, File> map = new TreeMap<String, File>();
        for (File jar : list(file)) {
            final String name = jar.getName().replaceFirst("-[0-9].+", "");
            map.put(name, jar);
        }

        return map;
    }

    private static File artifact(File repository, String artifactId, String version, String classifier) {

        final String zip = classifier != null ? artifactId + "-" + version + "-" + classifier + ".zip" : artifactId + "-" + version + ".zip";

        final File file = Files.file(repository, artifactId, version, zip);

        if (file.exists()) return file;

        // download the artifact
        final String artifact;

        if (classifier != null) {
            artifact = "-Dartifact=" + String.format("org.apache.openejb:%s:%s:%s:%s", artifactId, version, "zip", classifier);
        } else {
            artifact = "-Dartifact=" + String.format("org.apache.openejb:%s:%s:%s", artifactId, version, "zip");
        }

        final int i = Exec.exec("mvn", "-X", "org.apache.maven.plugins:maven-dependency-plugin:2.4:get", "-DrepoUrl=http://repo1.maven.apache.org/maven2", artifact);

        if (i != 0) {
            throw new IllegalStateException("Download failed: " + i);
        }

        return artifact(repository, artifactId, version, classifier);
    }

    private static List<File> list(File previousFile) throws IOException {
        final List<File> files = new ArrayList<File>();

        final ZipFile previousZip = new ZipFile(previousFile);
        final ArrayList<? extends ZipEntry> list = Collections.list(previousZip.entries());
        for (ZipEntry entry : list) {
            if (entry.isDirectory()) continue;
            if (!entry.getName().endsWith(".jar")) continue;
            files.add(new File(entry.getName()));
        }
        return files;
    }
}
