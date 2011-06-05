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

import org.apache.openejb.tools.twitter.Retweet;

/**
 * You can execute standalone java programs with Maven like so:
 * <p/>
 * mvn clean install exec:java -Dexec.mainClass=org.apache.openejb.tools.Hourly
 * <p/>
 * The idea is to setup this main class as an Hourly cron job
 * If we have other things we want to do hourly, we can do them here.
 *
 * @version $Rev$ $Date$
 */
public class Hourly {

    public static void main(String[] args) {
        Retweet.main(args);
    }
}
