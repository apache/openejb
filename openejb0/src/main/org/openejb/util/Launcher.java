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
 *    (http://www.openejb.org/).
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
 * Copyright 2003 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Helper class to dynamically create the full classpath and launch an OpenEJB application
 * 
 * TODO Introduce aliases so that 'org.openejb.EjbServer' would become 'server' TODO Introduce boolean variables to
 * indicate whether stdout/stdin/stderr shall be grabbed
 * 
 * More insight at http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps_p.html
 * 
 * @version $Revision$ $Date$
 */
public final class Launcher {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            printHelp();
            System.exit(-1);
        }

        String openejbHome = System.getProperty("openejb.home");
        if (openejbHome == null || openejbHome.length() == 0) {
            openejbHome = System.getProperty("user.dir");
        }

        String dir = null;
        StringBuffer classpath = new StringBuffer();

        //TODO make it configurable what directories to include in classpath
        dir = openejbHome + File.separator + "lib";
        appendJarsToClasspath(classpath, listDirectory(dir), dir);

        dir = openejbHome + File.separator + "dist";
        appendJarsToClasspath(classpath, listDirectory(dir), dir);

        // TODO shall it be prepended or appended? - make it configurable
        classpath.append(System.getProperty("java.class.path"));

        String[] cmd = new String[4 + args.length];
        cmd[0] = "java";
        cmd[1] = "-classpath";
        cmd[2] = classpath.toString();
        cmd[3] = "-Dopenejb.home=" + openejbHome;
        for (int i = 0; i < args.length; i++) {
            cmd[4 + i] = args[i];
        }

        Process process = null;
        try {
            Runtime rt = Runtime.getRuntime();
            process = rt.exec(cmd);

            StreamGrabber stderr = new StreamGrabber(process.getErrorStream(), System.err);
            StreamGrabber stdout = new StreamGrabber(process.getInputStream(), System.out);

            stderr.setName("Subprocess-STDERR");
            stdout.setName("Subprocess-STDOUT");

            stderr.start();
            stdout.start();

            // It seems not to work on Cygwin when the class is invoked directly
            // Once it'd been invoked from the shell script, it has started to work well 
            // Has anyone any clue about a possible solution?
            rt.addShutdownHook(new KillProcessShutdownHook(process));

            process.waitFor();
        } catch (Throwable t) {
            throw new RuntimeException("Could not start the executable: " + args[0], t);
        } finally {
            if (process != null) {
                try {
                    process.getErrorStream().close();
                    process.getOutputStream().close();
                    process.getInputStream().close();
                } catch (IOException ioe) {
                    //ignore it
                }
            }
        }
    }

    /**
	 * List directory
	 * 
	 * @param dir
	 * @return list of jars
	 */
    private static String[] listDirectory(String dir) {
        return new File(dir).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    /**
	 * Append jars to classpath
	 */
    private static void appendJarsToClasspath(StringBuffer classpath, String[] jars, String dir) {
        for (int i = 0; i < jars.length - 1; i++) {
            classpath.append(dir);
            classpath.append(File.separator);
            classpath.append(jars[i]);
            classpath.append(File.pathSeparator);
        }
    }

    public final static void printHelp() {
        System.err.println("No class specified");
        System.err.println("exiting...");
    }

    private static final class StreamGrabber extends Thread {
        InputStream is;
        OutputStream os;

        public StreamGrabber(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            try {
                int i;
                while ((i = in.read()) != -1) {
                    os.write(i);
                }
                os.flush();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
	 * Kill subprocess as Launcher finishes
	 */
    private static final class KillProcessShutdownHook extends Thread {
        Process process;

        public KillProcessShutdownHook(Process process) {
            this.process = process;
        }

        public void run() {
            process.destroy();
        }
    }
}
