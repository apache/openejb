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

import static java.lang.String.format;
import static org.apache.openejb.tools.release.util.Exec.exec;

/**
 * @version $Rev$ $Date$
 */
@Command
public class Tag {

    public static void main(String... args) throws Exception {

        final String branch = Release.branches + Release.openejbVersion;
        final String tag = Release.tags + Release.openejbVersion;

        if (exec("svn", "info", tag) == 0) {
            exec("svn", "-m", format("[release-tools] recreating tag for %s", Release.openejbVersion), "rm", tag);
        }

        exec("svn", "-m", format("[release-tools] creating tag for %s", Release.openejbVersion), "cp", branch, tag);
    }

}
