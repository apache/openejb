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
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.soap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;


/**
 * This class is a bit crufty.  Need something like this, but not static
 * and more along the lines of a collection of containers registered as gbeans
 */
public class WSContainerIndex implements ReferenceCollectionListener, GBeanLifecycle {
    // todo delete me
    private static WSContainerIndex containerIndex = new WSContainerIndex();

    public static WSContainerIndex getInstance() {
        return containerIndex;
    }

    /**
     * Index from jndi name to the index (Integer) number in the containers lookup table
     */
    private final HashMap urlToContainerMap = new HashMap();

    /**
     * GBean reference collection that we watch for new containers to register
     */
    private ReferenceCollection wsContainers;

    protected WSContainerIndex() {
    }

    public WSContainerIndex(Collection wsContainers) {
        WSContainerIndex.containerIndex = this;
        this.wsContainers = (ReferenceCollection) wsContainers;
        this.wsContainers.addReferenceCollectionListener(this);
    }

    public void doStart() throws Exception {
        for (Iterator iterator = wsContainers.iterator(); iterator.hasNext();) {
            WSContainer container = (WSContainer) iterator.next();
            urlToContainerMap.put(container.getLocation().getPath(), container);
        }
    }

    public void doStop() throws Exception {
        urlToContainerMap.clear();
    }

    public void doFail() {
        urlToContainerMap.clear();
    }

    public synchronized void addContainer(WSContainer container) {
        urlToContainerMap.put(container.getLocation().getPath(), container);
    }

    public synchronized void removeContainer(WSContainer container) {
        urlToContainerMap.remove(container.getLocation().getPath());
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        addContainer((WSContainer) event.getMember());
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        removeContainer((WSContainer) event.getMember());
    }

    public synchronized WSContainer getContainer(String urlPath) {
        return (WSContainer) urlToContainerMap.get(urlPath);
    }
}



