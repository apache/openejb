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
package org.apache.openejb.client;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

import org.apache.openejb.loader.Loader;

/**
 * LocalInitialContextFactory
 *
 * @since 10/5/2002
 */
public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    static Context intraVmContext;

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        if (intraVmContext == null) {
            try {
                getLoader(env).load(env);
            } catch (Exception e) {
                throw new javax.naming.NamingException("Attempted to load OpenEJB. " + e.getMessage());
            }
            intraVmContext = getIntraVmContext(env);
        }
        return intraVmContext;
    }

    private Loader getLoader(Hashtable env) throws Exception {
        Loader loader = null;
        String type = (String) env.get("openejb.loader");

        try {
            if (type == null || type.equals("context")) {
                loader = instantiateLoader("org.apache.openejb.loader.EmbeddingLoader");
            } else if (type.equals("embed")) {
                loader = instantiateLoader("org.apache.openejb.loader.EmbeddingLoader");
            } else if (type.equals("system")) {
                loader = instantiateLoader("org.apache.openejb.loader.SystemLoader");
            } else if (type.equals("bootstrap")) {
                loader = instantiateLoader("org.apache.openejb.loader.SystemLoader");
            } else if (type.equals("noload")) {
                loader = instantiateLoader("org.apache.openejb.loader.EmbeddedLoader");
            } else if (type.equals("embedded")) {
                loader = instantiateLoader("org.apache.openejb.loader.EmbeddedLoader");
            } // other loaders here
        } catch (Exception e) {
            throw new Exception("Loader " + type + ". " + e.getMessage());
        }
        return loader;
    }

    private ClassLoader getClassLoader() {
        try {
            return Thread.currentThread().getContextClassLoader();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    private Loader instantiateLoader(String loaderName) throws Exception {
        Loader loader = null;
        try {
            ClassLoader cl = getClassLoader();
            Class loaderClass = Class.forName(loaderName, true, cl);
            loader = (Loader) loaderClass.newInstance();
        } catch (Exception e) {
            throw new Exception(
                    "Could not instantiate the Loader " + loaderName + ". Exception " +
                    e.getClass().getName() + " " + e.getMessage());
        }
        return loader;
    }


    private Context getIntraVmContext(Hashtable env) throws javax.naming.NamingException {
        Context context = null;
        try {
            InitialContextFactory factory = null;
            ClassLoader cl = getClassLoader();
            Class ivmFactoryClass = Class.forName("org.apache.openejb.naming.GlobalInitialContextFactory", true, cl);

            factory = (InitialContextFactory) ivmFactoryClass.newInstance();
            context = factory.getInitialContext(env);
        } catch (Exception e) {
            throw new javax.naming.NamingException(
                    "Cannot instantiate an IntraVM InitialContext. Exception: " +
                    e.getClass().getName() + " " + e.getMessage());
        }

        return context;
    }

}


