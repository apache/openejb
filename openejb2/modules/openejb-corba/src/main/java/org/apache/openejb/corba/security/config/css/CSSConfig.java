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
package org.apache.openejb.corba.security.config.css;

import java.io.Serializable;
import java.util.List;

import org.apache.openejb.corba.security.config.tss.TSSCompoundSecMechListConfig;


/**
 * @version $Revision$ $Date$
 */
public final class CSSConfig implements Serializable {
    private final CSSCompoundSecMechListConfig mechList = new CSSCompoundSecMechListConfig();

    public CSSCompoundSecMechListConfig getMechList() {
        return mechList;
    }

    public List findCompatibleSet(TSSCompoundSecMechListConfig mechListConfig) {
        return mechList.findCompatibleSet(mechListConfig);
    }


    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    void toString(String spaces, StringBuffer buf) {
        buf.append(spaces).append("CSSConfig: [\n");
        mechList.toString(spaces + "  ", buf);
        buf.append(spaces).append("]\n");
    }
}
