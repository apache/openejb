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
import java.io.PrintStream;
import java.util.Enumeration;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class TestRunner extends junit.textui.TestRunner{
    /**
     * This method was created in VisualAge.
     * @param writer java.io.PrintStream
     */
    public TestRunner() {
        super();
    }
    
    /**
     * main entry point.
     */
    public static void main(String args[]) {
        TestRunner aTestRunner= new TestRunner();
        try
        {
         aTestRunner.start(args);
        }
        catch ( Exception ex )
        { }
    }

    protected TestResult start(String args[]) throws Exception {
        TestResult result = new TestResult();
        try{
            result =  super.start(args);
        } catch (Exception e){
        }
        return result;
    }

    /**
     * Prints the header of the report
     */
    public void printHeader(TestResult result) {
        if (result.wasSuccessful()) {
            writer().println();
            writer().print("OK");
            writer().println (" (" + result.runCount() + " tests)");
    
        } else {
            writer().println();
            writer().println("FAILURES!!!");
            writer().println("~~ Test Results ~~~~~~~~~~~~");
            writer().println("      Run: "+result.runCount());
            writer().println(" Failures: "+result.failureCount());
            writer().println("   Errors: "+result.errorCount());
        }
    }
    
    /**
     * Prints the errors to the standard output
     */
    public void printErrors(TestResult result) {
        if (result.errorCount() != 0) {
            writer().println("\n~~ Error Results ~~~~~~~~~~~\n");
            if (result.errorCount() == 1)
                writer().println("There was "+result.errorCount()+" error:");
            else
                writer().println("There were "+result.errorCount()+" errors:");
    
            writer().println("\nError Summary:");
            int i = 1;
            for (Enumeration e= result.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().println(i + ") " + failure.failedTest());
            }
            writer().println("\nError Details:");
            i = 1;
            for (Enumeration e= result.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure)e.nextElement();
                writer().println(i+") "+failure.failedTest());
                String trace = getRelevantStackTrace(failure.thrownException());
                writer().println(trace);
            }
        }
    }
    
    /**
     * Prints failures to the standard output
     */
    public void printFailures(TestResult result) {
        if (result.failureCount() != 0) {
            writer().println("\n~~ Failure Results ~~~~~~~~~\n");
            if (result.failureCount() == 1)
                writer().println("There was " + result.failureCount() + " failure:");
            else
                writer().println("There were " + result.failureCount() + " failures:");
            
            int i = 1;
            writer().println("\nFailure Summary:");
            for (Enumeration e= result.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().println(i + ") " + failure.failedTest());
            }
            i = 1;
            writer().println("\nFailure Details:");
            for (Enumeration e= result.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().println("\n"+ i + ") " + failure.failedTest());
                Throwable t= failure.thrownException();
                if (t.getMessage() != null)
                    writer().println("\t\"" + t.getMessage() + "\"");
                else {
                    writer().println();
                    failure.thrownException().printStackTrace();
                }
            }
        }
    }

    protected PrintStream writer() {
        return System.out;
    }

    /**
     * TO DO
     * 
     * @param t
     * @return 
     */
    public String getRelevantStackTrace(Throwable t){
        StringBuffer trace = new StringBuffer();
        
        try{
            // Cut the stack trace after "at junit.framework" is found
            // Return just the first part.
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.PrintWriter pw = new java.io.PrintWriter(bos);
            t.printStackTrace(pw);
            pw.close();
    
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(bos.toString()));
            String line = reader.readLine();
            while(line != null) {
                if (line.indexOf("at junit.framework") != -1) break;
                if (line.indexOf("at org.openejb.test.NumberedTestCase") != -1) break;
                if (line.indexOf("at org.openejb.test.TestSuite") != -1) break;
                
                trace.append(line).append('\n');
                line = reader.readLine();
            }
        } catch(Exception e){
        }
        
        return trace.toString();
    }



}
