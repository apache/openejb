package org.openejb.server.security;

/**
 * Interfaces with a specific security data repository.
 *
 * @version $Revision$
 */
public interface Authenticator {
    public boolean isValid(String username, String password) throws AuthenticationException;
    public String[] getGroups(String username) throws AuthenticationException;
}
