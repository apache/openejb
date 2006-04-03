/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.openejb.corba.CORBAHandleDelegate;
import org.openejb.corba.proxy.CORBAProxyReference;
import org.openejb.proxy.EJBProxyReference;
import org.openejb.proxy.ProxyInfo;

import javax.naming.Reference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @version $Revision$ $Date$
 */
public class OpenEJBReferenceBuilder implements EJBReferenceBuilder {
    private final static Map STATELESS = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATELESS_SESSION_BEAN);
    private final static Map STATEFUL = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATEFUL_SESSION_BEAN);
    private final static Map ENTITY = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.ENTITY_BEAN);

    private void checkLocalProxyInfo(AbstractNameQuery query, String localHome, String local, Configuration configuration) throws DeploymentException {
        GBeanData gbeanData;
        try {
            gbeanData = configuration.findGBeanData(query);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
        ProxyInfo proxyInfo = (ProxyInfo) gbeanData.getAttribute("proxyInfo");
        if (proxyInfo == null) {
            throw new IllegalStateException("BUG! no proxy info found in gbeanData: " + gbeanData);
        }
        if (!proxyInfo.getLocalHomeInterface().getName().equals(localHome)
                || !proxyInfo.getLocalInterface().getName().equals(local)) {
            throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                    "reference localHome: " + localHome + "\n" +
                    "ejb localHome: " + proxyInfo.getLocalHomeInterface().getName() + "\n" +
                    "reference local: " + local + "\n" +
                    "ejb local: " + proxyInfo.getLocalInterface().getName());
        }
    }

    private void checkRemoteProxyInfo(AbstractNameQuery query, String home, String remote, Configuration configuration) throws DeploymentException {
        GBeanData gbeanData;
        try {
            gbeanData = configuration.findGBeanData(query);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
        ProxyInfo proxyInfo = (ProxyInfo) gbeanData.getAttribute("proxyInfo");
        if (proxyInfo == null) {
            throw new IllegalStateException("BUG! no proxy info found in gbeanData: " + gbeanData);
        }
        if (!proxyInfo.getHomeInterface().getName().equals(home)
                || !proxyInfo.getRemoteInterface().getName().equals(remote)) {
            throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                    "reference home: " + home + "\n" +
                    "ejb home: " + proxyInfo.getHomeInterface().getName() + "\n" +
                    "reference remote: " + remote + "\n" +
                    "ejb remote: " + proxyInfo.getRemoteInterface().getName());
        }
    }

    public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
        try {
            configuration.findGBean(containerNameQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not find css bean matching " + containerNameQuery + " from configuration " + configuration.getId());
        }
        return new CORBAProxyReference(configuration.getId(), containerNameQuery, nsCorbaloc, objectName, home);
    }

    public Object createHandleDelegateReference() {
        return new CORBAHandleDelegate.HandleDelegateReference();
    }

    public Reference createEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) throws DeploymentException {
        AbstractNameQuery match;
        if (query != null) {
            checkRemoteProxyInfo(query, home, remote, configuration);
            match = query;
        } else if (name != null) {
            match = getMatch(refName, configuration, name, requiredModule, true, isSession, home, remote);
        } else {
            match = getImplicitMatch(refName, configuration, optionalModule, true, isSession, home, remote);
        }
        return buildRemoteReference(configuration.getId(), match, isSession, home, remote);
    }

    public Reference createEJBLocalRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local) throws DeploymentException {
        AbstractNameQuery match;
        if (query != null) {
            checkLocalProxyInfo(query, localHome, local, configuration);
            match = query;
        } else if (name != null) {
            match = getMatch(refName, configuration, name, requiredModule, false, isSession, localHome, local);
        } else {
            match = getImplicitMatch(refName, configuration, optionalModule, false, isSession, localHome, local);
        }
        return buildLocalReference(configuration.getId(), match, isSession, localHome, local);
    }

    protected Reference buildLocalReference(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean session, String localHome, String local) {
        return EJBProxyReference.createLocal(configurationId, abstractNameQuery, session, localHome, local);
    }

    protected Reference buildRemoteReference(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean session, String home, String remote) {
        return EJBProxyReference.createRemote(configurationId, abstractNameQuery, session, home, remote);
    }

    private AbstractNameQuery getMatch(String refName, Configuration context, String name, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
        Map nameQuery = new HashMap();
        nameQuery.put(NameFactory.J2EE_NAME, name);
        if (module != null) {
            nameQuery.put(NameFactory.EJB_MODULE, module);
        }
        Set gbeans = new HashSet();
        if (isSession) {
            Map q = new HashMap(nameQuery);
            q.putAll(STATELESS);
            gbeans.addAll(context.findGBeans(new AbstractNameQuery(context.getId(), q)));

            q = new HashMap(nameQuery);
            q.putAll(STATEFUL);
            gbeans.addAll(context.findGBeans(new AbstractNameQuery(context.getId(), q)));
        } else {
            nameQuery.putAll(ENTITY);
            gbeans.addAll(context.findGBeans(new AbstractNameQuery(context.getId(), nameQuery)));
        }

        Collection matches = new ArrayList();
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            GBeanData data;
            try {
                data = context.findGBeanData(new AbstractNameQuery(abstractName));
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("We just got this ejb name out of a query! It must be there!");
            }
            if (matchesProxyInfo(data, isRemote, home, remote)) {
                matches.add(abstractName);
            }
        }
        if (matches.isEmpty()) {
            throw new UnresolvedEJBRefException(refName, false, isSession, home, remote, false);
        }
        AbstractName match;
        if (matches.size() == 1) {
            match = (AbstractName) matches.iterator().next();
        } else {
            throw new UnresolvedEJBRefException(refName, false, isSession, home, remote, matches.size() > 0);
        }
        return new AbstractNameQuery(match);
    }

    private AbstractNameQuery getImplicitMatch(String refName, Configuration context, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
        Set gbeans;
        if (isSession) {
            gbeans = context.findGBeans(new AbstractNameQuery(context.getId(), STATELESS));
            gbeans.addAll(context.findGBeans(new AbstractNameQuery(context.getId(), STATEFUL)));
        } else {
            gbeans = context.findGBeans(new AbstractNameQuery(context.getId(), ENTITY));
        }
        Collection matches = new ArrayList();
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            GBeanData data;
            try {
                data = context.findGBeanData(new AbstractNameQuery(abstractName));
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("We just got this ejb name out of a query! It must be there!");
            }
            if (matchesProxyInfo(data, isRemote, home, remote)) {
                matches.add(abstractName);
            }
        }
        if (matches.isEmpty()) {
            throw new UnresolvedEJBRefException(refName, false, isSession, home, remote, false);
        }
        AbstractName match;
        if (matches.size() == 1) {
            match = (AbstractName) matches.iterator().next();
        } else {
            for (Iterator iterator = matches.iterator(); iterator.hasNext();) {
                AbstractName objectName = (AbstractName) iterator.next();
                if (!(objectName.getName().get(NameFactory.EJB_MODULE).equals(module))) {
                    iterator.remove();
                }
            }
            if (matches.size() == 1) {
                match = (AbstractName) matches.iterator().next();
            } else {
                throw new UnresolvedEJBRefException(refName, false, isSession, home, remote, matches.size() > 0);
            }
        }
        return new AbstractNameQuery(match);
    }

    private boolean matchesProxyInfo(GBeanData data, boolean isRemote, String home, String remote) {
        ProxyInfo proxyInfo = (ProxyInfo) data.getAttribute("proxyInfo");
        if (isRemote) {
            return matches(proxyInfo.getHomeInterface(), home)
                    && matches(proxyInfo.getRemoteInterface(), remote);
        } else {
            return matches(proxyInfo.getLocalHomeInterface(), home)
                    && matches(proxyInfo.getLocalInterface(), remote);
        }
    }

    private boolean matches(Class clazz, String name) {
        return clazz != null && clazz.getName().equals(name);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(OpenEJBReferenceBuilder.class, NameFactory.MODULE_BUILDER); //TODO decide what type this should be
        infoFactory.addInterface(EJBReferenceBuilder.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
