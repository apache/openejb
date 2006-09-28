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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.server.telnet;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

//import org.codehaus.groovy.runtime.InvokerHelper;
//import groovy.lang.GroovyShell;

/**
 */
public class GroovySh extends Command {

    public static void register() {
        //Command.register("groovysh", GroovySh.class);
    }
    
    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException{
/*        GroovyShell shell = new GroovyShell();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String version = InvokerHelper.getVersion();
        
        out.println("Lets get Groovy!");
        out.println("================");
        out.println("Version: " + version + " JVM: " + System.getProperty("java.vm.version"));
        out.println("Hit carriage return twice to execute a command");
        out.println("The command 'quit' will terminate the shell");
        
        int counter = 1;
        while (true) {
            StringBuffer buffer = new StringBuffer();
            while (true) {
                out.print("groovy> ");
                String line = reader.readLine();
                if (line != null) {
                    buffer.append(line);
                    buffer.append('\n');
                }
                if (line == null || line.trim().length() == 0) {
                    break;
                }
            }
            String command = buffer.toString().trim();
            if (command == null || command.equals("quit")) {
                break;
            }
            try {
                Object answer = shell.evaluate(command, "CommandLine" + counter++ +".groovy");
                out.println(InvokerHelper.inspect(answer));
            }
            catch (Exception e) {
                out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
*/    }
}

