package org.apache.openejb.maven.plugin;
/*
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
import java.util.ArrayList;
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
import static org.apache.openejb.util.JarExtractor.delete;
import static org.codehaus.plexus.util.FileUtils.copyDirectory;
import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.IOUtil.copy;

public abstract class AbstractTomEEMojo extends AbstractAddressMojo {
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
     * @parameter expression="${tomee-plugin.shutdown}" default-value="8005"
     */
    protected int tomeeShutdownPort = 8005;

    /**
     * @parameter expression="${tomee-plugin.args}"
     */
    protected String args;

    /**
     * @parameter expression="${tomee-plugin.debug}" default-value="false"
     */
    protected boolean debug;

    /**
     * @parameter expression="${tomee-plugin.debugPort}" default-value="5005"
     */
    private int debugPort;

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
    protected Map<String, String> systemVariables;

    /**
     * @parameter
     */
    protected List<String> libs;

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     * @readonly
     */
    protected File warFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        unzip(resolve(), catalinaBase);
        copyLibs();
        overrideConf(config);
        overrideConf(bin);
        overrideAddresses();
        copyWar();
        run();
    }

    private void copyLibs() {
        if (libs == null || libs.isEmpty()) {
            return;
        }

        final File destParent = new File(catalinaBase, "lib");
        for (String lib : libs) {
            copyLib(lib, destParent);
        }
    }

    private void copyLib(final String lib, final File destParent) {
        FileInputStream is = null;
        FileOutputStream os = null;
        final String[] infos = lib.split(":");
        final String classifier;
        final String type;
        if (infos.length < 3) {
            throw new TomEEException("format for librairies should be <groupId>:<artifactId>:<version>[:<type>[:<classifier>]]");
        }
        if (infos.length >= 4) {
            type = infos[3];
        } else {
            type = "jar";
        }
        if (infos.length == 5) {
            classifier = infos[4];
        } else {
            classifier = null;
        }

        try {
            final Artifact artifact = factory.createDependencyArtifact(infos[0], infos[1], createFromVersion(infos[2]), type, classifier, SCOPE_COMPILE);
            resolver.resolve(artifact, remoteRepos, local);
            final File file = artifact.getFile();
            is = new FileInputStream(file);
            os = new FileOutputStream(new File(destParent, file.getName()));
            copy(is, os);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(is);
            close(os);
        }
    }

    private void copyWar() {
        final String name = warFile.getName();
        final File out = new File(catalinaBase, webappDir + "/" + name);
        delete(out);
        if (!out.isDirectory()) {
            final File unpacked = new File(catalinaBase, webappDir + "/" + name.substring(0, name.lastIndexOf('.')));
            delete(unpacked);
        }

        if (warFile.isDirectory()) {
            try {
                copyDirectory(warFile, out);
            } catch (IOException e) {
                throw new TomEEException(e.getMessage(), e);
            }
        } else {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(warFile);
                os = new FileOutputStream(out);
                copy(is, os);
            } catch (Exception e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(is);
                close(os);
            }
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

    protected void run() {
        final ProcessBuilder builder = baseProcessBuilder();
        builder.command(java(),
                "-javaagent:" + new File(catalinaBase, "lib/openejb-javaagent.jar").getAbsolutePath(),
                "-cp", cp());
        if (args != null && args.length() > 0) {
            builder.command().addAll(Arrays.asList(args.split(" ")));
        }
        if (debug) {
            builder.command().addAll(Arrays.asList(
                    "-Xnoagent", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + debugPort
            ));
        }
        builder.command().addAll(systemProperties());
        builder.command().addAll(Arrays.asList("org.apache.catalina.startup.Bootstrap", getCmd()));
        builder.redirectErrorStream(true);

        final Process process;
        try {
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

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new TomEEException(e.getMessage(), e);
        }
    }

    protected static String java() {
        return new File(System.getProperty("java.home"), "/bin/java").getAbsolutePath();
    }

    protected ProcessBuilder baseProcessBuilder() {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(catalinaBase);
        return processBuilder;
    }

    protected List<String> systemProperties() {
        // using a map since it is easier to read
        final Map<String, String> prop = new HashMap<String, String>();
        prop.put("java.util.logging.config.file", new File(catalinaBase, "conf/logging.properties").getAbsolutePath());
        prop.put("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        prop.put("java.endorsed.dirs", new File(catalinaBase, "endorsed").getAbsolutePath());
        prop.put("catalina.base", catalinaBase.getAbsolutePath());
        prop.put("catalina.home", catalinaBase.getAbsolutePath());
        prop.put("java.io.tmpdir", new File(catalinaBase, "temp").getAbsolutePath());
        if (debug) {
            prop.put("java.compiler", "NONE");
        }
        if (getNoShutdownHook()) {
            prop.put("tomee.noshutdownhook", "true");
        }
        if (systemVariables != null) {
            prop.putAll(systemVariables);
        }

        // converting it
        final List<String> strings = new ArrayList<String>();
        for (Map.Entry<String, String> entry : prop.entrySet()) {
            if (entry.getValue().contains(" ")) {
                strings.add(String.format("'-D%s=%s'", entry.getKey(), entry.getValue()));
            } else {
                strings.add(String.format("-D%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        return strings;
    }

    protected boolean getNoShutdownHook() {
        return true;
    }

    protected String cp() {
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
                    new ArtifactRepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN),
                    new ArtifactRepositoryPolicy(false, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_WARN)));
        } else {
            try {
                new URI(apacheRepos); // to check it is a uri
                remoteRepos.add(new DefaultArtifactRepository("additional-repo-tomee-mvn-plugin", apacheRepos,
                        new DefaultRepositoryLayout(),
                        new ArtifactRepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN),
                        new ArtifactRepositoryPolicy(true, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_WARN)));
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

    public abstract String getCmd();
}
