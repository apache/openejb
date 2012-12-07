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
package org.apache.openejb.config.rules;

import javax.interceptor.AroundInvoke;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
// START SNIPPET : code
@RunWith(ValidationRunner.class)
public class CheckInjectionTargetsTest {
    @Keys(@Key(value="injectionTarget.nameContainsSet",count=2,type=KeyType.WARNING))
    public EjbJar test() {
        EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(CheeseEjb.class));  
        // Valid
        EnvEntry envEntry = new EnvEntry("count", Integer.class.getName(), "10");
        envEntry.getInjectionTarget().add(new InjectionTarget(CheeseEjb.class.getName(),CheeseEjb.class.getName()+"/count"));
        bean.getEnvEntry().add(envEntry);

        // Invalid - can't specify setColor, just color as a target and its setter will be calculated
        EnvEntry envEntry2 = new EnvEntry("color", String.class.getName(), "yellow");
        envEntry2.getInjectionTarget().add(new InjectionTarget(CheeseEjb.class.getName(),CheeseEjb.class.getName()+"/setColor"));
        bean.getEnvEntry().add(envEntry2);

        // Invalid - see the comment above
        EnvEntry envEntry3 = new EnvEntry("age", Integer.class.getName(), "5");
        envEntry3.getInjectionTarget().add(new InjectionTarget(CheeseEjb.class.getName(), "setAge"));
        bean.getEnvEntry().add(envEntry3);

        return ejbJar;
    }
    private static class CheeseEjb{
        @AroundInvoke // need to add this to cause validation to fail. Validation does not fail on warnings, which causes this framework to not work properly
        public void sayCheese(){}
    }
}
// END SNIPPET : code