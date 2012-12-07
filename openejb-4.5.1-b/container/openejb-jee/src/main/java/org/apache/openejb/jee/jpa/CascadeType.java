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

package org.apache.openejb.jee.jpa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         public enum CascadeType { ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH};
 * 
 *       
 * 
 * <p>Java class for cascade-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cascade-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cascade-all" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="cascade-persist" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="cascade-merge" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="cascade-remove" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="cascade-refresh" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="cascade-detach" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cascade-type", propOrder = {
    "cascadeAll",
    "cascadePersist",
    "cascadeMerge",
    "cascadeRemove",
    "cascadeRefresh",
    "cascadeDetach"
})
public class CascadeType {

    @XmlElement(name = "cascade-all")
    protected EmptyType cascadeAll;
    @XmlElement(name = "cascade-persist")
    protected EmptyType cascadePersist;
    @XmlElement(name = "cascade-merge")
    protected EmptyType cascadeMerge;
    @XmlElement(name = "cascade-remove")
    protected EmptyType cascadeRemove;
    @XmlElement(name = "cascade-refresh")
    protected EmptyType cascadeRefresh;
    @XmlElement(name = "cascade-detach")
    protected EmptyType cascadeDetach;

    /**
     * Gets the value of the cascadeAll property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isCascadeAll() {
        return cascadeAll != null;
    }

    /**
     * Sets the value of the cascadeAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setCascadeAll(boolean value) {
        this.cascadeAll = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cascadePersist property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isCascadePersist() {
        return cascadePersist != null;
    }

    /**
     * Sets the value of the cascadePersist property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setCascadePersist(boolean value) {
        this.cascadePersist = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cascadeMerge property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isCascadeMerge() {
        return cascadeMerge != null;
    }

    /**
     * Sets the value of the cascadeMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setCascadeMerge(boolean value) {
        this.cascadeMerge = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cascadeRemove property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isCascadeRemove() {
        return cascadeRemove != null;
    }

    /**
     * Sets the value of the cascadeRemove property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setCascadeRemove(boolean value) {
        this.cascadeRemove = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cascadeRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isCascadeRefresh() {
        return cascadeRefresh != null;
    }

    /**
     * Sets the value of the cascadeRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setCascadeRefresh(boolean value) {
        this.cascadeRefresh = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cascadeDetach property.
     *
     * @return
     *     possible object is
     *     {@link boolean }
     *
     */
    public boolean isCascadeDetach() {
        return cascadeDetach != null;
    }

    /**
     * Sets the value of the cascadeDetach property.
     *
     * @param value
     *     allowed object is
     *     {@link boolean }
     *
     */
    public void setCascadeDetach(boolean value) {
        this.cascadeDetach = value ? new EmptyType() : null;
    }
}
