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
package org.openejb.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class SerializationPerformanceTest{



    /**
     * You must first create the desired number of object graphs with
     * org.openejb.test.CreateSerializableObjectGraph.
     * 
     * Then you can serialize those graphs with this test.
     * 
     * @param args   args[0]  The number of object graph class definitions to use for the test.<br>
     *               args[1]  The number of times to instantiate and serialize each object graph for the test.<br>
     *               args[2]  The number of times to run the JavaTest and OpenEjbTest together.
     */
    public static void main(String[] args){


        try{

        /* The number of unique object graphs to intsantiate */
        int graphInstances = Integer.parseInt(args[0]);

        /* The number of times to serialize each instance of the unique object graph per test*/
        int graphIterations = Integer.parseInt(args[1]);

        /* The number of times to run each test. */
        int testcount = Integer.parseInt(args[2]);

        System.out.println("Serializing "+ graphInstances +" instances of the object graph "+ graphIterations + " times.");
        System.out.println("objects "+ (graphIterations*graphInstances));

        SerializationTest[] tests = new SerializationTest[2];

        tests[0] = new OpenEjbTest();
        tests[0].setGraphInstances(graphInstances);
        tests[0].setGraphIterations(graphIterations);

        tests[1] = new JavaTest();
        tests[1].setGraphInstances(graphInstances);
        tests[1].setGraphIterations(graphIterations);

        // Run the each test instance the number specified by testcount. */
        testcount *= tests.length;

        SerializationTest test = null;

        // Run the tests and alternates between OpenEjbTest and JavaTest.
        for (int i=0; i< testcount; i++){
            test = tests[i%tests.length];
//            System.out.println("Running "+ test.getTestName());
            float result = test.run();
            test.results = test.results + result;

            float average = result/(graphIterations*graphInstances);
//            System.out.println("Total time: " + result);
//            System.out.println("Average time: " + average);
//            System.out.println("\n");
        }

        // Average out the OpenEJB test.
        System.out.println("Stats "+ tests[0].getTestName());
        float average1 = tests[0].results/(graphIterations*graphInstances);
        System.out.println("Overall Average Time: " + average1);
        System.out.println("\n");

        // Average out the Java test.
        System.out.println("Stats "+ tests[1].getTestName());
        float average2 = tests[1].results/(graphIterations*graphInstances);
        System.out.println("Overall Average Time: " + average2);
        System.out.println("\n");

        System.out.println(tests[0].getTestName() +" average/" + tests[1].getTestName() + " average = " +average1/average2);

        /********************************************************************************
            Validate the output.
            Run the test one more time for each and compare the output.
            The output is differenced in a file called objectBB.diff under the serial
            directory created where the test is ran.
         *******************************************************************************/
        validateStreamOutput(null, (org.openejb.util.io.ObjectOutputStream)tests[0].getStream());

        }catch(Exception e){ e.printStackTrace(System.out); }
    }

    public static void validateStreamOutput(java.io.ObjectOutputStream javaOut, org.openejb.util.io.ObjectOutputStream ejbOut){
        try{

            Object object = Class.forName("org.openejb.test.object.ObjectBB0").newInstance();

            File dir = new File("results/serialization");
            dir.mkdirs();
            FileOutputStream fos1 = new FileOutputStream("results/serialization/java.objectBB");
            javaOut = new java.io.ObjectOutputStream(fos1);
            javaOut.writeObject(object);
            javaOut.flush();

            FileOutputStream fos2 = new FileOutputStream("results/serialization/openejb.objectBB");
            ejbOut.reset();
            ejbOut.serializeObject(object);
            ejbOut.flush();
            fos2.write(ejbOut.toByteArray());


            String binary1 = printBinayVersion("results/serialization/openejb.objectBB");
            String binary2 = printBinayVersion("results/serialization/java.objectBB");

            printDiff("results/serialization/objectBB.diff", binary1, binary2);

        }catch(Exception e){ e.printStackTrace(System.out); }
    }

    public static String printBinayVersion(String file){
        try{
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos3 = new FileOutputStream(file+"binary");

        int b = 0;
        int[] buf = {0,0,0,0,0,0,0,0,0,0};
        boolean done= false;
        while(!done){
            for (int i=0;i<buf.length; i++){
                b = fis.read();
                if (b == -1){ done = true; break;}
                buf[i] = b;
            }
            writeBuffer(fos3, buf);
        }
        return file+"binary";
        }catch(Exception e){ e.printStackTrace(System.out); }
        return null;
    }

    public static void writeBuffer(OutputStream fos3, int[] buf) throws IOException{

        int columnWidth = 4;
        String c = "";

        for (int i=0;i<buf.length;i++){
            c = "      " + buf[i];
            c = c.substring(c.length() - columnWidth);
            fos3.write(c.getBytes());
        }

        fos3.write("     ".getBytes());

        char x;
        for (int i=0;i<buf.length;i++){
            x = (char)buf[i];
            if (x == '\n') x = 'n';
            else if (x == '\r') x = 'r';
            fos3.write( (x+"").getBytes() );
        }
        fos3.write(13);
        fos3.write(10);
    }



    public static void printDiff(String outputFile, String fileName1, String fileName2){
        try{
            FileInputStream file1 = new FileInputStream(fileName1);
            FileInputStream file2 = new FileInputStream(fileName2);
            FileOutputStream out = new FileOutputStream(outputFile);

            int b1,b2;
            while(true){
                b1 = file1.read();
                b2 = file2.read();
                if (b1 == -1 && b2 == -1) return;
                if (b1 != b2) out.write((int)'Q');
                else out.write(b1);
            }
        }catch(Exception e){ e.printStackTrace(System.out); }
    }

    public static boolean equal(String fileName1, String fileName2){
        try{
            FileInputStream file1 = new FileInputStream(fileName1);
            FileInputStream file2 = new FileInputStream(fileName2);

            int b1,b2;
            while(true){
                b1 = file1.read();
                b2 = file2.read();
                if (b1 != b2) return false;
                if (b1 == -1 && b2 == -1) return true;
            }
        }catch(Exception e){ e.printStackTrace(System.out); }
        return false;
    }
}

abstract class SerializationTest{
    int graphInstances;
    int graphIterations;
    public float results;
    String testName;

    public void setTestName(String name){
        testName = name;
    }

    public String getTestName(){
        return testName;
    }

    public void setGraphInstances(int graphInstances){
        this.graphInstances = graphInstances;
    }

    public void setGraphIterations(int graphIterations){
        this.graphIterations = graphIterations;
    }

    public abstract float run() throws Exception;

    public abstract Object getStream();
}

class OpenEjbTest extends SerializationTest{
    org.openejb.util.io.ObjectOutputStream oos = null;

    public OpenEjbTest(){
        setTestName("OpenEjbTest");
    }

    public float run() throws Exception{
        Date start = new Date();
//        System.out.println("Start  : " + start.getTime());

        oos = new org.openejb.util.io.ObjectOutputStream(null);
        for (int i=0; i<graphInstances; i++){
            Object object = Class.forName("org.openejb.test.object.ObjectBB" +i).newInstance();
            for (int j=0; j<graphIterations; j++){
                oos.reset();
                oos.serializeObject(object);
                oos.flush();
            }
        }

        Date finish = new Date();
//        System.out.println("Finish : " + finish.getTime());
        return finish.getTime() - start.getTime();
    }

    public Object getStream(){
        return oos;
    }
}

class JavaTest extends SerializationTest{

    public JavaTest(){
        setTestName("JavaTest");
    }

    public float run() throws Exception{
        Date start = new Date();
//        System.out.println("Start  : " + start.getTime());

        ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
        for (int i=0; i<graphInstances; i++){
            Object object = Class.forName("org.openejb.test.object.ObjectBB" +i).newInstance();
            for (int j=0; j<graphIterations; j++){
                ObjectOutputStream oos2 = new ObjectOutputStream(bos);
                oos2.writeObject(object);
                bos.reset();
                oos2.flush();
            }
        }


        Date finish = new Date();
//        System.out.println("Finish : " + finish.getTime());
        return finish.getTime() - start.getTime();
    }

    public Object getStream(){
        return null;
    }
}