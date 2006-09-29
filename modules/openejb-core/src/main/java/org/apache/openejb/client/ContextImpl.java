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
package org.apache.openejb.client;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.ReadOnlyContext;

class ContextImpl extends ReadOnlyContext {

    public ContextImpl() {
        super();
    }

    public ContextImpl(Map entries) throws NamingException {
        super();
        for (Iterator iter = entries.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            internalBind(key, value);
        }
    }

    protected ContextImpl(ReadOnlyContext clone, Hashtable env) {
        super(clone, env);
    }

    protected Map internalBind(String name, Object value) throws NamingException {
        return super.internalBind(name, value);
    }
}
