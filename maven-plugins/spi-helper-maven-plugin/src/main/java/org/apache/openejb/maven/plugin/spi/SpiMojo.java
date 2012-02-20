package org.apache.openejb.maven.plugin.spi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.maven.plugin.spi.xml.Scan;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.FileArchive;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @goal generate
 * @phase compile
 */
public class SpiMojo extends AbstractMojo {
    /**
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File module;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private List<String> annotations;

    /**
     * @parameter
     */
    private List<String> subclasses;

    /**
     * @parameter
     */
    private List<String> implementations;

    /**
     * @parameter expression="${spi.output}" default-value="META-INF/scan.xml"
     */
    private String outputFilename;

    /**
     * @parameter expression="${spi.meta}" default-value="false"
     */
    private boolean useMeta;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final ClassLoader loader = createClassLoader();
            final Archive archive = new FileArchive(loader, module);
            final AnnotationFinder finder = new AnnotationFinder(archive);
            finder.link();

            final Scan scan = new Scan();

            //
            // find classes
            //

            if (annotations != null) {
                for (String annotation : annotations) {
                    final Class<? extends Annotation> annClazz = (Class<? extends Annotation>) load(loader, annotation);

                    if (!useMeta) {
                        for (Class<?> clazz : finder.findAnnotatedClasses(annClazz)) {
                            scan.getClassname().add(clazz.getName());
                        }
                    } else {
                        for (Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(annClazz)) {
                            scan.getClassname().add(clazz.get().getName());
                        }
                    }

                    if (!useMeta) {
                        for (Field clazz : finder.findAnnotatedFields(annClazz)) {
                            scan.getClassname().add(clazz.getDeclaringClass().getName());
                        }
                    } else {
                        for (Annotated<Field> clazz : finder.findMetaAnnotatedFields(annClazz)) {
                            scan.getClassname().add(clazz.get().getDeclaringClass().getName());
                        }
                    }

                    if (!useMeta) {
                        for (Method clazz : finder.findAnnotatedMethods(annClazz)) {
                            scan.getClassname().add(clazz.getDeclaringClass().getName());
                        }
                    } else {
                        for (Annotated<Method> clazz : finder.findMetaAnnotatedMethods(annClazz)) {
                            scan.getClassname().add(clazz.get().getDeclaringClass().getName());
                        }
                    }
                }
            }

            if (subclasses != null) {
                for (String subclass : subclasses) {
                    for (Class<?> clazz : finder.findSubclasses(load(loader, subclass))) {
                        scan.getClassname().add(clazz.getName());
                    }
                }
            }

            if (implementations != null) {
                for (String implementation : implementations) {
                    for (Class<?> clazz : finder.findImplementations(load(loader, implementation))) {
                        scan.getClassname().add(clazz.getName());
                    }
                }
            }

            //
            // dump found classes
            //

            File output = new File(outputFilename);
            if (!output.isAbsolute()) {
                output = new File(module, outputFilename);
            }
            if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
                getLog().error("can't create " + output.getParent());
                return;
            }

            final FileWriter writer = new FileWriter(output);
            try {
                final JAXBContext context = JAXBContext.newInstance(Scan.class);
                context.createMarshaller().marshal(scan, writer);
            } finally {
                writer.close();
            }

            getLog().info("generated " + output.getPath());
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    private Class<?> load(final ClassLoader loader, final String name) throws MojoFailureException {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new MojoFailureException("can't load " + name, e);
        }
    }

    private ClassLoader createClassLoader() {
        final List<URL> urls = new ArrayList<URL>();
        for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        if (module.exists()) {
            try {
                urls.add(module.toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("can't use path " + module.getPath());
            }
        } else {
            getLog().warn("can't find " + module.getPath());
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
    }
}
