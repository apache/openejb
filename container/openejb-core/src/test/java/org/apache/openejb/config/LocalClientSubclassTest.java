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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.webbeans.logger.JULLoggerFactory;

import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
*/
public class LocalClientSubclassTest extends LocalClientTest {

    public void setUp() {
        //avoid linkage error on mac, only used for tests so don't need to add it in Core
        JULLoggerFactory.class.getName();
    }

    @Override
    public void test() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        InitialContext context = new InitialContext(properties);
        context.bind("inject", this);

        assertRefs();

    }
}
