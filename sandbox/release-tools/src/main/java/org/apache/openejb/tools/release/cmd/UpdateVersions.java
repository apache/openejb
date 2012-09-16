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
import org.apache.openejb.tools.release.util.IO;
import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.ReplaceStringInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.apache.openejb.tools.release.util.Exec.cd;
import static org.apache.openejb.tools.release.util.Files.collect;

/**
 * @version $Rev$ $Date$
 */
@Command(dependsOn = Close.class)
public class UpdateVersions {

    public static void main(String... args) throws Exception {

        final File workingCopy = cd(new File("/Users/dblevins/work/openejb-4.0.0-branch"));

        updateVersions2(workingCopy);

    }

    private static void updateVersions(File workingCopy) throws IOException {

        final List<File> files = collect(workingCopy, ".*pom.xml");

        for (File file : files) {
            if (file.getAbsolutePath().contains("/maven/")) continue;
            if (file.getAbsolutePath().contains("/examples/")) continue;

            InputStream in = IO.read(file);

            in = new DelimitedTokenReplacementInputStream(in, "<parent>", "</parent>", new StringTokenHandler() {
                @Override
                public String handleToken(String s) throws IOException {

                    if (s.contains("tomee")) {
                        s = s.replaceAll("<version>.*</version>", "<version>1.0.1-SNAPSHOT</version>");
                    } else if (s.contains("<groupId>org.apache.openejb")) {
                        s = s.replaceAll("<version>.*</version>", "<version>4.0.1-SNAPSHOT</version>");
                    }
                    return "<parent>" + s + "</parent>";
                }
            });

            in = new DelimitedTokenReplacementInputStream(in, "<dependency>", "</dependency>", new StringTokenHandler() {
                @Override
                public String handleToken(String s) throws IOException {

                    if (s.contains("tomee")) {
                        s = s.replaceAll("<version>.*</version>", "<version>\\${tomee.version}</version>");

                    } else if (s.contains("<groupId>org.apache.openejb.patch") && s.contains("xbean")) {

                        s = s.replaceAll("<groupId>.*</groupId>", "<groupId>org.apache.xbean</groupId>");
                        s = s.replaceAll("<version>.*</version>", "<version>\\${xbean.version}</version>");

                    } else if (s.contains("<groupId>org.apache.xbean")) {

                        s = s.replaceAll("<groupId>.*</groupId>", "<groupId>org.apache.xbean</groupId>");
                        s = s.replaceAll("<version>.*</version>", "<version>\\${xbean.version}</version>");

                    } else if (s.contains("<groupId>org.apache.openejb<") && !s.contains("javaee")) {

                        s = s.replaceAll("<version>.*</version>", "<version>\\${openejb.version}</version>");

                    }
                    return "<dependency>" + s + "</dependency>";
                }
            });

            in = new ReplaceStringInputStream(in, "<xbeanVersion>3.10</xbeanVersion>", "<xbean.version>3.10</xbean.version>");

            update(file, in);
        }

    }

    private static void updateVersions2(File workingCopy) throws IOException {

        final List<File> files = collect(workingCopy, ".*pom.xml");

        for (File file : files) {
            if (!file.getAbsolutePath().contains("/examples/")) continue;

            InputStream in = IO.read(file);

            in = new DelimitedTokenReplacementInputStream(in, "<parent>", "</parent>", new StringTokenHandler() {
                @Override
                public String handleToken(String s) throws IOException {

                    if (s.contains("<version>1.0</version>")) {
                        s = s.replaceAll("<version>.*</version>", "<version>1.0-SNAPSHOT</version>");
                    }
                    return "<parent>" + s + "</parent>";
                }
            });


            in = new DelimitedTokenReplacementInputStream(in, "<dependency>", "</dependency>", new StringTokenHandler() {
                @Override
                public String handleToken(String s) throws IOException {

                    if (s.contains("tomee")) {
                        s = s.replaceAll("<version>.*</version>", "<version>1.0.1-SNAPSHOT</version>");

                    } else if (s.contains("<groupId>org.apache.openejb<") && !s.contains("javaee")) {

                        s = s.replaceAll("<version>.*</version>", "<version>4.0.1-SNAPSHOT</version>");

                    }
                    return "<dependency>" + s + "</dependency>";
                }
            });

            in = new ReplaceStringInputStream(in, "<openejb.version>.*</openejb.version>", "<openejb.version>4.0.1-SNAPSHOT</openejb.version>");
            in = new ReplaceStringInputStream(in, "<tomee.version>.*</tomee.version>", "<tomee.version>1.0.1-SNAPSHOT</tomee.version>");

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
