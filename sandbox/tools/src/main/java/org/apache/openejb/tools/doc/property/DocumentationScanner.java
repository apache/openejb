package org.apache.openejb.tools.doc.property;

import org.apache.openejb.documentation.Documentation;
import org.apache.openejb.documentation.Property;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.archive.JarArchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DocumentationScanner {
    private DocumentationScanner() {
        // no-op
    }

    private static Class<?>[] findDocumentationAnnotations() {
        // should be done on openejb-api if we want to do so
        try {
            final AnnotationFinder finder = new AnnotationFinder(new JarArchive(Thread.currentThread().getContextClassLoader(), openejbApiJar()), false);
            final List<Annotated<Class<?>>> annotations = finder.findMetaAnnotatedClasses(Documentation.class);
            final Class<?>[] classes = new Class<?>[annotations.size()];
            int i = 0;
            for (Annotated<Class<?>> clazz : annotations) {
                classes[i] = clazz.get();
            }
            return classes;
        } catch (Exception e) {
            return new Class<?>[] { Property.class };
        }
    }

    private static URL openejbApiJar() {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) tccl).getURLs();
            for (URL url : urls) {
                final String urlStr = url.toExternalForm();
                if (urlStr.contains("openejb-api")) {
                    if (urlStr.startsWith("jar")) {
                        return url;
                    }
                    try {
                        return new URL("jar:" + urlStr + "!/");
                    } catch (MalformedURLException e) {
                        // ignored
                    }
                }
            }
        }
        return null;
    }

    public static Map<AnnotationInfo, List<AnnotationInstanceInfo>> findDocumentation(final File jar) {
        final Map<AnnotationInfo, List<AnnotationInstanceInfo>> infos = new HashMap<AnnotationInfo, List<AnnotationInstanceInfo>>();
        final AnnotationFinder finder;
        try {
            finder = new AnnotationFinder(new JarArchive(new URLClassLoader(new URL[] { jar.toURI().toURL() }, Thread.currentThread().getContextClassLoader()), new URL("jar:" + jar.toURI().toURL().toExternalForm() + "!/")), false);
        } catch (MalformedURLException e) {
            throw new RuntimeException("can't look into " + jar.getPath());
        }

        for (Class<?> clazz : findDocumentationAnnotations()) {
            final AnnotationInfo key = key(clazz);
            infos.put(key, new ArrayList<AnnotationInstanceInfo>());
            try {
                final List<Annotated<Field>> fields = finder.findMetaAnnotatedFields((Class<? extends Annotation>) clazz);
                for (Annotated<Field> foundField : fields) {
                    infos.get(key).add(new AnnotationInstanceInfo(
                            AnnotationParser.annotationInfos(clazz.getName(), foundField.get().getName(), null, null, is(foundField.get())),
                            foundField.get().toGenericString()));
                }

                final List<Annotated<Class<?>>> classes = finder.findMetaAnnotatedClasses((Class<? extends Annotation>) clazz);
                for (Annotated<Class<?>> foundClazz : classes) {
                    infos.get(key).add(new AnnotationInstanceInfo(
                            AnnotationParser.annotationInfos(clazz.getName(), null, foundClazz.get().getName(), null, is(foundClazz.get())),
                            foundClazz.get().toString()));
                }

                final List<Annotated<Method>> methods = finder.findMetaAnnotatedMethods((Class<? extends Annotation>) clazz);
                for (Annotated<Method> method : methods) {
                    infos.get(key).add(new AnnotationInstanceInfo(
                            AnnotationParser.annotationInfos(clazz.getName(), null, null, method.get().getName(), is(method.get())),
                                    method.get().toGenericString()));
                }
            } catch (Exception e) {
                throw new RuntimeException("can't parse " + clazz.getName());
            }
        }

        return infos;
    }

    private static InputStream is(Method method) throws IOException {
        return url(method.getDeclaringClass()).openStream();
    }

    private static InputStream is(Class<?> aClass) throws IOException {
        return url(aClass).openStream();
    }

    private static InputStream is(Field field) throws IOException {
        return url(field.getDeclaringClass()).openStream();
    }

    private static AnnotationInfo key(final Class<?> clazz) {

        try {
            final Map<String, String> values = AnnotationParser.annotationInfos(Documentation.class.getName(), null, null, null, url(clazz).openStream());
            if (values.containsKey("title")) {
                return new AnnotationInfo(values.get("title"), values.get("description"));
            }
        } catch (IOException e) {
            // ignored
        }
        return new AnnotationInfo(clazz.getSimpleName(), null);
    }

    private static URL url(Class<?> clazz) throws MalformedURLException {
        String urlStr = clazz.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
        if (!urlStr.startsWith("jar")) {
            urlStr = "jar:" + urlStr + "!/";
        }
        urlStr += clazz.getName().replace(".", "/") + ".class";
        return new URL(urlStr);
    }
}
