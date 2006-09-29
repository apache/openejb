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
package org.apache.openejb.corba;

import com.sun.corba.se.internal.orbutil.ORBConstants;

/**
 * @version $Revision$ $Date$
 */
public interface MinorCodes {

    public static final int SUBSYSTEM_SIZE = 200;

    /**
     * GENERAL_BASE is used for orbutil/MinorCodes
     */
    // todo the hard coded value here is the Apache orb vendor id and Alan is going to replace this with a link to a Geronimo constant
    public static final int GENERAL_BASE = 0x41534000 + SUBSYSTEM_SIZE;

    /**
     * COMM_FAILURE minor codes
     */
    public static final int UNSUPPORTED_ENDPOINT_TYPE = ORBConstants.GENERAL_BASE + 1;
}
