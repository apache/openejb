package org.apache.openejb.maven.plugin.dd;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.xbean.finder.ResourceFinder;
import org.codehaus.plexus.interpolation.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @goal merge
 * @phase compile
 * @requiresDependencyResolution runtime
 */
public class MergeDDForWebappMojo extends AbstractMojo {
    private static final String[] MANAGED_DD = {
            "ejb-jar.xml", "openejb-jar.xml",
            "env-entries.properties",
            "validation.xml"
        };

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.build.outputDirectory}/${project.build.finalName}/WEB-INF"
     * @required
     * @readonly
     */
    private File webInf;

    /**
     * @parameter
     */
    private List<String> includes;

    /**
     * @parameter
     */
    private List<String> excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initIncludeExclude();
        final Map<String, Merger<?>> mergers = initMerger();

        getLog().info("looking for descriptors...");
        final List<Artifact> artifacts = getDependencies();

        final ResourceFinder webInfFinder;
        try {
            webInfFinder = new ResourceFinder(webInf.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new MojoFailureException("can't create a finder for webinf", e);
        }

        final Map<Artifact, ResourceFinder> finders = finders(artifacts);
        for (String dd : MANAGED_DD) {
            int ddCount = 0;
            Object reference;
            final Merger<Object> merger = (Merger<Object>) mergers.get(dd);
            try {
                final URL ddUrl = webInfFinder.find(dd);
                if (ddUrl != null) {
                    reference = merger.read(ddUrl);
                    ddCount++;
                } else {
                    reference = merger.createEmpty();
                }
            } catch (IOException e) {
                reference = merger.createEmpty();
            }

            for (Artifact artifact : artifacts) {
                try {
                    final URL otherDD = finders.get(artifact).find("META-INF/" + dd);
                    if (otherDD != null) {
                        merger.merge(reference, merger.read(otherDD));
                        ddCount++;
                    }
                } catch (IOException e) {
                    // ignore since it means the resource was not found
                }
            }

            // todo: dump it in web-inf...
            if (ddCount > 0) {
                getLog().info(dd = " => ");
                getLog().info(reference.toString());
            }
        }
    }

    private Map<String, Merger<?>> initMerger() {
        final Map<String, Merger<?>> mergers = new HashMap<String, Merger<?>>();
        for (String dd : MANAGED_DD) {
            final String name = "org.apache.openejb.maven.plugin.dd.merger." + StringUtils.capitalizeFirstLetter(dd).replace(".", "").replace("-", "") + "Merger";
            try {
                mergers.put(dd,
                        (Merger<?>) Thread.currentThread().getContextClassLoader()
                                            .loadClass(name)
                                            .getConstructor(Log.class).newInstance(getLog()));
            } catch (Exception e) {
                e.printStackTrace(); // TODO
            }
        }
        return mergers;
    }

    private Map<Artifact, ResourceFinder> finders(List<Artifact> artifacts) {
        final Map<Artifact, ResourceFinder> map = new HashMap<Artifact, ResourceFinder>(artifacts.size());
        for (Artifact artifact : artifacts) {
            try {
                map.put(artifact, new ResourceFinder(artifact.getFile().toURI().toURL()));
            } catch (MalformedURLException e) {
                getLog().warn("can't manage " + artifact);
            }
        }
        return map;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<Artifact> getDependencies() {
        final List<Artifact> dependencies = new ArrayList<Artifact>(project.getArtifacts());
        final Iterator<Artifact> it = dependencies.iterator();
        while (it.hasNext()) {
            final Artifact artifact = it.next();
            if (!keep(artifact.getArtifactId())) {
                it.remove();
            }
        }
        return dependencies;
    }

    private boolean keep(final String str) {
        return matches(includes, str) && !matches(excludes, str);
    }

    private void initIncludeExclude() {
        if (includes == null) {
            includes = new ArrayList<String>();
            includes.add("");
        }
        if (excludes == null) {
            excludes = Arrays.asList(NewLoaderLogic.getExclusions());
        }
    }

    private static boolean matches(final List<String> includes, final String artifact) {
        for (String pattern : includes) {
            if (artifact.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}
