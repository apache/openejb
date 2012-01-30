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
import org.apache.openejb.tools.release.Commit;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.Exec;
import org.apache.openejb.tools.release.util.ObjectList;
import org.apache.openejb.tools.release.util.Options;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.Version;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @version $Rev$ $Date$
 */
@Command
public class CloseJiras {

    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private static SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String... args) throws Exception {

        final String tag = Release.tags + Release.openejbVersionName;

        final InputStream in = Exec.read("svn", "log", "--verbose", "--xml", "-rHEAD:{" + Release.lastReleaseDate + "}", tag);

        final JAXBContext context = JAXBContext.newInstance(Commit.Log.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        final Commit.Log log = (Commit.Log) unmarshaller.unmarshal(in);

        final State state = new State();

        { // Collect the work that made it into the release
            ObjectList<Commit> commits = log.getCommits();
            commits = commits.ascending("revision");

            mine:
            for (Commit commit : commits) {
                final String[] tokens = commit.getMessage().toUpperCase().split("[^A-Z0-9-]+");
                for (String token : tokens) {
                    if (token.matches("(OPENEJB|TOMEE)-[0-9]+")) {
                        try {
                            state.get(token).add(commit);
//                            break mine;
                        } catch (Exception e) {
                            System.err.printf("Bad issue %s\n", token);
                        }
                    }
                }
            }
        }

        // Close those jiras with links to the commits
        for (IssueCommits ic : state.map.values()) {
            final Issue issue = ic.getIssue();

            final StringBuilder comment = new StringBuilder();
            for (Commit commit : ic.getCommits()) {
                comment.append(String.format("%s - http://svn.apache.org/viewvc?view=revision&revision=%s - %s\n", date.format(commit.getDate()), commit.getRevision(), commit.getAuthor()));
            }

            try {
                System.out.println("\n\n" + comment);
                System.out.printf("Adding comment to %s\n", issue.getKey());
                state.jira.addComment(issue.getKey(), comment.toString());
            } catch (Exception e) {
                synchronized (System.out) {
                    e.printStackTrace();
                }
            }
        }

        // Close those jiras with links to the commits
        if (false) for (IssueCommits ic : state.map.values()) {
            final Issue issue = ic.getIssue();

            final Version version;
            if (issue.getKey().startsWith("TOMEE-")) {
                version = state.jira.getVersion("TOMEE", Release.tomeeVersion);
            } else if (issue.getKey().startsWith("OPENEJB-")) {
                version = state.jira.getVersion("OPENEJB", Release.openejbVersion);
            } else {
                continue;
            }

            final Set<String> ids = new HashSet<String>();
            for (Version v : issue.getFixVersions()) {
                ids.add(v.getId() + "");
            }

            final int versions = ids.size();
            ids.add(version.getId() + "");

            if (versions != ids.size()) {
                try {
                    System.out.printf("Adding version to %s\n", issue.getKey());

                    final Hashtable map = new Hashtable();
                    map.put("fixVersions", new Vector(ids));
                    call(state.jira, "updateIssue", issue.getKey(), map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void updateIssue(Jira jira, Issue i) throws Exception {
        Hashtable issue = new Hashtable();
        Vector v = new Vector();

        v = new Vector();
        for (Version version : i.getFixVersions()) {
            v.add(version.getId() + ""); // version's ID
        }
        issue.put("fixVersions", v);

        call(jira, "updateIssue", i.getKey(), issue);
    }

    private static void call(Jira jira, String command, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method method = Jira.class.getDeclaredMethod("call", String.class, Object[].class);
        method.setAccessible(true);
        method.invoke(jira, command, args);
    }

    public static class State {

        private Jira jira;
        private Map<String, IssueCommits> map = new HashMap<String, IssueCommits>();

        public State() throws Exception {
            final Options options = new Options(System.getProperties());
            jira = new Jira("http://issues.apache.org/jira/rpc/xmlrpc");
            jira.login(options.get("username", ""), options.get("password", ""));
        }

        public IssueCommits get(String key) {
            final IssueCommits commits = map.get(key);
            if (commits != null) return commits;

            final IssueCommits issueCommits = new IssueCommits(jira.getIssue(key));
            map.put(issueCommits.getKey(), issueCommits);

            return issueCommits;
        }
    }

    public static class IssueCommits {
        private final String key;
        private final Issue issue;
        Set<Commit> commits = new LinkedHashSet<Commit>();

        public IssueCommits(Issue issue) {
            this.key = issue.getKey();
            this.issue = issue;
        }

        public Issue getIssue() {
            return issue;
        }

        public String getKey() {
            return key;
        }

        public Set<Commit> getCommits() {
            return commits;
        }

        public void add(Commit commit) {
            this.commits.add(commit);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IssueCommits that = (IssueCommits) o;

            if (!key.equals(that.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
