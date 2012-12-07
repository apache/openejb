/**
 *
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashSet;
import java.util.Set;

/**
 * javaee6.xsd
 *
 * <p>Java class for env-entryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="env-entryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="env-entry-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="env-entry-type" type="{http://java.sun.com/xml/ns/javaee}env-entry-type-valuesType" minOccurs="0"/>
 *         &lt;element name="env-entry-value" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "env-entryType", propOrder = {
        "descriptions",
        "envEntryName",
        "envEntryType",
        "envEntryValue",
        "mappedName",
        "injectionTarget",
        "lookupName"
        })
public class EnvEntry implements JndiReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "env-entry-name", required = true)
    protected String envEntryName;
    @XmlElement(name = "env-entry-type")
    protected String envEntryType;

    @XmlJavaTypeAdapter(StringAdapter.class)
    @XmlElement(name = "env-entry-value")
    protected String envEntryValue;

    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "lookup-name")
    protected String lookupName;
    @XmlElement(name = "injection-target", required = true)
    protected Set<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public EnvEntry() {
    }

    public EnvEntry(String envEntryName, String envEntryType, String envEntryValue) {
        this.setEnvEntryName(envEntryName);
        this.setEnvEntryType(envEntryType);
        this.setEnvEntryValue(envEntryValue);
    }

    public EnvEntry(String envEntryName, Class<?> envEntryType, String envEntryValue) {
        this(envEntryName, envEntryType.getName(), envEntryValue);
    }

    public EnvEntry name(String envEntryName) {
        this.setEnvEntryName(envEntryName);
        return this;
    }

    public EnvEntry type(String envEntryType) {
        this.setEnvEntryType(envEntryType);
        return this;
    }

    public EnvEntry type(Class<?> envEntryType) {
        return type(envEntryType.getName());
    }

    public EnvEntry value(String envEntryValue) {
        this.setEnvEntryValue(envEntryValue);
        return this;
    }

    public EnvEntry mappedName(String mappedName) {
        this.setMappedName(mappedName);
        return this;
    }

    public EnvEntry lookup(String lookupName) {
        this.setLookupName(lookupName);
        return this;
    }

    public EnvEntry injectionTarget(String className, String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.getEnvEntryName() == null) {
            this.setEnvEntryName("java:comp/env/" + className + "/" + property);
        }

        return this;
    }

    public EnvEntry injectionTarget(Class<?> clazz, String property) {
        return injectionTarget(clazz.getName(), property);
    }

    @XmlTransient
    public String getName() {
        return getEnvEntryName();
    }

    @XmlTransient
    public String getType() {
        return getEnvEntryType();
    }

    public void setName(String name) {
        setEnvEntryName(name);
    }

    public String getKey() {
        String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    public void setType(String type) {
        setEnvEntryType(type);
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getEnvEntryName() {
        return envEntryName;
    }

    public void setEnvEntryName(String value) {
        this.envEntryName = value;
    }

    /**
     * Gets the value of the envEntryType property.
     */
    public String getEnvEntryType() {
        return envEntryType;
    }

    public void setEnvEntryType(String value) {
        this.envEntryType = value;
    }

    public String getEnvEntryValue() {
        return envEntryValue;
    }

    public void setEnvEntryValue(String value) {
        this.envEntryValue = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(String lookupName) {
        this.lookupName = lookupName;
    }

    public Set<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new HashSet<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    @Override
    public String toString() {
        return "EnvEntry{" +
                "name='" + getEnvEntryName() + '\'' +
                ", type='" + getEnvEntryType() + '\'' +
                ", value='" + getEnvEntryValue() + '\'' +
                ", mappedName='" + getMappedName() + '\'' +
                ", lookupName='" + getLookupName() + '\'' +
                '}';
    }
}
