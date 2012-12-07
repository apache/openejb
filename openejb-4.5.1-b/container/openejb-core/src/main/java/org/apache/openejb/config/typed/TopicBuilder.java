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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.typed;

import org.apache.openejb.config.typed.util.*;
import org.apache.openejb.config.sys.*;
import javax.xml.bind.annotation.*;
import org.apache.openejb.util.Duration;
import java.util.*;
import java.util.concurrent.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Topic")
public class TopicBuilder extends Resource {

    @XmlAttribute
    private String destination = null;

    public TopicBuilder() {
        setClassName("org.apache.activemq.command.ActiveMQTopic");
        setType("javax.jms.Topic");
        setId("Topic");

        setConstructor("destination");

    }

    public TopicBuilder id(String id) {
        setId(id);
        return this;
    }

    public TopicBuilder withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
