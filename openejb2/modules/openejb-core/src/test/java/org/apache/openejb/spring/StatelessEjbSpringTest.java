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
package org.apache.openejb.spring;

import javax.ejb.EJBException;

import junit.framework.TestCase;
import org.apache.openejb.StatelessEjbDeployment;
import org.apache.openejb.slsb.MockLocal;
import org.apache.openejb.slsb.MockLocalHome;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$ $Date$
 */
public class StatelessEjbSpringTest extends TestCase {
    public void test() throws Exception {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        AbstractXmlApplicationContext context = new ClassPathXmlApplicationContext("org/apache/openejb/spring/spring.xml");

        StatelessEjbDeployment statelessEjbDeployment = (StatelessEjbDeployment) context.getBean("StatelessEjbDeployment");
        MockLocalHome mockLocalHome = (MockLocalHome) statelessEjbDeployment.getEjbLocalHome();
        MockLocal mockLocal = null;
        try {
            mockLocal = mockLocalHome.create();
        } catch (EJBException e) {
                          e.printStackTrace();
            throw e;
        }
        assertEquals(42 + 1, mockLocal.intMethod(42));
        context.destroy();
    }
}
