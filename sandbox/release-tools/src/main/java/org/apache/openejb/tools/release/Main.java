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
import org.apache.openejb.tools.release.util.Commands;
import org.apache.openejb.tools.release.util.JarLocation;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClasspathArchive;

import java.io.File;
import java.net.MalformedURLException;
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

    public static Map<String, Class<?>> commands = new HashMap<String, Class<?>>();

    static {
        try {
            final File file = JarLocation.jarLocation(Main.class);
            final AnnotationFinder finder = new AnnotationFinder(ClasspathArchive.archive(Main.class.getClassLoader(), file.toURI().toURL()));

            for (Class<?> clazz : finder.findAnnotatedClasses(Command.class)) {

                commands.put(Commands.name(clazz), clazz);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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

        final String command = (list.size() == 0) ? "help" : list.remove(0);
        args = list.toArray(new String[list.size()]);

        final Class clazz = commands.get(command);

        Commands.run(args, clazz);

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
