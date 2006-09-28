/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.openejb.entity.cmp.pkgenerator;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.tranql.pkgenerator.PrimaryKeyGenerator;

/**
 * @version $Revision$ $Date$
 */
public final class SQLPrimaryKeyGeneratorWrapperGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SQLPrimaryKeyGeneratorWrapperGBean.class, SQLPrimaryKeyGeneratorWrapper.class, NameFactory.KEY_GENERATOR);
        infoFactory.addInterface(PrimaryKeyGenerator.class);

        infoFactory.addReference("ManagedConnectionFactoryWrapper", ManagedConnectionFactoryWrapper.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoFactory.addAttribute("initSQL", String.class, true);
        infoFactory.addAttribute("sql", String.class, true);
        infoFactory.addAttribute("returnType", Class.class, true);

        infoFactory.setConstructor(new String[]{"ManagedConnectionFactoryWrapper", "initSQL", "sql", "returnType"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
