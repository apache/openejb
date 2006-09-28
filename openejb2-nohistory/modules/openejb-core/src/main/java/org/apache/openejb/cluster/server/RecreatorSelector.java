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
 * $Id: RecreatorSelector.java 445853 2005-12-21 14:21:56Z gdamour $
 */
package org.apache.openejb.cluster.server;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.wadi.PoolableInvocationWrapper;
import org.codehaus.wadi.PoolableInvocationWrapperPool;

/**
 * 
 * @version $Revision$ $Date$
 */
class RecreatorSelector implements PoolableInvocationWrapperPool {
    private final Map containerIdToRecreator;
    
    public RecreatorSelector() {
        containerIdToRecreator = new HashMap();
    }

    void addMapping(Object containerID, EJBInstanceContextRecreator recreator) {
        synchronized (containerIdToRecreator) {
            containerIdToRecreator.put(containerID, recreator);
        }
    }
    
    void removeMapping(Object containerID) {
        synchronized (containerIdToRecreator) {
            containerIdToRecreator.remove(containerID);
        }
    }
    
    public PoolableInvocationWrapper take() {
        return new EJBInvocationWrapper(this);
    }

    public void put(PoolableInvocationWrapper wrapper) {
        if (false == wrapper instanceof EJBInvocationWrapper) {
            throw new IllegalArgumentException(EJBInvocationWrapper.class +
                    " is expected.");
        }
        wrapper.destroy();
    }
   
    public EJBInstanceContextRecreator select(Object containerId) {
        EJBInstanceContextRecreator recreator;
        synchronized (containerIdToRecreator) {
            recreator = (EJBInstanceContextRecreator) containerIdToRecreator.get(containerId);
        }
        if (null == recreator) {
            throw new IllegalArgumentException("Container id " + containerId +
                    " is unknown.");
        }
        return recreator;
    }
}