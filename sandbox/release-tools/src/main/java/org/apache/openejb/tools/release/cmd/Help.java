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
import org.apache.openejb.tools.release.Main;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.Commands;
import org.apache.openejb.tools.release.util.Join;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
@Command
public class Help {
    public static void main(String[] args) {
        System.out.println("Commands: ");
        System.out.printf("   %-20s %s", "", "(depends on)");
        System.out.println();

        final List<Class<?>> commands = Commands.order(new ArrayList<Class<?>>(Main.commands.values()));

        for (Class<?> command : commands) {
            final List<String> dependencies = Commands.dependencies(command);
            System.out.printf("   %-20s %s", Commands.name(command), Join.join(", ", dependencies));
            System.out.println();
        }

        System.out.println();
        System.out.println("Properties: ");
        System.out.printf("   %-20s %s", "", "(default)");
        System.out.println();

        for (Field field : Release.class.getFields()) {
            try {
                System.out.printf("   %-20s %s", field.getName(), field.get(null));
                System.out.println();
            } catch (IllegalAccessException e) {
            }
        }

    }
}
