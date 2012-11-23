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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tools.release.cmd;

import org.apache.openejb.tools.release.Command;
import org.apache.openejb.tools.release.Release;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@Command
public class ReleaseNotes {

    public static void main(String[] args) throws Throwable {
        List<String> argsList = new ArrayList<String>();

        // lets add the template as the parameter
        argsList.add("release-notes-html.vm");

        // then add system properties to get values replaced in the template
        for (Field field : Release.class.getFields()) {
            try {
                argsList.add("-D" + field.getName() + "=" + field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        org.codehaus.swizzle.jirareport.Main.main((String[]) argsList.toArray(new String[] {}));
    }
}
