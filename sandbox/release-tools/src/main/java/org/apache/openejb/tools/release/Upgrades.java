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

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.JiraRss;
import org.codehaus.swizzle.jira.MapObjectList;
import org.codehaus.swizzle.jira.Project;
import org.codehaus.swizzle.jira.Version;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class Upgrades {

    private final List<Upgrade> upgrades = new ArrayList<Upgrade>();

    public Upgrades() {
        System.out.println();
    }

    public Upgrades add(String key, List<String> versions) {
        upgrades.add(new Upgrade(key, versions));
        return this;
    }

    public List<Issue> getIssues() throws Exception {

        final Options options = new Options(System.getProperties());
        Jira jira = new Jira("http://issues.apache.org/jira/rpc/xmlrpc");
        jira.login(options.get("username", ""), options.get("password", ""));

        final List<String> missing = new ArrayList<String>();
        final List<String> urls = new ArrayList<String>();

        for (Upgrade upgrade : upgrades) {
            final Project project = jira.getProject(upgrade.getKey());

            if (project == null) {
                missing.add(upgrade.getKey());
            }

            for (String version : upgrade.getVersions()) {
                try {

                    final Version ver = jira.getVersion(project, version);

                    if (ver == null) {
                        missing.add(project+":"+version);
                        continue;
                    }

                    urls.add(String.format("http://issues.apache.org/jira/secure/IssueNavigator.jspa?view=rss&&pid=%s&status=5&status=6&fixfor=%s&tempMax=1000&reset=true&decorator=none", project.getId(), ver.getId()));
                } catch (Exception e) {
                }
            }
        }

        if (missing.size() > 0) {
            for (String m : missing) {
                System.err.println("Missing " + m);
            }
            throw new IllegalStateException("Missing projects or versions");
        }

        final List<Issue> issues = new MapObjectList<Issue>();

        for (String url : urls) {
            issues.addAll(new JiraRss(url).getIssues());
        }

        return issues;
    }

    public List<Upgrade> get() {
        return upgrades;
    }

    public static class Upgrade {
        private final String key;
        private final List<String> versions;

        public Upgrade(String key, String... versions) {
            this(key, Arrays.asList(versions));
        }

        public Upgrade(String key, List<String> versions) {
            this.key = key;
            this.versions = versions;
        }

        public String getKey() {
            return key;
        }

        public List<String> getVersions() {
            return versions;
        }

        @Override
        public String toString() {
            return "Upgrade{" +
                    "key='" + key + '\'' +
                    ", versions=" + versions.size() +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Upgrades{" +
                "upgrades=" + upgrades.size() +
                '}';
    }
}
