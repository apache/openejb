/*
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
package org.apache.openejb.arquillian.tests.classloader;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.Map;

// bean manager holder to check we are compatible with such impl
public class HashCdiExtension implements Extension {
    public static Map<ClassLoader, BeanManager> BMS = new HashMap<ClassLoader, BeanManager>();

    protected void setBeanManager(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        BMS.put(tccl, beanManager);
    }
}
