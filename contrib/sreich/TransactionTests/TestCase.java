//
//  TestCase.java
//  TransactionTests
//
//  Created by Stefan Reich on Wed Dec 12 2001.
//  Copyright (c) 2001 OpenEJB Group. All rights reserved.
//

import transactiontests.*;
import java.util.*;

public class TestCase {

    public static void main(String argv[]) {
	try{
	    test();
	}catch(Exception e) {
	    e.printStackTrace();
	    return;
	}
    }

    public static void test() throws Exception {

	javax.naming.Context  context = new javax.naming.InitialContext();
	
	Object ref = context.lookup("TestBean");
	TestHome testHome = (TestHome) javax.rmi.PortableRemoteObject.narrow(ref, TestHome.class);
	Test test = testHome.create();
	test.test();
	System.out.println("------- Starting CMP tests --");
	testCMP(100);
	testBMP(100);
    }

    private static void testCMP(int iterations) throws Exception{
        javax.naming.Context  context = new javax.naming.InitialContext();
        Object ref = context.lookup("CMPEntityBean");

        CMPEntityHome cmpHome = (CMPEntityHome) javax.rmi.PortableRemoteObject.narrow(ref, CMPEntityHome.class);
        int i=0;

        Collection c = cmpHome.findAll();
        Iterator it = c.iterator ();
        System.out.println("Removing "+c.size()+" records");
        javax.transaction.UserTransaction ta=null;
        while (it.hasNext ()){
            CMPEntity d = (CMPEntity) javax.rmi.PortableRemoteObject.narrow(it.next(), CMPEntity.class);
            String triggerEJBLoad = d.getValue();
            if(ta==null) {
//                ta = d.getUserTransaction();
//                ta.begin();
            }
            d.remove();
        }
//        ta.commit();
        
        System.out.println("Creating "+iterations+" CMP beans.");
        try{
            CMPEntity[] instances = new CMPEntity[iterations];
            long t0=System.currentTimeMillis();
            for (i = 0 ; i < iterations; i++) {
                CMPEntity bean = cmpHome.create();
                instances[i]=bean;
                bean.setValue("Blabla");
            }
            long t=System.currentTimeMillis()-t0;
            System.out.println("Avg call for ejbCreate an a business method (ms):"+t/(float) iterations);
            t0=System.currentTimeMillis();
            for (i = 0 ; i < iterations; i++) {
                instances[i].remove();
            }
            System.out.println("Avg call for ejbRemove method (ms):"+t/(float) iterations);
        } catch (Throwable e) {
            System.err.println ("\nERROR: on CMP iteration: "+i+" "+e);
        }
    }

    private static void testBMP(int iterations) throws Exception{
        javax.naming.Context  context = new javax.naming.InitialContext();
        Object ref = context.lookup("BMPEntityBean");

        BMPEntityHome bmpHome = (BMPEntityHome) javax.rmi.PortableRemoteObject.narrow(ref, BMPEntityHome.class);
        Enumeration e = bmpHome.findAll();
        System.out.println("Removing records");
        while (e.hasMoreElements ()){
            BMPEntity d = (BMPEntity) javax.rmi.PortableRemoteObject.narrow(e.nextElement(), BMPEntity.class);
            d.remove();
        }
        int i=0;
        System.out.println("Creating "+iterations+" BMP beans.");
        try{
            BMPEntity[] instances = new BMPEntity[iterations];
            long t0=System.currentTimeMillis();
            for (i = 0 ; i < iterations; i++) {
                BMPEntity bean = bmpHome.create();
                instances[i]=bean;
            }
            long t=System.currentTimeMillis()-t0;
            System.out.println("Avg call for ejbCreate an a business method (ms):"+t/(float) iterations);
            t0=System.currentTimeMillis();
            for (i = 0 ; i < iterations; i++) {
                instances[i].remove();
            }
            System.out.println("Avg call for ejbRemove method (ms):"+t/(float) iterations);
        } catch (Throwable t) {
            System.err.println ("\nERROR: on BMP iteration: "+i+" "+t);
        }
    }
    
}
