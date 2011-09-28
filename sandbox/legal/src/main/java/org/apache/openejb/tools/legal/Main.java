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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.swizzle.stream.StreamLexer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final URI staging;
    private final File repository;
    private final File content;
    private Reports reports;
    private Map<String, String> licenses = new HashMap<String, String>();


    public Main(String... args) throws Exception {
        client = new DefaultHttpClient();

        this.staging = new URI(args[0]);

        String name = new File(this.staging.getPath()).getName();

        if (args.length > 1) {
            this.local = new File(args[1]);
        } else {
            this.local = new File(name);
        }

        mkdirs(local);

        this.repository = new File(local, "repo");
        this.content = new File(local, "content");

        mkdirs(repository);
        mkdirs(content);

        log.info("Repo: " + staging);
        log.info("Local: " + local);

        this.reports = new Reports();

        final URL style = this.getClass().getClassLoader().getResource("legal/style.css");
        IOUtil.copy(style.openStream(), new File(local, "style.css"));

        licenses("asl-2.0");
        licenses("cpl-1.0");
        licenses("cddl-1.0");
    }

    private void licenses(String s) throws IOException {
        URL aslURL = this.getClass().getClassLoader().getResource("licenses/" + s + ".txt");
        licenses.put(s, IOUtil.slurp(aslURL).trim());
    }

    public static void main(String[] args) throws Exception {
        new Main(args).main();
    }

    private void main() throws Exception {

        prepare();

        final List<File> jars = collect(repository, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        final List<Archive> archives = new ArrayList<Archive>();
        for (File file : jars) {
            final Archive archive = new Archive(file);
            archives.add(archive);
        }

        Templates.template("archives.vm")
                .add("archives", archives)
                .add("reports", reports)
                .write(new File(local, "archives.html"));

        reportLicenses(archives);
        reportNotices(archives);
        reportDeclaredLicenses(archives);
        reportDeclaredNotices(archives);
    }

    private void reportLicenses(List<Archive> archives) throws IOException {
        Map<License, License> licenses = new HashMap<License, License>();

        for (Archive archive : archives) {
            List<File> files = collect(contents(archive.getFile()), new LicenseFilter());
            for (File file : files) {
                final License license = new License(IOUtil.slurp(file));

                License existing = licenses.get(license);
                if (existing == null) {
                    licenses.put(license, license);
                    existing = license;
                }

                existing.locations.add(file);
                existing.getArchives().add(archive);
                archive.getLicenses().add(existing);
            }
        }

        Templates.template("licenses.vm")
                .add("licenses", licenses.values())
                .add("reports", reports)
                .write(new File(local, "licenses.html"));
    }

    private void reportDeclaredLicenses(List<Archive> archives) throws IOException {

        for (Archive archive : archives) {

            final Set<License> undeclared = new HashSet<License>(archive.getLicenses());

            final File contents = contents(archive.getFile());
            final List<File> files = collect(contents, new Filters(new DeclaredFilter(contents), new LicenseFilter()));

            for (File file : files) {

                final License license = new License(IOUtil.slurp(file));

                undeclared.remove(license);

            }

            archive.getOtherLicenses().addAll(undeclared);

            final Set<License> declared = new HashSet<License>(archive.getLicenses());
            declared.removeAll(undeclared);
            archive.getDeclaredLicenses().addAll(declared);


            for (License license : undeclared) {

                for (License declare : declared) {
                    if (license.implies(declare)) {
                        archive.getOtherLicenses().remove(license);
                    }
                }
            }

            Templates.template("archive-licenses.vm")
                    .add("archive", archive)
                    .add("reports", reports)
                    .write(new File(local, reports.licenses(archive)));
        }

    }

    private void reportDeclaredNotices(List<Archive> archives) throws IOException {

        for (Archive archive : archives) {

            final Set<Notice> undeclared = new HashSet<Notice>(archive.getNotices());

            final File contents = contents(archive.getFile());
            final List<File> files = collect(contents, new Filters(new DeclaredFilter(contents), new NoticeFilter()));

            for (File file : files) {

                final Notice notice = new Notice(IOUtil.slurp(file));

                undeclared.remove(notice);
            }

            archive.getOtherNotices().addAll(undeclared);

            final Set<Notice> declared = new HashSet<Notice>(archive.getNotices());
            declared.removeAll(undeclared);
            archive.getDeclaredNotices().addAll(declared);

            for (Notice notice : undeclared) {

                for (Notice declare : declared) {
                    if (notice.implies(declare)) {
                        archive.getOtherLicenses().remove(notice);
                    }
                }
            }


            Templates.template("archive-notices.vm")
                    .add("archive", archive)
                    .add("reports", reports)
                    .write(new File(local, reports.notices(archive)));
        }
    }

    private void reportNotices(List<Archive> archives) throws IOException {
        Map<Notice, Notice> notices = new HashMap<Notice, Notice>();

        for (Archive archive : archives) {
            List<File> files = collect(contents(archive.getFile()), new NoticeFilter());
            for (File file : files) {
                final Notice notice = new Notice(IOUtil.slurp(file));

                Notice existing = notices.get(notice);
                if (existing == null) {
                    notices.put(notice, notice);
                    existing = notice;
                }

                existing.locations.add(file);
                existing.getArchives().add(archive);
                archive.getNotices().add(existing);
            }
        }

        Templates.template("notices.vm")
                .add("notices", notices.values())
                .add("reports", reports)
                .write(new File(local, "notices.html"));
    }

    public class Reports {
        public String licenses(Archive archive) {
            return archive.uri.toString().replace('/', '.') + ".licenses.html";
        }

        public String notices(Archive archive) {
            return archive.uri.toString().replace('/', '.') + ".notices.html";
        }

    }


    private List<URI> allNoticeFiles() {
        List<File> legal = collect(content, new LegalFilter());
        for (File file : legal) {
            log.info("Legal " + file);
        }

        URI uri = local.toURI();
        List<URI> uris = new ArrayList<URI>();
        for (File file : legal) {
            URI full = file.toURI();
            URI relativize = uri.relativize(full);
            uris.add(relativize);
        }
        return uris;
    }

    private void prepare() throws URISyntaxException, IOException {

        final Set<URI> resources = crawl(staging);

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

            final File contents = contents(archive);

            try {
                ZipEntry entry = null;

                while ((entry = zip.getNextEntry()) != null) {

                    if (entry.isDirectory()) continue;

                    final String path = entry.getName();

                    final File fileEntry = new File(contents, path);

                    mkparent(fileEntry);

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

    public class License {
        private final String text;
        private String key;
        private Set<Archive> archives = new HashSet<Archive>();
        private List<File> locations = new ArrayList<File>();

        public License(String text) {
            key = text.replaceAll("[ \\n\\t\\r]+", "").toLowerCase().intern();

            for (Map.Entry<String, String> license : licenses.entrySet()) {
                text = text.replace(license.getValue(), String.format("---[%s - full text]---\n\n", license.getKey()));
            }
            this.text = text.intern();
        }

        public String getText() {
            return text;
        }

        public String getKey() {
            return key;
        }

        public Set<Archive> getArchives() {
            return archives;
        }

        public Set<URI> locations(Archive archive) {
            URI contents = contents(archive.getFile()).toURI();
            Set<URI> locations = new HashSet<URI>();
            for (File file : this.locations) {
                URI uri = file.toURI();
                URI relativize = contents.relativize(uri);
                if (!relativize.equals(uri)) {
                    locations.add(relativize);
                }
            }

            return locations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            License license = (License) o;

            if (!key.equals(license.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        public boolean implies(License fullLicense) {
            return fullLicense.key.contains(this.key);
        }
    }

    public class Notice {
        private final String text;
        private String key;
        private Set<Archive> archives = new HashSet<Archive>();
        private List<File> locations = new ArrayList<File>();

        public Notice(String text) {
            this.text = text.intern();
            key = text.replaceAll("[ \\n\\t\\r]+", "").toLowerCase().intern();
        }

        public String getText() {
            return text;
        }

        public String getKey() {
            return key;
        }

        public Set<Archive> getArchives() {
            return archives;
        }

        public Set<URI> locations(Archive archive) {
            URI contents = contents(archive.getFile()).toURI();
            Set<URI> locations = new HashSet<URI>();
            for (File file : this.locations) {
                URI uri = file.toURI();
                URI relativize = contents.relativize(uri);
                if (!relativize.equals(uri)) {
                    locations.add(relativize);
                }
            }

            return locations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Notice notice = (Notice) o;

            if (!key.equals(notice.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        public boolean implies(Notice fullLicense) {
            return fullLicense.key.contains(this.key);
        }

    }

    public List<File> collect(File dir, FileFilter filter) {
        final List<File> accepted = new ArrayList<File>();
        if (filter.accept(dir)) accepted.add(dir);

        final File[] files = dir.listFiles();
        if (files != null) for (File file : files) {
            accepted.addAll(collect(file, filter));
        }

        return accepted;
    }

    private File contents(File archive) {
        String path = archive.getAbsolutePath().substring(local.getAbsolutePath().length() + 1);

        if (path.startsWith("repo/")) path = path.substring("repo/".length());
        if (path.startsWith("content/")) path = path.substring("content/".length());

        final File contents = new File(content, path + ".contents");
        mkdirs(contents);
        return contents;
    }

    private File download(URI uri) throws IOException {

        final File file = getFile(uri);

        if (file.exists()) {

            long length = getConentLength(uri);

            if (file.length() == length) {
                log.info("Exists " + uri);
                return file;
            } else {
                log.info("Incomplete " + uri);
            }
        }

        log.info("Download " + uri);

        final HttpResponse response = get(uri);

        final InputStream content = response.getEntity().getContent();

        mkparent(file);

        IOUtil.copy(content, file);

        return file;
    }

    private static class LegalFilter implements FileFilter {

        private static final NoticeFilter notice = new NoticeFilter();
        private static final LicenseFilter license = new LicenseFilter();

        @Override
        public boolean accept(File pathname) {
            return notice.accept(pathname) || license.accept(pathname);
        }
    }

    private static class NoticeFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            final String name = pathname.getName().toLowerCase();

            if (name.equals("notice")) return true;
            if (name.equals("notice.txt")) return true;

            return false;
        }
    }

    private static class LicenseFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            final String name = pathname.getName().toLowerCase();

            if (name.equals("license")) return true;
            if (name.equals("license.txt")) return true;

            return false;
        }
    }

    private static class DeclaredFilter implements FileFilter {
        private final File file;

        private DeclaredFilter(File file) {
            this.file = file;
        }

        @Override
        public boolean accept(File file) {
            while (file != null) {
                if (file.equals(this.file)) break;

                if (file.isDirectory() && file.getName().endsWith(".contents")) return false;
                file = file.getParentFile();
            }

            return true;
        }
    }

    private static class Filters implements FileFilter {

        List<FileFilter> filters = new ArrayList<FileFilter>();

        private Filters(FileFilter... filters) {
            for (FileFilter filter : filters) {
                this.filters.add(filter);
            }
        }

        @Override
        public boolean accept(File file) {
            for (FileFilter filter : filters) {
                if (!filter.accept(file)) return false;
            }

            return true;
        }
    }

    public class Archive {

        private final URI uri;
        private final File file;
        private final Map<URI, URI> map;

        private final Set<License> licenses = new HashSet<License>();
        private final Set<Notice> notices = new HashSet<Notice>();

        private final Set<License> declaredLicenses = new HashSet<License>();
        private final Set<Notice> declaredNotices = new HashSet<Notice>();

        private final Set<License> otherLicenses = new HashSet<License>();
        private final Set<Notice> otherNotices = new HashSet<Notice>();

        public Archive(File file) {
            this.uri = repository.toURI().relativize(file.toURI());
            this.file = file;
            this.map = map();
        }

        public Set<License> getDeclaredLicenses() {
            return declaredLicenses;
        }

        public Set<Notice> getDeclaredNotices() {
            return declaredNotices;
        }

        public Set<License> getOtherLicenses() {
            return otherLicenses;
        }

        public Set<Notice> getOtherNotices() {
            return otherNotices;
        }

        public Set<License> getLicenses() {
            return licenses;
        }

        public Set<Notice> getNotices() {
            return notices;
        }

        public URI getUri() {
            return uri;
        }

        public File getFile() {
            return file;
        }

        public Map<URI, URI> getLegal() {
            return map;
        }

        private Map<URI, URI> map() {
            final File jarContents = contents(file);
            final List<File> legal = collect(jarContents, new LegalFilter());

            Map<URI, URI> map = new LinkedHashMap<URI, URI>();
            for (File file : legal) {
                URI name = jarContents.toURI().relativize(file.toURI());
                URI link = local.toURI().relativize(file.toURI());

                map.put(name, link);
            }
            return map;
        }
    }

    private long getConentLength(URI uri) throws IOException {
        HttpResponse head = head(uri);
        Header[] headers = head.getHeaders("Content-Length");

        for (Header header : headers) {
            return new Long(header.getValue());
        }

        return -1;
    }

    private File getFile(URI uri) {
        final String name = uri.toString().replace(staging.toString(), "").replaceFirst("^/", "");
        return new File(repository, name);
    }

    private void mkparent(File file) {
        mkdirs(file.getParentFile());
    }

    private void mkdirs(File file) {

        if (!file.exists()) {

            assert file.mkdirs() : "mkdirs " + file;

            return;
        }

        assert file.isDirectory() : "not a directory" + file;
    }

    private HttpResponse get(URI uri) throws IOException {
        final HttpGet request = new HttpGet(uri);
        request.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        return client.execute(request);
    }

    private HttpResponse head(URI uri) throws IOException {
        final HttpHead request = new HttpHead(uri);
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

                if (!uri.getPath().matches(".*(jar|zip|war|ear|tar.gz)")) continue;

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
