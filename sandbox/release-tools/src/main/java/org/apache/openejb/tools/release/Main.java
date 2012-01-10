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
package org.apache.openejb.tools.release;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Main {

    static {
        Logger root = Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%p - %m%n")));
        root.setLevel(Level.INFO);
    }

    private static Map<String, Class> commands = new HashMap<String, Class>();

    static {
        commands.put("download", DownloadDirectory.class);
        commands.put("legal", org.apache.rat.tentacles.Main.class);
        commands.put("notes", ReleaseNotes.class);
    }

    public static void main(String[] args) throws Exception {
        final ArrayList<String> list = new ArrayList<String>();

        // Read in and apply the properties specified on the command line
        for (String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String value = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(name, value);
            } else {
                list.add(arg);
            }
        }

        final String command = list.remove(0);
        args = (String[]) list.toArray(new String[list.size()]);

        final Class clazz = commands.get(command);

        final Method main = clazz.getMethod("main", String[].class);
        main.invoke(null, new Object[]{args});

    }

    public static String[] processSystemProperties(String[] args) {
        final ArrayList<String> list = new ArrayList<String>();

        // Read in and apply the properties specified on the command line
        for (String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String value = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(name, value);
            } else {
                list.add(arg);
            }
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

}
