package org.openejb;

import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTApplicationException extends NamedTestCase{
    
    public UTApplicationException(){
        super("org.openejb.ApplicationException.");
    }

    public void test01_constructor1(){
        ApplicationException ae = new ApplicationException();
    }

    public void test02_constructor2(){
        ApplicationException ae = new ApplicationException("A system exception has occured");
    }

    public void test03_constructor3(){
        ApplicationException ae = new ApplicationException("A system exception has occured" , new Exception("Root cause") );
    }

    public void test04_constructor4(){
        ApplicationException ae = new ApplicationException( new Exception("Root cause") );
    }
        
    public void test05_constructor5(){
        ApplicationException ae = new ApplicationException( new Throwable("Root cause") );
    }
}



