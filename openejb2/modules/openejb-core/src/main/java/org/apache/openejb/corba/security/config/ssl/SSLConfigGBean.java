/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.openejb.corba.security.config.ssl;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

import org.apache.geronimo.management.geronimo.KeystoreManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.apache.geronimo.management.geronimo.KeyIsLocked;
import org.apache.geronimo.management.geronimo.KeystoreIsLocked;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;

import org.apache.openejb.corba.security.config.tss.TSSConfig;

/**
 * Implementation of an SSLConfigGBean
 *
 * @version $Rev$ $Date$
 */
public class SSLConfigGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(SSLConfigGBean.class, "SSL Configuration Adapater", SSLConfig.class, NameFactory.CORBA_SSL);
        infoBuilder.addAttribute("provider", String.class, true, true);
        infoBuilder.addAttribute("protocol", String.class, true, true);
        infoBuilder.addAttribute("algorithm", String.class, true, true);
        infoBuilder.addAttribute("keyStore", String.class, true, true);
        infoBuilder.addAttribute("keyAlias", String.class, true, true);
        infoBuilder.addAttribute("trustStore", String.class, true, true);
        infoBuilder.addReference("KeystoreManager", KeystoreManager.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.setConstructor(new String[]{"KeystoreManager"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

