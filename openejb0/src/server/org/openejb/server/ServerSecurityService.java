package org.openejb.server;

import java.security.Principal;
import java.util.Properties;
import java.util.HashSet;
import javax.security.auth.Subject;
import org.openejb.spi.SecurityService;

/**
 * 
 *
 * @version $Revision$
 */
public class ServerSecurityService implements SecurityService {
    public boolean isCallerAuthorized(String[] roleNames) {
        return true;
    }

    public Subject getCallerSubject() throws UnsupportedOperationException {
        Object id = CallContext.getCallContext().getEJBRequest().getClientIdentity();
        if(id == null) {
            return null;
        } else if(id instanceof String) {
            return new Subject(true, new HashSet(), new HashSet(), new HashSet());
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Principal getCalledPrincipal() {
        Object id = CallContext.getCallContext().getEJBRequest().getClientIdentity();
        if(id == null) {
            return null;
        } else if(id instanceof String) {
            final String name = (String)id;
            return new Principal() {
                public String getName() {
                    return name;
                }
            };
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void init(Properties props) throws Exception {
    }
}
