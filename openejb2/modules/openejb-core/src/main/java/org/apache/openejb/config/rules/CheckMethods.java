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
package org.apache.openejb.config.rules;

import java.lang.reflect.Method;
import javax.ejb.EJBLocalObject;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.*;
import org.apache.openejb.util.SafeToolkit;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */

public class CheckMethods implements ValidationRule {


    EjbSet set;


    public void validate(EjbSet set) {

        this.set = set;

        Bean[] beans = set.getBeans();
        for (int i = 0; i < beans.length; i++) {
            Bean b = beans[i];
            if (b.getHome() != null) {
                check_remoteInterfaceMethods(b);
                check_homeInterfaceMethods(b);
            }
            if (b.getLocalHome() != null) {
                check_localInterfaceMethods(b);
                check_localHomeInterfaceMethods(b);
            }
        }

        SafeToolkit.unloadTempCodebase(set.getJarPath());
    }


    private void check_localHomeInterfaceMethods(Bean b) {
        Class home = null;
        Class bean = null;
        try {
            home = SafeToolkit.loadTempClass(b.getLocalHome(), set.getJarPath());
            bean = SafeToolkit.loadTempClass(b.getEjbClass(), set.getJarPath());
        } catch (OpenEJBException e) {
            return;
        }

        if (check_hasCreateMethod(b, bean, home)) {
            check_createMethodsAreImplemented(b, bean, home);
            check_postCreateMethodsAreImplemented(b, bean, home);
        }

        check_unusedCreateMethods(b, bean, home);
    }

    private void check_localInterfaceMethods(Bean b) {
        Class intrface = null;
        Class beanClass = null;
        try {
            intrface = SafeToolkit.loadTempClass(b.getLocal(), set.getJarPath());
            beanClass = SafeToolkit.loadTempClass(b.getEjbClass(), set.getJarPath());
        } catch (OpenEJBException e) {
            return;
        }

        Method[] interfaceMethods = intrface.getMethods();
        Method[] beanClassMethods = intrface.getMethods();

        for (int i = 0; i < interfaceMethods.length; i++) {
            if (interfaceMethods[i].getDeclaringClass() == EJBLocalObject.class) continue;
            try {
                String name = interfaceMethods[i].getName();
                Class[] params = interfaceMethods[i].getParameterTypes();
                Method beanMethod = beanClass.getMethod(name, params);
            } catch (NoSuchMethodException nsme) {
//  0 - method name
//  1 - full method name
//  2 - remote|home|local|local-home
//  3 - interface name
//  4 - EJB Class name
                ValidationFailure failure = new ValidationFailure("no.busines.method");
                failure.setDetails(interfaceMethods[i].getName(), interfaceMethods[i].toString(), "local", intrface.getName(), beanClass.getName());
                failure.setBean(b);

                set.addFailure(failure);

//set.addFailure( new ValidationFailure("no.busines.method", interfaceMethods[i].toString(), "remote", intrface.getName(), beanClass.getName()));
            }
        }

    }


    private void check_remoteInterfaceMethods(Bean b) {

        Class intrface = null;
        Class beanClass = null;
        try {
            intrface = SafeToolkit.loadTempClass(b.getRemote(), set.getJarPath());
            beanClass = SafeToolkit.loadTempClass(b.getEjbClass(), set.getJarPath());
        } catch (OpenEJBException e) {
            return;
        }

        Method[] interfaceMethods = intrface.getMethods();
        Method[] beanClassMethods = intrface.getMethods();

        for (int i = 0; i < interfaceMethods.length; i++) {
            if (interfaceMethods[i].getDeclaringClass() == javax.ejb.EJBObject.class) continue;
            try {
                String name = interfaceMethods[i].getName();
                Class[] params = interfaceMethods[i].getParameterTypes();
                Method beanMethod = beanClass.getMethod(name, params);
            } catch (NoSuchMethodException nsme) {
//  0 - method name
//  1 - full method name
//  2 - remote|home
//  3 - interface name
//  4 - EJB Class name
                ValidationFailure failure = new ValidationFailure("no.busines.method");
                failure.setDetails(interfaceMethods[i].getName(), interfaceMethods[i].toString(), "remote", intrface.getName(), beanClass.getName());
                failure.setBean(b);

                set.addFailure(failure);

//set.addFailure( new ValidationFailure("no.busines.method", interfaceMethods[i].toString(), "remote", intrface.getName(), beanClass.getName()));
            }
        }
    }

    private void check_homeInterfaceMethods(Bean b) {
        Class home = null;
        Class bean = null;
        try {
            home = SafeToolkit.loadTempClass(b.getHome(), set.getJarPath());
            bean = SafeToolkit.loadTempClass(b.getEjbClass(), set.getJarPath());
        } catch (OpenEJBException e) {
            return;
        }

        if (check_hasCreateMethod(b, bean, home)) {
            check_createMethodsAreImplemented(b, bean, home);
            check_postCreateMethodsAreImplemented(b, bean, home);
        }

        check_unusedCreateMethods(b, bean, home);
    }

    /**
     * Must have at least one create method in the home interface.
     *
     * @param b
     * @param bean
     * @param home
     */
    public boolean check_hasCreateMethod(Bean b, Class bean, Class home) {

        Method[] homeMethods = home.getMethods();

        boolean hasCreateMethod = false;

        for (int i = 0; i < homeMethods.length && !hasCreateMethod; i++) {
            hasCreateMethod = homeMethods[i].getName().equals("create");
        }

        if (!hasCreateMethod) {
            // 1 - home interface
            // 2 - remote interface
            ValidationFailure failure = new ValidationFailure("no.home.create");
            failure.setDetails(b.getHome(), b.getRemote());
            failure.setBean(b);

            set.addFailure(failure);
            //set.addFailure( new ValidationFailure("no.home.create", b.getHome(), b.getRemote()));
        }
        //-------------------------------------------------------------
        return hasCreateMethod;
    }

    /**
     * Create methods must me implemented
     *
     * @param b
     * @param bean
     * @param home
     * @return
     */
    public boolean check_createMethodsAreImplemented(Bean b, Class bean, Class home) {
        boolean result = true;

        Method[] homeMethods = home.getMethods();
        Method[] beanMethods = bean.getMethods();

        //-------------------------------------------------------------
        // Create methods must me implemented
        for (int i = 0; i < homeMethods.length; i++) {
            if (!homeMethods[i].getName().equals("create")) continue;
            Method create = homeMethods[i];
            Method ejbCreate = null;
            try {
                ejbCreate = bean.getMethod("ejbCreate", create.getParameterTypes());
            } catch (NoSuchMethodException e) {
                result = false;

                String paramString = getParameters(create);

                if (b instanceof EntityBean) {
                    EntityBean entity = (EntityBean) b;
                    // 1 - EJB Class name
                    // 2 - primary key class
                    // 3 - create params
                    ValidationFailure failure = new ValidationFailure("entity.no.ejb.create");
                    failure.setDetails(b.getEjbClass(), entity.getPrimaryKey(), paramString);
                    failure.setBean(b);

                    set.addFailure(failure);
                    //set.addFailure( new ValidationFailure("entity.no.ejb.create", b.getEjbClass(), entity.getPrimaryKey(), paramString));
                } else {
                    // 1 - EJB Class name
                    // 2 - create params
                    ValidationFailure failure = new ValidationFailure("session.no.ejb.create");
                    failure.setDetails(b.getEjbClass(), paramString);
                    failure.setBean(b);

                    set.addFailure(failure);
                    //set.addFailure( new ValidationFailure("session.no.ejb.create", b.getEjbClass(), paramString));
                }
            }
        }
        //-------------------------------------------------------------
        return result;
    }

    /**
     * Validate that the ejbPostCreate methods of entity beans are
     * implemented in the bean class
     *
     * @param b
     * @param bean
     * @param home
     * @return
     */
    public boolean check_postCreateMethodsAreImplemented(Bean b, Class bean, Class home) {
        boolean result = true;

        if (b instanceof SessionBean) return true;

        Method[] homeMethods = home.getMethods();
        Method[] beanMethods = bean.getMethods();

        //-------------------------------------------------------------
        // Create methods must me implemented
        for (int i = 0; i < homeMethods.length; i++) {
            if (!homeMethods[i].getName().equals("create")) continue;
            Method create = homeMethods[i];
            Method ejbCreate = null;
            try {
                ejbCreate = bean.getMethod("ejbPostCreate", create.getParameterTypes());
            } catch (NoSuchMethodException e) {
                result = false;

                String paramString = getParameters(create);

                // 1 - EJB Class name
                // 2 - primary key class
                // 3 - create params
                ValidationFailure failure = new ValidationFailure("no.ejb.post.create");
                failure.setDetails(b.getEjbClass(), paramString);
                failure.setBean(b);

                set.addFailure(failure);
                //set.addFailure( new ValidationFailure("no.ejb.post.create", b.getEjbClass(), paramString));
            }
        }
        //-------------------------------------------------------------
        return result;
    }

    /**
     * Check for any create methods in the bean that
     * aren't in the home interface as well
     *
     * @param b
     * @param bean
     * @param home
     * @return
     */
    public boolean check_unusedCreateMethods(Bean b, Class bean, Class home) {
        boolean result = true;

        Method[] homeMethods = home.getMethods();
        Method[] beanMethods = bean.getMethods();

        for (int i = 0; i < homeMethods.length; i++) {
            if (!beanMethods[i].getName().equals("ejbCreate")) continue;
            Method ejbCreate = beanMethods[i];
            Method create = null;
            try {
                create = home.getMethod("create", ejbCreate.getParameterTypes());
            } catch (NoSuchMethodException e) {
                result = false;

                String paramString = getParameters(ejbCreate);
                // 1 - bean class
                // 2 - create params
                // 3 - home interface
                ValidationWarning warning = new ValidationWarning("unused.ejb.create");
                warning.setDetails(b.getEjbClass(), paramString, home.getName());
                warning.setBean(b);

                set.addWarning(warning);
                //set.addWarning( new ValidationWarning("unused.ejb.create", b.getEjbClass(), paramString, home.getName()));
            }
        }
        //-------------------------------------------------------------
        return result;
    }

/// public void check_findMethods(){
///     if(this.componentType == this.BMP_ENTITY ){
///         // CMP 1.1 beans do not define a find method in the bean class
///         String beanMethodName = "ejbF"+method.getName().substring(1);
///         beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
///     }
/// }
///
/// public void check_homeMethods(){
///     String beanMethodName = "ejbHome"+method.getName().substring(0,1).toUpperCase()+method.getName().substring(1);
///     beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
/// }

    private String getParameters(Method method) {
        Class[] params = method.getParameterTypes();
        String paramString = "";

        if (params.length > 0) {
            paramString = params[0].getName();
        }

        for (int i = 1; i < params.length; i++) {
            paramString += ", " + params[i];
        }

        return paramString;
    }
}



