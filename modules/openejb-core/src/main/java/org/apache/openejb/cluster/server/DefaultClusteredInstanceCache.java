/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
