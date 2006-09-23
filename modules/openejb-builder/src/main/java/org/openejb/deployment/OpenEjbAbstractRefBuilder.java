/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.xmlbeans.QNameSet;
import org.openejb.RpcEjbDeployment;

/**
 * @version $Rev$ $Date$
 */
public abstract class OpenEjbAbstractRefBuilder extends AbstractNamingBuilder {
    private final static Map STATELESS = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATELESS_SESSION_BEAN);
    private final static Map STATEFUL = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATEFUL_SESSION_BEAN);
    private final static Map ENTITY = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.ENTITY_BEAN);

    protected OpenEjbAbstractRefBuilder() {
    }

    protected OpenEjbAbstractRefBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected AbstractNameQuery getMatch(String refName, Configuration context, String name, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
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
        return new AbstractNameQuery(stripVersion(match.getArtifact()), match.getName(), RpcEjbDeployment.class.getName());
    }

    private Artifact stripVersion(Artifact artifact) {
        return new Artifact(artifact.getGroupId(), artifact.getArtifactId(), (String)null, artifact.getType());
    }

    private Collection getMatchesFromName(boolean isSession, Map nameQuery, Configuration context, Artifact id, boolean isRemote, String home, String remote) {
        Set gbeanDatas = new HashSet();
        if (isSession) {
            Map q = new HashMap(nameQuery);
            q.putAll(STATELESS);
            gbeanDatas.addAll(context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, RpcEjbDeployment.class.getName()))));

            q = new HashMap(nameQuery);
            q.putAll(STATEFUL);
            gbeanDatas.addAll(context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, RpcEjbDeployment.class.getName()))));
        } else {
            Map q = new HashMap(nameQuery);
            q.putAll(ENTITY);
            gbeanDatas.addAll(context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, RpcEjbDeployment.class.getName()))));
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

    protected AbstractNameQuery getImplicitMatch(String refName, Configuration context, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
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

    private boolean matchesProxyInfo(GBeanData data, boolean isRemote, String home, String remote) {
        if (isRemote) {
            return home.equals(getHomeInterface(data))
                    && remote.equals(getRemoteInterface(data));
        } else {
            return home.equals(getLocalHomeInterface(data))
                    && remote.equals(getLocalInterface(data));
        }
    }

    protected static String getHomeInterface(GBeanData data) {
        return (String) data.getAttribute("homeInterfaceName");
    }

    protected static String getRemoteInterface(GBeanData data) {
        return (String) data.getAttribute("remoteInterfaceName");
    }

    protected static String getLocalHomeInterface(GBeanData data) {
        return (String) data.getAttribute("localHomeInterfaceName");
    }

    protected static String getLocalInterface(GBeanData data) {
        return (String) data.getAttribute("localInterfaceName");
    }

}
