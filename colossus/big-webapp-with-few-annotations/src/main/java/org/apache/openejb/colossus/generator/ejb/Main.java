package org.apache.openejb.colossus.generator.ejb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static final String BASE_PACKAGE = "generated";
    public static final String PACKAGE = BASE_PACKAGE + "/foo/bar";
    public static final String GENERATED_FOLDER = "src/test/java/";
    public static final String GENERATED_PACKAGE = GENERATED_FOLDER + PACKAGE;

    public static void main(String[] args) {
        generateEjbs(1000, 200, 5, 10);
    }

    private static void generateEjbs(int classNb, int mtdNb, int rsNb, int rsMtdNb) {
        final File packageFile = new File(GENERATED_PACKAGE);
        if (!packageFile.exists()) {
            packageFile.mkdirs();
        }

        // pojo
        for (int i = 0; i < classNb; i++) {
            final StringBuilder s = new StringBuilder("package ").append(PACKAGE.replace("/", "."))
                    .append(";\n\n")
                    .append("public class Foo").append(i).append(" {\n");

            for (int i2 = 0; i2 < (i * Math.random()) % 100; i2++) {
                s.append("  private final Foo").append(i2).append(" foo").append(i2)
                        .append(" = new Foo").append(i2).append("();\n");
            }

            s.append("\n");

            for (int k = 0; k < mtdNb; k++) {
                s.append("    public String foo").append(k).append("() {\n")
                        .append("        return getClass().getSimpleName();\n")
                        .append("    }\n");
            }
            s.append("}\n");

            try {
                FileWriter writer = new FileWriter(GENERATED_PACKAGE + "/Foo" + i + ".java");
                writer.write(s.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // annotated
        for (int i = 0; i < rsNb; i++) {
            final StringBuilder s = new StringBuilder("package ").append(PACKAGE.replace("/", "."))
                    .append(";\n\nimport javax.ws.rs.GET;\n")
                    .append("import javax.ws.rs.Path;\n\n")
                    .append("@Path(\"/foo-").append(i).append("\")\n")
                    .append("public class RestFoo").append(i).append(" {\n");

            for (int i2 = 0; i2 < Math.max(rsMtdNb, (classNb * Math.random()) % 100); i2++) {
                s.append("  private final Foo").append(i2).append(" foo").append(i2)
                        .append(" = new Foo").append(i2).append("();\n");
            }

            s.append("\n");

            for (int k = 0; k < rsMtdNb; k++) {
                s.append("@Path(\"/mtd-").append(k).append("\") @GET\n")
                        .append("    public String foo").append(k).append("() {\n")
                        .append("        return foo").append(k).append(".getClass().getSimpleName();\n")
                        .append("    }\n");
            }
            s.append("}\n");

            try {
                FileWriter writer = new FileWriter(GENERATED_PACKAGE + "/RestFoo" + i + ".java");
                writer.write(s.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
