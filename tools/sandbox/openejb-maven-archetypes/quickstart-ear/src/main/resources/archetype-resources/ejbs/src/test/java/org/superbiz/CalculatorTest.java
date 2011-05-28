package org.superbiz;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CalculatorTest extends TestCase {

	public CalculatorTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(CalculatorTest.class);
	}

	public void testApp() throws Exception {
		Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,	"org.apache.openejb.client.LocalInitialContextFactory");
		InitialContext context = new InitialContext(properties);

		CalculatorRemote calc = (CalculatorRemote) context.lookup("CalculatorBeanRemote");
		assertEquals(4, calc.add(2, 2));
	}
}
