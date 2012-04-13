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

import org.apache.openejb.tools.release.Command;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.Files;
import org.apache.openejb.tools.release.util.IO;
import org.codehaus.swizzle.stream.ReplaceStringInputStream;
import org.codehaus.swizzle.stream.StreamLexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.openejb.tools.release.util.Exec.cd;
import static org.apache.openejb.tools.release.util.Exec.exec;
import static org.apache.openejb.tools.release.util.Files.collect;

/**
 * @version $Rev$ $Date$
 */
@Command(dependsOn = Close.class)
public class AdvanceVersions {

    public static void main(String... args) throws Exception {

        final File dir = new File(Release.workdir);
        Files.mkdir(dir);

        cd(dir);

        exec("svn", "co", Release.trunk);

        final File workingCopy = cd(new File(dir + File.separator + "openejb"));

        final Set<String> versions = collectVersions(workingCopy);

        for (String version : versions) {
            System.out.println(version);
        }

        updateVersions(workingCopy);

//        exec("svn", "-m", "[release-tools] update staging repo for " + Release.openejbVersionName, "ci");
    }

    private static Set<String> collectVersions(File workingCopy) throws IOException {
        Set<String> versions = new HashSet<String>();


        final List<File> files = collect(workingCopy, ".*pom.xml");

        for (File file : files) {
            final InputStream read = IO.read(file);
            final StreamLexer lexer = new StreamLexer(read);

            while (lexer.readAndMark("<dependency>", "</dependency>")) {
                final String groupId = lexer.peek("<groupId>", "</groupId>");
                final String artifactId = lexer.peek("<artifactId>", "</artifactId>");
                final String version = lexer.peek("<version>", "</version>");
                lexer.unmark();
                if (version != null && version.contains("-SNAPSHOT")) versions.add(String.format("%s:%s:%s", groupId, artifactId, version));
            }

            read.close();
        }

        return versions;
    }

    private static void updateVersions(File workingCopy) throws IOException {

        final List<File> files = collect(workingCopy, ".*pom.xml");
        files.addAll(collect(workingCopy, ".*build.xml"));

        for (File file : files) {
            InputStream in = IO.read(file);

            in = new ReplaceStringInputStream(in, "4.0.0-beta-2-SNAPSHOT-SNAPSHOT", "4.0.0-beta-3-SNAPSHOT");
            in = new ReplaceStringInputStream(in, "4.0.0-beta-2-SNAPSHOT", "4.0.0-beta-3-SNAPSHOT");
            in = new ReplaceStringInputStream(in, "1.0.0-beta-2-SNAPSHOT", "1.0.0-beta-3-SNAPSHOT");
            in = new ReplaceStringInputStream(in, "6.0-3-SNAPSHOT", "6.0-3");
            in = new ReplaceStringInputStream(in, "1.3-SNAPSHOT", "1.2");
            in = new ReplaceStringInputStream(in, "1.1.2-SNAPSHOT", "1.1.3");
            in = new ReplaceStringInputStream(in, "2.5.1-SNAPSHOT", "2.5.1");

            update(file, in);
        }

    }

    private static void update(File dest, InputStream in) throws IOException {
        final File tmp = new File(dest.getAbsolutePath() + "~");

        final OutputStream out = IO.write(tmp);

        int i = -1;
        while ((i = in.read()) != -1) {
            out.write(i);
        }
        out.close();
        if (!tmp.renameTo(dest)) throw new RuntimeException(String.format("Rename failed: mv \"%s\" \"%s\"", tmp.getAbsolutePath(), dest.getAbsolutePath()));
    }
}
