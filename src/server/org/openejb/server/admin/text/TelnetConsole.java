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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.openejb.server.EjbDaemon;
import org.openejb.util.SafeProperties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class TelnetConsole implements Console, TelnetCodes {

    private InputStream  in  = null;
    private OutputStream out = null;

    private DataInputStream  consoleIn  = null;
    private PrintStream      consoleOut = null;

    private Socket       socket       = null; 
    private ServerSocket serverSocket = null;
    
    private TelnetOption[] options = new TelnetOption[256];
    private SafeProperties safeProps;

    /**
     * The port number console is to listen to
     */
    private int listenPort = 4202;

    public TelnetConsole() {
        try {
            safeProps = new SafeProperties(System.getProperties(),"Telnet Server");
            
            serverSocket = new ServerSocket(listenPort);                                    
        } catch ( Throwable t ) {
            t.printStackTrace();
        }
    }
    

    public void open() throws IOException {
        socket = serverSocket.accept();
        
        in  = socket.getInputStream();
        out = socket.getOutputStream();
        
        InetAddress client = socket.getInetAddress();
        InetAddress server = serverSocket.getInetAddress();

        try{            
            EjbDaemon.checkHostsAdminAuthorization(client, server);
        } catch (SecurityException e){
            String msg = "Permission denied. "+e.getMessage()+"\r\n";
            out.write( msg.getBytes() );                
            try{
                close();
            } catch (Exception dontCare){}
            throw new IOException(e.getMessage());
        }

        Thread.currentThread().setName(client.getHostAddress());

        setupClientTerminal();
        
        consoleIn   = new DataInputStream( new TelnetInputStream(in, this) );
        consoleOut  = new TelnetPrintStream( out );

        consoleOut.println("OpenEJB Remote Server Console");
        consoleOut.println("type \'help\' for a list of commands");
    }                              

    public void close() throws IOException {
        if ( in     != null ) in.close();
        if ( out    != null ) out.close();
        if ( socket != null ) socket.close();
    }

    public DataInputStream getInputStream(){
        return consoleIn;
    }

    public PrintStream getOutputStream(){
        return consoleOut;
    }

    /**
     * it is assumed that the IAC byte has already been read from the stream.
     * 
     * @param in
     * @exception IOException
     */
    protected void processCommand() throws IOException{
        print("C: IAC ");

        int command = in.read();

        switch ( command ) {
        case WILL: senderWillEnableOption(in.read()); break;
        case DO:   pleaseDoEnableOption(in.read());   break;
        case WONT: senderWontEnableOption(in.read()); break;
        case DONT: pleaseDontEnableOption(in.read()); break;
        default: unimplementedCommand(command); break;
        }

    }
    
    private void unimplementedCommand(int command){
        println(command+": command not found");
    }

    /**
     * We haven yet implemented any Telnet options, so we
     * just explicitly disable some common options for 
     * safety sake.
     * 
     * Certain Telnet clients (MS Windows Telnet) are enabling 
     * options without asking first. Shame, shame, shame.
     * 
     * @exception IOException
     */
    private void setupClientTerminal() throws IOException{

        negotiateOption(DONT,  1);
        negotiateOption(DONT,  6);
        negotiateOption(DONT, 24);
        negotiateOption(DONT, 33);
        negotiateOption(DONT, 34);

    }

    
    /**
     * Send an option negitiation command to the client
     * 
     * @param negotiate
     * @param optionID
     * @exception IOException
     */
    private void negotiateOption(int negotiate, int optionID) throws IOException{
        TelnetOption option = getOption( optionID );
        option.inNegotiation = true;

        String n = null;
        switch ( negotiate ) {
        case WILL: n = "WILL " ; break;
        case DO:   n = "DO   " ; break;
        case WONT: n = "WONT " ; break;
        case DONT: n = "DONT " ; break;
        }
        
        println("S: IAC "+n+optionID);

        synchronized (out) {
            out.write(IAC);
            out.write(negotiate);
            out.write(optionID);
        }
    }

    /**
     * Client says: I will enable OptionX
     * 
     * If the sender initiated the negotiation of the 
     * option, we must send a reply. Replies can be DO or DON'T.
     * 
     * @param optionID
     * @exception IOException
     */
    private void senderWillEnableOption(int optionID) throws IOException {
        println("WILL "+optionID);
        TelnetOption option = getOption(optionID);
        
        if ( option.hasBeenNegotiated() ) return;

        if ( option.isInNegotiation() ) {
            option.enable();
        } else if ( !option.isInNegotiation() && option.isSupported() ) {
            negotiateOption(DO, optionID);
            option.enable();
        } else if ( !option.isInNegotiation() && !option.isSupported() ) {
            negotiateOption(DONT, optionID);
            option.disable();
        }
    }

    /**
     * Client says: Please, do enable OptionX
     * 
     * If the sender initiated the negotiation of the 
     * option, we must send a reply. 
     * 
     * Replies can be WILL or WON'T.
     * 
     * @param option
     * @exception IOException
     */
    private void pleaseDoEnableOption(int optionID) throws IOException {
        println("DO   "+optionID);
        TelnetOption option = getOption(optionID);

        if ( option.hasBeenNegotiated() ) return;

        if ( option.isInNegotiation() ) {
            option.enable();
        } else if ( !option.isInNegotiation() && option.isSupported() ) {
            negotiateOption(WILL, optionID);
            option.enable();
        } else if ( !option.isInNegotiation() && !option.isSupported() ) {
            negotiateOption(WONT, optionID);
            option.disable();
        }
    }

    /**
     * Client says: I won't enable OptionX
     * 
     *                                               
     * If the sender initiated the negotiation of the
     * option, we must send a reply.                 
     *                                               
     * Replies can only be DON'T.
     * 
     * @param optionID
     * @exception IOException
     */
    private void senderWontEnableOption(int optionID) throws IOException {
        println("WONT "+optionID);
        TelnetOption option = getOption(optionID);

        if ( option.hasBeenNegotiated() ) return;

        if ( !option.isInNegotiation() ) {
            negotiateOption(DONT, optionID);
        }
        option.disable();
    }

    /**
     * Client says: Please, don't enable OptionX
     * 
     * If the sender initiated the negotiation of the
     * option, we must send a reply.                 
     *                                               
     * Replies can only be WON'T.
     * 
     * @param optionID
     * @exception IOException
     */
    private void pleaseDontEnableOption(int optionID) throws IOException {
        println("DONT "+optionID);
        TelnetOption option = getOption(optionID);

        if ( option.hasBeenNegotiated() ) return;

        if ( !option.isInNegotiation() ) {
            negotiateOption(WONT, optionID);
        }
        option.disable();
    }


    private TelnetOption getOption(int optionID) {
        TelnetOption opt = options[optionID];
        if ( opt == null ) {
            opt = new TelnetOption(optionID);
            options[optionID] = opt;
        }
        return opt;
    }
    
    // TODO:0: Replace with actual logging
    private void println(String s){
        // System.out.println(s);
    }
    
    // TODO:0: Replace with actual logging
    private void print(String s){
        // System.out.print(s);
    }
    
    public void setListenPort( int listenPort )
    {
        this.listenPort = listenPort;
    }
    
    public int getListenPort()
    {
        return listenPort;
    }
}


class TelnetOption {

    protected int optionCode;

    protected boolean supported;

    protected boolean enabled;

    protected boolean negotiated;

    protected boolean inNegotiation;

    public TelnetOption(int optionCode) {
        this.optionCode = optionCode;
    }

    public int getOptionId() {
        return optionCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
        negotiated = true;
    }

    public void disable() {
        enabled = false;
        negotiated = true;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean hasBeenNegotiated() {
        return negotiated;
    }

    public boolean isInNegotiation() {
        return inNegotiation;
    }

    public void hasBeenNegotiated(boolean negotiated) {
        this.negotiated = negotiated;
        this.inNegotiation = false;
    }
}
