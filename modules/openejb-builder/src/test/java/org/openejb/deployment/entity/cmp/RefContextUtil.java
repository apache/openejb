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
 * $Id: TranqlCmpSchemaBuilder.java 2455 2006-02-17 00:14:15Z dblevins $
 */
package org.openejb.deployment.entity.cmp;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;

public class RefContextUtil {

    private static final EJBReferenceBuilder ejbReferrenceBuilder = new EJBReferenceBuilder() {
        public Reference createEJBLocalReference(String arg0, GBeanData arg1, boolean arg2, String arg3, String arg4) throws DeploymentException {
            return null;
        }
        public Reference createEJBRemoteReference(String arg0, GBeanData arg1, boolean arg2, String arg3, String arg4) throws DeploymentException {
            return null;
        }
        public Reference createCORBAReference(URI arg0, String arg1, ObjectName arg2, String arg3) throws DeploymentException {
            return null;
        }
        public Reference getImplicitEJBRemoteRef(URI arg0, String arg1, boolean arg2, String arg3, String arg4, NamingContext arg5) throws DeploymentException {
            return null;
        }
        public Reference getImplicitEJBLocalRef(URI arg0, String arg1, boolean arg2, String arg3, String arg4, NamingContext arg5) throws DeploymentException {
            return null;
        }
    };
    
    private static final ResourceReferenceBuilder resourceReferenceBuilder = new ResourceReferenceBuilder() {
        public Reference createResourceRef(String arg0, Class arg1) throws DeploymentException {
            return null;
        }
        public Reference createAdminObjectRef(String arg0, Class arg1) throws DeploymentException {
            return null;
        }
        public GBeanData locateActivationSpecInfo(GBeanData arg0, String arg1) throws DeploymentException {
            return null;
        }
        public GBeanData locateResourceAdapterGBeanData(GBeanData arg0) throws DeploymentException {
            return null;
        }
        public GBeanData locateAdminObjectInfo(GBeanData arg0, String arg1) throws DeploymentException {
            return null;
        }
        public GBeanData locateConnectionFactoryInfo(GBeanData arg0, String arg1) throws DeploymentException {
            return null;
        }
    };
    
    private static final ServiceReferenceBuilder serviceReferenceBuilder = new ServiceReferenceBuilder() {
        public Object createService(Class arg0, URI arg1, URI arg2, QName arg3, Map arg4, List arg5, Object arg6, DeploymentContext arg7, Module arg8, ClassLoader arg9) throws DeploymentException {
            return null;
        }
    };

    public RefContext build() {
        return new RefContext(ejbReferrenceBuilder,
                resourceReferenceBuilder, 
                serviceReferenceBuilder, 
                null);
    }
}
