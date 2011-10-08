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
package org.superbiz.moviefun;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.api.ArquillianResource;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MoviesIT {
	
	@ArquillianResource
	private URL deploymentUrl;
	
	//@Drone
	//private DefaultSelenium driver;

	@Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        		.addPackage("org.superbiz.moviefun")
        		.addPackage("org.superbiz.moviefun.util")
        		.addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml") , "META-INF/ejb-jar.xml")
        		.addAsResource(new ClassLoaderAsset("META-INF/persistence.xml") , "META-INF/persistence.xml");
        		
        addResources("src/main/webapp", "", archive);
        System.out.println(archive.toString(true));
		return archive;
    }
	
    private static void addResources(String source, String target, WebArchive archive) {
		File sourceFile = new File(source);
		if (! sourceFile.exists()) return;
		if (sourceFile.isFile()) {
			archive.add(new FileAsset(sourceFile), target);
		}
		
		if (sourceFile.isDirectory()) {
			for (File file : sourceFile.listFiles()) {
				if (file.getName().startsWith(".")) continue;
				addResources(source + File.separator + file.getName(), target + File.separator + file.getName(), archive);
			}
		}
	}

	@Test
    public void testShouldMakeSureWebappIsWorking() throws Exception {
    	System.out.println("Hello");
    }

}
