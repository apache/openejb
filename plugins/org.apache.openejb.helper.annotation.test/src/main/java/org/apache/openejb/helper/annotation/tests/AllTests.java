package org.apache.openejb.helper.annotation.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(AddAnnotationTest.class);
		suite.addTestSuite(ConvertEjbCreateMethodToConstructorTest.class);
		suite.addTestSuite(ConvertFinderMethodToCodeTest.class);
		
		return suite;
	}
}
