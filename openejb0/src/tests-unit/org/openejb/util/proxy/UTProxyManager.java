/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util.proxy;

import java.lang.reflect.Method;

import junit.framework.Protectable;
import junit.framework.Test;

import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTProxyManager extends NamedTestCase{

    String previousDefaultFactory;
    String factoryName;
    ProxyFactory factory;
    InvocationHandler handler;

    public UTProxyManager(){
        super("org.openejb.util.proxy.ProxyManager.");
        String version = System.getProperties().getProperty("java.vm.version");
        try{
        if(version.indexOf("1.3")>-1){
            factory = (ProxyFactory)Class.forName("org.openejb.util.proxy.jdk13.Jdk13ProxyFactory").newInstance();
            factoryName = "JDK 1.3 Proxy";
        }else{
            factory = (ProxyFactory)Class.forName("org.openejb.util.proxy.jdk12.Jdk12ProxyFactory").newInstance();
            factoryName = "JDK 1.2 Proxy";
        }
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("java.vm.version = \'"+version+"\',  ProxyManager = \'"+factoryName+"\'");
    }
    
    public void setUp() throws Exception{
        String version = System.getProperties().getProperty("java.vm.specification.version");
        
        previousDefaultFactory = ProxyManager.getDefaultFactoryName();
        handler = new InvocationHandler(){
            public Object invoke(Object p, Method m, Object[] a) throws Throwable {
                throw new java.lang.UnsupportedOperationException(""+m.getName());
            }
        };
    }

    public void tearDown() throws Exception{
        ProxyManager.setDefaultFactory(previousDefaultFactory);
    }

    public void test01_constructor1(){
        ProxyManager mgr = new ProxyManager();
    }

    public void test02_registerFactory(){
        try{
            ProxyManager.registerFactory(factoryName, factory);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }
    
    public void test03_getFactory(){
        try{
            ProxyFactory expected = factory;
            ProxyFactory actual = ProxyManager.getFactory(factoryName);
            assertEquals(expected, actual);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test04_checkDefaultFactory(){
        try{
            ProxyManager.checkDefaultFactory();
        } catch (IllegalStateException e){
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test05_setDefaultFactory(){
        try{
            ProxyManager.setDefaultFactory(factoryName);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }
    
    public void test06_getDefaultFactory(){
        try{
            Object expected = factory;
            Object actual = ProxyManager.getDefaultFactory();
            assertEquals(expected, actual);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }
    
    public void test07_getDefaultFactoryName(){
        try{
            Object expected = factoryName;
            Object actual = ProxyManager.getDefaultFactoryName();
            assertEquals(expected, actual);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }
    
    public void BUG_test08_newProxyInstance1(){
        try{
            Object actual = ProxyManager.newProxyInstance(java.lang.Runnable.class, handler);
            assert("Proxy doesn't implement the desired interface.", (actual instanceof Runnable));
            Runnable runnable = (Runnable)actual;
            runnable.run();
        } catch (UnsupportedOperationException e){
            assertEquals("run", e.getMessage());
            return;
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
        assert("Handler was not invoked. UnsupportedOperationException should have been thrown.", false);
    }
    
    public void test09_newProxyInstance2(){
        try{
            Object actual = ProxyManager.newProxyInstance(Test.class, handler);
            assert("Proxy doesn't implement the desired interface.", (actual instanceof Test));
            Test test = (Test)actual;
            test.countTestCases();
        } catch (UnsupportedOperationException e){
            assertEquals("countTestCases", e.getMessage());
            return;
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
        assert("Handler was not invoked. UnsupportedOperationException should have been thrown.", false);
    }

    public void test10_newProxyInstance3(){
        try{
            Class[] interfaces = {Test.class, Protectable.class};
            Object obj = ProxyManager.newProxyInstance(interfaces, handler);
                        
            String actual = "";
            String expected = "countTestCases";
            
            assert("Proxy doesn't implement the desired interface.", (obj instanceof Test));
            try{
                /* Test */
                Test test = (Test)obj;
                test.countTestCases();
            } catch (UnsupportedOperationException e){
                actual = e.getMessage();
            }
            assertEquals(expected,actual);
            
            assert("Proxy doesn't implement the desired interface.", (obj instanceof Protectable));
            
            expected = "protect";
            try{
                /* Protectable */
                Protectable p = (Protectable)obj;
                p.protect();
            } catch (UnsupportedOperationException e){
                actual = e.getMessage();
            } catch (Throwable e){
                assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
            }
            assertEquals(expected,actual);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test11_newProxyInstance3(){
        try{
            Class proxyClass = ProxyManager.getProxyClass(Test.class);
            Object proxy = ProxyManager.newProxyInstance(proxyClass);
            assert("Proxy doesn't implement the desired interface.", (proxy instanceof Test));
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test12_getInvocationHandler(){
        try{
            InvocationHandler expected = handler;
            Object obj = ProxyManager.newProxyInstance(Test.class, expected);
            InvocationHandler actual = ProxyManager.getInvocationHandler( obj );
            assertEquals(expected,actual);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test13_setInvocationHandler(){
        try{
            InvocationHandler expected = new InvocationHandler(){
                public Object invoke(Object p, Method m, Object[] a) throws Throwable { return null;}
            };

            Object obj = ProxyManager.newProxyInstance(Test.class, expected);
            ProxyManager.setInvocationHandler( obj, expected );
            InvocationHandler actual = ProxyManager.getInvocationHandler( obj );
            assertEquals(expected,actual);
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test14_getProxyClass1(){
        try{
            Class superClass = Test.class;
            Class subClass = ProxyManager.getProxyClass(superClass);
            assert("Proxy class does not implement the interface.", superClass.isAssignableFrom(subClass));
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test15_getProxyClass2(){
        try{
            Class[] superClass = {Test.class, Protectable.class};
            Class subClass = ProxyManager.getProxyClass(superClass);
            assert("Proxy class does not implement the interface.", superClass[0].isAssignableFrom(subClass));
            assert("Proxy class does not implement the interface.", superClass[1].isAssignableFrom(subClass));
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }

    public void test16_isProxyClass(){
        try{
            Class proxyClass = ProxyManager.getProxyClass(Test.class);
            assert("Class is not a valid proxy class.", ProxyManager.isProxyClass(proxyClass));
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }
    
    public void test17_addProxyInterface(){
        try{
            ProxyManager.unregisterFactory(factoryName);
//            assert("Class is not a valid proxy class.", ProxyManager.isProxyClass(proxyClass));
        } catch (Exception e){
            assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
        }
    }
}


