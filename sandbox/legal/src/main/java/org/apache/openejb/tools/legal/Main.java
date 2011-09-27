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
package org.apache.openejb.tools.legal;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.swizzle.stream.StreamLexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @version $Rev$ $Date$
 */
public class Main {

    static {
        Logger root = Logger.getRootLogger();

        root.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
        root.setLevel(Level.INFO);
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Main.class);


    private final DefaultHttpClient client;
    private final File local;
    private final URI repo;
    private final File localRepo;

    public Main(String... args) throws Exception {
        client = new DefaultHttpClient();
        local = File.createTempFile("repository-check", "local");
        assert local.delete();
        assert local.mkdirs();

        localRepo = new File(local, "repo");
        assert localRepo.mkdirs();

        repo = new URI(args[0]);
    }

    public static void main(String[] args) throws Exception {
        new Main("https://repository.apache.org/content/repositories/orgapacheopenejb-094").main();
    }

    private void main() throws Exception {
        // https://repository.apache.org/content/repositories/orgapacheopenejb-094

        final URI index = new URI("https://repository.apache.org/content/repositories/orgapacheopenejb-094");

        final Set<URI> resources = crawl(index);

        final Set<File> files = new HashSet<File>();

        for (URI uri : resources) {
            files.add(download(uri));
        }

        for (File file : files) {
            unpack(file);
        }
    }

    private void unpack(File archive) throws IOException {
        log.info("Unpack " + archive);

        try {
            final ZipInputStream zip = IOUtil.unzip(archive);

            final File contents = new File(archive.getAbsolutePath() + ".contents");
            assert contents.mkdir();

            try {
                ZipEntry entry = null;

                while ((entry = zip.getNextEntry()) != null) {
                    final String path = entry.getName();

                    final File fileEntry = new File(contents, path);

                    mkdirs(fileEntry);

                    // Open the output file

                    IOUtil.copy(zip, fileEntry);

                    if (fileEntry.getName().endsWith(".jar")) {
                        unpack(fileEntry);
                    }
                }
            } finally {
                IOUtil.close(zip);
            }
        } catch (IOException e) {
            log.error("Not a zip " + archive);
        }
    }

    private File download(URI uri) throws IOException {
        log.info("Download " + uri);

        final HttpResponse response = get(uri);

        final InputStream content = response.getEntity().getContent();
        final String name = uri.toString().replace(repo.toString(), "").replaceFirst("^/", "");

        final File file = new File(localRepo, name);

        mkdirs(file);

        IOUtil.copy(content, file);

        return file;
    }

    private void mkdirs(File file) {

        final File parent = file.getParentFile();

        if (!parent.exists()) {
            assert parent.mkdirs() : "mkdirs " + parent;
            return;
        }

        assert parent.isDirectory() : "not a directory" + parent;
    }

    private HttpResponse get(URI uri) throws IOException {
        final HttpGet request = new HttpGet(uri);
        request.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        return client.execute(request);
    }

    private Set<URI> crawl(URI index) throws IOException {
        log.info("Crawl " + index);
        final Set<URI> resources = new LinkedHashSet<URI>();

        final HttpResponse response = get(index);

        final InputStream content = response.getEntity().getContent();
        final StreamLexer lexer = new StreamLexer(content);

        final Set<URI> crawl = new LinkedHashSet<URI>();

        //<a href="https://repository.apache.org/content/repositories/orgapacheopenejb-094/archetype-catalog.xml">archetype-catalog.xml</a>
        while (lexer.readAndMark("<a ", "/a>")) {

            try {
                final String link = lexer.peek("href=\"", "\"");
                final String name = lexer.peek(">", "<");

                final URI uri = index.resolve(link);

                if (name.equals("../")) continue;
                if (link.equals("../")) continue;

                if (name.endsWith("/")) {
                    crawl.add(uri);
                    continue;
                }

                if (!uri.getPath().matches(".*(jar|zip|war|tar.gz)")) continue;

                resources.add(uri);

            } finally {
                lexer.unmark();
            }
        }

        content.close();

        for (URI uri : crawl) {
            resources.addAll(crawl(uri));
        }
        return resources;
    }


}
