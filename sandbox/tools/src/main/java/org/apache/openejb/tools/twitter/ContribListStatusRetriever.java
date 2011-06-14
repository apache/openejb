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
/*
package org.apache.openejb.tools.twitter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

public class ContribListStatusRetriever {

    public static HttpResponse getStatusesFromOpenEJBContributorsList() {

        String listName = "contributors";
        String ownerScreenName = "OpenEJB";
        HttpGet httpGet = getHttpRequestToRetrieveListStatuses(listName, ownerScreenName);
        HttpResponse contributorsListStatusesResponse = getContribListStatusesResponse(httpGet);

        return contributorsListStatusesResponse;
    }

    private static HttpGet getHttpRequestToRetrieveListStatuses(String listName, String ownerScreenName) {

        HttpGet httpGet = new HttpGet("http://api.twitter.com/1/lists/statuses.json?slug=" + listName
                + "&owner_screen_name=" + ownerScreenName);
        System.out.println("Getting list using " + httpGet.getURI());
        return httpGet;
    }


    private static HttpResponse getContribListStatusesResponse(HttpGet httpGet) {

        HttpResponse response = null;
        try {
            response = Retweet.getHttpClient().execute(httpGet);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
*/