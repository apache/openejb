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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

import org.openejb.EJBContainer;
import org.openejb.corba.security.ServerPolicyFactory;
import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.corba.security.config.tss.TSSNULLTransportConfig;
import org.openejb.corba.util.TieLoader;


/**
 * @version $Revision$ $Date$
 */
public class TSSBean implements GBeanLifecycle, ReferenceCollectionListener {

    private final Log log = LogFactory.getLog(TSSBean.class);

    private final ClassLoader classLoader;
    private final String POAName;
    private final CORBABean server;
    private final TieLoader tieLoader;
    private POA localPOA;
    private NamingContextExt initialContext;
    private TSSConfig tssConfig;
    private Collection containers = Collections.EMPTY_SET;
    private Map adapters = new HashMap();
    private static final Map containerMap = new HashMap();
    private Policy securityPolicy;


    public TSSBean(ClassLoader classLoader, String POAName, CORBABean server, TieLoader tieLoader) {
        this.classLoader = classLoader;
        this.POAName = POAName;
        this.server = server;
        this.tieLoader = tieLoader;
    }

    public CORBABean getServer() {
        return server;
    }

    public String getPOAName() {
        return POAName;
    }

    public TSSConfig getTssConfig() {
        return tssConfig;
    }

    public void setTssConfig(TSSConfig tssConfig) {
        if (tssConfig == null) tssConfig = new TSSConfig();
        this.tssConfig = tssConfig;
    }

    public Collection getContainers() {
        return containers;
    }

    public void setContainers(Collection containers) {
        ReferenceCollection ref = (ReferenceCollection) containers;
        ref.addReferenceCollectionListener(this);

        this.containers = containers;
        for (Iterator iterator = ref.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            ReferenceCollectionEvent event = new ReferenceCollectionEvent(null, o);
            memberAdded(event);
        }
    }

    public TieLoader getTieLoader() {
        return tieLoader;
    }

    public static EJBContainer getContainer(String containerId) {
        return (EJBContainer) containerMap.get(containerId);
    }

    /**
     * TODO: Security policy really shouldn't be inserted if there is not CSI
     * config to put into it.
     * @throws Exception
     */
    public void doStart() throws Exception {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            ORB orb = server.getORB();
            POA rootPOA = server.getRootPOA();

            Any any = orb.create_any();
            any.insert_Value(createCSIv2Config());

            securityPolicy = orb.create_policy(ServerPolicyFactory.POLICY_TYPE, any);
            Policy[] policies = new Policy[]{
                securityPolicy,
                rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                rootPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            localPOA = rootPOA.create_POA(POAName, rootPOA.the_POAManager(), policies);

            localPOA.the_POAManager().activate();

            org.omg.CORBA.Object obj = server.getORB().resolve_initial_references("NameService");
            initialContext = NamingContextExtHelper.narrow(obj);

            for (Iterator iter = adapters.keySet().iterator(); iter.hasNext();) {
                AdapterWrapper adapterWrapper = (AdapterWrapper) adapters.get(iter.next());
                try {
                    adapterWrapper.start(server.getORB(), localPOA, initialContext, tieLoader, securityPolicy);
                    log.info("Linked container " + adapterWrapper.getContainer().getContainerID());
                } catch (CORBAException e) {
                    log.error("Unable to link container " + adapterWrapper.getContainer().getContainerID(), e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.info("Started CORBA Target Security Service in POA " + POAName);
    }

    public void doStop() throws Exception {
        if (localPOA != null) {
            for (Iterator iter = adapters.keySet().iterator(); iter.hasNext();) {
                AdapterWrapper adapterWrapper = (AdapterWrapper) adapters.get(iter.next());
                try {
                    adapterWrapper.stop();
                    log.info("Unlinked container " + adapterWrapper.getContainer().getContainerID());
                } catch (CORBAException e) {
                    log.error("Error unlinking container " + adapterWrapper.getContainer().getContainerID(), e);
                }
            }
            adapters.clear();
            localPOA.the_POAManager().deactivate(true, true);
            localPOA = null;
        }
        log.info("Stopped CORBA Target Security Service in POA " + POAName);
    }

    public void doFail() {
        log.info("Failed CORBA Target Security Service in POA " + POAName);
    }

    private TSSConfig createCSIv2Config() {
        if (tssConfig == null) return null;
        if (tssConfig.isInherit()) return server.getTssConfig();

        TSSConfig config = new TSSConfig();

        if (server.getTssConfig() != null)
            config.setTransport_mech(server.getTssConfig().getTransport_mech());
        else
            config.setTransport_mech(new TSSNULLTransportConfig());

        config.getMechListConfig().setStateful(tssConfig.getMechListConfig().isStateful());
        for (int i = 0; i < tssConfig.getMechListConfig().size(); i++) {
            config.getMechListConfig().add(tssConfig.getMechListConfig().mechAt(i));
        }

        return config;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(TSSBean.class, "CORBATSS");

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("POAName", String.class, true);
        infoFactory.addReference("Server", CORBABean.class, NameFactory.CORBA_SERVICE);
        infoFactory.addAttribute("tssConfig", TSSConfig.class, true);
        infoFactory.addReference("Containers", EJBContainer.class);//many types
        infoFactory.addReference("TieLoader", TieLoader.class, NameFactory.CORBA_SERVICE);
        infoFactory.setConstructor(new String[]{"classLoader", "POAName", "Server", "TieLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        EJBContainer container = (EJBContainer) event.getMember();
        containerMap.put(container.getContainerID(), container);

        if (localPOA != null) {
            try {
                AdapterWrapper adapterWrapper = new AdapterWrapper(container);

                adapterWrapper.start(server.getORB(), localPOA, initialContext, tieLoader, securityPolicy);
                adapters.put(container.getContainerID(), adapterWrapper);

                log.info("Linked container " + container.getContainerID());
            } catch (CORBAException e) {
                log.error("Unable to link container " + container.getContainerID(), e);
            }
        }
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        EJBContainer container = (EJBContainer) event.getMember();

        containerMap.remove(container.getContainerID());

        AdapterWrapper adapterWrapper = (AdapterWrapper) adapters.remove(container.getContainerID());
        if (adapterWrapper != null) {
            try {
                adapterWrapper.stop();
                log.info("Unlinked container " + container.getContainerID());
            } catch (CORBAException e) {
                log.error("Error unlinking container " + container.getContainerID());
                log.error(e);
            }
        }
    }
}
