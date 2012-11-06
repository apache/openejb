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
import org.apache.creadur.tentacles.NexusClient;
import org.apache.xbean.finder.UriSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Little utility that downloads the binaries into
 */
@Command(dependsOn = Close.class)
public class Binaries {

    public static void main(String[] args) throws Exception {
        final NexusClient client = new NexusClient();

        final File dir = Files.file(Release.builddir, "staging-" + Release.build, Release.openejbVersionName);
        final URI repo = URI.create(Release.staging);

        System.out.println("Downloads: " + dir.getAbsolutePath());

        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot make directory: " + dir.getAbsolutePath());


        final UriSet all = new UriSet(client.crawl(repo));

        UriSet binaries = all.include(".*\\.(zip|gz|war).*");
        binaries = binaries.exclude(".*\\.asc\\.(sha1|md5)");


        for (URI uri : binaries.include(".*\\/(tomee|openejb-|apache-tomee|examples)-.*|.*source-release.*")) {
            final File file = new File(dir, uri.getPath().replaceAll(".*/", ""));
            System.out.println("Downloading " + file.getName());
            client.download(uri, file);

            if (file.getName().endsWith(".zip")) {
                final PrintStream out = new PrintStream(IO.write(new File(file.getAbsolutePath() + ".txt")));

                list(file, out);
                out.close();
            }
        }
    }

    private static void list(File file, PrintStream out) throws IOException {
        final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        final ZipFile zip = new ZipFile(file);
        final Enumeration<? extends ZipEntry> enumeration = zip.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry entry = enumeration.nextElement();
            out.printf("%1$7s %2$2s %3$2s", entry.getSize(), format.format(entry.getTime()), entry.getName() );
            out.println();
        }
    }

}
