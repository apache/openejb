package org.apache.openejb.maven.plugin;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersion;
import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.IOUtil.copy;

/**
 * @goal run
 * @requiresDependencyResolution test
 */
public class RunTomEEMojo extends AbstractMojo {
    /**
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository local;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List<ArtifactRepository> remoteRepos;

    /**
     * @parameter expression="${tomee-plugin.version}" default-value="1.0.0-beta-2-SNAPSHOT"
     */
    protected String tomeeVersion;

    /**
     * @parameter expression="${tomee-plugin.groupId}" default-value="org.apache.openejb"
     */
    protected String tomeeGroupId;

    /**
     * @parameter expression="${tomee-plugin.artifactId}" default-value="apache-tomee"
     */
    protected String tomeeArtifactId;

    /**
     * @parameter expression="${tomee-plugin.type}" default-value="zip"
     * @readonly // while tar.gz is not managed
     */
    protected String tomeeType;

    /**
     * @parameter expression="${tomee-plugin.apache-repos}" default-value="snapshots"
     */
    protected String apacheRepos;

    /**
     * @parameter expression="${tomee-plugin.classifier}" default-value="webprofile"
     */
    protected String tomeeClassifier;

    /**
     * @parameter expression="${tomee-plugin.http}" default-value="8080"
     */
    protected int tomeeHttpPort = 8008;

    /**
     * @parameter expression="${tomee-plugin.shutdown}" default-value="8005"
     */
    protected int tomeeShutdownPort = 8005;

    /**
     * @parameter expression="${tomee-plugin.host}" default-value="localhost"
     */
    protected String tomeeHost;

    /**
     * @parameter expression="${tomee-plugin.cmd}" default-value="start"
     */
    protected String tomeeCmd;

    /**
     * @parameter expression="${tomee-plugin.args}"
     */
    protected String args;

    /**
     * @parameter default-value="${project.build.directory}/apache-tomee"
     * @readonly
     */
    protected File catalinaBase;

    /**
     * relative to tomee.base.
     *
     * @parameter default-value="webapps"
     */
    protected String webappDir;

    /**
     * @parameter expression="${tomee-plugin.conf}" default-value="${basedir}/src/main/tomee/conf"
     * @optional
     */
    protected File config;

    /**
     * @parameter expression="${tomee-plugin.conf}" default-value="${basedir}/src/main/tomee/bin"
     * @optional
     */
    protected File bin;

    /**
     * @parameter
     */
    protected Map<String, String> systemVariables = new HashMap<String, String>();

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     * @readonly
     */
    protected File warFile;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        unzip(resolve(), catalinaBase);
        overrideConf(config);
        overrideConf(bin);
        overrideAddresses();
        copyWar();
        run();
    }

    private void copyWar() {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(warFile);
            os = new FileOutputStream(new File(catalinaBase, webappDir + "/" + warFile.getName()));
            copy(is, os);
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(is);
            close(os);
        }
    }

    private void overrideAddresses() {
        final File serverXml = new File(catalinaBase, "conf/server.xml");
        final String value = read(serverXml);

        FileWriter writer = null;
        try {
            writer = new FileWriter(serverXml);
            writer.write(value
                    .replace("8080", Integer.toString(tomeeHttpPort))
                    .replace("8005", Integer.toString(tomeeShutdownPort))
                    .replace("localhost", tomeeHost)
                    .replace("webapps", webappDir));
            writer.close();
        } catch (IOException e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(writer);
        }
    }

    private static String read(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            final StringBuilder sb = new StringBuilder();
            int i = in.read();
            while (i != -1) {
                sb.append((char) i);
                i = in.read();
            }
            return sb.toString();
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(in);
        }
    }

    private void overrideConf(final File dir) {
        if (!dir.exists()) {
            return;
        }

        for (File f : dir.listFiles()) {
            if (f.isDirectory() || f.isHidden()) {
                continue;
            }

            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(f);
                out = new FileOutputStream(new File(catalinaBase, dir.getName() + "/" + f.getName()));
                copy(in, out);
            } catch (Exception e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(in);
                close(out);
            }
        }
    }

    private void run() {
        final Process process;
        try {
            final ProcessBuilder builder = baseProcessBuilder();
            builder.command(java(),
                    "-javaagent:" + new File(catalinaBase, "lib/openejb-javaagent.jar").getAbsolutePath(),
                    "-cp", cp());
            if (args != null && args.length() > 0) {
                builder.command().addAll(Arrays.asList(args.split(" ")));
            }
            builder.command().addAll(Arrays.asList("org.apache.catalina.startup.Bootstrap", tomeeCmd));
            builder.redirectErrorStream(true);
            process = builder.start();
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        }

        final InputStream is = process.getInputStream();
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        do {
            try {
                line = br.readLine();
                getLog().info(line);
            } catch (IOException e) {
                line = null;
            }
        } while (line != null);

        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new TomEEException(e.getMessage(), e);
        }
    }

    private static String java() {
        return new File(System.getProperty("java.home"), "/bin/java").getAbsolutePath();
    }

    private ProcessBuilder baseProcessBuilder() {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(catalinaBase);

        final Map<String, String> prop = new HashMap<String, String>();
        prop.put("java.util.logging.config.file", new File(catalinaBase, "conf/logging.properties").getAbsolutePath());
        prop.put("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        prop.put("java.endorsed.dirs", new File(catalinaBase, "endorsed").getAbsolutePath());
        prop.put("catalina.base", catalinaBase.getAbsolutePath());
        prop.put("catalina.home", catalinaBase.getAbsolutePath());
        prop.put("java.io.tmpdir", new File(catalinaBase, "temp").getAbsolutePath());
        processBuilder.environment().putAll(systemVariables);

        return processBuilder;
    }

    private String cp() {
        final boolean unix = !System.getProperty("os.name").toLowerCase().startsWith("win");
        final char cpSep;
        if (unix) {
            cpSep = ':';
        } else {
            cpSep = ';';
        }

        return "bin/bootstrap.jar" + cpSep + "bin/tomcat-juli.jar";
    }

    private File resolve() {
        if ("snapshots".equals(apacheRepos) || "true".equals(apacheRepos)) {
            remoteRepos.add(new DefaultArtifactRepository("apache", "https://repository.apache.org/content/repositories/snapshots/",
                new DefaultRepositoryLayout(),
                new ArtifactRepositoryPolicy(true, UPDATE_POLICY_DAILY , CHECKSUM_POLICY_WARN),
                new ArtifactRepositoryPolicy(false, UPDATE_POLICY_NEVER , CHECKSUM_POLICY_WARN)));
        } else {
            try {
                new URI(apacheRepos); // to check it is a uri
                remoteRepos.add(new DefaultArtifactRepository("additional-repo-tomee-mvn-plugin", apacheRepos,
                        new DefaultRepositoryLayout(),
                        new ArtifactRepositoryPolicy(true, UPDATE_POLICY_DAILY , CHECKSUM_POLICY_WARN),
                        new ArtifactRepositoryPolicy(true, UPDATE_POLICY_NEVER , CHECKSUM_POLICY_WARN)));
            } catch (URISyntaxException e) {
                // ignored, use classical repos
            }
        }

        try {
            final Artifact artifact = factory.createDependencyArtifact(tomeeGroupId, tomeeArtifactId, createFromVersion(tomeeVersion), tomeeType, tomeeClassifier, SCOPE_COMPILE);
            resolver.resolve(artifact, remoteRepos, local);
            return artifact.getFile();
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            throw new TomEEException(e.getMessage(), e);
        }
    }

    private static void unzip(File mvnTomEE, File catalinaBase) {
        ZipFile in = null;
        try {
            in = new ZipFile(mvnTomEE);

            final Enumeration<? extends ZipEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("apache-tomee-")) {
                    int idx = name.indexOf("/");
                    if (idx < 0) {
                        idx = name.indexOf(File.separator);
                    }
                    if (idx < 0) {
                        continue;
                    }
                    name = name.substring(idx + 1);
                }
                final File dest = new File(catalinaBase.getAbsolutePath(), name);
                if (!dest.exists()) {
                    final File parent = dest.getParentFile();
                    parent.mkdirs();
                    parent.setWritable(true);
                    parent.setReadable(true);
                }
                if (entry.isDirectory()) {
                    dest.mkdir();
                } else {
                    final FileOutputStream fos = new FileOutputStream(dest);
                    try {
                        copy(in.getInputStream(entry), fos);
                    } catch (IOException e) {
                        // ignored
                    }
                    close(fos);

                    dest.setReadable(true);
                    if (dest.getName().endsWith(".sh")) {
                        dest.setExecutable(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // no-op
                }
            }
        }
    }

    private class ShutdownThread extends Thread {
        @Override public void run() {
            try {
                baseProcessBuilder()
                        .command(java(), "-cp", cp(), "org.apache.catalina.startup.Bootstrap", "stop")
                        .start()
                        .waitFor();
            } catch (Exception e) {
                throw new TomEEException(e.getMessage(), e);
            }
        }
    }
}
