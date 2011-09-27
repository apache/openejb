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
import org.codehaus.swizzle.stream.StreamLexer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class Main {

    private DefaultHttpClient client;

    public Main() {
        client = new DefaultHttpClient();
    }

    public static void main(String[] args) throws Exception {
        new Main()._main(args);
    }

    private void _main(String... args) throws Exception {
        // https://repository.apache.org/content/repositories/orgapacheopenejb-094

        final URI index = new URI("https://repository.apache.org/content/repositories/orgapacheopenejb-094");

        final Set<URI> resources = crawl(index);

        for (URI uri : resources) {
            System.out.println(uri);
        }

    }

    private Set<URI> crawl(URI index) throws IOException {
        final Set<URI> resources = new LinkedHashSet<URI>();

        final HttpGet request = new HttpGet(index);
        request.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        final HttpResponse response = client.execute(request);

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

                if (!uri.getPath().matches(".*(jar|zip|tar.gz)")) continue;

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
