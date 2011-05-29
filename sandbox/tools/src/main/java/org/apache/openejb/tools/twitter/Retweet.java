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

/**
 *
 * We should monitor this feed http://twitter.com/#!/OpenEJB/contributors
 * and retweet anything that mentions OpenEJB
 *
 * So if anyone in the contributors list tweeted about OpenEJB, the OpenEJB twitter account would retweet it
 *
 * Two things will happen as a result:
 *   -  The more activity on the OpenEJB twitter account the more followers it will get
 *   -  The more @joe and other contributors are seen on the account, the more followers they will get
 *
 * The OpenEJB twitter account has more followers than most everyone else so getting it
 * to retweet is a good way to expose people to all our wonderful contributors
 * and get them some followers and help the project at the same time.
 *
 * The result is we as a community will have more ability overall to get the word out!
 *
 *
 * @version $Rev$ $Date$
 */
public class Retweet {

    // Implementation ideas

    //  Seems signpost is just what we need for OAuth http://code.google.com/p/oauth-signpost/wiki/GettingStarted

    //  Twitter API

    //  list - HTTP GET http://api.twitter.com/1/lists/statuses.xml?slug=contributors&owner_screen_name=OpenEJB

    //  retweet - HTTP POST http://api.twitter.com/1/statuses/retweet/<statusid>.xml

    // Little bit of Apache Commons HTTPClient and Signpost and we're good to go

    public static void main(String[] args) {


        // Grab the http://twitter.com/#!/OpenEJB/contributors feed via the Twitter API

        // Scan for new tweets from the last hour

        // Retweet any tweets that haven't been retweeted

        // We could look at the OpenEJB twitter feed itself to determine if a tweet
        // has already been retweeted

    }
}
