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
import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.ReplaceStringInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.String.format;
import static org.apache.openejb.tools.release.util.Exec.cd;
import static org.apache.openejb.tools.release.util.Exec.exec;

/**
 * @version $Rev$ $Date$
 */
@Command(dependsOn = Close.class)
public class Tck {

    public static void main(String... args) throws Exception {

        // https://svn.apache.org/repos/tck/openejb-tck/branches/tomee-1.0.0-beta-2

        final File dir = new File("/tmp/release/tck");
        Files.mkdir(dir);

        cd(dir);

        final String branch = Release.tckBranches + Release.tomeeVersionName;

        // Make the branch
        if (exec("svn", "info", branch) != 0) {

            exec("svn", "-m", format("[release-tools] tck branch for %s", Release.tomeeVersionName), "cp", Release.tckTrunk.toString(), branch);
        }

        // Checkout the branch
        exec("svn", "co", branch);

        final File tck = cd(new File(dir + "/" + Release.tomeeVersionName));

        updatePom(Release.staging, new File(tck, "pom.xml"));

        updateWebProfile(branch, tck, Release.tomeeVersionName);

        exec("svn", "-m", "[release-tools] update staging repo for " + Release.tomeeVersionName, "ci");
    }

    private static void updateWebProfile(final String branch, final File dir, String version) throws IOException {
        final File properties = new File(dir, "webprofile.properties");

        InputStream in = IO.read(properties);
        in = replaceProperty(in, "project.scmUrl", String.format("scm:svn:%s", branch));
        in = replaceProperty(in, "project.version", version);

        update(properties, in);
    }

    private static DelimitedTokenReplacementInputStream replaceProperty(InputStream in, final String property, final String value) {
        final String n = System.getProperty("line.separator");
        return new DelimitedTokenReplacementInputStream(in, property, n, new StringTokenHandler() {
            @Override
            public String handleToken(String token) throws IOException {
                return String.format("%s = %s%s", property, value, n);
            }
        });
    }

    private static void updatePom(String staging, File pom) throws IOException {

        System.out.println(pom.getAbsolutePath());

        final String n = System.getProperty("line.separator");

        final String repositoryDefinition = "    <repository>" + n +
                "      <id>nexus-staging</id>" + n +
                "      <name>Nexus Staging Repository</name>" + n +
                "      <url>" + staging + "</url>" + n +
                "      <layout>default</layout>" + n +
                "      <snapshots>" + n +
                "        <enabled>false</enabled>" + n +
                "      </snapshots>" + n +
                "      <releases>" + n +
                "        <enabled>true</enabled>" + n +
                "        <checksumPolicy>ignore</checksumPolicy>" + n +
                "      </releases>" + n +
                "    </repository>";

        InputStream in = IO.read(pom);

        // Yank any existing nexus-staging repo
        in = new DelimitedTokenReplacementInputStream(in, "<repository>", "</repository>", new StringTokenHandler() {
            @Override
            public String handleToken(String token) throws IOException {
                if (token.contains("<id>nexus-staging</id>")) {
                    return "";
                }

                return "<repository>" + token + "</repository>";
            }
        });

        // add the new one
        in = new ReplaceStringInputStream(in, "<repositories>", "<repositories>" + n + repositoryDefinition);

        // Yank SNAPSHOT
        in = new ReplaceStringInputStream(in, "-SNAPSHOT", "");

        update(pom, in);
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
