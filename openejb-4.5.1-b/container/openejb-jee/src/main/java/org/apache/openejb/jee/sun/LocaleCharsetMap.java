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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"description"})
public class LocaleCharsetMap {
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String locale;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String agent;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String charset;
    protected String description;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String value) {
        this.locale = value;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String value) {
        this.agent = value;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String value) {
        this.charset = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }
}
