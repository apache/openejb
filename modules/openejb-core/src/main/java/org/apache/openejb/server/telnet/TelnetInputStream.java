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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class TelnetInputStream extends FilterInputStream implements TelnetCodes{
   
    // state table for what options have been negotiated
    private TelnetOption[] options = new TelnetOption[256];
    
    private OutputStream out = null;
    
    /**
     * We haven yet implemented any Telnet options, so we just explicitly
     * disable some common options for safety sake.
     * 
     * Certain Telnet clients (MS Windows Telnet) are enabling options without
     * asking first. Shame, shame, shame.
     * 
     * @exception IOException
     */
    public TelnetInputStream(InputStream in, OutputStream out) throws IOException{
        super(in);
        this.out = out;
        negotiateOption(DONT, 1);
        negotiateOption(DONT, 6);
        negotiateOption(DONT, 24);
        negotiateOption(DONT, 33);
        negotiateOption(DONT, 34);
   }
    
    
    public int read() throws IOException {
        int b = super.read();
        
        if (b == IAC) {
            // The cosole has a reference
            // to this input stream
            processCommand();
            // Call read recursively as
            // the next character could
            // also be a command
            b = this.read();
        }

        //System.out.println("B="+b);
        return b;
    }
    
    /**
     * This is only called by TelnetInputStream
     * it is assumed that the IAC byte has already been read from the stream.
     *
     * @param in
     * @exception IOException
     */
    private void processCommand() throws IOException{
        // Debug statement
        print("C: IAC ");
        
        int command = super.read();
        
        switch ( command ) {
            case WILL: senderWillEnableOption(super.read()); break;
            case DO:   pleaseDoEnableOption(super.read()); break;
            case WONT: senderWontEnableOption(super.read()); break;
            case DONT: pleaseDontEnableOption(super.read()); break;
            default: unimplementedCommand(command); break;
        }
        
    }
    
    private void unimplementedCommand(int command){
        println(command+": command not found");
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
        // Debug statement
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
        // Debug statement
        println("DO "+optionID);
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
        // Debug statement
        println("DONT "+optionID);
        
        TelnetOption option = getOption(optionID);
        
        if ( option.hasBeenNegotiated() ) return;
        
        if ( !option.isInNegotiation() ) {
            negotiateOption(WONT, optionID);
        }
        option.disable();
    }
    
    
    
    // TODO:0: Replace with actual logging
    private void println(String s){
        // System.out.println(s);
    }
    
    // TODO:0: Replace with actual logging
    private void print(String s){
        // System.out.print(s);
    }
    
    /**
     * Send an option negitiation command to the client
     * 
     * @param negotiate
     * @param optionID
     * @exception IOException
     */
    private void negotiateOption(int negotiate, int optionID)
    throws IOException {
        TelnetOption option = getOption(optionID);
        option.inNegotiation = true;

        String n = null;
        switch (negotiate) {
            case WILL :
                n = "WILL ";
                break;
            case DO :
                n = "DO ";
                break;
            case WONT :
                n = "WONT ";
                break;
            case DONT :
                n = "DONT ";
                break;
        }

        // Debug statement
        println("S: IAC " + n + optionID);

        synchronized (out) {
            out.write(IAC);
            out.write(negotiate);
            out.write(optionID);
        }
    }

    private TelnetOption getOption(int optionID) {
        TelnetOption opt = options[optionID];
        if (opt == null) {
            opt = new TelnetOption(optionID);
            options[optionID] = opt;
        }
        return opt;
    }
    
    
}
