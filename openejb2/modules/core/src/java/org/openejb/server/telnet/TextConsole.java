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
package org.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openejb.util.Logger;

/**
 */
public class TextConsole  {

    Logger logger = Logger.getInstance( "OpenEJB.admin", "org.openejb.server" );

    Properties props;
    
    public TextConsole() {
    }

    public void init( Properties props ) throws Exception {
        this.props = props;
    }

    boolean stop = false;

    DataInputStream in = null;
    PrintStream out = null;

    public static final char ESC = ( char ) 27;

    public static final String TTY_Reset      = ESC + "[0m";
    public static final String TTY_Bright     = ESC + "[1m";
    public static final String TTY_Dim        = ESC + "[2m";
    public static final String TTY_Underscore = ESC + "[4m";
    public static final String TTY_Blink      = ESC + "[5m";
    public static final String TTY_Reverse    = ESC + "[7m";
    public static final String TTY_Hidden     = ESC + "[8m";

    /* Foreground Colors */
    public static final String TTY_FG_Black   = ESC + "[30m";
    public static final String TTY_FG_Red     = ESC + "[31m";
    public static final String TTY_FG_Green   = ESC + "[32m";
    public static final String TTY_FG_Yellow  = ESC + "[33m";
    public static final String TTY_FG_Blue    = ESC + "[34m";
    public static final String TTY_FG_Magenta = ESC + "[35m";
    public static final String TTY_FG_Cyan    = ESC + "[36m";
    public static final String TTY_FG_White   = ESC + "[37m";

    /* Background Colors */
    public static final String TTY_BG_Black   = ESC + "[40m";
    public static final String TTY_BG_Red     = ESC + "[41m";
    public static final String TTY_BG_Green   = ESC + "[42m";
    public static final String TTY_BG_Yellow  = ESC + "[43m";
    public static final String TTY_BG_Blue    = ESC + "[44m";
    public static final String TTY_BG_Magenta = ESC + "[45m";
    public static final String TTY_BG_Cyan    = ESC + "[46m";
    public static final String TTY_BG_White   = ESC + "[47m";

    public static String PROMPT = TTY_Reset + TTY_Bright + "[openejb]$ " + TTY_Reset;

    protected void exec( InputStream input, PrintStream out ) {
        DataInputStream in = new DataInputStream(input);
        while (!stop) {
            prompt(in,out);
        }
        
    }
        
    protected void prompt( DataInputStream in, PrintStream out ) {
        
        try {
            out.print( PROMPT );
            out.flush();

            String commandline = in.readLine();
            logger.debug( "command: " + commandline );
            commandline = commandline.trim();

            if ( commandline.length() < 1 ) return;

            String command = commandline;
            Command.Arguments args = null;

            int spacePosition = commandline.indexOf( ' ' );
            int tabPosition = commandline.indexOf( '\t' );
            if ( spacePosition != -1 || tabPosition != -1 ) {
                int cutPosition = ( spacePosition > tabPosition ? spacePosition : tabPosition );
                command = commandline.substring( 0, cutPosition );
                args = new Command.Arguments( commandline.substring( cutPosition + 1 ) );
            }

            Command cmd = Command.getCommand( command );

            if ( cmd == null ) {
                out.print( command );
                out.println( ": command not found" );
            } else {
                cmd.exec( args, in, out );
            }
        } catch ( UnsupportedOperationException e ) {
            this.stop = true;
        } catch ( Throwable e ) {
            e.printStackTrace( new PrintStream( out ) );
            //e.printStackTrace( );
            this.stop = true;
        }
    }

    protected void badCommand( DataInputStream in, PrintStream out ) throws IOException
    {
        //asdf: command not found
    }
}

