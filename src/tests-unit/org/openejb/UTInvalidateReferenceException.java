package org.openejb;

import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTInvalidateReferenceException extends NamedTestCase{
    
    public UTInvalidateReferenceException(){
        super("org.openejb.InvalidateReferenceException.");
    }

    public void test01_constructor1(){
        InvalidateReferenceException ie = new InvalidateReferenceException();
    }

    public void test02_constructor2(){
        InvalidateReferenceException ie = new InvalidateReferenceException("A system exception has occured");
    }

    public void test03_constructor3(){
        InvalidateReferenceException ie = new InvalidateReferenceException("A system exception has occured" , new Exception("Root cause") );
    }

    public void test04_constructor4(){
        InvalidateReferenceException ie = new InvalidateReferenceException( new Exception("Root cause") );
    }
        
    public void test05_constructor5(){
        InvalidateReferenceException ie = new InvalidateReferenceException( new Throwable("Root cause") );
    }
}




