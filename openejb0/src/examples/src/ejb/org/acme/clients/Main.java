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
