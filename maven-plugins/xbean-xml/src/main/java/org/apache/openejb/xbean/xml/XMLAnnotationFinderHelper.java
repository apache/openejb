package org.apache.openejb.xbean.xml;

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.FilterList;
import org.apache.xbean.finder.filter.PackageFilter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class XMLAnnotationFinderHelper {
    private static final JAXBContext JAXB_CONTEXT;
    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(Scan.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e); // shouldn't occur
        }
    }

    private XMLAnnotationFinderHelper() {
        // no-op
    }

    public static IAnnotationFinder finderFromXml(final InputStream is, final ClassLoader loader, final Iterable<URL> urls, final Class<?> archiveClazz) throws JAXBException {
        final Scan scan = (Scan) JAXB_CONTEXT.createUnmarshaller().unmarshal(new BufferedInputStream(is));

        final Archive packageArchive = packageArchive(scan.getPackagename(), loader, urls, archiveClazz);
        final Archive classesArchive = classesArchive(scan, loader);

        final Archive archive;
        if (packageArchive != null && classesArchive != null) {
            archive = new CompositeArchive(classesArchive, packageArchive);
        } else if (packageArchive != null) {
            archive = packageArchive;
        } else {
            archive = classesArchive;
        }
        return new AnnotationFinder(archive).link();
    }

    public static IAnnotationFinder finderFromXml(final InputStream is, final ClassLoader loader, final Iterable<URL> urls) throws JAXBException {
        final Scan scan = (Scan) JAXB_CONTEXT.createUnmarshaller().unmarshal(new BufferedInputStream(is));

        final Archive packageArchive = packageArchive(scan.getPackagename(), loader, urls, null);
        final Archive classesArchive = classesArchive(scan, loader);

        final Archive archive;
        if (packageArchive != null && classesArchive != null) {
            archive = new CompositeArchive(classesArchive, packageArchive);
        } else if (packageArchive != null) {
            archive = packageArchive;
        } else {
            archive = classesArchive;
        }
        return new AnnotationFinder(archive); // don't link here
    }

    public static Archive classesArchive(final Scan scan, final ClassLoader loader) {
        Class<?>[] classes = new Class<?>[scan.getClassname().size()];
        int i = 0;
        final Set<String> packages = scan.getPackagename();
        for (String clazz : scan.getClassname()) {
            // skip classes managed by package filtering
            if (packages != null && clazzInPackage(packages, clazz)) {
                continue;
            }

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

        return new ClassesArchive(classes);
    }

    public static Archive packageArchive(final Set<String> packagenames, final ClassLoader loader, final Iterable<URL> urls, final Class<?> archiveClazz) {
        if (!packagenames.isEmpty()) {
            Archive rawArchive = null;
            if (archiveClazz != null) {
                try {
                    rawArchive = (Archive) archiveClazz.getConstructor(ClassLoader.class, Iterable.class).newInstance(loader, urls);
                } catch (Exception e) {
                    // ignored
                }
            }
            if (rawArchive == null) {
                rawArchive = new ClasspathArchive(loader, urls);
            }
            return new FilteredArchive(rawArchive, filters(packagenames));
        }
        return null;
    }

    private static boolean clazzInPackage(final Collection<String> packagename, final String clazz) {
        for (String str : packagename) {
            if (clazz.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    private static Filter filters(final Set<String> packageNames) {
        final List<Filter> filters = new ArrayList<Filter>();
        for (String packageName : packageNames) {
            filters.add(new PackageFilter(packageName));
        }
        return new FilterList(filters);
    }
}
