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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.openejb.tools.release.util.Base64;
import org.apache.openejb.tools.release.util.IO;
import org.apache.openejb.tools.release.util.ObjectList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Rev$ $Date$
 */
public class Nexus {
    private final String authorization;
    private final DefaultHttpClient client;

    public Nexus(String user, String pass) {
        final String s = user + ":" + pass;
        final byte[] bytes = Base64.encodeBase64(s.getBytes());

        authorization = "Basic " + new String(bytes);
        client = new DefaultHttpClient();
    }

    public ObjectList<Repository> getRepositories() throws IOException, JAXBException {

        final HttpGet get = new HttpGet("https://repository.apache.org/service/local/staging/profile_repositories");
        get.setHeader("Authorization", authorization);

        final HttpResponse response = client.execute(get);
        final InputStream in = response.getEntity().getContent();
        final JAXBContext context = JAXBContext.newInstance(StagingRepositories.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        final StagingRepositories stagingRepositories = (StagingRepositories) unmarshaller.unmarshal(in);

        return stagingRepositories.getRepositories();
    }

    public void close(String repository) throws IOException {
        close(repository, "");
    }

    public void close(String repository, String description) throws IOException {
        bulk("close", repository, description);
    }

    public void drop(String repository) throws IOException {
        drop(repository, "");
    }

    public void drop(String repository, String description) throws IOException {
        bulk("drop", repository, description);
    }

    private void bulk(String operation, String repository, String description) throws IOException {

        final HttpPost post = new HttpPost("https://repository.apache.org/service/local/staging/bulk/" + operation + "?undefined");
        post.addHeader("Authorization", authorization);
        post.addHeader("Content-Type", "application/json");

        final String data = String.format("{\"data\":{\"stagedRepositoryIds\":[\"%s\"],\"description\":\"%s\"}}", repository, description);
        post.setEntity(new StringEntity(data));

        final HttpResponse execute = client.execute(post);

        String message = IO.slurp(execute.getEntity().getContent());

        System.out.println(message);

        final int statusCode = execute.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
            throw new IOException(String.format("%s\n%s", execute.getStatusLine().toString(), message));
        }
    }

}
