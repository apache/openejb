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
package org.apache.openejb.jee;

import javax.xml.bind.annotation.*;

/**
 * The connectorType defines a resource adapter.
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.NONE)
//@XmlType(name = "connectorType", propOrder = {
//        "moduleName",
//        "descriptions",
//        "displayNames",
//        "icon",
//        "vendorName",
//        "eisType",
//        "resourceAdapterVersion",
//        "license",
//        "resourceAdapter",
//        "requiredWorkContext"
//})
public class Connector extends ConnectorBase {

    public Connector() {
    }

    public Connector(String id) {
        super(id);
    }

    @XmlElement(name = "resourceadapter-version")
    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public void setResourceAdapterVersion(String value) {
        this.resourceAdapterVersion = value;
    }

    @XmlElement(name = "resourceadapter", required = true)
    public ResourceAdapter getResourceAdapter() {
        if (resourceAdapter == null) {
            resourceAdapter = new ResourceAdapter();
        }
        return (ResourceAdapter) resourceAdapter;
    }

    public ResourceAdapter setResourceAdapter(ResourceAdapter resourceAdapter16) {
        this.resourceAdapter = resourceAdapter16;
        return (ResourceAdapter) this.resourceAdapter;
    }

    @XmlAttribute(required = true)
    public String getVersion() {
        if (version == null) {
            return "1.6";
        } else {
            return version;
        }
    }

    public void setVersion(String value) {
        this.version = value;
    }
    
}