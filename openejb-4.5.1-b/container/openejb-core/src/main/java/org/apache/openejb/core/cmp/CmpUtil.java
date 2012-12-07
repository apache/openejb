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
package org.apache.openejb.core.cmp;

public class CmpUtil {
    public static String getCmpImplClassName(String abstractSchemaName, String ejbClass) {
        // locate the end of the package portion 
        int packageEnd = ejbClass.lastIndexOf('.'); 
        String cmpImplClass;
        // we have a package, pull that out and add "openejb." in front of the package 
        if (packageEnd != -1) {
            return "openejb." + ejbClass.substring(0, packageEnd + 1) + abstractSchemaName;
        }
        else {
            // no package, construct this from just "openejb." and the schema name 
            return "openejb." + abstractSchemaName; 
        }
    }
}
