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
package org.apache.openejb.tools.twitter.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;

public class RetweetAppUtil {
	
	public static Properties getTwitterAppProperties() {

		Properties retweetAppProperties = new Properties();
		try {
			ClassPathResource retweetToolPropertiesFile = new ClassPathResource(
			"RetweetTool.properties");
			retweetAppProperties.load(retweetToolPropertiesFile
					.getInputStream());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.print("Using the following properties: ");
		System.out.print("---------------------------------");
		retweetAppProperties.list(System.out);
		return retweetAppProperties;

	}

}
