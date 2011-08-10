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
package org.apache.openejb.tools.twitter.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * One instance for each contributor. Holds the valid tweet-ids of that particular contributor
 * 
 */
public class ValidStatusesOfUser {
	
    List<String> tweetIDsForOpenEJBTwitterAccount = new ArrayList<String>();
    List<String> tweetIDsForTomEETwitterAcount = new ArrayList<String>();
    
	public List<String> getTweetIDsForOpenEJBTwitterAccount() {
		return tweetIDsForOpenEJBTwitterAccount;
	}
	public void setTweetIDsForOpenEJBTwitterAccount(
			List<String> tweetIDsForOpenEJBTwitterAccount) {
		this.tweetIDsForOpenEJBTwitterAccount = tweetIDsForOpenEJBTwitterAccount;
	}
	public List<String> getTweetIDsForTomEETwitterAcount() {
		return tweetIDsForTomEETwitterAcount;
	}
	public void setTweetIDsForTomEETwitterAcount(
			List<String> tweetIDsForTomEETwitterAcount) {
		this.tweetIDsForTomEETwitterAcount = tweetIDsForTomEETwitterAcount;
	}


}
