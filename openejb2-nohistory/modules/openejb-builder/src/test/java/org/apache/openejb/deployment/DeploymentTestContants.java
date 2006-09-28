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
package org.apache.openejb.deployment;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Revision$ $Date$
 */
public interface DeploymentTestContants {
    public static final AbstractName CONNECTION_OBJECT_NAME = IGNORE.createConnectionObjectName();

    public static final ActivationSpecInfoLocator ACTIVATION_SPEC_INFO_LOCATOR = new ActivationSpecInfoLocator() {

        public GBeanData locateActivationSpecInfo(AbstractNameQuery resourceAdapterModuleName, String messageListenerInterface, Configuration configuration) {
            return DeploymentHelper.ACTIVATION_SPEC_INFO;
        }
    };

    public static class IGNORE {
        private static AbstractName createConnectionObjectName() {
            Naming naming = new Jsr77Naming();
            Artifact artifact = new Artifact("geronimo", "defaultDatabase", "1.1", "car");
            AbstractName appName = naming.createRootName(artifact, NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            AbstractName moduleName = naming.createChildName(appName, artifact.toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
            AbstractName resourceAdapterName = naming.createChildName(moduleName, artifact.toString(), NameFactory.RESOURCE_ADAPTER);
            AbstractName jcaResourceName = naming.createChildName(resourceAdapterName, artifact.toString(), NameFactory.JCA_RESOURCE);
            AbstractName jcaConnectionFactoryName = naming.createChildName(jcaResourceName, "DefaultDatasource", NameFactory.JCA_CONNECTION_FACTORY);
            return naming.createChildName(jcaConnectionFactoryName, "DefaultDatasource", NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        }
    }
}
