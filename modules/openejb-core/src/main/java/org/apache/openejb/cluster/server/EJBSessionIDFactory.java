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
package org.apache.openejb.cluster.server;

import org.codehaus.wadi.SessionIdFactory;

/**
 * 
 * @version $Revision$ $Date$
 */
public class EJBSessionIDFactory implements SessionIdFactory {
    private final String nodeName;
    private int index;
    private int lenght;
    
    public EJBSessionIDFactory(String nodeName) {
        this.nodeName = nodeName;
    }

    public String create() {
        return nodeName + index++;
    }

    public int getSessionIdLength() {
        // TODO investigate the purpose of the session ID length.
        return lenght;
    }

    public void setSessionIdLength(int lenght) {
        this.lenght = lenght;
    }
}
