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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.remote;

import org.apache.openejb.arquillian.remote.ejb.TstMdb;
import org.apache.openejb.arquillian.remote.rest.TstRestService;
import org.apache.openejb.arquillian.remote.servlet.TstMdbServlet;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.apache.openejb.arquillian.remote.util.RemoteUtil.readContent;

@RunWith(Arquillian.class)
public class MdbIT {
    @Deployment(testable = false) public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test-mdb.war")
                .addClass(TstMdb.class)
                .addClass(TstMdbServlet.class)
                .setWebXML(new StringAsset(
                    Descriptors.create(WebAppDescriptor.class)
                        .version("3.0").displayName("Rest Test")
                        .servlet(TstMdbServlet.class, "/test")
                        .exportAsString()));
    }

    @Test public void helloRestService() throws Exception {
        final String content = readContent("http://localhost:8080/test-mdb/test");
        assertTrue("last message = a servlet sent a message to a MDB".equals(content));
    }

}
