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

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.openejb.tools.release.util.Files;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class for Maven.
 */
public class Maven {

    // in most cases, we don't need to read the file everything. Let's get a static picture
    public static final Settings settings = getMavenSettings();

    public static File getMavenSettingsFile() {
        return Files.file(System.getProperty("user.home"), ".m2", "settings.xml");
    }

    public static Settings getMavenSettings() {
        Settings settings = null;
        SettingsXpp3Reader reader = new SettingsXpp3Reader();
        try {
            settings = reader.read(new BufferedInputStream(new FileInputStream(getMavenSettingsFile())));

        } catch (Exception e) {
            System.err.println("Can't load Maven settings file: " + getMavenSettingsFile());
            e.printStackTrace();
        }
        return settings;
    }



}
