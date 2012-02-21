package org.apache.openejb.xbean.xml;

import org.apache.xbean.finder.IAnnotationFinder;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;

public class XmlPackageAnnotationFinderTest extends AbstractXmlAnnotationFinderTest {
    private static IAnnotationFinder finder;

    @BeforeClass
    public static void initFinder() throws JAXBException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        finder = XMLAnnotationFinderHelper.finderFromXml(loader.getResourceAsStream("test-package-scan.xml"), loader, null, null);
    }

    @Override
    protected IAnnotationFinder finder() {
        return finder;
    }
}
