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

import org.apache.log4j.Logger;
import org.apache.openejb.tools.twitter.util.RetweetAppUtil;
import org.apache.openejb.tools.twitter.vo.ValidStatusesOfUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenEJBMessageFilterUtil implements RetweetAppConstants {

    static SimpleDateFormat dateFormat = new SimpleDateFormat(TWITTER_DATE_FORMAT, TWITTER_LOCALE);
    private static Logger logger = Logger.getLogger(OpenEJBMessageFilterUtil.class);

    /**
     * @param keyValuePairs
     * @return RetweetableStatusesVO contains tweetIDs (i.e StatusIDs ) for OpenEJB account and TomEE account
     */
    @SuppressWarnings("rawtypes")
    public static ValidStatusesOfUser getNonRetweetedValidStatusIDs(List<Map> keyValuePairs) {

        ValidStatusesOfUser retweetableStatusesVO = new ValidStatusesOfUser();
        List<String> tweetIDsForOpenEJBTwitterAccount = new ArrayList<String>();
        List<String> tweetIDsForTomEETwitterAcount = new ArrayList<String>();

        for (Object keyValuePair : keyValuePairs) {
            Map keyValue = (Map) keyValuePair;
            if (keyValue.containsKey("text")) {
                addValidTweetsForOpenEJBAccount(tweetIDsForOpenEJBTwitterAccount, keyValue);
                addValidTweetsForApacheTomEEAccount(tweetIDsForTomEETwitterAcount, keyValue);
            }
        }

        retweetableStatusesVO.setTweetIDsForOpenEJBTwitterAccount(tweetIDsForOpenEJBTwitterAccount);
        retweetableStatusesVO.setTweetIDsForTomEETwitterAcount(tweetIDsForTomEETwitterAcount);

        return retweetableStatusesVO;
    }

    static void addValidTweetsForApacheTomEEAccount(List<String> tweetIDsForTomEETwitterAcount, Map keyValue) {


        String tweet = (String) keyValue.get("text");
        if (!isOlderThanAnHour(keyValue) & isTomEETweet(tweet) & !isRetweeted(keyValue)) {
            addAcceptedTweetIDs(tweetIDsForTomEETwitterAcount, keyValue, tweet);
        } else {
            logWhyTweetWasRejectedForTomEEAccount(keyValue, tweet);
        }


    }

    private static void logWhyTweetWasRejectedForTomEEAccount(Map keyValue, String tweet) {
        logger.debug("Is TomEE Tweet?:" + isTomEETweet(tweet));
        logger.debug("Was it retweeted before:" + isRetweeted(keyValue));
        logger.info("Tweet Not Considered:" + keyValue.get("text"));
    }

    /**
     * Considers as a TomEE tweet, if it contains any of the hashtags configured
     * Allowed hashtags are configured in RetweetTool.properties
     * NOTE: The hashtags in the property file should be pipe separated
     */
    static boolean isTomEETweet(String tweet) {

        String hashTagsSupported = (String) RetweetAppUtil.getTwitterAppProperties().get("tomee.supported.hashtags");
        Pattern pattern = Pattern.compile(".*(#|@)(" + hashTagsSupported + ").*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tweet);
        return matcher.matches();
    }

    @SuppressWarnings("rawtypes")
    static void addValidTweetsForOpenEJBAccount(List<String> openEJBStatusIDs, Map keyValue) {
        String tweet = (String) keyValue.get("text");
        if (!isOlderThanAnHour(keyValue) & isOpenEJBTweet(tweet) & !isRetweeted(keyValue)) {
            addAcceptedTweetIDs(openEJBStatusIDs, keyValue, tweet);
        } else {
            logWhyTweetWasRejectedForOpenEJBAccount(keyValue, tweet);
        }
    }


    @SuppressWarnings("rawtypes")
    static void addAcceptedTweetIDs(List<String> openEJBStatusIDs, Map keyValue, String tweet) {
        logger.info("Adding Tweet:" + tweet);
        Number tweetId = (Number) keyValue.get("id");
        openEJBStatusIDs.add(tweetId.toString());
    }

    @SuppressWarnings("rawtypes")
    private static void logWhyTweetWasRejectedForOpenEJBAccount(Map keyValue, String tweet) {
        logger.debug("IsOpenEJBTweet?:" + isOpenEJBTweet(tweet));
        logger.debug("Was it retweeted before:" + isRetweeted(keyValue));
        logger.info("Tweet Not Considered:" + keyValue.get("text"));
    }

    @SuppressWarnings("rawtypes")
    private static boolean isOlderThanAnHour(Map keyValue) {
        String dateAsString = (String) keyValue.get("created_at");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);

        Date tweetDate;
        try {
            tweetDate = dateFormat.parse(dateAsString);
        } catch (ParseException e) {
            logger.error("can't parse date " + dateAsString, e);
            return false;
        }

        logger.debug("Older than an hour?: " + tweetDate.before(calendar.getTime()));
        return tweetDate.before(calendar.getTime());
    }

    @SuppressWarnings("rawtypes")
    private static boolean isRetweeted(Map keyValue) {

        Integer retweetCount;
        try {
            retweetCount = getRetweetCount(keyValue, null);
        } catch (NumberFormatException ignoredException) {
            //Sometimes retweet-count returned by twitter is "100+" A non Number.
            //Ignoring such exception
            logger.debug("Skipping this status...");
            return true;
        }

        return retweetCount > 0;
    }

    @SuppressWarnings("rawtypes")
    private static Integer getRetweetCount(Map keyValue, Integer retweetCount) {
        if (keyValue.get("retweet_count") instanceof String) {
            retweetCount = new Integer((String) keyValue.get("retweet_count"));
        } else if (keyValue.get("retweet_count") instanceof Integer) {
            retweetCount = (Integer) keyValue.get("retweet_count");
        }
        return retweetCount;
    }


    /*
      * tweet.contains(string) can't help since mentions can end with period, like "#openejb."
      */
    static boolean isOpenEJBTweet(String tweet) {


        String hashTagsSupported = (String) RetweetAppUtil.getTwitterAppProperties().get("openejb.supported.hashtags");
        Pattern pattern = Pattern.compile(".*(#|@)(" + hashTagsSupported + ").*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tweet);
        return matcher.matches();
    }
}
