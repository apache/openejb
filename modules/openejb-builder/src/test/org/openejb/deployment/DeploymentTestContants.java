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

import java.net.URI;
import java.util.Map;
import java.util.List;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.xbeans.j2ee.ServiceRefHandlerType;

/**
 * @version $Revision$ $Date$
 */
public interface DeploymentTestContants {
    public static final ObjectName CONFIGURATION_OBJECT_NAME = IGNORE.createConfigurationObjectName();
    public static final ObjectName CONNECTION_OBJECT_NAME = IGNORE.createConnectionObjectName();
    public static final String DOMAIN_NAME = DeploymentHelper.j2eeDomainName;
    public static final String SERVER_NAME = DeploymentHelper.j2eeServerName;

    public static final ResourceReferenceBuilder resourceReferenceBuilder = new ResourceReferenceBuilder() {
        public Reference createResourceRef(String containerId, Class iface) {
            return null;
        }

        public Reference createAdminObjectRef(String containerId, Class iface) {
            return null;
        }

        public ObjectName locateResourceName(ObjectName query) {
            return DeploymentHelper.RESOURCE_ADAPTER_NAME;
        }

        public GBeanData locateActivationSpecInfo(ObjectName resourceAdapterModuleName, String messageListenerInterface) {
            return DeploymentHelper.ACTIVATION_SPEC_INFO;
        }

        public GBeanData locateResourceAdapterGBeanData(ObjectName resourceAdapterModuleName) {
            return null;
        }

        public GBeanData locateAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName) {
            return null;
        }

        public GBeanData locateConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName) {
            return null;
        }
    };

    public static final ServiceReferenceBuilder serviceReferenceBuilder = new ServiceReferenceBuilder() {
        //it could return a Service or a Reference, we don't care
        public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) {
            return null;
        }
    };

    public static class IGNORE {
        private static ObjectName createConfigurationObjectName()  {
            try {
                return Configuration.getConfigurationObjectName(URI.create("test-ejb-jar"));
            } catch (MalformedObjectNameException e) {
                return null;
            }
        }

        private static ObjectName createConnectionObjectName() {
            try {
                J2eeContext j2eeContext = new J2eeContextImpl(DOMAIN_NAME, SERVER_NAME, NameFactory.NULL, NameFactory.RESOURCE_ADAPTER_MODULE, "testejbmodule", "testapp", NameFactory.J2EE_APPLICATION);
                return NameFactory.getComponentName(null, null, NameFactory.NULL, NameFactory.JCA_RESOURCE, "org/apache/geronimo/DefaultDatabase", "DefaultDatasource", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            } catch (MalformedObjectNameException e) {
                return null;
            }
        }

    }
}
