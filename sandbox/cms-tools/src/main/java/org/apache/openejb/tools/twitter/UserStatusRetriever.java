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
package org.apache.openejb.tools.twitter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.openejb.tools.twitter.vo.ValidStatuses;
import org.apache.openejb.tools.twitter.vo.ValidStatusesOfUser;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserStatusRetriever {

    public static HttpGet getHttpRequestToRetrieveUserStatuses(String screenName) {

        HttpGet requestForUserStatus = new HttpGet(RetweetAppConstants.USER_TIMELINE_STATUS_URL + screenName);
        return requestForUserStatus;
    }

    public static HttpResponse getUserStatusResponse(HttpGet userStatusRequest) {
        HttpResponse response = null;
        try {
            response = Retweet.getHttpClient().execute(userStatusRequest);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;

    }

    /**
     * Retrieves non-retweeted, less-than-an-hour-old statuses from a specified contributor
     */
    @SuppressWarnings("rawtypes")
    public static ValidStatusesOfUser getAcceptedStatusesOfContributor(String screenName) {
        HttpResponse userStatusResponse = getUserStatusResponse(getHttpRequestToRetrieveUserStatuses(screenName));
        String responseBody = JsonResponseParser.getResponseBody(userStatusResponse);
        StringReader dataToParse = new StringReader(responseBody);
        List<Map> listFromJson = JsonResponseParser.getListFromJson(dataToParse);

        ValidStatusesOfUser validStatusesOfUser = OpenEJBMessageFilterUtil.getNonRetweetedValidStatusIDs(listFromJson);
        return validStatusesOfUser;
    }

    public static ValidStatuses getAllContributorsStatuses() {
        List<String> contributorsNames = ScreenNamesRetriever.getContributorsNames();
        Set<String> openEJBStatuses = new HashSet<String>();
        Set<String> tomEEStatuses = new HashSet<String>();
        for (String screenName : contributorsNames) {
            openEJBStatuses.addAll(getAcceptedStatusesOfContributor(screenName).getTweetIDsForOpenEJBTwitterAccount());
            tomEEStatuses.addAll(getAcceptedStatusesOfContributor(screenName).getTweetIDsForTomEETwitterAcount());
        }

        ValidStatuses validStatuses = new ValidStatuses();
        validStatuses.setValidTweetIDsForOpenEJBAccount(openEJBStatuses);
        validStatuses.setValidTweetIDsForTomEEAccount(tomEEStatuses);

        return validStatuses;
    }

}
