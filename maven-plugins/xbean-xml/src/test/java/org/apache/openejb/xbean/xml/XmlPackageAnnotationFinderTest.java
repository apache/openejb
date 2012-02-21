package org.apache.openejb.xbean.xml;

import org.apache.xbean.finder.IAnnotationFinder;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

public class XmlPackageAnnotationFinderTest extends AbstractXmlAnnotationFinderTest {
    private static IAnnotationFinder finder;

    @BeforeClass
    public static void initFinder() throws JAXBException, MalformedURLException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        finder = XMLAnnotationFinderHelper.finderFromXml(loader.getResourceAsStream("test-package-scan.xml"), loader, Arrays.asList(new File("target/test-classes").toURI().toURL()), null);
    }

    @Override
    protected IAnnotationFinder finder() {
        return finder;
    }
}
