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

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ScreenNamesRetriever {

    private static Logger logger = Logger.getLogger(ScreenNamesRetriever.class);

    public static void main(String[] args) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        getContributorsNames();
    }

    public static List<String> getContributorsNames() {
        List<String> contributorsScreenNames = null;
        try {
            contributorsScreenNames = getContributorsScreenNames();
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contributorsScreenNames;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> getContributorsScreenNames()
            throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException,
            IOException, ClientProtocolException {
        HttpResponse response = getContribListMembersResponse();
        String responseBody = JsonResponseParser.getResponseBody(response);
        StringReader reader = new StringReader(responseBody);
        Map mapFromJson = JsonResponseParser.getMapFromJson(reader);
        logger.debug("MAP:" + mapFromJson);
        List listFromJson = (List) mapFromJson.get("users");
        return getScreenNamesAlone(listFromJson);
    }

    @SuppressWarnings("rawtypes")
    private static List<String> getScreenNamesAlone(List listFromJson) {
        List<String> contribMembersList = new ArrayList<String>();
        for (Object object : listFromJson) {
            LinkedHashMap map = (LinkedHashMap) object;
            contribMembersList.add((String) map.get("screen_name"));
        }
        return contribMembersList;
    }


    private static HttpResponse getContribListMembersResponse()
            throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException,
            IOException, ClientProtocolException {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = getRequestForContribListMembers();
        Retweet.initConsumer();
        Retweet.consumer.sign(httpGet);
        HttpResponse response = client.execute(getRequestForContribListMembers());
        return response;
    }

    public static HttpGet getRequestForContribListMembers() {
        String listName = "contributors";
        String ownerScreenName = "OpenEJB";
        HttpGet httpGet = new HttpGet("http://api.twitter.com/1/lists/members.json?slug=" + listName
                + "&owner_screen_name=" + ownerScreenName);
        return httpGet;

    }

}
