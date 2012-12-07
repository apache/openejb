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
package org.apache.openejb.test.stateless;

import javax.ejb.EJB;

/**
 * 
 * @version $Rev$ $Date$
 */
public abstract class BasicStatelessLocalTestClient extends StatelessTestClient {

	@EJB(name="client/tests/stateless/BasicStatelessPojoHomeLocal",
		 beanInterface = BasicStatelessLocalHome.class)
	protected BasicStatelessLocalHome ejbLocalHome = null;
    protected BasicStatelessLocalObject ejbLocalObject = null;

    public BasicStatelessLocalTestClient(String name){
        super(name);
    }
    
    protected Object cast(Object object, Class type) {
    	return type.cast(object);
    }
    
}


