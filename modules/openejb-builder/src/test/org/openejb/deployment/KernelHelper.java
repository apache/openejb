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
package org.openejb.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.registry.BasicGBeanRegistry;

/**
 * @version $Rev:  $ $Date$
 */
public class KernelHelper {
    public static final URI DEFAULT_PARENTID = URI.create("org/apache/geronimo/Server");

    public static Kernel getPreparedKernel() throws Exception {
        Kernel kernel = new Kernel("bar", new BasicGBeanRegistry());
        kernel.boot();
        GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
        kernel.loadGBean(store, KernelHelper.class.getClassLoader());
        kernel.startGBean(store.getName());

        GBeanData baseConfig = (GBeanData) kernel.invoke(store.getName(), "getConfiguration", new Object[]{DEFAULT_PARENTID}, new String[]{URI.class.getName()});
        kernel.loadGBean(baseConfig, KernelHelper.class.getClassLoader());
        kernel.startGBean(baseConfig.getName());

        return kernel;
    }


    public static class MockConfigStore implements ConfigurationStore {
        public URI install(URL source) throws IOException, InvalidConfigException {
            return null;
        }

        public URI install(File source) throws IOException, InvalidConfigException {
            return null;
        }

        public void uninstall(URI configID) throws NoSuchConfigException, IOException {

        }

        public boolean containsConfiguration(URI configID) {
            return true;
        }

        public GBeanData getConfiguration(URI id) throws NoSuchConfigException, IOException, InvalidConfigException {
            GBeanData configData = null;
            try {
                configData = new GBeanData(Configuration.getConfigurationObjectName(id), Configuration.GBEAN_INFO);
            } catch (MalformedObjectNameException e) {
                throw new InvalidConfigException(e);
            }
            configData.setAttribute("ID", id);
            configData.setAttribute("domain", "test");
            configData.setAttribute("server", "bar");
            configData.setAttribute("gBeanState", NO_OBJECTS_OS);
            return configData;
        }

        public void updateConfiguration(Configuration configuration) throws NoSuchConfigException, Exception {

        }

        public URL getBaseURL(URI id) throws NoSuchConfigException {
            return null;
        }

        public String getObjectName() {
            return null;
        }

        public List listConfiguations() {
            return null;
        }

        public File createNewConfigurationDir() {
            return null;
        }

        public final static GBeanInfo GBEAN_INFO;

        private static final byte[] NO_OBJECTS_OS;

        static {
            GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(MockConfigStore.class);
            infoBuilder.addInterface(ConfigurationStore.class);
            GBEAN_INFO = infoBuilder.getBeanInfo();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.flush();
                NO_OBJECTS_OS = baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };


}
