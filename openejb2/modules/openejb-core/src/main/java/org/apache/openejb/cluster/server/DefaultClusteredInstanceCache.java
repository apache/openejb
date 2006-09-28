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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.cluster.server;

import java.io.Serializable;

import org.apache.openejb.cache.InstanceCache;

/**
 * 
 * @version $Revision$ $Date$
 */
public class DefaultClusteredInstanceCache implements ClusteredInstanceCache, Serializable {
    private final InstanceCache cache;
    private transient InstanceCache delegate;
    private transient EJBClusterManager clusterManager;

    public DefaultClusteredInstanceCache(InstanceCache cache) {
        this.cache = cache;
        delegate = new StoppedClusterCache();
    }

    public void setEJBClusterManager(EJBClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        if (null == clusterManager) {
            delegate = new StoppedClusterCache();
        } else {
            delegate = new RunningClusterCache();
        }
    }

    public Object get(Object key) throws Exception {
        return delegate.get(key);
    }

    public boolean isActive(Object key) {
        return delegate.isActive(key);
    }

    public Object peek(Object key) {
        return delegate.peek(key);
    }

    public void putActive(Object key, Object value) {
        delegate.putActive(key, value);
    }

    public void putInactive(Object key, Object value) {
        delegate.putInactive(key, value);
    }

    public Object remove(Object key) {
        return delegate.remove(key);
    }
    
    private class StoppedClusterCache implements InstanceCache {
        public Object get(Object key) throws Exception {
            throw new IllegalStateException("No ClusteredManager is set.");
        }

        public Object peek(Object key) {
            throw new IllegalStateException("No ClusteredManager is set.");
        }

        public void putActive(Object key, Object value) {
            throw new IllegalStateException("No ClusteredManager is set.");
        }

        public void putInactive(Object key, Object value) {
            throw new IllegalStateException("No ClusteredManager is set.");
       }

        public Object remove(Object key) {
            throw new IllegalStateException("No ClusteredManager is set.");
        }

        public boolean isActive(Object key) {
            throw new IllegalStateException("No ClusteredManager is set.");
        }
    }
    
    private class RunningClusterCache implements InstanceCache {
        public Object get(Object key) throws Exception {
            String theKey = ensureTypeKey(key);
            Object opaque = cache.get(theKey);
            if (null == opaque) {
                clusterManager.putInstanceInCache(cache, theKey);
                opaque = cache.get(key);
            }
            return opaque;
        }

        public Object peek(Object key) {
            String theKey = ensureTypeKey(key);
            Object opaque = cache.peek(theKey);
            if (null == opaque) {
                clusterManager.putInstanceInCache(cache, theKey);
                opaque = cache.peek(key);
            }
            return opaque;
        }

        public void putActive(Object key, Object value) {
            String theKey = ensureTypeKey(key);
            cache.putActive(theKey, value);
        }

        public void putInactive(Object key, Object value) {
            String theKey = ensureTypeKey(key);
            cache.putInactive(theKey, value);
        }

        public Object remove(Object key) {
            String theKey = ensureTypeKey(key);
            clusterManager.removeInstance(theKey);
            return cache.remove(theKey);
        }

        public boolean isActive(Object key) {
            String theKey = ensureTypeKey(key);
            return cache.isActive(theKey);
        }
        
        private String ensureTypeKey(Object key) {
            if (false == key instanceof String) {
                throw new IllegalArgumentException("key must be a String. " +
                        "Was :" + key.getClass().getName());
            }
            return (String) key;
        }
    }
}
