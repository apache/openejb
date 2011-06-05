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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OpenEJBMessageFilterUtil {

	@SuppressWarnings("rawtypes")
	public static List<String> getNonRetweetedOpenEJBStatusIDs( List<Map> keyValuePairs) {
		
		List<String> openEJBStatusIDs = new ArrayList<String>();
		
		for (Object keyValuePair : keyValuePairs) {
			Map keyValue = (Map) keyValuePair;
			if (keyValue.containsKey("text")) {
				String tweet = (String) keyValue.get("text");
				if(isOpenEJBTweet(tweet) && !isRetweeted(keyValue))
				{
					System.out.println("Adding Tweet:"+tweet);
					Number tweetId=(Number) keyValue.get("id");
					openEJBStatusIDs.add(tweetId.toString());					
				}
				else
				{
					System.out.println("Tweet Not Considered:" +keyValue.get("text"));
					System.out.println("IsOpenEJBTweet?:"+isOpenEJBTweet(tweet));
					System.out.println("Was it retweeted before:"+isRetweeted(keyValue));
				}
				
			}
		}
		
		return openEJBStatusIDs;
	}

	@SuppressWarnings("rawtypes")
	private static boolean isRetweeted( Map keyValue) {
		Integer retweetCount= new Integer((Integer) keyValue.get("retweet_count"));
		if(retweetCount>0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	private static boolean isOpenEJBTweet(String tweet) {
		String[] words = tweet.split(" ");
		List<String> wordsAsList = Arrays.asList(words);
		for (String word : wordsAsList) {
			if (isOpenEJBMentioned(word))
			{
				String mentionName=word.trim().substring(1,8);
				if(mentionName.equalsIgnoreCase("openejb"))
				{return true;}
			}
		}
		return false;
	}


	private static boolean isOpenEJBMentioned(String word) {
		return (word.startsWith("#") || word.startsWith("@")) &&word.trim().length()>=8;
	}
	
	
}
