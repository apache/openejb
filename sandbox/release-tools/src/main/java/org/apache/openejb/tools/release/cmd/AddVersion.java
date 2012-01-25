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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.openejb.tools.release.Command;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.IO;
import org.codehaus.swizzle.stream.FixedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.StreamTokenHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Add the next revision
 * @version $Rev$ $Date$
 */
@Command
public class AddVersion {
    private static final String NUMBER = "\\p{Digit}+";
    private static final Pattern MAVEN_VERSION_PATTERN = Pattern.compile(NUMBER + "\\." + NUMBER + "\\.(" + NUMBER + ")");
    private static final Pattern BETA_PATTERN = Pattern.compile(".*-beta-(" + NUMBER + ")");
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    /**
     * can be ran in 2 cases:
     * 1) replacing -SNAPSHOT
     *  -> arg = set-snapshot
     * 2) setting new versions
     *  -> to inc last digit and add snapshot to the version: arg = replace-snapshot
     *  -> to simply inc last digit no arg is enough
     *
     * Note: version is replace like a sed so if another lib is using this version it will be replaced too
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        boolean setSnapshot = false;
        if (args.length > 0 && "set-snapshot".equals(args[0])) {
            setSnapshot = true;
        }

        boolean replaceSnapshot = false;
        if (args.length > 0 && "replace-snapshot".equals(args[0])) {
            replaceSnapshot = true;
        }

        final File workingDir = new File(Release.builddir, Release.openejbVersion);
        final String[][] replacements = new String[][] {
                new String[] { currentVersion(Release.openejbSimpleVersion, replaceSnapshot),
                                nextVersion("openejb next version", Release.openejbSimpleVersion, replaceSnapshot, setSnapshot) },
                new String[] { currentVersion(Release.tomeeSimpleVersion, replaceSnapshot),
                                nextVersion("openejb next version", Release.tomeeSimpleVersion, replaceSnapshot, setSnapshot) }
        };

        // we hardcode the tomee or openejb version in some java, properties, pom (of course ;))...so simply take eveything we can
        // filtering image we have (gif and png) and win exe
        // .svn and target folders are ignored too
        final Collection<File> files = FileUtils.listFiles(workingDir,
                new AndFileFilter(
                        new AndFileFilter(
                            new NotFileFilter(new SuffixFileFilter(".gif", IOCase.INSENSITIVE)),
                            new NotFileFilter(new SuffixFileFilter(".png", IOCase.INSENSITIVE))),
                        new NotFileFilter(new SuffixFileFilter(".exe", IOCase.INSENSITIVE))),
                new AndFileFilter(
                        new NotFileFilter(new NameFileFilter(".svn", IOCase.INSENSITIVE)),
                        new NotFileFilter(new NameFileFilter("target", IOCase.INSENSITIVE))));

        for (File file : files) {
            for (String[] replacement : replacements) {
                final CountingFixedStringValueTokenHandler handlerCounting = new CountingFixedStringValueTokenHandler(replacement[1]);
                update(file, new FixedTokenReplacementInputStream(IO.read(file), replacement[0], handlerCounting));
                final int nbReplacements = handlerCounting.getReplacements();
                if (nbReplacements > 0) {
                    System.out.println(String.format("replaced %d times %s => %s in %s", nbReplacements, replacement[0], replacement[1], file.getPath()));
                }
            }
        }
    }

    private static String currentVersion(String version, boolean appenSnapshot) {
        if (appenSnapshot && !version.endsWith(SNAPSHOT_SUFFIX)) {
            return version + SNAPSHOT_SUFFIX;
        }
        return version;
    }

    private static void update(File dest, InputStream in) throws IOException {
        final File tmp = new File(dest.getAbsolutePath() + "~");
        final OutputStream out = IO.write(tmp);
        int i;
        while ((i = in.read()) != -1) {
            out.write(i);
        }
        out.close();
        if (!tmp.renameTo(dest)) {
            throw new RuntimeException(String.format("Rename failed: mv \"%s\" \"%s\"", tmp.getAbsolutePath(), dest.getAbsolutePath()));
        }
    }

    private static String nextVersion(final String systemProperty, final String version, boolean fromSnapshot, boolean toSnapshot) {
        if (fromSnapshot) { // we want to replace version-SNAPSHOT by version so keep it
            return version;
        }

        // if we forced the next version
        final String next = System.getProperty(systemProperty + ".next");
        if (next != null) {
            return next;
        }

        // we want to increment the last digit
        String newVersion = null;

        final Matcher betaMatcher = BETA_PATTERN.matcher(version);
        if (betaMatcher.matches()) {
            newVersion = replace(version, betaMatcher.group(1));
        }

        final Matcher mavenMatcher = MAVEN_VERSION_PATTERN.matcher(version);
        if (newVersion == null && mavenMatcher.matches()) {
            newVersion = replace(version, mavenMatcher.group(1));
        }

        if (newVersion == null) {
            throw new IllegalArgumentException("can't manage the version " + version);
        }

        if (toSnapshot) {
            return newVersion + SNAPSHOT_SUFFIX;
        }
        return newVersion;
    }

    private static String replace(final String version, final String minor) {
        return version.substring(0, version.length() - minor.length()) + increment(minor);
    }

    private static String increment(final String minor) {
        return Integer.toString(Integer.parseInt(minor) + 1);
    }

    /**
     * forking to be able to count number of replacements.
     */
    public static class CountingFixedStringValueTokenHandler implements StreamTokenHandler {
        private final String value;
        private int replacements = 0;

        public CountingFixedStringValueTokenHandler(String value) {
            this.value = value;
        }

        public InputStream processToken(String token) {
            replacements++;
            return new ByteArrayInputStream(value.getBytes());
        }

        public int getReplacements() {
            return replacements;
        }
    }
}
