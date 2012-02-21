package org.apache.openejb.xbean.xml;

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.InputStream;

public final class XMLAnnotationFinderHelper {
    private static final JAXBContext JAXB_CONTEXT;
    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(Scan.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e); // TODO: handle it with a custom exception
        }
    }

    private XMLAnnotationFinderHelper() {
        // no-op
    }

    public static IAnnotationFinder finderFromXml(final InputStream is, final ClassLoader loader) throws JAXBException {
        final Scan scan = (Scan) JAXB_CONTEXT.createUnmarshaller().unmarshal(new BufferedInputStream(is));

        // TODO: manage packages

        Class<?>[] classes = new Class<?>[scan.getClassname().size()];
        int i = 0;
        for (String clazz : scan.getClassname()) {
            try {
                classes[i++] = loader.loadClass(clazz);
            } catch (ClassNotFoundException e) {
                // ignored
            }
        }

        if (i != classes.length) { // shouldn't occur
            final Class<?>[] updatedClasses = new Class<?>[i];
            System.arraycopy(classes, 0, updatedClasses, 0, i);
            classes = updatedClasses;
        }

        final Archive archive = new ClassesArchive(classes);
        return new AnnotationFinder(archive); // don't link here
    }
}
