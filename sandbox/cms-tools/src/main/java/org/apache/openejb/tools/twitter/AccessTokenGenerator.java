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
import oauth.signpost.exception.OAuthNotAuthorizedException;

import java.util.Scanner;

import static org.apache.openejb.tools.twitter.AuthorizationUrlGenerator.consumer;
import static org.apache.openejb.tools.twitter.AuthorizationUrlGenerator.provider;

public class AccessTokenGenerator {

    public static void main(String[] args) {
        AuthorizationUrlGenerator.getAuthorizationUrlForUser();
        getTokensToReadWriteIntoTwitterAccountOfUser();

    }

    private static void getTokensToReadWriteIntoTwitterAccountOfUser() {

        try {
            retrieveAccessTokens();
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (OAuthNotAuthorizedException e) {
            e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
        }
    }

    private static void retrieveAccessTokens()
            throws OAuthMessageSignerException, OAuthNotAuthorizedException,
            OAuthExpectationFailedException, OAuthCommunicationException {

        Scanner keyBoardInputScanner = new Scanner(System.in);
        String pinFromUser = scanPIN(keyBoardInputScanner);

        System.out.println("User has provided this PIN:" + pinFromUser);

        provider.retrieveAccessToken(consumer, pinFromUser);

        System.out.println("Consumer Token: (Copy and Paste this value in the RetweetTool.properties against retweetApp< concerned account name >.authorizedUser.consumer.token)");
        System.out.println(consumer.getToken());


        System.out.println("Consumer Token Secret: (Copy and Paste this value in RetweetTool.properties against retweetApp< concerned account name >.authorizedUser.consumer.tokenSecret)");
        System.out.println(consumer.getTokenSecret());

    }

    private static String scanPIN(Scanner keyBoardInputScanner) {
        String pinFromUser = null;
        System.out.println("Please enter the PIN number to complete authorization:");
        if (keyBoardInputScanner.hasNext()) {
            pinFromUser = keyBoardInputScanner.next();
        }
        return pinFromUser;
    }


}
