package org.openejb.server.security;

/**
 * The authenticator has a problem connecting to its data store (file, DB, etc.)
 *
 * @version $Revision$
 */
public class AuthenticationException extends Exception {
    public AuthenticationException() {
    }

    public AuthenticationException(String s) {
        super(s);
    }
}
