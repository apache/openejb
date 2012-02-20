package org.apache.openejb.maven.plugin.spi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.FileArchive;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * @parameter expression="${spi.output}" default-value="META-INF/scan.xml"
     */
    private String outputFilename;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final Archive archive = new FileArchive(createClassLoader(), module);
            final ExtendedAnnotationFinder finder = new ExtendedAnnotationFinder(archive);
            finder.link();
            final Map<String, List<AnnotationFinder.Info>> info = finder.getAnnotated();

            final Scan scan = new Scan();
            for (Map.Entry<String, List<AnnotationFinder.Info>> entry : info.entrySet()) {
                final Annotation annotation = new Annotation();
                annotation.name = entry.getKey();

                if (annotations != null && !annotations.isEmpty()) {
                    if (!annotations.contains(annotation.name)) {
                        continue;
                    }
                } else if (annotation.name.startsWith("java.lang")) {
                    continue;
                }

                scan.annotation.add(annotation);

                for (AnnotationFinder.Info value : entry.getValue()) {
                    if (value instanceof AnnotationFinder.ClassInfo) {
                        final Class clazz = new Class();
                        clazz.name = value.getName();
                        annotation.classes.add(clazz);
                    } else if (value instanceof AnnotationFinder.FieldInfo) {
                        final Field field = new Field();
                        field.classname = ((AnnotationFinder.FieldInfo) value).getDeclaringClass().getName();
                        field.name = value.getName();
                        annotation.fields.add(field);
                    } else if (value instanceof AnnotationFinder.MethodInfo) {
                        final Method method = new Method();
                        method.classname = ((AnnotationFinder.MethodInfo) value).getDeclaringClass().getName();
                        method.name = value.getName();
                        annotation.methods.add(method);
                    }
                }                
            }

            File output = new File(outputFilename);
            if (!output.isAbsolute()) {
                output = new File(module, outputFilename);
            }
            if (!output.getParentFile().mkdirs()) {
                getLog().error("can't create " + output.getParent());
                return;
            }

            final JAXBContext context = JAXBContext.newInstance(Scan.class);
            final FileWriter writer = new FileWriter(output);
            try {
                context.createMarshaller().marshal(scan, writer);
            } finally {
                writer.close();
            }

            getLog().info("generated " + output.getPath());
        } catch (Exception e) {
            getLog().error(e);
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

    @XmlRootElement
    public static class Scan {
        @XmlElementWrapper(name = "annotations")
        public List<Annotation> annotation = new ArrayList<Annotation>();
    }

    public static class Annotation {
        @XmlAttribute
        public String name;
        public List<Class> classes = new ArrayList<Class>();
        public List<Method> methods = new ArrayList<Method>();
        public List<Field> fields = new ArrayList<Field>();
    }

    public static class Method {
        @XmlAttribute
        public String classname;
        @XmlAttribute
        public String name;
    }

    public static class Field {
        @XmlAttribute
        public String classname;
        @XmlAttribute
        public String name;
    }

    public static class Class {
        @XmlAttribute
        public String name;
    }
}
