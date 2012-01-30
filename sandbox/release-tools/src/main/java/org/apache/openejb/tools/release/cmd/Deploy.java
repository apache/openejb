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
import org.apache.openejb.tools.release.util.Exec;
import org.apache.openejb.tools.release.util.Files;

import java.io.File;

import static java.lang.String.format;

/**
 * @version $Rev$ $Date$
 */
@Command
public class Deploy {

    public static void main(String... args) throws Exception {

        // TODO Look for gpg on the path, report error if not found

        final String tag = Release.tags + Release.openejbVersionName;

        final File dir = new File(Release.workdir);
        Files.mkdir(dir);
        Exec.cd(dir);

        Exec.exec("svn", "co", tag);

        Exec.cd(new File(dir + File.separator + Release.openejbVersionName));

        Exec.export("MAVEN_OPTS", Release.mavenOpts);
        Exec.exec("mvn",
                "-Darguments=-Dmaven.test.skip=true -DfailIfNoTests=false",
                "release:perform",
                format("-DconnectionUrl=scm:svn:%s", tag)
        );
    }
}
