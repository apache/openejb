package org.apache.openejb.colossus.generator.ejb;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MoveClassesTest {
    @Test
    public void moveGeneratedCompiledClassesToPackageClasses() throws IOException {
        FileUtils.copyDirectory(new File("target/test-classes/" + Main.BASE_PACKAGE), new File("target/classes/" + Main.BASE_PACKAGE));
    }
}
