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
package org.acme.clients;

import com.titan.clients.Client_1;
import com.titan.clients.Client_2;
import com.titan.clients.Client_3;
import com.titan.clients.Client_4;
import com.titan.clients.Client_cleanup;

/**
 * @version $Revision$ $Date$
 */
public final class Main {

    final static String HELLOWORLD = "HelloWorld";
    final static String CLIENT_1 = "Client_1";
    final static String CLIENT_2 = "Client_2";
    final static String CLIENT_3 = "Client_3";
    final static String CLIENT_4 = "Client_4";
    final static String CLIENT_CLEANUP = "Client_cleanup";

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }

        if (HELLOWORLD.equals(args[0])) {
            HelloWorld.main(null);
        } else if (CLIENT_1.equals(args[0])) {
            Client_1.main(null);
        } else if (CLIENT_2.equals(args[0])) {
            Client_2.main(null);
        } else if (CLIENT_3.equals(args[0])) {
            Client_3.main(null);
        } else if (CLIENT_4.equals(args[0])) {
            Client_4.main(null);
        } else if (CLIENT_CLEANUP.equals(args[0])) {
            Client_cleanup.main(null);
        }
    }

    public static final void usage() {
        println("");
        println("Usage: ");
        println("\tjava -jar dist/examples_clients-$VERSION$.jar CLIENT");
        println("");
        println("where CLIENT may become:");
        println("");
        println("\to " + HELLOWORLD);
        println("\to " + CLIENT_1);
        println("\to " + CLIENT_2);
        println("\to " + CLIENT_3);
        println("\to " + CLIENT_4);
        println("\to " + CLIENT_CLEANUP);
    }

    private static void println(String message) {
        System.out.println(message);
    }
}
