package org.openejb;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTOpenEJBException extends NamedTestCase{
    
    public UTOpenEJBException(){
        super("org.openejb.OpenEJBException.");
    }

    public void test01_constructor1(){
        OpenEJBException oe = new OpenEJBException();
    }

    public void test02_constructor2(){
        OpenEJBException oe = new OpenEJBException("An exception has occured");
    }

    public void test03_constructor3(){
        OpenEJBException oe = new OpenEJBException("sa0001" , "test" );
    }

    public void test04_constructor4(){
        OpenEJBException oe = new OpenEJBException("ge0003" , "X", "Y" );
    }
    
    public void test05_constructor5(){
        Object[] args = {"X","Y","Z"};
        OpenEJBException oe = new OpenEJBException("ge0006" , args);
    }

    public void test06_constructor6(){
        OpenEJBException oe = new OpenEJBException("An exception has occured", new Exception("Root cause") );
    }
        
    public void test07_constructor7(){
        OpenEJBException oe = new OpenEJBException( new Exception("Root cause") );
    }

    public void test08_getMessage1(){
        OpenEJBException oe = new OpenEJBException("sa0001" , "test" );
        String expected = "test: Connection to reset by peer.";
        String actual = oe.getMessage();
        assertEquals(expected, actual);
    }

    public void test09_getMessage2(){
        OpenEJBException oe = new OpenEJBException("Exception One", new Exception("Root Exception.") );
        String expected = "Exception One: Root Exception.";
        String actual = oe.getMessage();
        assertEquals(expected, actual);
    }

    public void test10_printStackTrace1(){
        OpenEJBException oe = new OpenEJBException("Exception One", new Exception("Root Exception.") );
        PrintStream err = System.err;
        ByteArrayOutputStream tmpErr = new ByteArrayOutputStream();
        try{
        System.setErr(new PrintStream(tmpErr));
        oe.printStackTrace();
        System.setErr(err);
        } catch (SecurityException e){}
        String actual = new String( tmpErr.toByteArray() );

        String expected1 = "org.openejb.OpenEJBException: Exception One: Root Exception.";
        String expected2 = "java.lang.Exception: Root Exception.";

        assertNotNull("Stack trace is null", actual );
        assert("Stack trace is invalid", actual.indexOf(expected1) != -1 );
        assert("Stack trace is invalid", actual.indexOf(expected2) != -1 );
    }

    public void test11_printStackTrace2(){
        OpenEJBException oe = new OpenEJBException("Exception One", new Exception("Root Exception.") );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        
        oe.printStackTrace(new PrintStream(err) );
        
        String actual = new String( err.toByteArray() );

        String expected1 = "org.openejb.OpenEJBException: Exception One: Root Exception.";
        String expected2 = "java.lang.Exception: Root Exception.";

        assertNotNull("Stack trace is null", actual );
        assert("Stack trace is invalid", actual.indexOf(expected1) != -1 );
        assert("Stack trace is invalid", actual.indexOf(expected2) != -1 );
    }

    public void test12_printStackTrace3(){
        OpenEJBException oe = new OpenEJBException("Exception One", new Exception("Root Exception.") );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(err); 
        oe.printStackTrace( writer );
        try{
        writer.flush();
        } catch (Exception e){}
        
        String actual = new String( err.toByteArray() );

        String expected1 = "org.openejb.OpenEJBException: Exception One: Root Exception.";
        String expected2 = "java.lang.Exception: Root Exception.";

        assertNotNull("Stack trace is null", actual );
        assert("Stack trace is invalid: " + actual, actual.indexOf(expected1) != -1 );
        assert("Stack trace is invalid: " + actual, actual.indexOf(expected2) != -1 );
    }

    public void test13_getRootCause(){
        Throwable expected = new Throwable("Root Exception.");
        
        OpenEJBException oe = new OpenEJBException("Exception One", expected );
        
        Throwable actual = oe.getRootCause();

        assertEquals(expected, actual );
    }

}




