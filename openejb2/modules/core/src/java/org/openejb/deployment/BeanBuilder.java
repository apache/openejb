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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.openejb.ResourceEnvironmentBuilder;


abstract class BeanBuilder {
    protected final OpenEJBModuleBuilder moduleBuilder;

    protected BeanBuilder(OpenEJBModuleBuilder moduleBuilder) {
        this.moduleBuilder = moduleBuilder;
    }

    public OpenEJBModuleBuilder getModuleBuilder() {
        return moduleBuilder;
    }

    protected void setResourceEnvironment(ResourceEnvironmentBuilder builder, ResourceRefType[] resourceRefArray, GerLocalRefType[] openejbResourceRefArray) {
	    Map openejbNames = new HashMap();
	    for (int i = 0; i < openejbResourceRefArray.length; i++) {
	        GerLocalRefType openejbLocalRefType = openejbResourceRefArray[i];
	        openejbNames.put(openejbLocalRefType.getRefName(), openejbLocalRefType.getTargetName());
	    }
	    Set unshareableResources = new HashSet();
	    Set applicationManagedSecurityResources = new HashSet();
	    for (int i = 0; i < resourceRefArray.length; i++) {
	        ResourceRefType resourceRefType = resourceRefArray[i];
	        String name = (String) openejbNames.get(resourceRefType.getResRefName().getStringValue());
	        if ("Unshareable".equals(OpenEJBModuleBuilder.getJ2eeStringValue(resourceRefType.getResSharingScope()))) {
	            unshareableResources.add(name);
	        }
	        if ("Application".equals(resourceRefType.getResAuth().getStringValue())) {
	            applicationManagedSecurityResources.add(name);
	        }
	    }
	    builder.setUnshareableResources(unshareableResources);
	    builder.setApplicationManagedSecurityResources(applicationManagedSecurityResources);
	}

	protected ReadOnlyContext buildComponentContext(EARContext earContext,
	                                                     EJBModule ejbModule,
	                                                     EnvEntryType[] envEntries,
	                                                     EjbRefType[] ejbRefs,
	                                                     GerRemoteRefType[] openejbEjbRefs,
	                                                     EjbLocalRefType[] ejbLocalRefs,
	                                                     GerLocalRefType[] openejbEjbLocalRefs,
	                                                     ResourceRefType[] resourceRefs,
	                                                     GerLocalRefType[] openejbResourceRefs,
	                                                     ResourceEnvRefType[] resourceEnvRefs,
	                                                     GerLocalRefType[] openejbResourceEnvRefs,
	                                                     MessageDestinationRefType[] messageDestinationRefs, UserTransaction userTransaction,
	                                                     ClassLoader cl) throws NamingException, DeploymentException {
	
	    Map ejbRefMap = mapRefs(openejbEjbRefs);
	    Map ejbLocalRefMap = mapRefs(openejbEjbLocalRefs);
	    Map resourceRefMap = mapRefs(openejbResourceRefs);
	    Map resourceEnvRefMap = mapRefs(openejbResourceEnvRefs);

        return ENCConfigBuilder.buildComponentContext(earContext, ejbModule.getModuleURI(), userTransaction, envEntries, ejbRefs, ejbRefMap, ejbLocalRefs, ejbLocalRefMap, resourceRefs, resourceRefMap, resourceEnvRefs, resourceEnvRefMap, messageDestinationRefs, cl);
	
	}

    protected Map mapRefs(GerLocalRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerLocalRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
    }

    protected Map mapRefs(GerRemoteRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerRemoteRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
    }

	protected ObjectName createEJBObjectName(EARContext earContext, String moduleName, String type, String ejbName) throws DeploymentException {
	    Properties nameProps = new Properties();
	    nameProps.put("j2eeType", type);
	    nameProps.put("name", ejbName);
	    nameProps.put("J2EEServer", earContext.getJ2EEServerName());
	    nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());
	    //TODO should this be EJBModule rather than J2EEModule???
	    nameProps.put("J2EEModule", moduleName);
	
	    try {
	        return new ObjectName(earContext.getJ2EEDomainName(), nameProps);
	    } catch (MalformedObjectNameException e) {
	        throw new DeploymentException("Unable to construct ObjectName", e);
	    }
	}
}