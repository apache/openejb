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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CreateSerializableObjectGraph {

    /**
     * Genterates the desired number of object graphs to be used by the the SerializationPerformanceTest.java.
     * The object graphs are generated from the templates ObjectAA, ObjectBB, ObjectCC, and ObjectDD found in the
     * openejb/test/src/org/openejb/test/object directory.  The generated files will be placed in the directory
     * as such:
     * ObjectAA0.java
     * ObjectAA1.java
     * ObjectBB0.java
     * ObjectBB1.java
     * ObjectCC0.java
     * ObjectCC1.java
     * ObjectDD0.java
     * ObjectDD1.java
     * 
     * The object graph definitions must be compiled before you can run the SerializationPerformanceTest.
     * This program must be run in the directory openejb/test/src/org/openejb/test
     * 
     * @param args args[0]  The number of object graph class definitions to create.
     */
    public static void main(String[] args){
        try{
        if (args.length != 1){
            System.out.println("Usage: java org.openejb.test.CreateSerializableObjectGraph <graphs>");
            System.out.println();
            System.out.println("\texample: java org.openejb.test.CreateSerializableObjectGraph 100");
            System.out.println();
            System.out.println("\tgraphs\t\tthe number of unique object graphs you want created.");
            System.exit(-1);
        }
        File dir = new File("object");
        //dir.mkdir();
        String[] fileNames = {"ObjectAA","ObjectBB","ObjectCC","ObjectDD"};
        File[] templateFiles = new File[fileNames.length];
        for (int i=0; i< templateFiles.length; i++){
            templateFiles[i] = new File(dir, fileNames[i]);
            if(!templateFiles[i].exists()){
                System.out.println("Template file "+templateFiles[i].getPath()+" not found in current working directory " + System.getProperty("user.dir"));
                System.exit(-2);
            }
        }

        int iterations = Integer.parseInt(args[0]);

        FileOutputStream fout;
        FileInputStream in;
        DataOutputStream out;

        for(int i=0; i < iterations; i++){
            for(int j=0; j < templateFiles.length; j++){

                fout = new FileOutputStream("object\\"+ fileNames[j]+ i + ".java");
                in = new FileInputStream(templateFiles[j]);
                out = new DataOutputStream(fout);

                while(true){
                    int token = (int)'~';
                    int c = in.read();
                    if (c == -1) break;
                    if (c == token) out.writeBytes(""+i);
                    else out.write(c);
                }
            }
        }

        }catch(Exception e){ e.printStackTrace(System.out); }
    }
}
