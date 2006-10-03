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
package org.apache.openejb.sunorb;

import com.sun.corba.se.connection.EndPointInfo;
import com.sun.corba.se.internal.iiop.EndPointImpl;

import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;

/**
 * An extended endpoint information object used
 * by the OpenEJBSocketFactory object to pass extended information
 * between the various stages.
 */
public class OpenEJBEndPointImpl extends EndPointImpl {
    // supports information for an SSL connection
    private int supports;
    // requires flags information for an SSL connection
    private int requires;
    // the supported and requires booleans derived from the flag settings.
    private boolean authSupported = false;
    private boolean authRequired = false;

    public OpenEJBEndPointImpl(String type, int port, String host, int supports, int requires) {
        super(type, port, host);
        this.requires = requires;
        this.supports = supports;

        if ((supports & EstablishTrustInClient.value) != 0) {
            authSupported = true;

            if ((requires & EstablishTrustInClient.value) != 0) {
                authRequired = true;
            }
        }

        if ((supports & EstablishTrustInTarget.value) != 0) {
            authSupported = true;

            if ((requires & EstablishTrustInTarget.value) != 0) {
                authSupported = true;
            }
        }
    }


    public int getRequires() {
        return requires;
    }

    public int getSupports() {
        return supports;
    }

    public boolean clientAuthSupported() {
        return authSupported;
    }

    public boolean clientAuthRequired() {
        return authRequired;
    }
}
