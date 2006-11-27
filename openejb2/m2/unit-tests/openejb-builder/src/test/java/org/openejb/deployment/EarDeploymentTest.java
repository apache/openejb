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
package org.openejb.deployment;

import java.io.File;

import junit.framework.Test;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Revision$ $Date$
 */
public class EarDeploymentTest extends AbstractDeploymentTest {

    private static final DeploymentTestSuite SUITE =
            new DeploymentTestSuite(EarDeploymentTest.class, new File(System.getProperty("localRepository"), "openejb/test-ear/2.0-SNAPSHOT/test-ear-2.0-SNAPSHOT.ear"));

    public static Test suite() {
        return SUITE;
    }

    public String getJ2eeApplicationName() {
        return "org/apache/geronimo/j2ee/deployment/test";
    }

    public String getJ2eeModuleName() {
        return "test-ejb-jar.jar";
    }

    public Kernel getKernel() {
        return SUITE.getKernel();
    }

    public ClassLoader getApplicationClassLoader() {
        return SUITE.getApplicationClassLoader();
    }    
}
