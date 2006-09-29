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
package org.apache.openejb.util;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Adapts backport Executor instances to concurrent Executor instances.
 */
public class BackportExecutorAdapter
    implements EDU.oswego.cs.dl.util.concurrent.Executor
{
    private edu.emory.mathcs.backport.java.util.concurrent.Executor exector;
    
    public BackportExecutorAdapter(final edu.emory.mathcs.backport.java.util.concurrent.Executor exector) {
        this.exector = exector;
    }
    
    public void execute(final Runnable r) {
        exector.execute(r);
    }
    
    //
    // GBean
    //
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(BackportExecutorAdapter.class);

        infoFactory.addReference("TargetExecutor", edu.emory.mathcs.backport.java.util.concurrent.Executor.class);

        infoFactory.setConstructor(new String[]{
            "TargetExecutor",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
