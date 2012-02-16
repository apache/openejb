package org.apache.openejb.tools.doc.property;

import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.FieldVisitor;
import org.apache.xbean.asm.MethodVisitor;
import org.apache.xbean.asm.commons.EmptyVisitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AnnotationParser {
    public static Map<String, String> annotationInfos(final String name, final String field, final String clazz, final String mtd, final InputStream is) throws IOException {
        final ClassReader reader = new ClassReader(new BufferedInputStream(is));
        final AnnotationVisitor visitor = new AnnotationVisitor(name, field, clazz, mtd);
        reader.accept(visitor, ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);
        return visitor.values();
    }

    private static class AnnotationVisitor extends EmptyVisitor {
        private boolean targetFound = false;
        private boolean entityFound = false;

        private final Map<String, String> values = new HashMap<String, String>();
        private String target;

        // one max != null
        private String field;
        private String clazz;
        private String method;

        private AnnotationVisitor(String target, String field, String clazz, String method) {
            this.target = target;
            this.field = field;
            this.clazz = clazz;
            this.method = method;

            if (field == null && clazz == null && method == null) {
                entityFound = true;
            }
        }

        @Override
        public void visit(int i, int i1, java.lang.String s, java.lang.String s1, java.lang.String s2, java.lang.String[] strings) {
            if (clazz != null && s.equals(clazz.replace(".", "/"))) {
                entityFound = true;
            }
        }

        @Override
        public FieldVisitor visitField(int i, String s, String s1, String s2, java.lang.Object o) {
            if (s.equals(field)) {
                entityFound = true;
            }
            return this;
        }

        @Override
        public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
            if (s.equals(method)) { // todo: manage all parameters?
                entityFound = true;
            }
            return this;
        }

        @Override
        public org.apache.xbean.asm.AnnotationVisitor visitAnnotation(String s, boolean b) {
            if (s.equals("L" + target.replace(".", "/") + ";")) {
                targetFound = true;
            }
            return this;
        }

        @Override
        public void visit(final String mtdName, final Object value) {
            if (targetFound && entityFound) {
                values.put(mtdName, value.toString());
            }
        }

        @Override
        public void visitEnd() {
            if (entityFound && targetFound) {
                entityFound = false;
                targetFound = false;
            }
        }

        public String get(final String key) {
            return values.get(key);
        }

        public Map<String, String> values() {
            return values;
        }
    }
}
