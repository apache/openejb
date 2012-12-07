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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class TextMap {

    protected Map<String, String> string = new LinkedHashMap<String, String>();

    public Text[] toArray() {
        List<Text> list = new ArrayList<Text>();
        for (Map.Entry<String, String> entry : string.entrySet()) {
            list.add(new Text(entry.getKey(), entry.getValue()));
        }
        return list.toArray(new Text[]{});
    }

    public void set(Text[] text) {
        string.clear();
        for (Text t : text) {
            string.put(t.getLang(), t.getValue());
        }
    }

    public void add(Text text) {
        if (!string.containsKey(text.getLang())) {
            string.put(text.getLang(), text.getValue());
        }
    }

    public String get() {
        return getLocal(string);
    }

    private String getLocal(Map<String, ?> map) {
        String lang = Locale.getDefault().getLanguage();
        return (String) (map.get(lang) != null ? map.get(lang) : map.get(null));
    }

}
