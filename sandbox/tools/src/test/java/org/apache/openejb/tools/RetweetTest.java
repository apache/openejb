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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.openejb.tools.twitter.Retweet;
import org.junit.BeforeClass;
import org.junit.Test;


public class RetweetTest {

	
	@BeforeClass
	public static void setUp()
	{
	  Retweet.initConsumer();
	}
	
	@Test
	public void basicRequestShouldGiveValidResponse() throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, IOException
	{
		  URL url = new URL("http://twitter.com/statuses/mentions.xml");
	         HttpURLConnection request = (HttpURLConnection) url.openConnection();

	         // sign the request
	         Retweet.consumer.sign(request);

	         // send the request
	         request.connect();

	         // response status should be 200 OK
	         int statusCode = request.getResponseCode();
	         System.out.println("Status Code:"+statusCode);
	         
	         assertTrue(statusCode==200);

	}
	
	@Test
	public void contributorsListStatusesShouldBeRetrieved() throws ClientProtocolException, IOException
	{
		HttpResponse response=Retweet.getStatusesFromOpenEJBContributorsList();
		
		assertTrue(response.getStatusLine().getStatusCode()==200);
		
		ResponseHandler<String> responseHander = new BasicResponseHandler();
		String responseBody = (String)responseHander.handleResponse(response);
		System.out.println(responseBody);
	}
}
