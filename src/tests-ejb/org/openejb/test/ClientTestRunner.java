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

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.Test;
//import junit.util.StringUtil;
import java.io.PrintStream;
import java.util.Enumeration;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ClientTestRunner extends org.openejb.test.TestRunner{
    /**
     * This method was created in VisualAge.
     * @param writer java.io.PrintStream
     */
    public ClientTestRunner() {
        super();
    }
    
    /**
     * main entry point.
     */
    public static void main(String args[]) {

        TestRunner aTestRunner = new ClientTestRunner();

        if ( args.length > 2 && args[0].equals("-s") ) {
            try {
                TestManager.init(args[1]);
            } catch ( Exception e ){ 
                System.out.println("Cannot initialize the test environment: "+e.getClass().getName()+" "+e.getMessage());
                e.printStackTrace();
                System.exit(-1);
            }
            String[] newArgs = new String[args.length-2];
            System.arraycopy(args, 2, newArgs, 0, newArgs.length);
            args = newArgs;
            
        } else {
            try {
                TestManager.init(null);
            } catch ( Exception e ){ 
                System.out.println("Cannot initialize the test environment: "+e.getClass().getName()+" "+e.getMessage());
                e.printStackTrace();
                System.exit(-1);
            }
        }
        
        try {
            aTestRunner.start(args);
            System.exit(0); // Added for JUNIT 3.5
        } catch ( Exception ex )
        { }
    }

    public TestResult doRun(Test suite, boolean wait) {
        TestResult result= createTestResult();
        result.addListener(this);
        long startTime = 0L;
        long endTime = 0L; 
        long runTime = 0L; 
        
        try{
            TestManager.start();           
        } catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            startTime= System.currentTimeMillis();
            suite.run(result);
        } catch( Exception e){
        } finally {
            endTime = System.currentTimeMillis();
            runTime = endTime-startTime;
            try{
                TestManager.stop();           
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        
        
        //writer().println();
        //writer().println("Time: "+StringUtil.elapsedTimeAsString(runTime));
        print(result);

        writer().println();
        
        if (wait) {
            writer().println("<RETURN> to continue");
            try {
                System.in.read();
            }
            catch(Exception e) {
            }
        }
        return result;
    }

}
