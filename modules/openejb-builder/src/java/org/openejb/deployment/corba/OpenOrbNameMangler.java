/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.openejb.deployment.corba;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.system.url.resource.ResourceURLConnection;
import org.openorb.compiler.CompilerHost;
import org.openorb.compiler.orb.Configurator;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.rmi.JavaToIdl;
import org.openorb.compiler.rmi.RmiCompilerProperties;
import org.openorb.compiler.rmi.parser.JavaParser;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.util.JarUtils;

/**
 * Uses openorb's java2idl classes to map the method signatures of an interface to the IDL names.
 *
 * @version $Rev:  $ $Date$
 */
public class OpenOrbNameMangler {
    private static final URL IDLS;
    private static final Properties props = new Properties();

    static {
        JarUtils.setHandlerSystemProperty();
        try {
            IDLS = new URL("resource:/org/openorb/idl/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map mapMethodNames(Class intf, ClassLoader classLoader, boolean isHome) throws DeploymentException, MalformedURLException {
        Method[] methods = intf.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            InterfaceMethodSignature sig = new InterfaceMethodSignature(method, isHome);
            System.out.println("InterfaceMethodSignature: " + sig);
        }
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
        RmiCompilerProperties compilerProperties = new RmiCompilerProperties();
        compilerProperties.setClassloader(classLoader);
        compilerProperties.setM_portableHelper(true);
        compilerProperties.setM_verbose(false);
        compilerProperties.getM_includeList().add(IDLS);
        CompilerHost compilerHost = new JavaToIdl();
        JavaParser javaParser = new JavaParser(compilerProperties, compilerHost, null, null, null);
        Configurator configurator = new Configurator(new String[0], props);
        javaParser.load_standard_idl(configurator, compilerProperties.getM_includeList());
        javaParser.add_idl_files(compilerProperties.getIncludedFiles(), compilerProperties.getM_includeList());


        javaParser.parse_class(intf);
        IdlObject idlObject = javaParser.getIdlTreeRoot();
            writeIDLObject(idlObject);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        return null;
    }

    private void writeIDLObject(IdlObject idlObject) {
        for (Enumeration e = idlObject.content(); e.hasMoreElements();) {
            IdlObject member = (IdlObject) e.nextElement();
            System.out.println("idlObject: " + member);
            writeIDLObject(member);
        }
    }

}
