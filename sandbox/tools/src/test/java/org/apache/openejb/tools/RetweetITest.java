/**
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
package org.apache.openejb.tools;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.openejb.tools.twitter.ScreenNamesRetriever;
import org.apache.openejb.tools.twitter.UserStatusRetriever;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RetweetITest {

    @Test
    public void screenNamesListShouldNotBeEmpty() {
        assertFalse(ScreenNamesRetriever.getContributorsNames().isEmpty());
    }

    @Test
    public void userStatusShouldBeRetrieved() throws ClientProtocolException, IOException {
        HttpGet httpGet = UserStatusRetriever.getHttpRequestToRetrieveUserStatuses("stratwine");
        HttpResponse userStatusResponse = UserStatusRetriever.getUserStatusResponse(httpGet);
        ResponseHandler<String> responseHander = new BasicResponseHandler();
        String responseBody = (String) responseHander.handleResponse(userStatusResponse);
        System.out.println(responseBody);
        assertTrue(userStatusResponse.getStatusLine().getStatusCode() == 200);

    }

}
