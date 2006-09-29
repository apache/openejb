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

import com.sun.corba.se.interceptor.IORInfoExt;
import com.sun.corba.se.interceptor.UnknownType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;


/**
 * @version $Revision$ $Date$
 */
final class IORSSLInterceptor extends LocalObject implements IORInterceptor {

    private final Log log = LogFactory.getLog(IORSSLInterceptor.class);

    public void establish_components(IORInfo info) {

        try {
            IORInfoExt ext = (IORInfoExt) info;

            int port = ext.getServerPort(OpenEJBSocketFactory.IIOP_SSL);

//            info.add_ior_component(policy.getConfig().generateIOR(Util.getORB(), Util.getCodec()), TAG_INTERNET_IOP.value);
        } catch (UnknownType unknownType) {
            log.error("Unknown type", unknownType);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.apache.openejb.corba.ssl.IORSSLInterceptor";
    }

}
