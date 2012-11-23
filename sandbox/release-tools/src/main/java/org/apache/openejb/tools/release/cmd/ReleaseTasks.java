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

/**
 * @version $Rev$ $Date$
 */

import org.apache.commons.lang.Validate;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.settings.Server;
import org.apache.openejb.tools.release.Command;
import org.apache.openejb.tools.release.Maven;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.Templates;
import org.apache.openejb.tools.release.util.Base64;
import org.apache.openejb.tools.release.util.IO;
import org.apache.openejb.tools.release.util.ListAdapter;
import org.apache.openejb.tools.release.util.Options;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Command
public class ReleaseTasks {

    public static void main(String... args) throws Exception {
        final Options options = new Options(System.getProperties());

        Server server = Maven.settings.getServer("apache.jira");
        final String username = server.getUsername();
        final String password = server.getPassword();

        final ReleaseTasks tasks = new ReleaseTasks(username, password);
        tasks.run();
    }

    private final String authorization;
    private final DefaultHttpClient client;

    public ReleaseTasks(String user, String pass) {
        // sanity checks
        Validate.notEmpty(user);
        Validate.notEmpty(pass);

        final String s = user + ":" + pass;
        final byte[] bytes = Base64.encodeBase64(s.getBytes());

        authorization = "Basic " + new String(bytes);
        client = new DefaultHttpClient();
    }

    public void run() throws IOException, JAXBException {
        String response = post(task());
        response = response.replaceAll("[{} \n\"]", "");
        response = response.replaceAll(",", "\n");
        final Properties properties = IO.readProperties(IO.read(response));

        final String key = properties.getProperty("key");

        final JAXBContext context = JAXBContext.newInstance(Tasks.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        final URL resource = this.getClass().getResource("/release-tasks.xml");
        final Tasks tasks = (Tasks) unmarshaller.unmarshal(IO.read(resource));

        for (Task task : tasks.getTasks()) {
            String description = task.getDescription();

            if (task.getCommands().size() > 0) {
                description += "\n\nCommands:\n";
                for (String command : task.getCommands()) {
                    description += String.format("http://svn.apache.org/repos/asf/openejb/trunk/sandbox/release-tools/src/main/java/org/apache/openejb/tools/release/cmd/%s.java\n", command);
                }
            }

            description = description.replaceAll("\n", "\\\\n");
            description = description.replaceAll("\t", "\\\\t");
            description = description.replaceAll("\r", "\\\\r");
            description = description.replaceAll("'", "\\'");
            description = description.replaceAll("\"", "\\\\\"");
            description = description.replaceAll("&", "\\\\&");

            final String subtask = subtask(key, task.summary, description);
            System.out.println(task);
            post(subtask);

//            if (true) break;
        }
    }

    private String task() {
        final Templates.Builder template = Templates.template("task.json");
        template.add("version", Release.tomeeVersion);
        return template.apply();
    }

    private String subtask(final String parentKey, final String summary, final String description) {
        final Templates.Builder template = Templates.template("subtask.json");
        template.add("parentKey", parentKey);
        template.add("summary", summary);
        template.add("description", description);
        return template.apply();
    }

    private String post(final String data) throws IOException {

        final HttpPost post = new HttpPost("https://issues.apache.org/jira/rest/api/2/issue/");
        post.addHeader("Authorization", authorization);
        post.addHeader("Content-Type", "application/json");

        post.setEntity(new StringEntity(data));

        final HttpResponse execute = client.execute(post);

        String message = IO.slurp(execute.getEntity().getContent());

        final int statusCode = execute.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
            throw new IOException(String.format("%s\n%s", execute.getStatusLine().toString(), message));
        }

        return message;
    }

    private void link(final String blocks, final String issue) throws IOException {
        link("{\n" +
                "    \"type\":{\n" +
                "        \"name\":\"Blocker\"\n" +
                "    },\n" +
                "    \"inwardIssue\":{\n" +
                "        \"key\":\"" + blocks + "\"\n" +
                "    },\n" +
                "    \"outwardIssue\":{\n" +
                "        \"key\":\"" + issue + "\"\n" +
                "    }}");
    }

    private String link(final String data) throws IOException {

        final HttpPost post = new HttpPost("https://issues.apache.org/jira/rest/api/2/issueLink/");
        post.addHeader("Authorization", authorization);
        post.addHeader("Content-Type", "application/json");

        post.setEntity(new StringEntity(data));

        final HttpResponse execute = client.execute(post);

        String message = IO.slurp(execute.getEntity().getContent());

        final int statusCode = execute.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
            throw new IOException(String.format("%s\n%s", execute.getStatusLine().toString(), message));
        }

        return message;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Tasks {

        @XmlElement(name = "task")
        private List<Task> tasks = new ArrayList<Task>();

        public List<Task> getTasks() {
            return tasks;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Task {
        @XmlAttribute
        private String summary;

        @XmlAttribute
        @XmlJavaTypeAdapter(ListAdapter.class)
        private List<String> commands = new ArrayList<String>();

        @XmlValue
        private String description;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "summary='" + summary + '\'' +
                    ", commands=" + commands.size() +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
