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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Reference;

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
import org.openejb.EJBContainer;
import org.openejb.corba.proxy.CORBAProxyReference;
import org.openejb.proxy.EJBProxyReference;

/**
 * @version $Revision$ $Date$
 */
public class OpenEJBReferenceBuilder implements EJBReferenceBuilder {
    private final static Map STATELESS = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATELESS_SESSION_BEAN);
    private final static Map STATEFUL = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATEFUL_SESSION_BEAN);
    private final static Map ENTITY = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.ENTITY_BEAN);
    private static final Class[] NO_CLASSES = new Class[] {};
    private static final Object[] NO_OBJECTS = new Object[] {};

    private void checkLocalProxyInfo(AbstractNameQuery query, String localHome, String local, Configuration configuration) throws DeploymentException {
        GBeanData gbeanData;
        try {
            gbeanData = configuration.findGBeanData(query);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
        Object proxyInfo = gbeanData.getAttribute("proxyInfo");
        if (proxyInfo == null) {
            throw new IllegalStateException("BUG! no proxy info found in gbeanData: " + gbeanData);
        }
        if (!getLocalHomeInterface(proxyInfo).getName().equals(localHome)
                || !getLocalInterface(proxyInfo).getName().equals(local)) {
            throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                    "reference localHome: " + localHome + "\n" +
                    "ejb localHome: " + getLocalHomeInterface(proxyInfo).getName() + "\n" +
                    "reference local: " + local + "\n" +
                    "ejb local: " + getLocalInterface(proxyInfo).getName());
        }
    }

    private Class getLocalInterface(Object proxyInfo) throws DeploymentException {
        Method m;
        try {
            m = proxyInfo.getClass().getMethod("getLocalInterface", NO_CLASSES);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("BUG: proxyInfo does not implement getLocalInterface");
        }
        try {
            return (Class) m.invoke(proxyInfo, NO_OBJECTS);
        } catch (IllegalAccessException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        } catch (InvocationTargetException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        }
    }

    private Class getLocalHomeInterface(Object proxyInfo) throws DeploymentException {
        Method m;
        try {
            m = proxyInfo.getClass().getMethod("getLocalHomeInterface", NO_CLASSES);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("BUG: proxyInfo does not implement getLocalHomeInterface");
        }
        try {
            return (Class) m.invoke(proxyInfo, NO_OBJECTS);
        } catch (IllegalAccessException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        } catch (InvocationTargetException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        }
    }

    private void checkRemoteProxyInfo(AbstractNameQuery query, String home, String remote, Configuration configuration) throws DeploymentException {
        if (remote.equals("javax.management.j2ee.Management") && home.equals("javax.management.j2ee.ManagementHome")) {
            // Don't verify the MEJB because it doesn't have a proxyInfo attribute
            return;
        }
        GBeanData gbeanData;
        try {
            gbeanData = configuration.findGBeanData(query);
        } catch (GBeanNotFoundException e) {
            return;
            //we can't check anything, hope for the best.
//            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
        Object proxyInfo = gbeanData.getAttribute("proxyInfo");
        if (proxyInfo == null) {
            throw new IllegalStateException("BUG! no proxy info found in gbeanData: " + gbeanData);
        }
        if (!getHomeInterface(proxyInfo).getName().equals(home)
                || !getRemoteInterface(proxyInfo).getName().equals(remote)) {
            throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                    "reference home: " + home + "\n" +
                    "ejb home: " + getHomeInterface(proxyInfo).getName() + "\n" +
                    "reference remote: " + remote + "\n" +
                    "ejb remote: " + getRemoteInterface(proxyInfo).getName());
        }
    }

    private Class getRemoteInterface(Object proxyInfo) throws DeploymentException {
        Method m;
        try {
            m = proxyInfo.getClass().getMethod("getRemoteInterface", NO_CLASSES);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("BUG: proxyInfo does not implement getRemoteInterface");
        }
        try {
            return (Class) m.invoke(proxyInfo, NO_OBJECTS);
        } catch (IllegalAccessException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        } catch (InvocationTargetException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        }
    }

    private Class getHomeInterface(Object proxyInfo) throws DeploymentException {
        Method m;
        try {
            m = proxyInfo.getClass().getMethod("getHomeInterface", NO_CLASSES);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("BUG: proxyInfo does not implement getHomeInterface");
        }
        try {
            return (Class) m.invoke(proxyInfo, NO_OBJECTS);
        } catch (IllegalAccessException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
        } catch (InvocationTargetException e) {
            throw new DeploymentException("Could not access proxyInfo", e);
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

    public Reference createEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) throws DeploymentException {
        AbstractNameQuery match;
        if (query != null) {
            checkRemoteProxyInfo(query, home, remote, configuration);
            match = new AbstractNameQuery(query.getArtifact(), query.getName(), EJBContainer.class.getName());
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
            match = new AbstractNameQuery(query.getArtifact(), query.getName(), EJBContainer.class.getName());
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
        Artifact id = context.getId();
        Collection matches = getMatchesFromName(isSession, nameQuery, context, id, isRemote, home, remote);
        if (matches.isEmpty()) {
            matches = getMatchesFromName(isSession, nameQuery, context, null, isRemote, home, remote);
        }
        if (matches.isEmpty()) {
            throw new UnresolvedEJBRefException(refName, !isRemote, isSession, home, remote, false);
        }
        AbstractName match;
        if (matches.size() == 1) {
            match = (AbstractName) matches.iterator().next();
        } else {
            throw new UnresolvedEJBRefException(refName, !isRemote, isSession, home, remote, matches.size() > 0);
        }
        return new AbstractNameQuery(stripVersion(match.getArtifact()), match.getName(), EJBContainer.class.getName());
    }

    private Artifact stripVersion(Artifact artifact) {
        return new Artifact(artifact.getGroupId(), artifact.getArtifactId(), (String)null, artifact.getType());
    }

    private Collection getMatchesFromName(boolean isSession, Map nameQuery, Configuration context, Artifact id, boolean isRemote, String home, String remote) throws DeploymentException {
        Set gbeanDatas = new HashSet();
        if (isSession) {
            Map q = new HashMap(nameQuery);
            q.putAll(STATELESS);
            gbeanDatas.addAll(context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, EJBContainer.class.getName()))));

            q = new HashMap(nameQuery);
            q.putAll(STATEFUL);
            gbeanDatas.addAll(context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, EJBContainer.class.getName()))));
        } else {
            Map q = new HashMap(nameQuery);
            q.putAll(ENTITY);
            gbeanDatas.addAll(context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, EJBContainer.class.getName()))));
        }

        Collection matches = new ArrayList();
        for (Iterator iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData data = (GBeanData) iterator.next();
            if (matchesProxyInfo(data, isRemote, home, remote)) {
                matches.add(data.getAbstractName());
            }
        }
        return matches;
    }

    private AbstractNameQuery getImplicitMatch(String refName, Configuration context, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
        Collection matches = getMatchesFromName(isSession, Collections.EMPTY_MAP, context, context.getId(), isRemote, home, remote);
        if (matches.isEmpty()) {
            matches = getMatchesFromName(isSession, Collections.EMPTY_MAP, context, null, isRemote, home, remote);
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
                if (module != null && !(objectName.getName().get(NameFactory.EJB_MODULE).equals(module))) {
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

    private boolean matchesProxyInfo(GBeanData data, boolean isRemote, String home, String remote) throws DeploymentException {
        Object proxyInfo = data.getAttribute("proxyInfo");
        if (isRemote) {
            return matches(getHomeInterface(proxyInfo), home)
                    && matches(getRemoteInterface(proxyInfo), remote);
        } else {
            return matches(getLocalHomeInterface(proxyInfo), home)
                    && matches(getLocalInterface(proxyInfo), remote);
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
