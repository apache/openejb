package org.apache.openejb.server.cxf.fault;

/**
 * @author Romain Manni-Bucau
 */
public class SecurityContext {
    public String name;

    public SecurityContext(String n) {
        name = n;
    }

    public SecurityContext() {
        // no-op
    }
}
