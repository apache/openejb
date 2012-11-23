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
import org.apache.openejb.tools.release.Command;
import org.apache.openejb.tools.release.Commit;
import org.apache.openejb.tools.release.Maven;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.Exec;
import org.apache.openejb.tools.release.util.Join;
import org.apache.openejb.tools.release.util.ObjectList;
import org.apache.openejb.tools.release.util.Options;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.IssueType;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.MapObject;
import org.codehaus.swizzle.jira.Version;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
@Command
public class ReviewCommits {

    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String... args) throws Exception {

        final String tag = Release.tags + Release.openejbVersionName;

        final InputStream in = Exec.read("svn", "log", "--verbose", "--xml", "-rHEAD:{" + Release.lastReleaseDate + "}", tag);

        final JAXBContext context = JAXBContext.newInstance(Commit.Log.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        final Commit.Log log = (Commit.Log) unmarshaller.unmarshal(in);

        ObjectList<Commit> commits = log.getCommits();
        commits = commits.ascending("revision");

        for (Commit commit : commits) {
            final String[] tokens = commit.getMessage().split("[^A-Z0-9-]+");
            for (String token : tokens) {
                if (token.matches("(OPENEJB|TOMEE)-[0-9]+")) {
                    try {
                        addIssue(getJira().getIssue(token));
                    } catch (Exception e) {
                        System.out.printf("Invalid JIRA '%s'\n", token);
                    }
                }
            }
        }

        final Date reviewed = new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-05");
        commits = commits.greater("date", reviewed);
        commits = commits.subtract(commits.contains("message", "OPENEJB-"));
        commits = commits.subtract(commits.contains("message", "TOMEE-"));

        System.out.printf("Are you ready to review %s commits?", commits.size());
        System.out.println();

        for (Commit commit : commits) {
            handle(commit);
        }

//        for (Commit commit : commits) {
//            System.out.println(commit);
//        }
//

    }

    public static boolean handle(Commit commit) {
        for (Commit.Path path : commit.getPaths()) {
            System.out.printf(" %s %s", path.getAction(), path.getPath());
            System.out.println();
        }
        System.out.println(commit);

        System.out.printf("[%s]: ", Join.join(", ", Key.values()));

        final String line = readLine().toUpperCase();

        try {
            final Key key = Key.valueOf(line);
            if (!key.pressed(commit)) handle(commit);
        } catch (IllegalArgumentException e) {
            return handle(commit);
        }

        return true;
    }

    private static String prompt(String s) {
        System.out.printf("%s : ", s);
        final String value = readLine();
        return (value == null || value.length() == 0) ? s : value;
    }

    /**
     * Sort of a mini clipboard of recently seen issues
     */
    private static List<Issue> last = new ArrayList<Issue>();

    private static void addIssue(Issue issue) {
        last.remove(issue);
        last.add(0, issue);
        while (last.size() > 20) {
            last.remove(last.size() - 1);
        }
    }

    private static Jira jira;
    private static List<IssueType> issueTypes = new ArrayList<IssueType>();

    public static Jira getJira() {
        Server server = Maven.settings.getServer("apache.jira");
        final String username = server.getUsername();
        final String password = server.getPassword();

        if (jira == null) {
            try {
                final Options options = new Options(System.getProperties());
                Jira jira = new Jira("http://issues.apache.org/jira/rpc/xmlrpc");
                jira.login(username, password);
                ReviewCommits.jira = jira;

                issueTypes.add(jira.getIssueType("Improvement"));
                issueTypes.add(jira.getIssueType("New Feature"));
                issueTypes.add(jira.getIssueType("Bug"));
                issueTypes.add(jira.getIssueType("Task"));
                issueTypes.add(jira.getIssueType("Dependency upgrade"));

            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return jira;
    }

    public static enum Key {
        V(new Action() {
            @Override
            public boolean perform(Commit commit) {
                Exec.exec("open", String.format("http://svn.apache.org/viewvc?view=revision&revision=%s", commit.getRevision()));
                return false;
            }
        }),

        // ASSOCIATE with a JIRA issue
        A(new Action() {
            @Override
            public boolean perform(Commit commit) {
                int i = 0;

                final List<Issue> issues = new ArrayList<Issue>(last);

                for (Issue issue : issues) {
                    System.out.printf("%s) %s: %s\n", i++, issue.getKey(), issue.getSummary());
                }

                final String[] split = prompt("issues?").split(" +");
                for (String key : split) {

                    final Issue issue = resolve(key, issues);
                    if (issue == null) {
                        System.out.println("No such issue " + key);
                        continue;
                    }

                    addIssue(issue);
                    System.out.printf("Associating %s", issue.getKey());
                    System.out.println();
                    updateCommitMessage(commit, issue);
                }

                return false;
            }

        }),

        // NEXT commit
        N(new Action() {
            @Override
            public boolean perform(Commit commit) {
                return true;
            }
        }),

        // CREATE jira
        C(new Action() {

            @Override
            public boolean perform(Commit commit) {

                try {
                    final Jira jira = getJira();

                    final String summary = prompt("summary");
                    final String project = prompt("TOMEE ('o' for OPENEJB else TOMEE)");
                    final String version = prompt("TOMEE".equals(project) ? Release.tomeeVersion : Release.openejbVersion);
                    final String type = prompt("Improvement (type first letters)").toLowerCase();

                    Issue issue = new Issue();

                    if (project.equalsIgnoreCase("o")) {
                        issue.setProject(jira.getProject("OPENEJB"));
                    } else {
                        issue.setProject(jira.getProject(project));
                    }
                    issue.setSummary(summary);

                    // Set default to Improvement
                    issue.setType(issueTypes.get(0));
                    for (IssueType issueType : issueTypes) {
                        if (issueType.getName().toLowerCase().startsWith(type)) {
                            issue.setType(issueType);
                            break;
                        }
                    }

                    final Version v = jira.getVersion(issue.getProject(), version);
                    issue.getFixVersions().add(v);

                    System.out.printf("%s %s\n%s %s\n", issue.getProject(), issue.getSummary(), issue.getType(), Join.join(",", issue.getFixVersions()));
                    final String prompt = prompt("create? (yes or no)");

                    if (prompt.equals("create?") || prompt.equals("yes")) {
                        try {
                            final Issue jiraIssue = createIssue(jira, issue);
                            addIssue(jiraIssue);

                            System.out.println(jiraIssue.getKey());

                            updateCommitMessage(commit, jiraIssue);
                        } catch (Exception e) {
                            System.out.println("Could not create jira issue");
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }

        });

        private static Issue createIssue(Jira jira, Issue issue) throws Exception {
            trimIssue(issue);

            return jira.createIssue(issue);
        }


        private static Issue resolve(String key, List<Issue> issues) {
            try {
                return issues.get(new Integer(key));
            } catch (Exception e) {
            }


            try {
                return getJira().getIssue(key);
            } catch (Exception e) {
            }

            return null;
        }

        private static void updateCommitMessage(Commit commit, Issue issue) {
            final String oldMessage = commit.getMessage();

            if (oldMessage.contains(issue.getKey())) return;

            final String newMessage = String.format("%s\n%s: %s", oldMessage, issue.getKey(), issue.getSummary());

            Exec.exec("svn", "propset", "-r", commit.getRevision() + "", "--revprop", "svn:log", newMessage, "https://svn.apache.org/repos/asf");
        }


        private final Action action;

        Key(Action action) {
            this.action = action;
        }

        public boolean pressed(Commit commit) {
            return action.perform(commit);
        }
    }

    public static String v(String version) {
        return version.replaceFirst("^[a-z]+-", "");
    }

    public static Issue trimIssue(Issue issue) throws NoSuchFieldException, IllegalAccessException {
        toMap(issue).remove("votes");

        for (Version version : issue.getFixVersions()) {
            toMap(version).remove("archived");
            toMap(version).remove("sequence");
            toMap(version).remove("released");
            toMap(version).remove("releaseDate");
        }

        return issue;
    }

    public static Map toMap(MapObject issue) throws NoSuchFieldException, IllegalAccessException {
        final Field fields = MapObject.class.getDeclaredField("fields");
        fields.setAccessible(true);
        return (Map) fields.get(issue);
    }

    public static interface Action {

        boolean perform(Commit commit);
    }

    private static String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
