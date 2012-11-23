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

import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.openejb.tools.release.*;
import org.apache.openejb.tools.release.util.Files;
import org.apache.openejb.tools.release.util.IO;
import org.apache.openejb.tools.release.util.ObjectList;
import org.codehaus.swizzle.stream.StreamLexer;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
@Command(dependsOn = Deploy.class)
public class Close {

    public static void main(String... args) throws Exception {

        Server server = Maven.settings.getServer("apache.releases.https");
        final String user = server.getUsername();
        final String pass = server.getPassword();

        final Nexus nexus = new Nexus(user, pass);

        ObjectList<Repository> repositories = nexus.getRepositories();
        repositories = repositories.equals("profileName", "org.apache.openejb");
        repositories = repositories.descending("createdDate");

        for (Repository repository : repositories) {
            System.out.println(repository.getRepositoryURI());
        }

        repositories = repositories.equals("type", "open");

        if (repositories.size() == 0) return;

        final Repository repository = repositories.get(0);

        nexus.close(repository.getRepositoryId());

        System.out.println(repository.getRepositoryURI());

        Release.staging = repository.getRepositoryURI().toString();
        Release.build = Release.staging.replaceAll(".*-", "");
    }

}
