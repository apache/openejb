package org.openejb;

import junit.framework.TestSuite;

import org.openejb.test.ClientTestSuite;

public class OpenEJBTestSuite extends junit.framework.TestCase{
    
    public OpenEJBTestSuite(String str){
        super(str);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest( UnitTestSuite.suite() );
        suite.addTest( ClientTestSuite.suite() );
        return suite;
    }

}


