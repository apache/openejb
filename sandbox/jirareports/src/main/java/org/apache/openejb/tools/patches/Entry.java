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
package org.apache.openejb.tools.patches;

import com.sun.org.apache.regexp.internal.RE;
import org.codehaus.swizzle.jira.MapObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;
import java.util.Map;

/**
* @version $Rev$ $Date$
*/
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Entry extends MapObject {

    public Entry() {
    }

    public Entry(Map data) {
        super(data);
    }

    public String getMessage() {
        return getString("message");
    }

    @XmlElement(name = "msg")
    public void setMessage(String message) {
        setString("message", message);
    }

    public long getRevision() {
        return getLong("revision");
    }

    @XmlAttribute
    public void setRevision(long revision) {
        setLong("revision", revision);
    }

    public Date getDate() {
        return getDate("date");
    }

    @XmlElement
    public void setDate(Date date) {
        setDate("date", date);
    }

    public String getAuthor() {
        return getString("author");
    }

    @XmlElement
    public void setAuthor(String author) {
        setString("author", author);
    }
    
    protected long getLong(String key) {
        String value = getString(key);
        if (value == null) return 0;
        return Long.parseLong(value);
    }

    protected void setLong(String key, long value) {
        fields.put(key, Long.toString(value));
    }

}
