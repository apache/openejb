package org.apache.openejb.tools.doc.property;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.tools.doc.property.DocumentationScanner.findDocumentation;

public final class DocumentationGenerator {
    private DocumentationGenerator() {
        // no-op
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage:");
            System.err.println("======");
            System.err.println("\tjava -cp tools.jar " + DocumentationGenerator.class.getName() + " <directory to scan>");
            return;
        }

        final File dir = new File(args[0]);
        if (!dir.isDirectory()) {
            System.err.println("args0 should be a directory");
            return;
        }

        // get info
        final Collection<File > files = FileUtils.listFiles(dir, new OrFileFilter(filter("openejb-"), filter("tomee-")), TrueFileFilter.INSTANCE);
        final Map<AnnotationInfo, List<AnnotationInstanceInfo>> info = new HashMap<AnnotationInfo, List<AnnotationInstanceInfo>>();
        for (File file : files) {
            add(info, findDocumentation(file));
        }

        for (Map.Entry<AnnotationInfo, List<AnnotationInstanceInfo>> entry : info.entrySet()) {
            dump(entry.getKey(), entry.getValue());
            System.out.println();
        }
    }

    private static void dump(AnnotationInfo key, List<AnnotationInstanceInfo> value) {
        System.out.println(DocTemplate.get()
                .apply("doc.vm", key, value));
    }

    private static void add(Map<AnnotationInfo, List<AnnotationInstanceInfo>> info, Map<AnnotationInfo, List<AnnotationInstanceInfo>> documentation) {
        for (Map.Entry<AnnotationInfo, List<AnnotationInstanceInfo>> entry : documentation.entrySet()) {
            final AnnotationInfo key = entry.getKey();
            if (info.containsKey(key)) {
                info.get(key).addAll(entry.getValue());
            } else {
                info.put(key, entry.getValue());
            }
        }
    }

    private static IOFileFilter filter(String s) {
        return new AndFileFilter(new PrefixFileFilter(s), new SuffixFileFilter(".jar"));
    }
}
