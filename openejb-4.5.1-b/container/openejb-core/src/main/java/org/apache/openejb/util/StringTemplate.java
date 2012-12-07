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
package org.apache.openejb.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTemplate {

	public static final Pattern PATTERN = Pattern.compile("(\\{)((\\.|\\w)+)(})");
	private final String template;

	public StringTemplate(String template) {
		this.template = template;
	}

	public String apply(Map<String, String> map) {
    	Matcher matcher = PATTERN.matcher(template);
        StringBuffer buf = new StringBuffer();
 
    	while (matcher.find()) {
    		String key = matcher.group(2);

            if (key == null) throw new IllegalStateException("Key is null. Template '" + template + "'");

    		String value = map.get(key);
    		
    		if (key.toLowerCase().endsWith(".lc")) {
    			value = map.get(key.substring(0, key.length() - 3)).toLowerCase();
    		} else if (key.toLowerCase().endsWith(".uc")) {
    			value = map.get(key.substring(0, key.length() - 3)).toUpperCase();
    		} else if (key.toLowerCase().endsWith(".cc")) {
    			value = Strings.camelCase(map.get(key.substring(0, key.length() - 3)));
			}

            if (value == null) throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + template + "'. Keys: " + Join.join(", ", map.keySet()));
            matcher.appendReplacement(buf, value);
    	}

    	matcher.appendTail(buf);
    	return buf.toString();
	}

}
