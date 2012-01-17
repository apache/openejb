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
package org.apache.openejb.tools.release.util;

import org.apache.openejb.tools.release.Command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class Commands {

    public static String name(Class<?> clazz) {
        final Command command = clazz.getAnnotation(Command.class);
        String name = command.value();
        if (name.length() == 0) name = clazz.getSimpleName().toLowerCase();
        return name;
    }

    public static void run(String[] args, Class clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method main = clazz.getMethod("main", String[].class);
        main.invoke(null, new Object[]{args});
    }

    public static List<String> dependencies(Class<?> clazz) {
        List<String> list = new ArrayList<String>();
        final Command command = clazz.getAnnotation(Command.class);
        for (Class dep : command.dependsOn()) {
            list.add(name(dep));
        }

        return list;
    }

    public static List<Class<?>> order(List<Class<?>> commands) {
        return References.sort(commands, new References.Visitor<Class<?>>() {
            @Override
            public String getName(Class<?> aClass) {
                return name(aClass);
            }

            @Override
            public Set<String> getReferences(Class<?> aClass) {

                return new HashSet<String>(dependencies(aClass));
            }
        });
    }
}
