package org.openejb.alt.containers.castor_cmp11;

import org.openejb.util.Stack;

/**
 * Holds Castor-specific deployment information.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class CastorDeploymentInfo {
    private KeyGenerator keyGenerator;
    private Stack methodReadyPool;

    /**
     * The KeyGenerator objects provide quick extraction of primary keys from
     * entity bean classes and conversion between a primary key and a Castor
     * Complex identity.
     */
    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    /**
     * The KeyGenerator objects provide quick extraction of primary keys from
     * entity bean classes and conversion between a primary key and a Castor
     * Complex identity.
     */
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public Stack getMethodReadyPool() {
        return methodReadyPool;
    }

    public void setMethodReadyPool(Stack methodReadyPool) {
        this.methodReadyPool = methodReadyPool;
    }
}
