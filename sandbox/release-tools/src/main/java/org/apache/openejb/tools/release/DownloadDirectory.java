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
package org.apache.openejb.tools.release;

import org.apache.log4j.Logger;
import org.apache.rat.tentacles.NexusClient;
import org.apache.xbean.finder.UriSet;

import java.io.File;
import java.net.URI;

/**
 * Little utility that downloads the binaries into
 */
public class DownloadDirectory {

    public static void main(String[] args) throws Exception {
        final NexusClient client = new NexusClient();

        final Options options = new Options(System.getProperties());
        options.setLogger(new Log4jLog(Logger.getLogger(DownloadDirectory.class)));

        final File dir = options.get("dir", new File("/tmp/openejb-4.0.0-beta-2"));
        final URI repo = options.get("staging", URI.create("https://repository.apache.org/content/repositories/orgapacheopenejb-029/"));

        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot make directory: " + dir.getAbsolutePath());


        final UriSet all = new UriSet(client.crawl(repo));

        UriSet binaries = all.include(".*\\.(zip|gz|war).*");
        binaries = binaries.exclude(".*\\.asc\\.(sha1|md5)");

        for (URI uri : binaries.include(".*\\/(openejb-standalone|openejb-tomcat|apache-tomee|examples)-.*|.*source-release.*")) {
            final File file = new File(dir, uri.getPath().replaceAll(".*/", ""));
            client.download(uri, file);
        }
    }

}
