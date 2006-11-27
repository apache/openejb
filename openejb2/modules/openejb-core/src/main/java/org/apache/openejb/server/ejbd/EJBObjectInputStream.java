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
package org.apache.openejb.server.ejbd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.apache.geronimo.kernel.ClassLoading;

public class EJBObjectInputStream extends ObjectInputStream {

    private ClassLoader ejbClassLoader;

    public EJBObjectInputStream() throws IOException, SecurityException {
        super();
    }

    public EJBObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Set the classloader that belongs to the ejb
     *
     */
    public void setClassLoader(ClassLoader ejbClassLoader) {
        this.ejbClassLoader = ejbClassLoader;
    }

    protected Class resolveClass(ObjectStreamClass desc)
        throws IOException, ClassNotFoundException {
        if (ejbClassLoader == null) {
            //TODO is the TCCL every going to be set at this point?
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                return ClassLoading.loadClass(desc.getName(), contextClassLoader);
            } catch (ClassNotFoundException e) {
                return super.resolveClass(desc);
            }
        }
        return ClassLoading.loadClass(desc.getName(), ejbClassLoader);
    }

}
