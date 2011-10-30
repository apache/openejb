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

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.openejb.tools.twitter.OpenEJBMessageFilterUtil.isOpenEJBTweet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class RetweetTest {

    /**
     * Any hashtag not specified specified in
     * RetweetTool.properties against the 'openejb.supported.hashtags' key should be ignored
     */
    @Test
    public void nonOpenEJBMessageShouldBeRejected() {
        assertFalse(isOpenEJBTweet("some random message"));
    }

    /**
     * Any hashtag not specified specified in
     * RetweetTool.properties against the 'tomee.supported.hashtags' key should be ignored
     */

    @Test
    public void nonTomEEMessagesShouldBeRejected() {
        assertFalse(OpenEJBMessageFilterUtil.isTomEETweet("some random message"));
    }

    @Test
    public void openEJBTweetsShouldBeIdentified() {
        assertTrue(isOpenEJBTweet("this is a #openejb tweet"));
        assertTrue(isOpenEJBTweet("this is an @openejb tweet"));
        assertTrue(isOpenEJBTweet("this is an #OPEnEJB tweet")); //case insensitive check
        assertTrue(isOpenEJBTweet("this is an #openejb-tweet.")); // also accept if hashtag is immediately followed by punctuations etc
    }


    /**
     * A tweet which contains any of the supported hashtags
     * RetweetTool.properties 'tomee.supported.hashtags' key holds the supported hashtags
     */
    @Test
    public void tomEETweetsShouldBeIdentified() {
        assertTrue(OpenEJBMessageFilterUtil.isTomEETweet("this is a #tomee message"));

    }


    @Test
    public void regexPatternShouldMatchWithMessagesContainingHashTags() {
        //testing for correctness of regex pattern
        String hashTagsSupported = "openejb|tomee";
        Pattern pattern = Pattern.compile(".*(#|@)(" + hashTagsSupported + ").*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher("this is some @tomEE message");
        Matcher matcher2 = pattern.matcher("this is some @openEjb-message");
        assertTrue(matcher.matches());
        assertTrue(matcher2.matches());
    }

    @Test
    public void regexPatternShouldNotMatchWithRandomMessage() {
        //testing for correctness of regex pattern
        Pattern pattern = Pattern.compile(".*(#|@)openejb.*");
        Matcher matcher = pattern.matcher("this is a random message");
        assertFalse(matcher.matches());
    }

    @Test
    public void testEnumUsageWithIfBlocks() {
        TwitterAccount tAccount = TwitterAccount.OPENEJB;
        if (!tAccount.equals(TwitterAccount.OPENEJB)) {
            fail("Enum was not matched");
        }
    }
}
