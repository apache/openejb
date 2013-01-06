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

import junit.framework.TestCase;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.*;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class VagueEnvEntriesMatchTest extends TestCase {

    @EJB
    public Blue blue;

    @Module
    public SingletonBean foo() {

        final SingletonBean singletonBean = new SingletonBean(Blue.class);

        singletonBean.getEnvEntry().add(new EnvEntry().name("one").type(String.class).value("hello"));
        singletonBean.getEnvEntry().add(new EnvEntry().name("two").type(String.class).value("false"));
        singletonBean.getEnvEntry().add(new EnvEntry().name(Blue.class.getName() + "/two").type(String.class).value("true"));

        return singletonBean;
    }

    @Test
    public void test() {

        assertEquals("hello", blue.getOne());
        assertEquals("true", blue.getTwo());
    }


    public static class Blue {

        @Resource
        private String one;

        @Resource
        private String two;

        public String getOne() {
            return one;
        }

        public String getTwo() {
            return two;
        }
    }

}
