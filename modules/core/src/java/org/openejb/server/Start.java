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

 *    please contact info@openejb.org.

 *

 * 4. Products derived from this Software may not be called "OpenEJB"

 *    nor may "OpenEJB" appear in their names without prior written

 *    permission of The OpenEJB Group. OpenEJB is a registered

 *    trademark of The OpenEJB Group.

 *

 * 5. Due credit should be given to the OpenEJB Project

 *    (http://openejb.org/).

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

package org.openejb.server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class Start {

    public static void main(String[] args) {
//        System.exit(new Start().start()?0:1);
        new Start().start();
    }

    public boolean start(){
        if (!connect()) {
            forkServerProcess();
            return connect(10);
        } else {
            System.out.println(":: server already started ::");
            return true;
        }
    }

    private void forkServerProcess() {
        try{
            ArrayList cmd = new ArrayList();

            String s = java.io.File.separator;

            String java = System.getProperty("java.home")+s+"bin"+s+"java";

            cmd.add(java);

            addSystemProperties(cmd);

            cmd.add("-classpath");
            cmd.add(getClasspath());
            cmd.add("org.openejb.server.Main");

            String[] command = (String[]) cmd.toArray(new String[0]);

            Runtime runtime = Runtime.getRuntime();
            Process server = runtime.exec( command );

            // Pipe the processes STDOUT to ours
            InputStream out = server.getInputStream();
            Thread serverOut = new Thread(new Pipe(out, System.out));
            serverOut.setDaemon(true);
            serverOut.start();

            // Pipe the processes STDERR to ours
            InputStream err = server.getErrorStream();
            Thread serverErr = new Thread(new Pipe(err, System.err));
            serverErr.setDaemon(true);
            serverErr.start();
        } catch (Exception e){
            throw new RuntimeException("Cannot start the server.");
        }
    }

    private void addSystemProperties(ArrayList cmd) {
        Set set = System.getProperties().entrySet();
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if ( key.matches("^-X.*") ){
                cmd.add(key+value);
            } else if ( !key.matches("^(java|javax|os|sun|user|file|awt|line|path)\\..*") ){
                cmd.add("-D"+key+"="+value);
            }
        }
    }

    private String getClasspath() {
        String classpath = System.getProperty("java.class.path");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String antLoader = "org.apache.tools.ant.AntClassLoader";
        if (cl.getClass().getName().equals(antLoader)){
            try {
                Class ant = cl.getClass();
                Method getClasspath = ant.getMethod("getClasspath", new Class[0]);
                classpath += File.pathSeparator + getClasspath.invoke(cl,new Object[0]);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return classpath;
    }

    private boolean connect() {
        return connect( 1 );
    }

    private boolean connect(int tries) {
        try{
            Socket socket = new Socket("localhost", 4201);
            OutputStream out = socket.getOutputStream();
        } catch (Exception e){
            if ( tries < 2 ) {
                return false;
            } else {
                try{
                    Thread.sleep(2000);
                } catch (Exception e2){
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        }
        return true;
    }

    private static final class Pipe implements Runnable {
        private final InputStream is;
        private final OutputStream out;
        private Pipe(InputStream is, OutputStream out) {
            super();
            this.is = is;
            this.out = out;
        }
        public void run() {
            try{
                int i = is.read();
                out.write( i );
                while ( i != -1 ){
                    i = is.read();
                    out.write( i );
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
