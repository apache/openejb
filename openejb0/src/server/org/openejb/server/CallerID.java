package org.openejb.server;

import java.io.Serializable;

/**
 * Holds authentication information for the caller.
 *
 * @version $Revision$
 */
public class CallerID implements Serializable {
    private String username;
    private String password;

    public CallerID(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
