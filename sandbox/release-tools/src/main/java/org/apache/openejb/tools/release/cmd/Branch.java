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
public class Branch {

    public static void main(String... args) throws Exception {

        final String branch = Release.branches + Release.openejbVersionName;
        final String trunk = Release.trunk;

        if (exec("svn", "info", branch) == 0) {
            exec("svn", "-m", format("[release-tools] recreating branch for %s", Release.openejbVersionName), "rm", branch);
        }

        exec("svn", "-m", format("[release-tools] creating branch for %s", Release.openejbVersionName), "cp", trunk, branch);
    }

}
