package org.openejb;

import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTSystemException extends NamedTestCase{
    
    public UTSystemException(){
        super("org.openejb.SystemException.");
    }

    public void test01_constructor1(){
        SystemException se = new SystemException();
    }

    public void test02_constructor2(){
        SystemException se = new SystemException("A system exception has occured");
    }

    public void test03_constructor3(){
        SystemException se = new SystemException("A system exception has occured" , new Exception("Root cause") );
    }

    public void test04_constructor4(){
        SystemException se = new SystemException( new Exception("Root cause") );
    }
}


