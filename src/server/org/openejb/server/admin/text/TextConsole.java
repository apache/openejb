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
package org.openejb.server.admin.text;

import java.io.*;
import java.net.*;
import java.util.*;
import org.openejb.util.Logger;
import org.openejb.server.EjbDaemon;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class TextConsole implements Runnable {
    
    Logger logger = new Logger( "OpenEJB" );

    // The EJB Server Port
    Properties props;
    EjbDaemon ejbd;

    public TextConsole(EjbDaemon ejbd) {
        this.ejbd = ejbd;
    }

    public void init(Properties props) throws Exception{
        this.props = props;
    }

    boolean stop = false;

    DataInputStream  in  = null;
    DataOutputStream out = null;

    public void start(){
        try{
            this.setConsole( new RemoteConsole() );
            Thread d = new Thread(this);
            d.setName("Remote Text Console");
            d.setDaemon(true);
            d.start();
            
            /* In the future we can create other types
             * of consoles (Terminals).
            TextConsole remote = new TextConsole(ejbd);
            remote.init( props );
            remote.setConsole( new TelnetConsole() );
            d = new Thread(remote);
            d.setName("Remote Text Console");
            d.setDaemon(true);
            d.start();
            */
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    Console console;

    private Console getConsole(){
        return console;
    }

    private void setConsole(Console c){
        this.console = c;
    }

    public static final char ESC = (char)27;
    
    public static final String TTY_Reset      = ESC+"[0m";
    public static final String TTY_Bright     = ESC+"[1m";
    public static final String TTY_Dim        = ESC+"[2m";
    public static final String TTY_Underscore = ESC+"[4m";
    public static final String TTY_Blink      = ESC+"[5m";
    public static final String TTY_Reverse    = ESC+"[7m";
    public static final String TTY_Hidden     = ESC+"[8m";
    
    /* Foreground Colors */
    public static final String TTY_FG_Black   = ESC+"[30m";
    public static final String TTY_FG_Red     = ESC+"[31m";
    public static final String TTY_FG_Green   = ESC+"[32m";
    public static final String TTY_FG_Yellow  = ESC+"[33m";
    public static final String TTY_FG_Blue    = ESC+"[34m";
    public static final String TTY_FG_Magenta = ESC+"[35m";
    public static final String TTY_FG_Cyan    = ESC+"[36m";
    public static final String TTY_FG_White   = ESC+"[37m";
        
    /* Background Colors */
    public static final String TTY_BG_Black   = ESC+"[40m";
    public static final String TTY_BG_Red     = ESC+"[41m";
    public static final String TTY_BG_Green   = ESC+"[42m";
    public static final String TTY_BG_Yellow  = ESC+"[43m";
    public static final String TTY_BG_Blue    = ESC+"[44m";
    public static final String TTY_BG_Magenta = ESC+"[45m";
    public static final String TTY_BG_Cyan    = ESC+"[46m";
    public static final String TTY_BG_White   = ESC+"[47m";
        
    public static String PROMPT = TTY_Bright+"[openejb]$ "+TTY_Reset;
    
    public void run( ) {
        
        Console console = getConsole();
        
        // can't ever truely close the console
        while ( true ) {
            try {
                console.open();
                stop = false;

                // TODO:1: Login
                //...need a security service first
                
                DataInputStream  in  = console.getInputStream();
                DataOutputStream out = console.getOutputStream();

                while ( !stop ) {
                    exec(in, out);
                }
                   
                console.close();
            } catch (Throwable t){
                t.printStackTrace();
                break;
            }
        }
    }

    protected void exec(DataInputStream in, DataOutputStream out){
        try {
            out.writeBytes(PROMPT);
            out.flush();

            String command = in.readLine();
            command = command.trim();
                        
            if (command.length() < 1) return;

            StringTokenizer cmdstr = new StringTokenizer(command);
            command = cmdstr.nextToken();

            // Get parameters
            Vector p = new Vector();
            while ( cmdstr.hasMoreTokens() ) {
                p.add(cmdstr.nextToken());
            }
            String[] args = new String[p.size()];
            p.copyInto(args);

            Command cmd = Command.getCommand(command);

            if (cmd == null) {
                out.writeBytes(command);
                out.writeBytes(": command not found\n");
            } else {
                cmd.exec( args, in, out );
            }
        } catch (UnsupportedOperationException e){
            this.stop = true;
        } catch (Throwable e){
            e.printStackTrace( new PrintStream(out) );
            //e.printStackTrace( );
            this.stop = true;
        }
    }

    protected void badCommand(DataInputStream in, DataOutputStream out) throws IOException{
        //asdf: command not found
    }
}


abstract class Console {

    DataInputStream  in  = null;
    DataOutputStream out = null;

    public abstract void open() throws IOException;
    public abstract void close() throws IOException;
    
    public DataInputStream  getInputStream() throws IOException{
        return in;
    }

    public DataOutputStream getOutputStream() throws IOException{
        return out;
    }
}

class LocalConsole extends Console{

    private boolean opened = false;

    public void open() throws IOException {
        if (opened) return;

        in  = new DataInputStream(  System.in );
        out = new DataOutputStream( System.out );
        
        out.writeBytes("OpenEJB Remote Server Console\n");
        out.writeBytes("type \'help\' for a list of commands\n");
        
        opened = true;
    }
    
    public void close() throws IOException {
    }
}

class RemoteConsole extends Console{
    
    Socket socket = null; 
    ServerSocket serverSocket = null;

    public RemoteConsole(){
        try{
            serverSocket = new ServerSocket(4200);                                    
        } catch(Throwable t){
            t.printStackTrace();
        }
    }

    public void open() throws IOException {
        socket = serverSocket.accept();
        out = new DataOutputStream( socket.getOutputStream() );
        in  = new DataInputStream(  socket.getInputStream() );

        out.writeBytes("OpenEJB Remote Server Console\n");
        out.writeBytes("type \'help\' for a list of commands\n");
    }                              
    
    public void close() throws IOException {
        if (in != null ) in.close();
        if (out != null ) out.close();
        if (socket != null ) socket.close();
    }
}
