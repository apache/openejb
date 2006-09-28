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
package org.apache.openejb.corba;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.corba.security.ServerPolicy;
import org.apache.openejb.corba.security.ServerPolicyFactory;
import org.apache.openejb.corba.security.config.tss.TSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSNULLTransportConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class TSSBean implements GBeanLifecycle {

    private final Log log = LogFactory.getLog(TSSBean.class);

    private final ClassLoader classLoader;
    private final String POAName;
    private final CORBABean server;
    private POA localPOA;
    private NamingContextExt initialContext;
    private TSSConfig tssConfig;
    private final Map adapters = new HashMap();
    private Policy securityPolicy;

    /**
     * gbean endpoint constructor
     */
    public TSSBean() {
        classLoader = null;
        POAName = null;
        server = null;
    }

    public TSSBean(ClassLoader classLoader, String POAName, CORBABean server) {
        this.classLoader = classLoader;
        this.POAName = POAName;
        this.server = server;
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

    /**
     * TODO: Security policy really shouldn't be inserted if there is not CSI
     * config to put into it.
     *
     * @throws Exception
     */
    public void doStart() throws Exception {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            ORB orb = server.getORB();
            POA rootPOA = server.getRootPOA();

            Any any = orb.create_any();
            any.insert_Value(new ServerPolicy.Config(createCSIv2Config(), classLoader));

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
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.debug("Started CORBA Target Security Service in POA " + POAName);
    }

    public void doStop() throws Exception {
        if (localPOA != null) {
            try {
                localPOA.the_POAManager().deactivate(true, false);
            } catch (AdapterInactive adapterInactive) {
                // do nothing - this may have already been deactivated.
            }
            localPOA = null;
        }
        log.debug("Stopped CORBA Target Security Service in POA " + POAName);
    }

    public void doFail() {
        log.warn("Failed CORBA Target Security Service in POA " + POAName);
    }

    private TSSConfig createCSIv2Config() {
        if (tssConfig == null) return null;
        if (tssConfig.isInherit()) return server.getTssConfig();

        TSSConfig config = new TSSConfig();

        if (server.getTssConfig() != null) {
            config.setTransport_mech(server.getTssConfig().getTransport_mech());
        } else {
            config.setTransport_mech(new TSSNULLTransportConfig());
        }

        config.getMechListConfig().setStateful(tssConfig.getMechListConfig().isStateful());
        for (int i = 0; i < tssConfig.getMechListConfig().size(); i++) {
            config.getMechListConfig().add(tssConfig.getMechListConfig().mechAt(i));
        }

        return config;
    }

    public void registerContainer(RpcEjbDeployment container) throws CORBAException {
        AdapterWrapper adapterWrapper = new AdapterWrapper(container);

        adapterWrapper.start(server.getORB(), localPOA, initialContext, securityPolicy);
        adapters.put(container.getContainerId(), adapterWrapper);

        log.debug(POAName + " - Linked container " + container.getContainerId());
    }

    public void unregisterContainer(RpcEjbDeployment container) {
        AdapterWrapper adapterWrapper = (AdapterWrapper) adapters.remove(container.getContainerId());
        if (adapterWrapper != null) {
            try {
                adapterWrapper.stop();
                log.debug(POAName + " - Unlinked container " + container.getContainerId());
            } catch (CORBAException e) {
                log.error(POAName + " - Error unlinking container " + container.getContainerId(), e);
            }
        }
    }

}
