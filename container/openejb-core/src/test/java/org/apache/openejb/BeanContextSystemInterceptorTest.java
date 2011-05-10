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
package org.apache.openejb;

import org.apache.openejb.bval.BeanValidationAppendixInterceptor;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.jee.EmptyType;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author Romain Manni-Bucau
 */
@RunWith(ApplicationComposer.class)
public class BeanContextSystemInterceptorTest {
    @EJB private Manager manager;

    @Configuration public Properties config() {
        final Properties p = new Properties();
        p.put(CoreDeploymentInfo.USER_DEFAULT_INTERCEPTOR_KEY, BeanValidationAppendixInterceptor.class.getName());
        return p;
    }

    @Module public StatelessBean app() throws Exception {
        final StatelessBean bean = new StatelessBean(Manager.class);
        bean.setLocalBean(new EmptyType());
        return bean;
    }

    @LocalBean
    @Stateless
    public static class Manager {
        @Size(min = 1, max = 5) @NotNull public String shouldBeValidated(String returned, @Max(5) int param) {
            return returned;
        }
    }

    @Test public void valid()  {
        manager.shouldBeValidated("hi", 1);
    }

    @Test public void notValid()  {
        try {
            manager.shouldBeValidated(null, 1);
            fail();
        } catch (Exception ejbException) {
            assertTrue(ejbException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ejbException.getCause();
            assertEquals(1, constraintViolationException.getConstraintViolations().size());
        }
    }

    @Test public void notValid2() throws Exception {
        try {
            manager.shouldBeValidated("hello", 6);
            fail();
        } catch (Exception ejbException) {
            assertTrue(ejbException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ejbException.getCause();
            assertEquals(1, constraintViolationException.getConstraintViolations().size());
        }
    }
}
