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
        generateEjbs(1000, 200);
    }

    private static void generateEjbs(int ejbNb, int mtdNb) {
        final File packageFile = new File(GENERATED_PACKAGE);
        if (!packageFile.exists()) {
            packageFile.mkdirs();
        }

        for (int i = 0; i < ejbNb; i++) {
            final StringBuilder s = new StringBuilder("package ").append(PACKAGE.replace("/", "."))
                    .append(";\n\nimport javax.ejb.Stateless;\n\n")
                    .append("@Stateless(name = \"Foo").append(i).append("\")\n")
                    .append("public class Foo").append(i).append(" {\n");
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
    }
}
