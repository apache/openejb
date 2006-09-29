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
package org.apache.openejb.corba.sunorb;

import com.sun.corba.se.internal.Interceptors.PIORB;
import com.sun.corba.se.internal.POA.POAImpl;
import com.sun.corba.se.internal.POA.POAManagerImpl;
import com.sun.corba.se.internal.POA.Policies;
import com.sun.corba.se.internal.orbutil.ORBConstants;


/**
 * This class is used to assist in the interception of IOR creation.
 *
 * @version $Revision$ $Date$
 * @see OpenEJBPOA
 */
public class OpenEJBORB extends PIORB {

    protected POAImpl makeRootPOA() {

        POAManagerImpl poaManager = new POAManagerImpl(this);
        POAImpl result = new OpenEJBPOA(ORBConstants.ROOT_POA_NAME, poaManager, Policies.rootPOAPolicies, null, null, this);

        return result;
    }

}
