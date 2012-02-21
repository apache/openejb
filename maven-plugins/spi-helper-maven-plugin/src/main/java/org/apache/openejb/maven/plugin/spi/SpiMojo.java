package org.apache.openejb.maven.plugin.spi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.xbean.xml.Scan;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.FileArchive;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @goal generate
 * @phase compile
 */
public class SpiMojo extends AbstractMojo {
    private static final Map<String, Profile> DEFAULT_PROFILES = new HashMap<String, Profile>();

    static {
        final Profile jee6 = new Profile(
                Arrays.asList( // annotations, TODO: possibly remove some redundant annotations
                        "javax.annotation.ManagedBean",
                        "javax.annotation.PostConstruct",
                        "javax.annotation.PreDestroy",
                        "javax.annotation.Resource",
                        "javax.annotation.Resources",
                        "javax.annotation.security.DenyAll",
                        "javax.annotation.security.PermitAll",
                        "javax.annotation.security.RolesAllowed",
                        "javax.annotation.sql.DataSourceDefinition",
                        "javax.ejb.AfterBegin",
                        "javax.ejb.AfterCompletion",
                        "javax.ejb.ApplicationException",
                        "javax.ejb.Asynchronous",
                        "javax.ejb.BeforeCompletion",
                        "javax.ejb.ConcurrencyManagement",
                        "javax.ejb.DependsOn",
                        "javax.ejb.EJB",
                        "javax.ejb.EJBs",
                        "javax.ejb.Init",
                        "javax.ejb.LocalBean",
                        "javax.ejb.LocalHome",
                        "javax.ejb.MessageDriven",
                        "javax.ejb.PostActivate",
                        "javax.ejb.PrePassivate",
                        "javax.ejb.RemoteHome",
                        "javax.ejb.Remove",
                        "javax.ejb.Schedule",
                        "javax.ejb.Schedules",
                        "javax.ejb.Singleton",
                        "javax.ejb.Stateful",
                        "javax.ejb.Stateless",
                        "javax.ejb.Timeout",
                        "javax.enterprise.inject.Specializes",
                        "javax.interceptor.AroundInvoke",
                        "javax.interceptor.AroundTimeout",
                        "javax.interceptor.ExcludeClassInterceptors",
                        "javax.interceptor.ExcludeDefaultInterceptors",
                        "javax.interceptor.Interceptors",
                        "javax.jws.WebService",
                        "javax.persistence.PersistenceContext",
                        "javax.persistence.PersistenceContexts",
                        "javax.persistence.PersistenceUnit",
                        "javax.persistence.PersistenceUnits",
                        "javax.ws.rs.Path",
                        "javax.xml.ws.WebServiceProvider",
                        "javax.xml.ws.WebServiceRef",
                        "javax.xml.ws.WebServiceRefs"
                ),
                Arrays.asList( // subclasses
                        "javax.ws.rs.core.Application"
                ),
                new ArrayList<String>( // implementations
                        // no implementations
                )
        );
        DEFAULT_PROFILES.put("jee6", jee6);
    }

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
     * @parameter
     */
    private List<String> profiles;

    /**
     * @parameter expression="${spi.output}" default-value="META-INF/org/apache/xbean/scan.xml"
     */
    private String outputFilename;

    /**
     * @parameter expression="${spi.meta}" default-value="false"
     */
    private boolean useMeta;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //
        // create profiles to use
        //
        final List<Profile> profileToUse = new ArrayList<Profile>();
        if (profiles != null) {
            for (String profile : profiles) {
                if (DEFAULT_PROFILES.containsKey(profile)) {
                    profileToUse.add(DEFAULT_PROFILES.get(profile));
                } else {
                    getLog().info("can't find profile " + profile + ", available ones are " + DEFAULT_PROFILES.keySet());
                }
            }
        }
        profileToUse.add(new Profile(annotations, subclasses, implementations));

        // the result
        final Scan scan = new Scan();

        try {
            final ClassLoader loader = createClassLoader();
            final Archive archive = new FileArchive(loader, module);
            final AnnotationFinder finder = new AnnotationFinder(archive);
            finder.link();

            //
            // find classes
            //

            for (Profile profile : profileToUse) {
                if (profile.getAnnotations() != null) {
                    for (String annotation : profile.getAnnotations()) {
                        final Class<? extends Annotation> annClazz;
                        try {
                            annClazz = (Class<? extends Annotation>) load(loader, annotation);
                        } catch (MojoFailureException mfe) {
                            getLog().warn("can't find " + annotation);
                            continue;
                        }

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

                if (profile.getSubclasses() != null) {
                    for (String subclass : profile.getSubclasses()) {
                        try {
                            for (Class<?> clazz : finder.findSubclasses(load(loader, subclass))) {
                                scan.getClassname().add(clazz.getName());
                            }
                        } catch (MojoFailureException mfe) {
                            getLog().warn("can't find " + subclass);
                        }
                    }
                }

                if (profile.getImplementations() != null) {
                    for (String implementation : profile.getImplementations()) {
                        try {
                            for (Class<?> clazz : finder.findImplementations(load(loader, implementation))) {
                                scan.getClassname().add(clazz.getName());
                            }
                        } catch (MojoFailureException mfe) {
                            getLog().warn("can't find " + implementation);
                        }
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
                final TransformerFactory factory = TransformerFactory.newInstance();
                final Transformer transformer = factory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                final StringWriter tempWriter = new StringWriter();
                context.createMarshaller().marshal(scan, tempWriter);

                final StreamResult result = new StreamResult(writer);
                transformer.transform(new StreamSource(new StringReader(tempWriter.toString())), result);
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

    public static final class Profile {
        private List<String> annotations;
        private List<String> subclasses;
        private List<String> implementations;

        public Profile(final List<String> annotations, final List<String> subclasses, final List<String> implementations) {
            this.annotations = annotations;
            this.subclasses = subclasses;
            this.implementations = implementations;
        }

        public List<String> getAnnotations() {
            return annotations;
        }

        public List<String> getSubclasses() {
            return subclasses;
        }

        public List<String> getImplementations() {
            return implementations;
        }
    }
}
