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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
package org.openejb;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;


/**
 * This class is a bit crufty.  Need something like this, but not static
 * and more along the lines of a collection of containers registered as gbeans
 */
public class ContainerIndex implements ReferenceCollectionListener, GBeanLifecycle {
    // todo delete me
    private static ContainerIndex containerIndex = new ContainerIndex();

    public static ContainerIndex getInstance() {
        return containerIndex;
    }

    /**
     * The container lookup table.
     */
    private EJBContainer[] containers = new EJBContainer[1];

    /**
     * Index from the container id to the index (Integer) number in the containers lookup table
     */
    private final HashMap containerIdToIndex = new HashMap();

    /**
     * Index from jndi name to the index (Integer) number in the containers lookup table
     */
    private final HashMap jndiNameToIndex = new HashMap();

    /**
     * GBean reference collection that we watch for new containers to register
     */
    private ReferenceCollection ejbContainers;

    protected ContainerIndex() {
    }

    public ContainerIndex(Collection ejbContainers) {
        ContainerIndex.containerIndex = this;
        this.ejbContainers = (ReferenceCollection) ejbContainers;
        this.ejbContainers.addReferenceCollectionListener(this);
    }

    public void doStart() throws Exception {
        containers = new EJBContainer[ejbContainers.size() + 1];
        Iterator iterator = ejbContainers.iterator();
        for (int i = 1; i < containers.length && iterator.hasNext(); i++) {
            EJBContainer container = (EJBContainer) iterator.next();
            container = container.getUnmanagedReference();
            containers[i] = container;
            containerIdToIndex.put(container.getContainerID(), new Integer(i));
            addJNDINames(container, i);
        }
    }

    private void addJNDINames(EJBContainer container, int i) {
        String[] jnidNames = container.getJndiNames();
        if (jnidNames != null) {
            for (int j = 0; j < jnidNames.length; j++) {
                String jnidName = jnidNames[j];
                jndiNameToIndex.put(jnidName, new Integer(i));
            }
        }
    }

    public void doStop() throws Exception {
        containerIdToIndex.clear();
        Arrays.fill(containers, null);
        jndiNameToIndex.clear();
    }

    public void doFail() {
        containerIdToIndex.clear();
        Arrays.fill(containers, null);
        jndiNameToIndex.clear();
    }

    public synchronized void addContainer(EJBContainer container) {
        container = container.getUnmanagedReference();
        Object containerID = container.getContainerID();
        if (containerIdToIndex.containsKey(containerID)) {
            return;
        }

        int i = containers.length;

        EJBContainer[] newArray = new EJBContainer[i + 1];
        System.arraycopy(containers, 0, newArray, 0, i);
        containers = newArray;

        containers[i] = container;
        containerIdToIndex.put(containerID, new Integer(i));
        addJNDINames(container, i);
    }

    public synchronized void removeContainer(EJBContainer container) {
        Integer index = (Integer) containerIdToIndex.remove(container.getContainerID());
        if (index != null) {
            containers[index.intValue()] = null;
        }

        String[] jnidNames = container.getJndiNames();
        for (int i = 0; i < jnidNames.length; i++) {
            String jnidName = jnidNames[i];
            jndiNameToIndex.remove(jnidName);
        }
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        addContainer((EJBContainer) event.getMember());
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        removeContainer((EJBContainer) event.getMember());
    }

    public synchronized int length() {
        return containers.length;
    }

    public synchronized int getContainerIndex(Object containerID) {
        return getContainerIndex((String) containerID);
    }

    public synchronized int getContainerIndex(String containerID) {
        Integer index = (Integer) containerIdToIndex.get(containerID);
        return (index == null) ? -1 : index.intValue();
    }

    public synchronized int getContainerIndexByJndiName(String jndiName) {
        Integer index = (Integer) jndiNameToIndex.get(jndiName);
        return (index == null) ? -1 : index.intValue();
    }

    public synchronized EJBContainer getContainer(String containerID) {
        //TODO return an informative exception if there is no such containerId.  Currently returns ArrayIndexOutOfBoundsException(-1)
        return getContainer(getContainerIndex(containerID));
    }

    public synchronized EJBContainer getContainer(Integer index) {
        return (index == null) ? null : getContainer(index.intValue());
    }

    public synchronized EJBContainer getContainerByJndiName(String jndiName) {
        return getContainer(getContainerIndexByJndiName(jndiName));
    }

    public synchronized EJBContainer getContainer(int index) {
        return containers[index];
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ContainerIndex.class); //name apparently hardcoded

        infoFactory.setConstructor(new String[]{"EJBContainers"});

        infoFactory.addOperation("getContainerIndex", new Class[]{Object.class});
        infoFactory.addOperation("getContainerIndex", new Class[]{String.class});
        infoFactory.addOperation("getContainerIndexByJndiName", new Class[]{String.class});
        infoFactory.addOperation("getContainer", new Class[]{String.class});
        infoFactory.addOperation("getContainer", new Class[]{Integer.class});
        infoFactory.addOperation("getContainer", new Class[]{Integer.TYPE});
        infoFactory.addOperation("getContainerByJndiName", new Class[]{String.class});

        infoFactory.addReference("EJBContainers", EJBContainer.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}



