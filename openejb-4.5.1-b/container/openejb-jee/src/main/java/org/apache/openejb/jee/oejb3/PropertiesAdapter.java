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
package org.apache.openejb.jee.oejb3;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesAdapter extends XmlAdapter<String, Properties> {
    public Properties unmarshal(String s) throws Exception {
        Properties properties = new Properties();
        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        properties.load(in);
        return properties;
    }

    public String marshal(Properties properties) throws Exception {
        if (properties == null) return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);

        // First comment is added by properties.store() 
        String string = new String(out.toByteArray());
        return string.replaceFirst("#.*?" + System.getProperty("line.separator"), "");
    }
}
