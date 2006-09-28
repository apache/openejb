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
 * $Id: DefaultEJBClusterManager.java 446003 2006-02-17 00:14:15Z dblevins $
 */
package org.apache.openejb.cluster.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.codehaus.wadi.Collapser;
import org.codehaus.wadi.Contextualiser;
import org.codehaus.wadi.InvocationException;
import org.codehaus.wadi.ManagerConfig;
import org.codehaus.wadi.Session;
import org.codehaus.wadi.SessionPool;
import org.codehaus.wadi.Streamer;
import org.codehaus.wadi.gridstate.Dispatcher;
import org.codehaus.wadi.gridstate.activecluster.ActiveClusterDispatcher;
import org.codehaus.wadi.gridstate.impl.DummyPartitionManager;
import org.codehaus.wadi.impl.AbsoluteEvicter;
import org.codehaus.wadi.impl.ClusterContextualiser;
import org.codehaus.wadi.impl.ClusteredManager;
import org.codehaus.wadi.impl.DistributableAttributesFactory;
import org.codehaus.wadi.impl.DistributableSessionFactory;
import org.codehaus.wadi.impl.DistributableValueFactory;
import org.codehaus.wadi.impl.DummyContextualiser;
import org.codehaus.wadi.impl.DummyRouter;
import org.codehaus.wadi.impl.HashingCollapser;
import org.codehaus.wadi.impl.MemoryContextualiser;
import org.codehaus.wadi.impl.SessionToContextPoolAdapter;
import org.codehaus.wadi.impl.SimpleSessionPool;
import org.codehaus.wadi.impl.SimpleStreamer;
import org.codehaus.wadi.impl.SimpleValuePool;
import org.codehaus.wadi.impl.StandardManager;
import org.codehaus.wadi.impl.StandardSessionWrapperFactory;
import org.apache.openejb.cache.InstanceCache;
import org.apache.openejb.client.ServerMetaData;

import javax.ejb.EnterpriseBean;
import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class DefaultEJBClusterManager implements GBeanLifecycle, EJBClusterManager {
    private static final Log log = LogFactory.getLog(DefaultEJBClusterManager.class);

    private final String nodeName;
    private final ServerMetaData server;
    private final RecreatorSelector recreatorSelector;
    private final ClusteredManager clusteredManager;
    private final SessionPool pool;
    private final Map contIdToServersMDHolder;
    private final DefaultEJBContainerMonitor monitor;
    private final Dispatcher dispatcher;
    private final DefaultEJBContainerAdvertiser advertiser;

    public DefaultEJBClusterManager(String clusterName,
                                    String clusterUri,
                                    String nodeName,
                                    String host,
                                    int port,
                                    int nbPartitions) throws Exception {
        this.nodeName = nodeName;

        server = new ServerMetaData(nodeName, host, port);

        contIdToServersMDHolder = new HashMap();

        Streamer streamer = new SimpleStreamer();
        Collapser collapser = new HashingCollapser(10, 2000);
        Map mmap = new HashMap();

        Contextualiser contextualiser = new ClusterContextualiser(
                new DummyContextualiser(),
                collapser,
                new EJBHybridRelocater(5000, 1000, true));

        pool = new SimpleSessionPool(new DistributableSessionFactory());
        recreatorSelector = new RecreatorSelector();
        contextualiser = new MemoryContextualiser(
                contextualiser,
                new AbsoluteEvicter(10000, false, 1000 * 60 * 30),
                mmap,
                streamer,
                new SessionToContextPoolAdapter(pool),
                recreatorSelector);
        // TODO add a contextualiser ensuring that the instance is not active.

        dispatcher =
                new ActiveClusterDispatcher(nodeName, clusterName, clusterUri, 5000L);

        monitor =
                new DefaultEJBContainerMonitor(dispatcher, new DefaultEJBContainerCallback());

        advertiser = new DefaultEJBContainerAdvertiser(server, dispatcher);

        clusteredManager = new ClusteredManager(
                pool,
                new DistributableAttributesFactory(),
                new SimpleValuePool(new DistributableValueFactory()),
                new StandardSessionWrapperFactory(),
                new EJBSessionIDFactory(nodeName),
                contextualiser,
                mmap,
                new DummyRouter(),
                true,
                streamer,
                true,
                null, // TODO replication is not yet supported.
                new EJBProxiedLocation(server),
                new EJBInvocationProxy(),
                dispatcher,
                new DummyPartitionManager(nbPartitions),
                null);
    }

    public void addEJBContainer(ClusteredEjbDeployment container) {
        Object containerID = container.getContainerId();
        ClusteredInstanceCache cache = container.getInstanceCache();
        ClusteredInstanceContextFactory factory = container.getInstanceContextFactory();

        EJBInstanceContextRecreator recreator = factory.getInstanceContextRecreator();
        recreatorSelector.addMapping(containerID, recreator);
        cache.setEJBClusterManager(this);
        factory.setClusterManager(this);

        ServerMetaDataArrayHolder holder;
        synchronized (contIdToServersMDHolder) {
            holder = (ServerMetaDataArrayHolder) contIdToServersMDHolder.get(containerID);
            if (null == holder) {
                holder = new ServerMetaDataArrayHolder(new ServerMetaData[]{server});
                contIdToServersMDHolder.put(containerID, holder);
            }
        }
        factory.setServersHolder(holder);

        advertiser.advertiseJoin(containerID);
    }

    public void removeEJBContainer(ClusteredEjbDeployment container) {
        Object containerID = container.getContainerId();
        ClusteredInstanceCache cache = container.getInstanceCache();
        ClusteredInstanceContextFactory factory = container.getInstanceContextFactory();

        recreatorSelector.removeMapping(containerID);
        cache.setEJBClusterManager(null);
        factory.setClusterManager(null);
        factory.setServersHolder(null);

        advertiser.advertiseLeave(containerID);
    }

    public void putInstanceInCache(InstanceCache cache, String beanId) {
        Contextualiser contextualiser = clusteredManager.getContextualiser();
        EJBInvocationContext context = new EJBInvocationContext(cache);
        try {
            contextualiser.contextualise(context, beanId, null, null, false);
        } catch (InvocationException e) {
            throw (IllegalStateException)
                    new IllegalStateException("Clustering failure.").initCause(e);
        }
    }

    public String addInstance(EnterpriseBean bean, Object containerId) {
        Session session = clusteredManager.create();
        EJBSessionUtil sessionUtil = new EJBSessionUtil(session);
        String id = session.getId();
        sessionUtil.setId(id);
        sessionUtil.setEnterpriseBean(bean);
        sessionUtil.setContainerId(containerId);
        return id;
    }

    public void removeInstance(String beanId) {
        clusteredManager.destroy(beanId);
    }

    public void doStart() throws Exception {
        clusteredManager.init(new OpenEJBManagerConfig());
        monitor.start();
        clusteredManager.start();
    }

    public void doStop() throws Exception {
        clusteredManager.stop();
        monitor.stop();
    }

    public void doFail() {
        try {
            clusteredManager.stop();
            monitor.stop();
        } catch (Exception e) {
            log.error(e);
        }
    }

    // TODO refactor WADI to get ride of these servlet contracts.
    private static class OpenEJBManagerConfig implements ManagerConfig {
        public ServletContext getServletContext() {
            return null;
        }

        public void callback(StandardManager manager) {
        }
    }

    private class DefaultEJBContainerCallback implements EJBContainerCallback {
        private final Map nodeNameToServersMDHolder;

        public DefaultEJBContainerCallback() {
            nodeNameToServersMDHolder = new HashMap();
        }

        public void fireEJBContainerJoin(ServerMetaData server, Object containerID) {
            if (server.getNodeName().equals(nodeName)) {
                return;
            }

            ServerMetaDataArrayHolder holder = getHolder(containerID);
            if (null == holder) {
                return;
            }
            synchronized (nodeNameToServersMDHolder) {
                nodeNameToServersMDHolder.put(nodeName, holder);
            }
            holder.add(server);
        }

        public void fireEJBContainerLeave(ServerMetaData server, Object containerID) {
            if (server.getNodeName().equals(nodeName)) {
                return;
            }

            ServerMetaDataArrayHolder holder = getHolder(containerID);
            if (null == holder) {
                return;
            }
            synchronized (nodeNameToServersMDHolder) {
                nodeNameToServersMDHolder.remove(nodeName);
            }
            holder.remove(server);
        }

        public void fireNodeLeave(String nodeName) {
            ServerMetaDataArrayHolder holder;
            synchronized (nodeNameToServersMDHolder) {
                holder = (ServerMetaDataArrayHolder) nodeNameToServersMDHolder.get(nodeName);
            }
            holder.removeNode(nodeName);
        }

        private ServerMetaDataArrayHolder getHolder(Object containerID) {
            ServerMetaDataArrayHolder holder;
            synchronized (contIdToServersMDHolder) {
                holder = (ServerMetaDataArrayHolder) contIdToServersMDHolder.get(containerID);
            }
            return holder;
        }
    }

}
