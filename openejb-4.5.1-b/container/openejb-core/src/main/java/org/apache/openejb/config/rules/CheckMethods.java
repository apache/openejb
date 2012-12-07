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
package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

public class CheckMethods extends ValidationBase {

    public void validate(EjbModule ejbModule) {

        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            if (!(bean instanceof RemoteBean)) continue;
            RemoteBean b = (RemoteBean) bean;

            if (b.getHome() != null) {
                check_remoteInterfaceMethods(b);
                check_homeInterfaceMethods(b);
            }
            if (b.getLocalHome() != null) {
                check_localInterfaceMethods(b);
                check_localHomeInterfaceMethods(b);
            }

            check_unusedCreateMethods(b);
            check_unusedPostCreateMethods(b);

        }
    }

    private void check_localHomeInterfaceMethods(RemoteBean b) {
        Class home = null;
        Class bean = null;
        try {
            home = loadClass(b.getLocalHome());
            bean = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
            return;
        }

        if (!EJBLocalHome.class.isAssignableFrom(home)) {
            return;
        }

        if (check_hasCreateMethod(b, bean, home)) {
            check_createMethodsAreImplemented(b, bean, home);
//            check_postCreateMethodsAreImplemented(b, bean, home);
        }
    }

    private void check_localInterfaceMethods(RemoteBean b) {
        Class intrface = null;
        Class beanClass = null;
        try {
            intrface = loadClass(b.getLocal());
            beanClass = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
            return;
        }

        if (!EJBLocalObject.class.isAssignableFrom(intrface)) {
            return;
        }

        Method[] interfaceMethods = intrface.getMethods();

        for (int i = 0; i < interfaceMethods.length; i++) {
            if (interfaceMethods[i].getDeclaringClass() == EJBLocalObject.class) continue;
            String name = interfaceMethods[i].getName();
            try {
                Class[] params = interfaceMethods[i].getParameterTypes();
                Method beanMethod = beanClass.getMethod(name, params);
            } catch (NoSuchMethodException nsme) {
                List<Method> differentArgs = new ArrayList<Method>();
                List<Method> differentCase = new ArrayList<Method>();

                for (Method method : beanClass.getMethods()) {
                    if (method.getName().equals(name)){
                        differentArgs.add(method);
                    } else if (method.getName().equalsIgnoreCase(name)){
                        differentCase.add(method);
                    }
                }

                if (differentArgs.size() > 0) {
                    fail(b, "no.busines.method.args", interfaceMethods[i].getName(), interfaceMethods[i].toString(), "local", intrface.getName(), beanClass.getName(), differentArgs.size());
                } 
                if (differentCase.size() > 0){
                    fail(b, "no.busines.method.case", interfaceMethods[i].getName(), interfaceMethods[i].toString(), "local", intrface.getName(), beanClass.getName(), differentCase.size());
                } 
                if(differentArgs.size() == 0 && differentCase.size() == 0){
                    fail(b, "no.busines.method", interfaceMethods[i].getName(), interfaceMethods[i].toString(), "local", intrface.getName(), beanClass.getName());
                }
            }
        }

    }

    private void check_remoteInterfaceMethods(RemoteBean b) {

        Class intrface = null;
        Class beanClass = null;
        try {
            intrface = loadClass(b.getRemote());
            beanClass = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
            return;
        }

        if (!EJBObject.class.isAssignableFrom(intrface)) {
            return;
        }

        Method[] interfaceMethods = intrface.getMethods();

        for (int i = 0; i < interfaceMethods.length; i++) {
            if (interfaceMethods[i].getDeclaringClass() == javax.ejb.EJBObject.class) continue;
            String name = interfaceMethods[i].getName();
            try {
                Class[] params = interfaceMethods[i].getParameterTypes();
                Method beanMethod = beanClass.getMethod(name, params);
            } catch (NoSuchMethodException nsme) {
                List<Method> differentArgs = new ArrayList<Method>();
                List<Method> differentCase = new ArrayList<Method>();

                for (Method method : beanClass.getMethods()) {
                    if (method.getName().equals(name)){
                        differentArgs.add(method);
                    } else if (method.getName().equalsIgnoreCase(name)){
                        differentCase.add(method);
                    }
                }

                if (differentArgs.size() > 0) {
                    fail(b, "no.busines.method.args", interfaceMethods[i].getName(), interfaceMethods[i].toString(), "remote", intrface.getName(), beanClass.getName(), differentArgs.size());
                } 
                if (differentCase.size() > 0){
                    fail(b, "no.busines.method.case", interfaceMethods[i].getName(), interfaceMethods[i].toString(), "remote", intrface.getName(), beanClass.getName(), differentCase.size());
                }
                if (differentArgs.size() == 0 && differentCase.size() == 0){
                    fail(b, "no.busines.method", interfaceMethods[i].getName(), interfaceMethods[i].toString(), "remote", intrface.getName(), beanClass.getName());
                }
            }
        }
    }


    private void check_homeInterfaceMethods(RemoteBean b) {
        Class home = null;
        Class bean = null;
        try {
            home = loadClass(b.getHome());
            bean = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
            return;
        }

        if (!EJBHome.class.isAssignableFrom(home)) {
            return;
        }

        if (check_hasCreateMethod(b, bean, home)) {
            check_createMethodsAreImplemented(b, bean, home);
            // ejbPostCreate methods are now automatically generated
//            check_postCreateMethodsAreImplemented(b, bean, home);
        }
    }

    public boolean check_hasCreateMethod(RemoteBean b, Class bean, Class home) {

        if (b instanceof SessionBean && !javax.ejb.SessionBean.class.isAssignableFrom(bean)) {
            // This is a pojo-style bean
            return false;
        }

        Method[] homeMethods = home.getMethods();

        boolean hasCreateMethod = false;

        for (int i = 0; i < homeMethods.length && !hasCreateMethod; i++) {
            hasCreateMethod = homeMethods[i].getName().startsWith("create");
        }

        if (!hasCreateMethod && !(b instanceof EntityBean)) {

            fail(b, "no.home.create", b.getHome(), b.getRemote());

        }

        return hasCreateMethod;
    }

    public boolean check_createMethodsAreImplemented(RemoteBean b, Class bean, Class home) {
        boolean result = true;

        Method[] homeMethods = home.getMethods();

        for (int i = 0; i < homeMethods.length; i++) {
            if (!homeMethods[i].getName().startsWith("create")) continue;

            Method create = homeMethods[i];

            StringBuilder ejbCreateName = new StringBuilder(create.getName());
            ejbCreateName.replace(0, 1, "ejbC");

            try {
                if (javax.ejb.EnterpriseBean.class.isAssignableFrom(bean)) {
                    bean.getMethod(ejbCreateName.toString(), create.getParameterTypes());
                } else {
                    // TODO: Check for Init method in pojo session bean class
                }
            } catch (NoSuchMethodException e) {
                result = false;

                String paramString = getParameters(create);

                if (b instanceof EntityBean) {
                    EntityBean entity = (EntityBean) b;

                    fail(b, "entity.no.ejb.create", b.getEjbClass(), entity.getPrimKeyClass(), ejbCreateName.toString(), paramString);

                } else {
                    if (b instanceof SessionBean) {
                        SessionBean sb = (SessionBean) b;
                        // Under EJB 3.1, it is not required that a stateless session bean have an ejbCreate method, even when it has a home interface
                        if (!sb.getSessionType().equals(SessionType.STATELESS))
                            fail(b, "session.no.ejb.create", b.getEjbClass(), ejbCreateName.toString(), paramString);
                    }
                }
            }
        }

        return result;
    }

    public boolean check_postCreateMethodsAreImplemented(RemoteBean b, Class bean, Class home) {
        boolean result = true;

        if (b instanceof SessionBean) return true;

        Method[] homeMethods = home.getMethods();
        Method[] beanMethods = bean.getMethods();

        for (int i = 0; i < homeMethods.length; i++) {
            if (!homeMethods[i].getName().startsWith("create")) continue;
            Method create = homeMethods[i];
            StringBuilder ejbPostCreateName = new StringBuilder(create.getName());
            ejbPostCreateName.replace(0, 1, "ejbPostC");
            try {
                bean.getMethod(ejbPostCreateName.toString(), create.getParameterTypes());
            } catch (NoSuchMethodException e) {
                result = false;

                String paramString = getParameters(create);

                fail(b, "no.ejb.post.create", b.getEjbClass(), ejbPostCreateName.toString(), paramString);

            }
        }

        return result;
    }

    public void check_unusedCreateMethods(RemoteBean b) {

        Class home = null;
        Class localHome = null;
        Class bean = null;
        try {
            if (b.getLocalHome() != null) {
                localHome = loadClass(b.getLocalHome());
            }

            if (b.getHome() != null) {
                home = loadClass(b.getHome());
            }

            bean = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
            return;
        }

        for (Method ejbCreate : bean.getMethods()) {

            if (!ejbCreate.getName().startsWith("ejbCreate")) continue;

            StringBuilder create = new StringBuilder(ejbCreate.getName());
            create.replace(0, "ejbC".length(), "c");


            boolean inLocalHome = false;
            boolean inHome = false;

            try {
                if (localHome != null) {
                    localHome.getMethod(create.toString(), ejbCreate.getParameterTypes());
                    inLocalHome = true;
                }
            } catch (NoSuchMethodException e) {
            }

            try {
                if (home != null) {
                    home.getMethod(create.toString(), ejbCreate.getParameterTypes());
                    inHome = true;
                }
            } catch (NoSuchMethodException e) {
            }

            if (!inLocalHome && !inHome){
                String paramString = getParameters(ejbCreate);

                warn(b, "unused.ejb.create", b.getEjbClass(), ejbCreate.getName(),  paramString, create.toString());
            }
        }
    }

    public void check_unusedPostCreateMethods(RemoteBean b) {

        Class bean = null;
        try {
            bean = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
            return;
        }

        for (Method postCreate : bean.getMethods()) {

            if (!postCreate.getName().startsWith("ejbPostCreate")) continue;

            StringBuilder ejbCreate = new StringBuilder(postCreate.getName());
            ejbCreate.replace(0, "ejbPostCreate".length(), "ejbCreate");

            try {
                bean.getMethod(ejbCreate.toString(), postCreate.getParameterTypes());
            } catch (NoSuchMethodException e) {

                String paramString = getParameters(postCreate);

                warn(b, "unused.ejbPostCreate", b.getEjbClass(), postCreate.getName(), paramString, ejbCreate.toString());

            }
        }
    }

/// public void check_findMethods(){
///     if(this.componentType == this.BMP_ENTITY ){
///
///         String beanMethodName = "ejbF"+method.getName().substring(1);
///         beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
///     }
/// }
///
/// public void check_homeMethods(){
///     String beanMethodName = "ejbHome"+method.getName().substring(0,1).toUpperCase()+method.getName().substring(1);
///     beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
/// }

}

