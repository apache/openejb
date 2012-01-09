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
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersion;
import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.IOUtil.copy;

/**
 * @goal run
 * @requiresDependencyResolution test
 */
public class RunTomEEMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject mavenProject;

    /**
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
     * @required
     * @readonly
     */
    protected ArtifactCollector artifactCollector;

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
     * @component
     */
    protected ArchiverManager archiverManager;

    /**
     * @parameter expression="${tomee-plugin.version}" default-value="1.0.0-beta-2"
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
     * @parameter default-value="${project.build.directory}/apache-tomee"
     * @readonly
     */
    protected File catalinaBase;

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
     * @parameter default-value="${project.build.directory}/${project.build.finalName}"
     * @readonly
     */
    protected File webappOutputDirectory;

    /**
     * @parameter
     */
    protected Map<String, String> systemVariables = new HashMap<String, String>();


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        unzip(resolve(), catalinaBase);
        overrideConf(config);
        overrideConf(bin);
        overridePorts();
        run();
    }

    private void overridePorts() {
        final File serverXml = new File(catalinaBase, "conf/server.xml");
        final String value = read(serverXml);

        FileWriter writer = null;
        try {
            writer = new FileWriter(serverXml);
            writer.write(value
                    .replace("8080", Integer.toString(tomeeHttpPort))
                    .replace("8005", Integer.toString(tomeeShutdownPort)));
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
        try {
            baseProcessBuilder()
                .command(java(),
                        "-javaagent:" + new File(catalinaBase, "lib/openejb-javaagent.jar").getAbsolutePath(),
                        "-cp", cp(), "org.apache.catalina.startup.Bootstrap", tomeeCmd)
                .start().waitFor();
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    baseProcessBuilder()
                            .command(java(), "-cp", cp(), "org.apache.catalina.startup.Bootstrap", "stop")
                            .start()
                            .waitFor();
                } catch (Exception e) {
                    throw new TomEEException(e.getMessage(), e);
                }
            }
        });
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

        return new File(catalinaBase, "bin/bootstrap.jar").getAbsolutePath()
                + cpSep + new File(catalinaBase, "bin/tomcat-juli.jar").getAbsolutePath();
    }

    private File resolve() {
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
                    dest.getParentFile().mkdirs();
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
}
