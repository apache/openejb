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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"messageSecurity"})
public class MessageSecurityBinding {
    @XmlAttribute(name = "auth-layer", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String authLayer;
    @XmlAttribute(name = "provider-id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String providerId;
    @XmlElement(name = "message-security")
    protected List<MessageSecurity> messageSecurity;

    public String getAuthLayer() {
        return authLayer;
    }

    public void setAuthLayer(String value) {
        this.authLayer = value;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String value) {
        this.providerId = value;
    }

    public List<MessageSecurity> getMessageSecurity() {
        if (messageSecurity == null) {
            messageSecurity = new ArrayList<MessageSecurity>();
        }
        return this.messageSecurity;
    }
}
