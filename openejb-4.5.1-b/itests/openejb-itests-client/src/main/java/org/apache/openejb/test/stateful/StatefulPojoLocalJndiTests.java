/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.stateful;


/**
 * [1] Should be run as the first test suite of the BasicStatefulTestClients
 * 
 * 
 * @version $Rev$ $Date$
 */
public class StatefulPojoLocalJndiTests extends BasicStatefulLocalTestClient {

    public StatefulPojoLocalJndiTests(){
        super("LocalJNDI.");
    }

    public void test01_Jndi_lookupHome(){
        try{
        	// Here we use the Java casting as what is done while looking-up a local bean
        	ejbLocalHome = (BasicStatefulLocalHome) initialContext.lookup("client/tests/stateful/BasicStatefulPojoHomeLocal");
            assertNotNull("The EJBLocalHome is null", ejbLocalHome);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /* TO DO:  
     * public void test00_enterpriseBeanAccess()       
     * public void test00_jndiAccessToJavaCompEnv()
     * public void test00_resourceManagerAccess()
     */

}

