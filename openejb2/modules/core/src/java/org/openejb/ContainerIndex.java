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

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.kernel.Kernel;


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

    private static final Log log = LogFactory.getLog(ContainerIndex.class);

    /**
     * The kernel in which this index is registered.  This is only needed due to the inability to control the order of
     * notifications in the kernel.
     */
    private final Kernel kernel;

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
        kernel = null;
    }

    public ContainerIndex(Collection ejbContainers, Kernel kernel) {
        this.kernel = kernel;
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

    public synchronized Integer addContainer(EJBContainer container) {
        container = container.getUnmanagedReference();
        Object containerID = container.getContainerID();
        Integer index;
        if ((index = (Integer) containerIdToIndex.get(containerID)) != null) {
            return index;
        }

        int i = containers.length;

        EJBContainer[] newArray = new EJBContainer[i + 1];
        System.arraycopy(containers, 0, newArray, 0, i);
        containers = newArray;

        containers[i] = container;
        index = new Integer(i);
        containerIdToIndex.put(containerID, index);
        addJNDINames(container, i);
        return index;
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

        // try to fault in the container using the kernel directly
        if ((index == null)) {
            URI uri = URI.create(containerID);
            AbstractNameQuery abstractNameQuery = new AbstractNameQuery(uri);
            Set results = kernel.listGBeans(abstractNameQuery);
            if (results.size() != 1) {
                log.error( "Name query " + abstractNameQuery + " not satisfied in kernel, matches: " + results);
                return -1;
            }
            AbstractName name = (AbstractName) results.iterator().next();
            EJBContainer ejbContainer;
            try {
                ejbContainer = (EJBContainer) kernel.getGBean(name);
            } catch (Exception e) {
                // couldn't find the container
                log.debug("Container not found: " + containerID, e);
                return -1;
            }
            index = addContainer(ejbContainer);
        }

        if (index == null) {
            return -1;
        } else {
            return index.intValue();
        }
    }

    public synchronized int getContainerIndexByJndiName(String jndiName) {
        Integer index = (Integer) jndiNameToIndex.get(jndiName);
        return (index == null) ? -1 : index.intValue();
    }

    public synchronized EJBContainer getContainer(String containerID) throws ContainerNotFoundException {
        int containerIndex = getContainerIndex(containerID);
        if (containerIndex < 0) {
            throw new ContainerNotFoundException(containerID);
        }
        return getContainer(containerIndex);
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
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ContainerIndex.class); //name apparently hardcoded

        infoFactory.addReference("EJBContainers", EJBContainer.class);//many types, specify type in patterns
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.setConstructor(new String[]{"EJBContainers", "kernel"});

        infoFactory.addOperation("getContainerIndex", new Class[]{Object.class});
        infoFactory.addOperation("getContainerIndex", new Class[]{String.class});
        infoFactory.addOperation("getContainerIndexByJndiName", new Class[]{String.class});
        infoFactory.addOperation("getContainer", new Class[]{String.class});
        infoFactory.addOperation("getContainer", new Class[]{Integer.class});
        infoFactory.addOperation("getContainer", new Class[]{Integer.TYPE});
        infoFactory.addOperation("getContainerByJndiName", new Class[]{String.class});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}



