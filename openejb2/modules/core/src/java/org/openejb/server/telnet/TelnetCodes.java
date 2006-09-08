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



/**
 */
public interface TelnetCodes {

    /**
     * End of subnegotiation parameters.
     * 
     * Name: SE
     * Code: 240
     */
    public static final int SE = 240;

    /**
     * No operation.
     * 
     * Name: NOP
     * Code: 241
     */
    public static final int NOP = 241;

    /**
     * The data stream portion of a Synch.
     * This should always be accompanied
     * by a TCP Urgent notification.
     *           
     * Name: Data Mark
     * Code: 242
     */
    public static final int Data_Mark = 242;

    /**
     * NVT character BRK.
     *  
     * Name: Break
     * Code: 243
     */
    public static final int Break = 243;

    /**
     * The function IP.
     *  
     * Name: Interrupt Process
     * Code: 244
     */
    public static final int Interrupt_Process = 244;

    /**
     * The function AO.
     *  
     * Name: Abort output
     * Code: 245
     */
    public static final int Abort_output = 245;

    /**
     * The function AYT.
     *  
     * Name: Are You There
     * Code: 246
     */
    public static final int Are_You_There = 246;

    /**
     * The function EC.
     *  
     * Name: Erase character
     * Code: 247
     */
    public static final int Erase_character = 247;

    /**
     * The function EL.
     *  
     * Name: Erase Line
     * Code: 248
     */
    public static final int Erase_Line = 248;

    /**
     * The GA signal.
     *  
     * Name: Go ahead
     * Code: 249
     */
    public static final int Go_ahead = 249;

    /**
     * Indicates that what follows is
     * subnegotiation of the indicated
     * option.
     *  
     * Name: SB
     * Code: 250
     */
    public static final int SB = 250;

    /**
     * Indicates the desire to begin
     * performing, or confirmation that
     * you are now performing, the
     * indicated option.
     *  
     * Name: WILL (option code)
     * Code: 251
     */
    public static final int WILL = 251;

    /**
     * Indicates the refusal to perform,
     * or continue performing, the
     * indicated option.
     *  
     * Name: WON'T (option code)
     * Code: 252
     */
    public static final int WONT = 252;

    /**
     * Indicates the request that the
     * other party perform, or
     * confirmation that you are expecting
     * he other party to perform, the
     * ndicated option.
     *  
     * Name: DO (option code)
     * Code: 253
     */
    public static final int DO = 253;

    /**
     * Indicates the demand that the
     * other party stop performing,
     * or confirmation that you are no
     * longer expecting the other party
     * to perform, the indicated option.
     *  
     * Name: DON'T (option code)
     * Code: 254
     */
    public static final int DONT = 254;

    /**
     * Interpret as command
     * aka Data Byte
     *  
     * Name: IAC
     * Code: 255
     */
    public static final int IAC = 255;

}
