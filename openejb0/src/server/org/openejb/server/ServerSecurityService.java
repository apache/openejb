package org.openejb.server;

import java.security.Principal;
import java.util.*;
import java.io.File;
import java.net.URL;
import javax.security.auth.Subject;
import org.openejb.spi.SecurityService;
import org.openejb.server.security.*;
import org.openejb.util.Logger;
import org.openejb.util.FileUtils;

/**
 * Actually tracks and reads security information.
 *
 * @version $Revision$
 */
public class ServerSecurityService implements SecurityService {
    private final static String AUTH_FILE_NAME="auth.file";
    private final static Logger logger = Logger.getInstance("OpenEJB.server.security", "org.openejb.util.resources"); //todo: proper resource name
    private Authenticator auth;
    private ThreadLocal caller = new ThreadLocal();

    public boolean isCallerAuthorized(String[] roleNames) {
        if(roleNames == null) {
            return false;
        }
        Principal principal = getCallerPrincipal();
        if(principal == null) {
            return false;
        }
        try {
            String[] groups = auth.getGroups(principal.getName());
            for(int i=0; i<roleNames.length; i++) {
                for(int j=0; j<groups.length; j++) {
                    if(roleNames[i].equals(groups[j])) {
                        return true;
                    }
                }
            }
        } catch(AuthenticationException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public Subject getCallerSubject() throws UnsupportedOperationException {
        return (Subject) caller.get();

    }

    public Principal getCallerPrincipal() {
        Subject s = (Subject) caller.get();
        if(s == null) {
            return null;
        }
        return (Principal) s.getPrincipals().iterator().next();
    }

    /**
     * Sets the current caller's authentication information.  If the username is
     * null, the caller is assumed to have not authenticated, but that's OK so
     * long as they don't attempt to access any protected resources.
     *
     * @param username The username supplied by the caller
     * @param password The password supplied by the caller
     *
     * @return <tt>true</tt> if the caller did not authenticate at all, or
     *         authenticated properly.  <tt>false</tt> if the authentication
     *         failed.
     */
    public boolean authenticateCaller(final String username, String password) {
        try {
            if(username == null) {
                caller.set(null);
                return true;
            } else if(password == null) {
                caller.set(null);
                return false;
            } else {
                if(auth.isValid(username, password)) {
                    Principal p = new Principal() {
                        public String getName() {
                            return username;
                        }
                    };
                    Set ps = new HashSet();
                    ps.add(p);
                    Set cs = new HashSet();
                    cs.add(password);
                    Subject s = new Subject(true, ps, new HashSet(), cs);
                    caller.set(s);
                    return true;
                } else {
                    caller.set(null);
                    return false;
                }
            }
        } catch(AuthenticationException e) {
            logger.error(e.getMessage(), e);
            caller.set(null);
            return false;
        }
    }

    /**
     * Connects to the security data store.  Currently only file stores are
     * supported, and a file must be named with the <tt>auth.file</tt>
     * property.
     */
    public void init(Properties props) {
        String fileName = props.getProperty(AUTH_FILE_NAME);
        if(fileName != null) {
            File f = new File(fileName);
            if(f.exists() && !f.isDirectory() && f.canRead()) {
                auth = new FileAuthenticator(f);
            } else {
                try {
                    f = FileUtils.getBase().getFile(fileName);
                    if(f.exists() && !f.isDirectory() && f.canRead()) {
                        auth = new FileAuthenticator(f);
                    }
                } catch(java.io.IOException e) {
                    try {
                        f = FileUtils.getBase().getFile("conf/"+fileName);
                        if(f.exists() && !f.isDirectory() && f.canRead()) {
                            auth = new FileAuthenticator(f);
                        }
                    } catch(java.io.IOException e2) {}
                }
            }
            if(auth == null) {
                URL u = getClass().getClassLoader().getResource(fileName);
                if(u != null) {
                    f = new File(u.getPath());
                    if(f.exists() && !f.isDirectory() && f.canRead()) {
                        auth = new FileAuthenticator(f);
                    }
                }
                if(auth == null) {
                    u = Thread.currentThread().getContextClassLoader().getResource(fileName);
                    if(u != null) {
                        f = new File(u.getPath());
                        if(f.exists() && !f.isDirectory() && f.canRead()) {
                            auth = new FileAuthenticator(f);
                        }
                    }
                }
            }
        }
        if(auth == null) {
            logger.error("No authenticator configuration found -- all authentication will fail!");
            auth = new NullAuthenticator();
        }
    }
}
