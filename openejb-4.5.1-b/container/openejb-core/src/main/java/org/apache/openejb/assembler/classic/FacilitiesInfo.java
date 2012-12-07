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
package org.apache.openejb.assembler.classic;

import java.util.List;
import java.util.ArrayList;

public class FacilitiesInfo extends InfoObject {

    public ProxyFactoryInfo intraVmServer;
    public final List<JndiContextInfo> remoteJndiContexts = new ArrayList<JndiContextInfo>();
    public final List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
    public final List<ConnectionManagerInfo> connectionManagers = new ArrayList<ConnectionManagerInfo>();
    public TransactionServiceInfo transactionService;
    public SecurityServiceInfo securityService;

    // Don't add anything here unless it's overridable using the -DserviceId.property=value convention
    public final List<ServiceInfo> services = new ArrayList<ServiceInfo>();
    public final List<String> serverObservers = new ArrayList<String>();
}
