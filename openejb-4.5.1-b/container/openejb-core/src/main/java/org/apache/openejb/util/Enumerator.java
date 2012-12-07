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

import java.util.List;
import java.util.NoSuchElementException;

public class Enumerator implements java.io.Serializable, java.util.Enumeration {
    private static final long serialVersionUID = 8382575322402414896L;
    private final List list;
    private int index;

    public Enumerator(List list) {
        this.list = list;
        index = 0;
    }

    public boolean hasMoreElements() {
        return (index < list.size());
    }

    public Object nextElement() {
        if (!hasMoreElements()) throw new NoSuchElementException();
        return list.get(index++);
    }
}
