package org.openejb.server.security;

/**
 * Used when no authenticator is configured.
 *
 * @version $Revision$
 */
public class NullAuthenticator implements Authenticator {
    public boolean isValid(String username, String password) throws AuthenticationException {
        return false;
    }

    public String[] getGroups(String username) throws AuthenticationException {
        return new String[0];
    }
}
