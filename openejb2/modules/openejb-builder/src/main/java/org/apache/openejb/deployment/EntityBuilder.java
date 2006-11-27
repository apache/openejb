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
package org.apache.openejb.deployment;

import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Revision$ $Date$
 */
public abstract class EntityBuilder extends RpcEjbBuilder {
    private String primaryKeyClassName;
    private boolean reentrant;

    public void setPrimaryKeyClassName(String primaryKeyClassName) {
        this.primaryKeyClassName = primaryKeyClassName;
    }


    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    public GBeanData createConfiguration() throws Exception {
        GBeanData gbean = super.createConfiguration();
        gbean.setAttribute("reentrant", Boolean.valueOf(reentrant));
        gbean.setAttribute("primaryKeyClassName", primaryKeyClassName);
        return gbean;
    }

}
