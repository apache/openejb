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

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.openejb.tools.twitter.util.RetweetAppUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/**
 * We should monitor this feed http://twitter.com/#!/OpenEJB/contributors
 * and retweet anything that mentions OpenEJB
 * <p/>
 * So if anyone in the contributors list tweeted about OpenEJB, the OpenEJB twitter account would retweet it
 * <p/>
 * Two things will happen as a result:
 * -  The more activity on the OpenEJB twitter account the more followers it will get
 * -  The more @joe and other contributors are seen on the account, the more followers they will get
 * <p/>
 * The OpenEJB twitter account has more followers than most everyone else so getting it
 * to retweet is a good way to expose people to all our wonderful contributors
 * and get them some followers and help the project at the same time.
 * <p/>
 * The result is we as a community will have more ability overall to get the word out!
 * <p/>
 * Implemented using :http://code.google.com/p/oauth-signpost/wiki/GettingStarted
 * list - HTTP GET http://api.twitter.com/1/lists/statuses.xml?slug=contributors&owner_screen_name=OpenEJB
 * retweet - HTTP POST http://api.twitter.com/1/statuses/retweet/<statusid>.xml
 *
 * @version $Rev$ $Date$
 */
public class Retweet {

    public static Properties retweetToolProperties = RetweetAppUtil.getTwitterAppProperties();
    public static OAuthConsumer consumer;
    private static Logger logger = Logger.getLogger(Retweet.class);

    public static void main(String[] args) {

        Set<String> validOpenEJBTweetIDs = UserStatusRetriever.getAllContributorsStatuses().getValidTweetIDsForOpenEJBAccount();
        Set<String> validTomEETweetIDs = UserStatusRetriever.getAllContributorsStatuses().getValidTweetIDsForTomEEAccount();
        retweetIfNotEmpty(validOpenEJBTweetIDs, TwitterAccount.OPENEJB);
        retweetIfNotEmpty(validTomEETweetIDs, TwitterAccount.TOMEE);

    }

    private static void retweetIfNotEmpty(Collection<String> tweetIDs, TwitterAccount twitterAccount) {

        if (!tweetIDs.isEmpty()) {
            logger.info("About to retweet " + tweetIDs + " at " + twitterAccount.toString() + " twitter account");
            retweetThisCollectionOfStatuses(tweetIDs, twitterAccount);
        } else {
            logger.info("No message to retweet at " + twitterAccount.toString() + " twitter account");
        }
    }

    private static void retweetThisCollectionOfStatuses(Collection<String> nonRetweetedOpenEJBStatusIDs, TwitterAccount twitterAccount) {

        for (String statusIDToRetweet : nonRetweetedOpenEJBStatusIDs) {
            try {
                retweet(statusIDToRetweet, twitterAccount);
                pauseBeforeTheNextRetweet();
            } catch (OAuthMessageSignerException e) {
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initConsumerForOpenEJBAccount() {

        consumer = new CommonsHttpOAuthConsumer(
                retweetToolProperties.getProperty("retweetApp.consumer.key"),
                retweetToolProperties
                        .getProperty("retweetApp.consumerSecret.key"));


        consumer.setTokenWithSecret(retweetToolProperties.getProperty("retweetApp.openejb.authorizedUser.consumer.token"),
                retweetToolProperties.getProperty("retweetApp.openejb.authorizedUser.consumer.tokenSecret"));

    }

    public static void initConsumerForTomEEAccount() {
        consumer = new CommonsHttpOAuthConsumer(
                retweetToolProperties.getProperty("retweetApp.consumer.key"),
                retweetToolProperties
                        .getProperty("retweetApp.consumerSecret.key"));


        consumer.setTokenWithSecret(retweetToolProperties.getProperty("retweetApp.tomee.authorizedUser.consumer.token"),
                retweetToolProperties.getProperty("retweetApp.tomee.authorizedUser.consumer.tokenSecret"));

    }

    public static HttpResponse retweet(String statusIDToRetweet, TwitterAccount twitterAccount) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        HttpPost httpPost = new HttpPost(RetweetAppConstants.RETWEET_URL + statusIDToRetweet + ".json");

        initBasedOnAccountToRetweetAt(twitterAccount);
        consumer.sign(httpPost);
        HttpResponse response = null;
        try {
            response = getHttpClient().execute(httpPost);
            logger.debug(response.getStatusLine());

            logger.info("Retweeted " + statusIDToRetweet + " at " + twitterAccount.toString() + " twitter account");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private static void initBasedOnAccountToRetweetAt(
            TwitterAccount twitterAccount) {
        if (twitterAccount.equals(TwitterAccount.OPENEJB)) {
            initConsumerForOpenEJBAccount();
        } else if (twitterAccount.equals(TwitterAccount.TOMEE)) {
            initConsumerForTomEEAccount();
        }
    }

    public static HttpClient getHttpClient() {
        return new DefaultHttpClient();
    }

    private static void pauseBeforeTheNextRetweet() {
        try { //So it doesn't look like spamming
            Thread.sleep(1000 * 60 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
