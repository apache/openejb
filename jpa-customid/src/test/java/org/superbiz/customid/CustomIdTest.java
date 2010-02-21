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
package org.superbiz.customid;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class CustomIdTest extends TestCase {

    public void test() throws Exception {

        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        p.put("dataSource", "new://Resource?type=DataSource");

        Context context = new InitialContext(p);
        TestLocal bean = (TestLocal) context.lookup("TestBeanLocal");
        bean.sql("CREATE MEMORY TABLE INVOICE(BRANDNAME VARCHAR(255) NOT NULL,ID INTEGER NOT NULL,PRICE NUMERIC,PRIMARY KEY(BRANDNAME,ID))");
        bean.sql("CREATE MEMORY TABLE ITEM(ID VARCHAR(255) NOT NULL PRIMARY KEY,QUANTITY INTEGER,FK_FOR_ID INTEGER,FK_FOR_BRANDNAME VARCHAR(255))");

        bean.tx1();
        bean.tx2();


    }
}
