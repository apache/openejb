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

import com.sun.xml.internal.bind.v2.runtime.Location;
import org.apache.openejb.tools.release.util.Files;
import org.apache.openejb.tools.release.util.Options;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @version $Rev$ $Date$
 */
public class Release {

    public static String openejbSimpleVersion = "4.0.0-beta-2";
    public static String tomeeSimpleVersion = "1.0.0-beta-2";
    public static String openejbVersion = "openejb-" + openejbSimpleVersion;
    public static String tomeeVersion = "tomee-" + tomeeSimpleVersion;

    public static String trunk = "https://svn.apache.org/repos/asf/openejb/trunk/openejb/";
    public static String branches = "https://svn.apache.org/repos/asf/openejb/branches/";
    public static String tags = "https://svn.apache.org/repos/asf/openejb/tags/";
    public static String tckBranches = "https://svn.apache.org/repos/tck/openejb-tck/branches/";
    public static String tckTrunk = "https://svn.apache.org/repos/tck/openejb-tck/trunk";
    public static String staging = "https://repository.apache.org/content/repositories/orgapacheopenejb-075";
    public static String builddir = "/tmp/downloads";
    public static String workdir = "/tmp/release";
    public static String mavenOpts = "-Xmx2048m -XX:MaxPermSize=1024m";
    public static String from = "dblevins@apache.org";
    //    public static String to = from;
    public static String to = "dev@openejb.apache.org";
    public static String user = System.getProperty("user.name");
    public static String build = "075";
    public static String lastReleaseDate = "2011-10-05";

    static {
        final File public_html = Files.file(System.getProperty("user.home"), "public_html");

        if (public_html.exists())  {
            builddir = public_html.getAbsolutePath();
        }

        final Options options = new Options(System.getProperties());

        for (Field field : Release.class.getFields()) {
            try {
                final Object value = options.get(field.getName(), field.get(null));
                field.set(null, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        build = Release.staging.replaceAll(".*-", "");
    }


}
